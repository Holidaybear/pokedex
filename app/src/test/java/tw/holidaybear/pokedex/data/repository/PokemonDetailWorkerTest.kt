package tw.holidaybear.pokedex.data.repository

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import tw.holidaybear.pokedex.data.local.PokemonDao
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
        coEvery { workerParameters.inputData } returns workDataOf("POKEMON_ID" to 1)
        worker = PokemonDetailWorker(context, workerParameters, pokeApiService, pokemonDao)
    }

    @Test
    fun `doWork should update imageUrl and description in Pokemon entity`() = runTest {
        // Arrange
        val pokemonId = 813
        val imageUrl = "https://example.com/scorbunny.png"
        val description = "A cute rabbit."
        val detailResponse = PokemonDetailResponse(
            id = pokemonId,
            name = "scorbunny",
            sprites = Sprites(other = OtherSprites(officialArtwork = OfficialArtwork(frontDefault = imageUrl))),
            types = listOf(PokemonType(TypeDetail(name = "grass")))
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
        coEvery { pokemonDao.insertType(any()) } returns Unit
        coEvery { pokemonDao.insertPokemonType(any()) } returns Unit
        coEvery { pokemonDao.updatePokemonDetails(pokemonId, imageUrl, description) } returns Unit

        // Act
        val result = worker.doWork()

        // Assert
        coVerify { pokemonDao.updatePokemonDetails(pokemonId, imageUrl, description) }
        assert(result == androidx.work.ListenableWorker.Result.success()) { "Worker failed, result was $result" }
    }

    @Test
    fun `doWork should retry on API failure`() = runTest {
        // Arrange
        val pokemonId = 1
        coEvery { pokeApiService.getPokemonDetail(pokemonId) } throws RuntimeException("API error")

        // Act
        val result = worker.doWork()

        // Assert
        assert(result == androidx.work.ListenableWorker.Result.retry()) { "Worker did not retry, result was $result" }
    }
}
