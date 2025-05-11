package tw.holidaybear.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class Pokemon(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val description: String? = null,
    val isProcessed: Boolean = false
)

@Entity(tableName = "type")
data class Type(
    @PrimaryKey val id: Int,
    val name: String
)

@Entity(tableName = "pokemon_type", primaryKeys = ["pokemonId", "typeId"])
data class PokemonType(
    val pokemonId: Int,
    val typeId: Int
)

@Entity(tableName = "capture_record")
data class CaptureRecord(
    @PrimaryKey(autoGenerate = true) val captureId: Long,
    val pokemonId: Int,
    val captureTimestamp: Long,
    val categoryType: String
)
