package tw.holidaybear.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction
import tw.holidaybear.pokedex.data.model.PokemonAndType
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

    @Transaction
    @Query("SELECT p.id AS pokemon_id, p.name AS pokemon_name, p.imageUrl AS pokemon_imageUrl, " +
            "p.description AS pokemon_description, p.evolvesFromId AS pokemon_evolvesFromId, p.isProcessed AS pokemon_isProcessed, " +
            "t.id AS type_id, t.name AS type_name " +
            "FROM pokemon p " +
            "INNER JOIN pokemon_type pt ON p.id = pt.pokemonId " +
            "INNER JOIN type t ON pt.typeId = t.id " +
            "WHERE p.isProcessed = 1 " +
            "ORDER BY t.name ASC, p.id ASC")
    fun getProcessedPokemonAndTheirTypes(): Flow<List<PokemonAndType>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPokemon(pokemon: Pokemon)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertType(type: Type)

    @Insert
    suspend fun insertPokemonType(pokemonType: PokemonType)

    @Query("UPDATE pokemon SET isProcessed = 1, imageUrl = :imageUrl, description = :description, evolvesFromId = :evolvesFromId WHERE id = :pokemonId")
    suspend fun updatePokemonDetails(pokemonId: Int, imageUrl: String, description: String?, evolvesFromId: Int?)

    @Query("SELECT COUNT(*) FROM pokemon WHERE isProcessed = 1")
    suspend fun getProcessedPokemonCount(): Int

    @Query("SELECT id FROM pokemon WHERE isProcessed = 0")
    suspend fun getUnprocessedPokemonIds(): List<Int>

    @Query("SELECT * FROM type WHERE name = :name LIMIT 1")
    suspend fun getTypeByName(name: String): Type?

    @Query("SELECT * FROM pokemon WHERE id = :pokemonId")
    suspend fun getPokemonById(pokemonId: Int): Pokemon?

    @Query("SELECT t.* " +
            "FROM type t " +
            "INNER JOIN pokemon_type pt ON t.id = pt.typeId " +
            "WHERE pt.pokemonId = :pokemonId")
    fun getTypesForPokemon(pokemonId: Int): Flow<List<Type>>
}
