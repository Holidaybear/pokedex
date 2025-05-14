package tw.holidaybear.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.repository.PokemonRepository
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _pokemon = MutableStateFlow<Pokemon?>(null)
    val pokemon: StateFlow<Pokemon?> = _pokemon

    private val _evolvesFromPokemon = MutableStateFlow<Pokemon?>(null)
    val evolvesFromPokemon: StateFlow<Pokemon?> = _evolvesFromPokemon

    fun loadPokemonDetails(pokemonId: Int) {
        viewModelScope.launch {
            val pokemonDetails = pokemonRepository.getPokemonDetails(pokemonId)
            _pokemon.value = pokemonDetails
        }
    }

    fun loadEvolvesFromPokemonDetails(evolvesFromId: Int) {
        viewModelScope.launch {
            val pokemonDetails = pokemonRepository.getPokemonDetails(evolvesFromId)
            _evolvesFromPokemon.value = pokemonDetails
        }
    }
}
