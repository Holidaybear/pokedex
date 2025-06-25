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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import tw.holidaybear.pokedex.R
import tw.holidaybear.pokedex.ui.component.CapturedPokemonList
import tw.holidaybear.pokedex.ui.component.TypeListItem
import tw.holidaybear.pokedex.ui.viewmodel.MainViewModel
import tw.holidaybear.pokedex.ui.navigation.Screen

@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
