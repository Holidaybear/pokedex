package tw.holidaybear.pokedex.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import tw.holidaybear.pokedex.R
import tw.holidaybear.pokedex.data.model.CapturedPokemon

@Composable
fun CapturedPokemonCard(
    capturedPokemon: CapturedPokemon,
    onRelease: () -> Unit,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .testTag("CapturedPokemonCard_${capturedPokemon.captureId}")
            .clickable(enabled = onCardClick != null) { onCardClick?.invoke() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(capturedPokemon.pokemon.imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = capturedPokemon.pokemon.name,
                modifier = Modifier.size(100.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_pokeball),
                contentDescription = "Release",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .testTag("ReleasePokeball_${capturedPokemon.captureId}")
                    .clickable { onRelease() }
            )
        }
        Text(
            text = capturedPokemon.pokemon.name.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
