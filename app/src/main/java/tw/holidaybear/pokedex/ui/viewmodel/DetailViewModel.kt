package tw.holidaybear.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.data.repository.PokemonRepository
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _pokemon = MutableStateFlow<Pokemon?>(null)
    val pokemon: StateFlow<Pokemon?> = _pokemon

    private val _types = MutableStateFlow<List<Type>>(emptyList())
    val types: StateFlow<List<Type>> = _types

    private val _prevPokemon = MutableStateFlow<Pokemon?>(null)
    val prevPokemon: StateFlow<Pokemon?> = _prevPokemon

    fun loadPokemonDetails(pokemonId: Int) {
        viewModelScope.launch {
            // Ensure species info is loaded before observing the details
            pokemonRepository.ensureSpeciesInfoIsLoaded(pokemonId)
                .onFailure {
                    // Optionally handle the failure, e.g., show a toast or log it
                    // For now, we'll just let the UI show the data without description
                }

            pokemonRepository.getPokemonDetails(pokemonId)?.let { pokemon ->
                _pokemon.value = pokemon
                pokemon.evolvesFromId?.let { evolvesFromId ->
                    _prevPokemon.value = pokemonRepository.getPokemonDetails(evolvesFromId)
                }
            }
            pokemonRepository.getTypesForPokemon(pokemonId).collect { types ->
                _types.value = types
            }
        }
    }
}
