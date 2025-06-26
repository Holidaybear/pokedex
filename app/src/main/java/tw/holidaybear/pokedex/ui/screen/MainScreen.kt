package tw.holidaybear.pokedex.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.mockk.mockk
import tw.holidaybear.pokedex.R
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.data.model.CapturedPokemon
import tw.holidaybear.pokedex.data.model.TypeWithCount
import tw.holidaybear.pokedex.ui.component.CapturedPokemonList
import tw.holidaybear.pokedex.ui.component.TypeListItem
import tw.holidaybear.pokedex.ui.navigation.Screen
import tw.holidaybear.pokedex.ui.theme.PokedexTheme
import tw.holidaybear.pokedex.ui.viewmodel.MainScreenUiState
import tw.holidaybear.pokedex.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreenContent(uiState = uiState, navController = navController, viewModel = viewModel)
}

@Composable
fun MainScreenContent(
    uiState: MainScreenUiState,
    navController: NavController,
    viewModel: MainViewModel
) {
    when {
        uiState.error != null -> {
            // Error Handling
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.error ?: "Something went wrong",
                        color = Color.Black,
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("ErrorMessage")
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchAndSync() },
                        modifier = Modifier.testTag("RetryButton")
                    ) {
                        Text(stringResource(id = R.string.retry))
                    }
                }
            }
        }

        uiState.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("ProgressIndicator"))
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "captured_list") {
                    CapturedPokemonList(
                        capturedPokemon = uiState.capturedPokemon,
                        capturedCount = uiState.capturedPokemon.size,
                        onRelease = { captureId -> viewModel.releasePokemon(captureId) },
                        onCardClick = { pokemonId -> navController.navigate(Screen.Detail.createRoute(pokemonId)) },
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
                items(
                    items = uiState.typesWithCount,
                    key = { typeWithCount -> typeWithCount.type.id }
                ) { typeWithCount ->
                    TypeListItem(
                        type = typeWithCount.type,
                        count = typeWithCount.count,
                        pokemonList = uiState.pokemonByType[typeWithCount.type.id] ?: emptyList(),
                        onCapture = { pokemonId ->
                            viewModel.capturePokemon(
                                pokemonId,
                                typeWithCount.type.name
                            )
                        },
                        onCardClick = { pokemonId -> navController.navigate(Screen.Detail.createRoute(pokemonId)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    val pokemon1 = Pokemon(1, "bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png", isProcessed = true)
    val pokemon4 = Pokemon(4, "charmander", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png", isProcessed = true)
    val pokemon7 = Pokemon(7, "squirtle", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png", isProcessed = true)

    val previewState = MainScreenUiState(
        isLoading = false,
        capturedPokemon = listOf(
            CapturedPokemon(pokemon1, 1L, 0L, "grass")
        ),
        typesWithCount = listOf(
            TypeWithCount(Type(12, "grass"), 1),
            TypeWithCount(Type(10, "fire"), 1),
            TypeWithCount(Type(11, "water"), 1)
        ),
        pokemonByType = mapOf(
            12 to listOf(pokemon1),
            10 to listOf(pokemon4),
            11 to listOf(pokemon7)
        )
    )

    PokedexTheme {
        MainScreenContent(
            uiState = previewState,
            navController = navController,
            viewModel = mockk(relaxed = true) // Mock ViewModel for preview
        )
    }
}