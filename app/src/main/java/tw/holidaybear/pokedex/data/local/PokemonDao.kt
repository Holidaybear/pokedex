package tw.holidaybear.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tw.holidaybear.pokedex.data.model.TypeWithCount

@Dao
interface PokemonDao {

    @Query("SELECT t.*, COUNT(pt.pokemonId) as count " +
            "FROM type t " +
            "LEFT JOIN pokemon_type pt ON t.id = pt.typeId " +
            "LEFT JOIN pokemon p ON pt.pokemonId = p.id " +
            "WHERE p.isProcessed = 1 " +
            "GROUP BY t.id " +
            "ORDER BY t.name ASC")
    fun getTypesWithCount(): Flow<List<TypeWithCount>>

    @Query("SELECT p.* " +
            "FROM pokemon p " +
            "INNER JOIN pokemon_type pt ON p.id = pt.pokemonId " +
            "WHERE pt.typeId = :typeId AND p.isProcessed = 1")
    fun getPokemonByType(typeId: Int): Flow<List<Pokemon>>

    @Insert
    suspend fun insertPokemon(pokemon: Pokemon)

    @Insert
    suspend fun insertType(type: Type)

    @Insert
    suspend fun insertPokemonType(pokemonType: PokemonType)

    @Query("UPDATE pokemon SET isProcessed = 1, imageUrl = :imageUrl, description = :description WHERE id = :pokemonId")
    suspend fun updatePokemonDetails(pokemonId: Int, imageUrl: String, description: String?)
}
