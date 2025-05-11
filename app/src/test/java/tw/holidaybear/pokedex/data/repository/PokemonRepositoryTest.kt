package tw.holidaybear.pokedex.data.repository

import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import tw.holidaybear.pokedex.data.local.CaptureRecordDao
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.PokemonDao
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.data.model.CapturedPokemon
import tw.holidaybear.pokedex.data.model.TypeWithCount
import tw.holidaybear.pokedex.data.remote.PokeApiService
import tw.holidaybear.pokedex.data.remote.PokemonListItem
import tw.holidaybear.pokedex.data.remote.PokemonListResponse

class PokemonRepositoryTest {

    private lateinit var repository: PokemonRepository
    private val pokeApiService: PokeApiService = mockk()
    private val pokemonDao: PokemonDao = mockk()
    private val captureRecordDao: CaptureRecordDao = mockk()
    private val workManager: WorkManager = mockk()

    @Before
    fun setup() {
        every { workManager.enqueue(any<OneTimeWorkRequest>()) } returns mockk<Operation>()
        repository = PokemonRepository(pokeApiService, pokemonDao, captureRecordDao, workManager)
    }

    @Test
    fun `fetchAndStorePokemonList should fetch from API and store in Room`() = runTest {
        // Arrange
        val pokemonListResponse = PokemonListResponse(
            results = listOf(
                PokemonListItem(name = "scorbunny", url = "https://pokeapi.co/api/v2/pokemon/813/")
            )
        )
        coEvery { pokeApiService.getPokemonList(151) } returns pokemonListResponse
        coEvery { pokemonDao.insertPokemon(any()) } returns Unit

        // Act
        repository.fetchAndStorePokemonList()

        // Assert
        coVerify { pokemonDao.insertPokemon(Pokemon(id = 813, name = "scorbunny", imageUrl = "", isProcessed = false)) }
        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `getTypesWithCount should return types with count from DAO`() = runTest {
        // Arrange
        val type = Type(id = 1, name = "Fire")
        val typeWithCount = TypeWithCount(type = type, count = 3)
        every { pokemonDao.getTypesWithCount() } returns flowOf(listOf(typeWithCount))

        // Act
        val result = repository.getTypesWithCount()

        // Assert
        result.collect { types ->
            assert(types.size == 1)
            assert(types[0].type.name == "Fire")
            assert(types[0].count == 3)
        }
    }

    @Test
    fun `getPokemonByType should return Pokemon for given type from DAO`() = runTest {
        // Arrange
        val typeId = 1
        val pokemon = Pokemon(id = 1, name = "Scorbunny", imageUrl = "url", isProcessed = true)
        every { pokemonDao.getPokemonByType(typeId) } returns flowOf(listOf(pokemon))

        // Act
        val result = repository.getPokemonByType(typeId)

        // Assert
        result.collect { pokemonList ->
            assert(pokemonList.size == 1)
            assert(pokemonList[0].name == "Scorbunny")
        }
    }

    @Test
    fun `capturePokemon should insert capture record into DAO`() = runTest {
        // Arrange
        val pokemonId = 1
        val categoryType = "Fire"
        coEvery { captureRecordDao.insertCapture(any()) } returns Unit

        // Act
        repository.capturePokemon(pokemonId, categoryType)

        // Assert
        coVerify { captureRecordDao.insertCapture(any()) }
    }

    @Test
    fun `releasePokemon should delete capture record from DAO`() = runTest {
        // Arrange
        val captureId = 1L
        coEvery { captureRecordDao.deleteCapture(captureId) } returns Unit

        // Act
        repository.releasePokemon(captureId)

        // Assert
        coVerify { captureRecordDao.deleteCapture(captureId) }
    }

    @Test
    fun `getCapturedPokemon should return captured Pokemon from DAO`() = runTest {
        // Arrange
        val pokemon = Pokemon(id = 1, name = "Scorbunny", imageUrl = "url", isProcessed = true)
        val capturedPokemon = CapturedPokemon(pokemon, captureId = 1L, captureTimestamp = 1234567890L, categoryType = "Fire")
        every { captureRecordDao.getCapturedPokemon() } returns flowOf(listOf(capturedPokemon))

        // Act
        val result = repository.getCapturedPokemon()

        // Assert
        result.collect { capturedList ->
            assert(capturedList.size == 1)
            assert(capturedList[0].pokemon.name == "Scorbunny")
            assert(capturedList[0].categoryType == "Fire")
        }
    }
}
