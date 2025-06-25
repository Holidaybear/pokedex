package tw.holidaybear.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val processedPokemonAndTypes = pokemonRepository.getProcessedPokemonAndTheirTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val typesWithCount: StateFlow<List<TypeWithCount>> = processedPokemonAndTypes
        .combine(MutableStateFlow(Unit)) { pokemonAndTypes, _ ->
            pokemonAndTypes
                .groupBy { it.type }
                .map { (type, pokemonList) ->
                    TypeWithCount(type = type, count = pokemonList.size)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pokemonByType: StateFlow<Map<Int, List<Pokemon>>> = processedPokemonAndTypes
        .combine(MutableStateFlow(Unit)) { pokemonAndTypes, _ ->
            pokemonAndTypes
                .groupBy { it.type.id }
                .mapValues { entry -> entry.value.map { it.pokemon } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


    init {
        fetchAndSync()
        observeCapturedPokemon()
    }

    private fun observeCapturedPokemon() {
        viewModelScope.launch {
            pokemonRepository.getCapturedPokemon().collectLatest { capturedList ->
                _capturedPokemon.value = capturedList
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
