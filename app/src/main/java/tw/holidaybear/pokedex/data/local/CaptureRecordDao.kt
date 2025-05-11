package tw.holidaybear.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tw.holidaybear.pokedex.data.model.CapturedPokemon

@Dao
interface CaptureRecordDao {

    @Query("SELECT p.*, c.captureId, c.captureTimestamp, c.categoryType " +
            "FROM pokemon p " +
            "INNER JOIN capture_record c ON p.id = c.pokemonId " +
            "WHERE p.isProcessed = 1 " +
            "ORDER BY c.captureTimestamp DESC")
    fun getCapturedPokemon(): Flow<List<CapturedPokemon>>

    @Insert
    suspend fun insertCapture(capture: CaptureRecord)

    @Query("DELETE FROM capture_record WHERE captureId = :captureId")
    suspend fun deleteCapture(captureId: Long)
}
