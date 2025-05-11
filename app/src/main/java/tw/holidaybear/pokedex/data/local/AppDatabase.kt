package tw.holidaybear.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Pokemon::class, Type::class, PokemonType::class, CaptureRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun captureRecordDao(): CaptureRecordDao
}
