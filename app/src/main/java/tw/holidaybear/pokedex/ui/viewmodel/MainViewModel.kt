package tw.holidaybear.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tw.holidaybear.pokedex.data.model.CapturedPokemon
import tw.holidaybear.pokedex.data.model.TypeWithCount
import tw.holidaybear.pokedex.data.repository.PokemonRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _capturedPokemon = MutableStateFlow<List<CapturedPokemon>>(emptyList())
    val capturedPokemon: StateFlow<List<CapturedPokemon>> = _capturedPokemon

    private val _typesWithCount = MutableStateFlow<List<TypeWithCount>>(emptyList())
    val typesWithCount: StateFlow<List<TypeWithCount>> = _typesWithCount

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchAndSync()
        viewModelScope.launch {
            pokemonRepository.getCapturedPokemon().collectLatest { capturedList ->
                _capturedPokemon.value = capturedList
            }
        }
        viewModelScope.launch {
            pokemonRepository.getTypesWithCount().collectLatest { typesList ->
                _typesWithCount.value = typesList.filter { it.count > 0 }
            }
        }
    }

    fun fetchAndSync() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                pokemonRepository.fetchAndStorePokemonList()
            } catch (e: Exception) {
                _error.value = e.message ?: "同步失敗"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPokemonByType(typeId: Int) = pokemonRepository.getPokemonByType(typeId)

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

