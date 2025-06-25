package tw.holidaybear.pokedex.data.model

import androidx.room.Embedded
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.Type

data class CapturedPokemon(
    @Embedded val pokemon: Pokemon,
    val captureId: Long,
    val captureTimestamp: Long,
    val categoryType: String
)

// For dynamic loading of Type List
data class TypeWithCount(
    @Embedded val type: Type,
    val count: Int // Number of processed Pokemon in this type
)

data class PokemonAndType(
    @Embedded val pokemon: Pokemon,
    @Embedded val type: Type
)
