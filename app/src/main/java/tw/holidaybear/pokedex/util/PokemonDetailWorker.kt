package tw.holidaybear.pokedex.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import tw.holidaybear.pokedex.data.local.PokemonDao
import tw.holidaybear.pokedex.data.local.PokemonType
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.data.remote.PokeApiService

@HiltWorker
class PokemonDetailWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pokemonId = inputData.getInt("POKEMON_ID", -1)
        if (pokemonId == -1) return Result.failure()

        return try {
            val detailResponse = pokeApiService.getPokemonDetail(pokemonId)
            val imageUrl = detailResponse.sprites.other.officialArtwork.frontDefault

            val speciesResponse = pokeApiService.getPokemonSpecies(pokemonId)
            val description = speciesResponse.flavorTextEntries
                .firstOrNull { it.isEnglish() }
                ?.flavorText
                ?.replace("\n", " ") ?: ""

            val evolvesFromId = speciesResponse.evolvesFromSpecies?.url
                ?.let { url -> url.split("/").lastOrNull { it.isNotBlank() }?.toIntOrNull() }

            detailResponse.types.forEach { pokemonType ->
                val typeName = pokemonType.type.name
                val existingType = pokemonDao.getTypeByName(typeName)
                val typeId = if (existingType != null) {
                    existingType.id
                } else {
                    val newTypeId = typeName.hashCode() // Use typeName hashCode for typeId
                    pokemonDao.insertType(Type(id = newTypeId, name = typeName))
                    newTypeId
                }
                pokemonDao.insertPokemonType(PokemonType(pokemonId = pokemonId, typeId = typeId))
            }

            pokemonDao.updatePokemonDetails(pokemonId, imageUrl, description, evolvesFromId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}