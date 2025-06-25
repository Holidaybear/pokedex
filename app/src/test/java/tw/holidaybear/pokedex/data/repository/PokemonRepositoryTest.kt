package tw.holidaybear.pokedex.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import tw.holidaybear.pokedex.data.local.CaptureRecordDao
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.PokemonDao
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.data.model.PokemonAndType
import tw.holidaybear.pokedex.data.remote.*

class PokemonRepositoryTest {

    private lateinit var repository: PokemonRepository
    private val pokeApiService: PokeApiService = mockk(relaxed = true)
    private val pokemonDao: PokemonDao = mockk(relaxed = true)
    private val captureRecordDao: CaptureRecordDao = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = PokemonRepository(pokeApiService, pokemonDao, captureRecordDao)
    }

    @Test
    fun `fetchAndStorePokemonList should fetch from API and store in Room when database is empty`() = runTest {
        val pokemonListResponse = PokemonListResponse(
            results = listOf(PokemonListItem(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/"))
        )
        coEvery { pokemonDao.getProcessedPokemonCount() } returns 0
        coEvery { pokemonDao.getUnprocessedPokemonIds() } returns emptyList() andThen listOf(1)
        coEvery { pokeApiService.getPokemonList(151) } returns pokemonListResponse

        repository.fetchAndStorePokemonList()

        coVerify { pokemonDao.insertPokemon(any()) }
        coVerify { pokeApiService.getPokemonDetail(1) }
    }

    @Test
    fun `fetchAndStorePokemonList should skip API fetch when all Pokemon are processed`() = runTest {
        coEvery { pokemonDao.getProcessedPokemonCount() } returns 151

        repository.fetchAndStorePokemonList()

        coVerify(exactly = 0) { pokeApiService.getPokemonList(any()) }
    }

    @Test
    fun `getProcessedPokemonAndTheirTypes should return pokemon and type data from DAO`() = runTest {
        val pokemon = Pokemon(1, "bulbasaur", "url", null, null, true)
        val type = Type(1, "grass")
        val pokemonAndType = PokemonAndType(pokemon, type)
        coEvery { pokemonDao.getProcessedPokemonAndTheirTypes() } returns flowOf(listOf(pokemonAndType))

        val result = repository.getProcessedPokemonAndTheirTypes()

        result.collect { list ->
            assert(list.size == 1)
            assert(list[0].pokemon.name == "bulbasaur")
            assert(list[0].type.name == "grass")
        }
    }

    @Test
    fun `ensureSpeciesInfoIsLoaded should fetch from API when description is null`() = runTest {
        val pokemonId = 1
        val pokemonWithoutDesc = Pokemon(pokemonId, "bulbasaur", "url", null, null, true)
        val speciesResponse = PokemonSpeciesResponse(
            flavorTextEntries = listOf(FlavorTextEntry(flavorText = "desc", language = Language("en"))),
            evolvesFromSpecies = null
        )

        coEvery { pokemonDao.getPokemonById(pokemonId) } returns pokemonWithoutDesc
        coEvery { pokeApiService.getPokemonSpecies(pokemonId) } returns speciesResponse

        repository.ensureSpeciesInfoIsLoaded(pokemonId)

        coVerify { pokeApiService.getPokemonSpecies(pokemonId) }
        coVerify { pokemonDao.updatePokemonDetails(pokemonId, "url", "desc", null) }
    }

    @Test
    fun `ensureSpeciesInfoIsLoaded should not fetch from API when description exists`() = runTest {
        val pokemonId = 1
        val pokemonWithDesc = Pokemon(pokemonId, "bulbasaur", "url", "desc", null, true)

        coEvery { pokemonDao.getPokemonById(pokemonId) } returns pokemonWithDesc

        repository.ensureSpeciesInfoIsLoaded(pokemonId)

        coVerify(exactly = 0) { pokeApiService.getPokemonSpecies(any()) }
    }

    @Test
    fun `capturePokemon should insert capture record into DAO`() = runTest {
        repository.capturePokemon(1, "grass")
        coVerify { captureRecordDao.insertCapture(any()) }
    }

    @Test
    fun `releasePokemon should delete capture record from DAO`() = runTest {
        repository.releasePokemon(1L)
        coVerify { captureRecordDao.deleteCapture(1L) }
    }
}