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
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.Type

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
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}
