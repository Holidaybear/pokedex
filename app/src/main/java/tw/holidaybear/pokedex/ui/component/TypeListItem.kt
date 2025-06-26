package tw.holidaybear.pokedex.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.ui.theme.PokedexTheme

@Composable
fun TypeListItem(
    type: Type,
    count: Int,
    pokemonList: List<Pokemon>,
    onCapture: (Int) -> Unit,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = type.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 16.dp)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 16.dp
            )
        ) {
            items(
                items = pokemonList,
                key = { pokemon -> pokemon.id }
            ) { pokemon ->
                PokemonCard(
                    pokemon = pokemon,
                    onCapture = { onCapture(pokemon.id) },
                    onCardClick = { onCardClick(pokemon.id) },
                    modifier = Modifier.width(110.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TypeListItemPreview() {
    val previewType = Type(id = 4, name = "fire")
    val previewPokemonList = listOf(
        Pokemon(id = 4, name = "charmander", imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png", isProcessed = true),
        Pokemon(id = 5, name = "charmeleon", imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/5.png", isProcessed = true),
        Pokemon(id = 6, name = "charizard", imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png", isProcessed = true)
    )
    PokedexTheme {
        TypeListItem(
            type = previewType,
            count = previewPokemonList.size,
            pokemonList = previewPokemonList,
            onCapture = {},
            onCardClick = {}
        )
    }
}
