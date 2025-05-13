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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tw.holidaybear.pokedex.ui.component.CapturedPokemonList
import tw.holidaybear.pokedex.ui.component.TypeListItem
import tw.holidaybear.pokedex.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val error by viewModel.error.collectAsStateWithLifecycle()
    val capturedPokemon by viewModel.capturedPokemon.collectAsStateWithLifecycle()
    val typesWithCount by viewModel.typesWithCount.collectAsStateWithLifecycle()

    when {
        error != null -> {
            // Error Handling
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error ?: "Something Wrong", color = Color.Black, modifier = Modifier.padding(16.dp))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchAndSync() }) {
                        Text("Retry")
                    }
                }
            }
        }
        typesWithCount.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            val lazyListState = rememberLazyListState()
            val capturedListHeight = 200.dp

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    CapturedPokemonList(
                        capturedPokemon = capturedPokemon,
                        capturedCount = capturedPokemon.size,
                        onRelease = { captureId -> viewModel.releasePokemon(captureId) },
                        modifier = Modifier
                            .height(capturedListHeight)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
                items(typesWithCount) { typeWithCount ->
                    TypeListItem(
                        type = typeWithCount.type,
                        count = typeWithCount.count,
                        pokemonList = viewModel.getPokemonByType(typeWithCount.type.id)
                            .collectAsStateWithLifecycle(initialValue = emptyList()).value,
                        onCapture = { pokemonId -> viewModel.capturePokemon(pokemonId, typeWithCount.type.name) }
                    )
                }
            }
        }
    }
}
