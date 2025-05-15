package tw.holidaybear.pokedex.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import tw.holidaybear.pokedex.ui.viewmodel.DetailViewModel

@Composable
fun DetailScreen(navController: NavController, pokemonId: Int, viewModel: DetailViewModel = hiltViewModel()) {
    val pokemon = viewModel.pokemon.collectAsState().value

    if (pokemon == null) {
        viewModel.loadPokemonDetails(pokemonId)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "#${pokemon.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(pokemon.imageUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build(),
            contentDescription = "${pokemon.name} image",
            modifier = Modifier
                .size(150.dp)
                .padding(top = 16.dp),
        )

        Text(
            text = pokemon.name.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(top = 16.dp),
        )

        pokemon.evolvesFromId?.let { evolvesFromId ->
            val evolvesFromPokemon = viewModel.evolvesFromPokemon.collectAsState().value
            viewModel.loadEvolvesFromPokemonDetails(evolvesFromId)
            Row(
                modifier = Modifier
                    .clickable {
                        navController.navigate("detail/$evolvesFromId")
                    }
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Evolves from",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = evolvesFromPokemon?.name?.replaceFirstChar { it.uppercase() } ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(evolvesFromPokemon?.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${evolvesFromPokemon?.name} image",
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = pokemon.description ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
