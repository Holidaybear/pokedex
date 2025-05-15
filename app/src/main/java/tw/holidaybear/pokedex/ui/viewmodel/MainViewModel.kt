package tw.holidaybear.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tw.holidaybear.pokedex.data.local.Pokemon
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

    private val _pokemonByType = MutableStateFlow<Map<Int, List<Pokemon>>>(emptyMap())
    val pokemonByType: StateFlow<Map<Int, List<Pokemon>>> = _pokemonByType

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchAndSync()
        observeCapturedPokemon()
        observeTypesAndPokemon()
    }

    private fun observeCapturedPokemon() {
        viewModelScope.launch {
            pokemonRepository.getCapturedPokemon().collectLatest { capturedList ->
                _capturedPokemon.value = capturedList
            }
        }
    }

    private fun observeTypesAndPokemon() {
        viewModelScope.launch {
            pokemonRepository.getTypesWithCount().collectLatest { types ->
                val updatedPokemonByType = mutableMapOf<Int, List<Pokemon>>()
                types.forEach { typeWithCount ->
                    val pokemonList = pokemonRepository.getPokemonByType(typeWithCount.type.id).firstOrNull() ?: emptyList()
                    updatedPokemonByType[typeWithCount.type.id] = pokemonList
                }
                _typesWithCount.value = types
                _pokemonByType.value = updatedPokemonByType
            }
        }
    }

    fun fetchAndSync() {
        viewModelScope.launch {
            try {
                pokemonRepository.fetchAndStorePokemonList()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Something went wrong"
            }
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
