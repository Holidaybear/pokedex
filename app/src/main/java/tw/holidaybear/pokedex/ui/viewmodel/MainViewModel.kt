package tw.holidaybear.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.model.CapturedPokemon
import tw.holidaybear.pokedex.data.model.TypeWithCount
import tw.holidaybear.pokedex.data.repository.PokemonRepository
import javax.inject.Inject

data class MainScreenUiState(
    val isLoading: Boolean = true,
    val capturedPokemon: List<CapturedPokemon> = emptyList(),
    val typesWithCount: List<TypeWithCount> = emptyList(),
    val pokemonByType: Map<Int, List<Pokemon>> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    init {
        fetchAndSync()

        val capturedPokemonFlow = pokemonRepository.getCapturedPokemon()
        val processedPokemonAndTypesFlow = pokemonRepository.getProcessedPokemonAndTheirTypes()

        viewModelScope.launch {
            combine(
                capturedPokemonFlow,
                processedPokemonAndTypesFlow
            ) { captured, processedAndTypes ->
                val typesWithCount = processedAndTypes
                    .groupBy { it.type }
                    .map { (type, pokemonList) ->
                        TypeWithCount(type = type, count = pokemonList.size)
                    }

                val pokemonByType = processedAndTypes
                    .groupBy { it.type.id }
                    .mapValues { entry -> entry.value.map { it.pokemon } }

                MainScreenUiState(
                    isLoading = false,
                    capturedPokemon = captured,
                    typesWithCount = typesWithCount,
                    pokemonByType = pokemonByType
                )
            }.catch { e ->
                _uiState.update { it.copy(error = e.message) }
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }

    fun fetchAndSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = pokemonRepository.fetchAndStorePokemonList()
            if (result.isFailure) {
                _uiState.update {
                    it.copy(error = result.exceptionOrNull()?.message ?: "An unknown error occurred")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun capturePokemon(pokemonId: Int, categoryType: String) {
        viewModelScope.launch {
            pokemonRepository.capturePokemon(pokemonId, categoryType)
        }
    }

    fun releasePokemon(captureId: Long) {
        viewModelScope.launch {
            pokemonRepository.releasePokemon(captureId)
        }
    }
}
