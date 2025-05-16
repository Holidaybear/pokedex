package tw.holidaybear.pokedex.data.repository

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import tw.holidaybear.pokedex.data.local.PokemonDao
import tw.holidaybear.pokedex.data.remote.EvolvesFromSpecies
import tw.holidaybear.pokedex.data.remote.PokeApiService
import tw.holidaybear.pokedex.data.remote.FlavorTextEntry
import tw.holidaybear.pokedex.data.remote.Language
import tw.holidaybear.pokedex.data.remote.OfficialArtwork
import tw.holidaybear.pokedex.data.remote.OtherSprites
import tw.holidaybear.pokedex.data.remote.PokemonDetailResponse
import tw.holidaybear.pokedex.data.remote.PokemonSpeciesResponse
import tw.holidaybear.pokedex.data.remote.PokemonType
import tw.holidaybear.pokedex.data.remote.Sprites
import tw.holidaybear.pokedex.data.remote.TypeDetail
import tw.holidaybear.pokedex.util.PokemonDetailWorker

class PokemonDetailWorkerTest {

    private lateinit var worker: PokemonDetailWorker
    private val pokeApiService: PokeApiService = mockk()
    private val pokemonDao: PokemonDao = mockk()
    private val context: Context = mockk()
    private val workerParameters: WorkerParameters = mockk()

    @Before
    fun setup() {
        worker = PokemonDetailWorker(context, workerParameters, pokeApiService, pokemonDao)
    }

    @Test
    fun `doWork should update imageUrl, description, and evolvesFromId in Pokemon entity`() = runTest {
        // Arrange
        val pokemonId = 814
        val imageUrl = "https://example.com/raboot.png"
        val description = "A cute rabbit."
        val evolvesFromId = 813
        coEvery { workerParameters.inputData } returns workDataOf("POKEMON_ID" to pokemonId)
        val detailResponse = PokemonDetailResponse(
            id = pokemonId,
            name = "raboot",
            sprites = Sprites(other = OtherSprites(officialArtwork = OfficialArtwork(frontDefault = imageUrl))),
            types = listOf(PokemonType(TypeDetail(name = "fire")))
        )
        val speciesResponse = PokemonSpeciesResponse(
            flavorTextEntries = listOf(
                FlavorTextEntry(
                    flavorText = description,
                    language = Language(name = "en")
                )
            ),
            evolvesFromSpecies = EvolvesFromSpecies(url = "https://pokeapi.co/api/v2/pokemon-species/813/")
        )
        coEvery { pokeApiService.getPokemonDetail(pokemonId) } returns detailResponse
        coEvery { pokeApiService.getPokemonSpecies(pokemonId) } returns speciesResponse
        coEvery { pokemonDao.getTypeByName("fire") } returns null
        coEvery { pokemonDao.insertType(any()) } returns Unit
        coEvery { pokemonDao.insertPokemonType(any()) } returns Unit
        coEvery { pokemonDao.updatePokemonDetails(pokemonId, imageUrl, description, evolvesFromId) } returns Unit

        // Act
        val result = worker.doWork()

        // Assert
        coVerify { pokemonDao.updatePokemonDetails(pokemonId, imageUrl, description, evolvesFromId) }
        assert(result == Result.success()) { "Worker failed, result was $result" }
    }

    @Test
    fun `doWork should handle null evolvesFromSpecies`() = runTest {
        // Arrange
        val pokemonId = 813
        val imageUrl = "https://example.com/scorbunny.png"
        val description = "A cute rabbit."
        coEvery { workerParameters.inputData } returns workDataOf("POKEMON_ID" to pokemonId)
        val detailResponse = PokemonDetailResponse(
            id = pokemonId,
            name = "scorbunny",
            sprites = Sprites(other = OtherSprites(officialArtwork = OfficialArtwork(frontDefault = imageUrl))),
            types = listOf(PokemonType(TypeDetail(name = "fire")))
        )
        val speciesResponse = PokemonSpeciesResponse(
            flavorTextEntries = listOf(
                FlavorTextEntry(
                    flavorText = description,
                    language = Language(name = "en")
                )
            ),
            evolvesFromSpecies = null
        )
        coEvery { pokeApiService.getPokemonDetail(pokemonId) } returns detailResponse
        coEvery { pokeApiService.getPokemonSpecies(pokemonId) } returns speciesResponse
        coEvery { pokemonDao.getTypeByName("fire") } returns null
        coEvery { pokemonDao.insertType(any()) } returns Unit
        coEvery { pokemonDao.insertPokemonType(any()) } returns Unit
        coEvery { pokemonDao.updatePokemonDetails(pokemonId, imageUrl, description, null) } returns Unit

        // Act
        val result = worker.doWork()

        // Assert
        coVerify { pokemonDao.updatePokemonDetails(pokemonId, imageUrl, description, null) }
        assert(result == Result.success()) { "Worker failed, result was $result" }
    }

    @Test
    fun `doWork should retry on API failure`() = runTest {
        // Arrange
        val pokemonId = 1
        coEvery { workerParameters.inputData } returns workDataOf("POKEMON_ID" to pokemonId)
        coEvery { pokeApiService.getPokemonDetail(pokemonId) } throws RuntimeException("API error")

        // Act
        val result = worker.doWork()

        // Assert
        assert(result == Result.retry()) { "Worker did not retry, result was $result" }
    }
}
