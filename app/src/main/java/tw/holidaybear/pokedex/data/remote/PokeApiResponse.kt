package tw.holidaybear.pokedex.data.remote

import com.google.gson.annotations.SerializedName

data class PokemonListResponse(
    @SerializedName("results")
    val results: List<PokemonListItem>
)

data class PokemonListItem(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String // e.g., "https://pokeapi.co/api/v2/pokemon/813/"
) {
    // Extract ID from URL (e.g., "https://pokeapi.co/api/v2/pokemon/813/" -> 813)
    val id: Int
        get() = url.split("/").last { it.isNotEmpty() }.toInt()
}

data class PokemonDetailResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("sprites")
    val sprites: Sprites,
    @SerializedName("types")
    val types: List<PokemonType>
)

data class Sprites(
    @SerializedName("other")
    val other: OtherSprites
)

data class OtherSprites(
    @SerializedName("official-artwork")
    val officialArtwork: OfficialArtwork
)

data class OfficialArtwork(
    @SerializedName("front_default")
    val frontDefault: String // e.g., "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/813.png"
)

data class PokemonType(
    @SerializedName("type")
    val type: TypeDetail
)

data class TypeDetail(
    @SerializedName("name")
    val name: String // e.g., "fire", "flying"
)

data class PokemonSpeciesResponse(
    @SerializedName("flavor_text_entries")
    val flavorTextEntries: List<FlavorTextEntry>,
    @SerializedName("evolves_from_species")
    val evolvesFromSpecies: EvolvesFromSpecies?
)

data class FlavorTextEntry(
    @SerializedName("flavor_text")
    val flavorText: String,
    @SerializedName("language")
    val language: Language
) {
    fun isEnglish(): Boolean = language.name == "en"
}

data class Language(
    @SerializedName("name")
    val name: String // e.g., "en"
)

data class EvolvesFromSpecies(
    @SerializedName("name")
    val name: String // e.g., "scorbunny" for raboot
)