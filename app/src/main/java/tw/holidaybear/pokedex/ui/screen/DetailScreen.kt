package tw.holidaybear.pokedex.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import tw.holidaybear.pokedex.R
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.ui.navigation.Screen
import tw.holidaybear.pokedex.ui.theme.Gray300
import tw.holidaybear.pokedex.ui.theme.PokedexTheme
import tw.holidaybear.pokedex.ui.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    navController: NavController,
    pokemonId: Int,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val pokemon = viewModel.pokemon.collectAsState().value
    val types = viewModel.types.collectAsState().value
    val prevPokemon = viewModel.prevPokemon.collectAsState().value

    if (pokemon == null) {
        viewModel.loadPokemonDetails(pokemonId)
        // You can show a loading indicator here
        return
    }

    DetailScreenContent(navController, pokemon, types, prevPokemon)
}

@Composable
fun DetailScreenContent(
    navController: NavController,
    pokemon: Pokemon,
    types: List<Type>,
    prevPokemon: Pokemon?
) {
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
                onClick = { navController.navigateUp() },
                modifier = Modifier.testTag("BackButton")
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
                .padding(top = 16.dp)
                .testTag("PokemonImage_${pokemon.id}")
        )

        Text(
            text = pokemon.name.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(top = 16.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            types.forEach { type ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Gray300,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(36.dp)
                ) {
                    Text(
                        text = type.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        prevPokemon?.let { prev ->
            Row(
                modifier = Modifier
                    .clickable { 
                        navController.navigate(Screen.Detail.createRoute(prev.id))
                    }
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .testTag("PrevPokemon_${prev.id}")
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.evolves_from),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = prev.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(prev.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${prev.name} image",
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("PrevPokemonImage_${prev.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = pokemon.description ?: "",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    val navController = rememberNavController()
    val pokemon = Pokemon(
        id = 6,
        name = "charizard",
        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png",
        description = "Charizard flies around the sky in search of powerful opponents. It breathes fire of such great heat that it melts anything. However, it never turns its fiery breath on any opponent weaker than itself.",
        evolvesFromId = 5,
        isProcessed = true
    )
    val types = listOf(Type(10, "fire"), Type(3, "flying"))
    val prevPokemon = Pokemon(
        id = 5,
        name = "charmeleon",
        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/5.png",
        isProcessed = true
    )

    PokedexTheme {
        DetailScreenContent(navController, pokemon, types, prevPokemon)
    }
}