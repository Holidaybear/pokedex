package tw.holidaybear.pokedex.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tw.holidaybear.pokedex.data.model.CapturedPokemon

@Composable
fun CapturedPokemonList(
    capturedPokemon: List<CapturedPokemon>,
    capturedCount: Int,
    onRelease: (Long) -> Unit,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                    text = "My Pocket",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )
                Text(
                    text = capturedCount.toString(),
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
                items(capturedPokemon) { captured ->
                    CapturedPokemonCard(
                        capturedPokemon = captured,
                        onRelease = { onRelease(captured.captureId) },
                        onCardClick = { onCardClick(captured.pokemon.id) },
                        modifier = Modifier.width(110.dp)
                    )
                }
            }
        }
        HorizontalDivider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}
