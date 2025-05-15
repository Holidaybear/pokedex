package tw.holidaybear.pokedex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.verify
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.ui.screen.DetailScreen
import tw.holidaybear.pokedex.ui.viewmodel.DetailViewModel

@RunWith(AndroidJUnit4::class)
class DetailScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pokemonInfo_showsCorrectly() {
        val testPokemon = Pokemon(1, "bulbasaur", "", "desc", null, true)
        val mockViewModel = mockk<DetailViewModel>(relaxed = true)
        every { mockViewModel.pokemon } returns MutableStateFlow(testPokemon)
        every { mockViewModel.types } returns MutableStateFlow(listOf(Type(12, "grass")))
        every { mockViewModel.prevPokemon } returns MutableStateFlow(null)

        composeTestRule.setContent {
            DetailScreen(
                navController = rememberNavController(),
                pokemonId = 1,
                viewModel = mockViewModel
            )
        }
        // Check id, name, description, image exist
        composeTestRule.onNodeWithText("#1", substring = true).assertExists()
        composeTestRule.onNodeWithText("Bulbasaur", substring = true).assertExists()
        composeTestRule.onNodeWithText("desc", substring = true).assertExists()
        composeTestRule.onNodeWithTag("PokemonImage_1").assertExists()
    }

    @Test
    fun types_areShownAsChips() {
        val testPokemon = Pokemon(1, "bulbasaur", "", "", null, true)
        val mockViewModel = mockk<DetailViewModel>(relaxed = true)
        every { mockViewModel.pokemon } returns MutableStateFlow(testPokemon)
        every { mockViewModel.types } returns MutableStateFlow(listOf(Type(12, "grass"), Type(4, "poison")))
        every { mockViewModel.prevPokemon } returns MutableStateFlow(null)

        composeTestRule.setContent {
            DetailScreen(
                navController = rememberNavController(),
                pokemonId = 1,
                viewModel = mockViewModel
            )
        }
        // Check type tag exist
        composeTestRule.onNodeWithText("grass").assertExists()
        composeTestRule.onNodeWithText("poison").assertExists()
    }

    @Test
    fun prevPokemon_evolutionChain_shown() {
        val testPokemon = Pokemon(2, "ivysaur", "", "", 1, true)
        val prevPokemon = Pokemon(1, "bulbasaur", "", "", null, true)
        val mockViewModel = mockk<DetailViewModel>(relaxed = true)
        every { mockViewModel.pokemon } returns MutableStateFlow(testPokemon)
        every { mockViewModel.types } returns MutableStateFlow(listOf(Type(12, "grass")))
        every { mockViewModel.prevPokemon } returns MutableStateFlow(prevPokemon)

        composeTestRule.setContent {
            DetailScreen(
                navController = rememberNavController(),
                pokemonId = 2,
                viewModel = mockViewModel
            )
        }
        // Check evolution info exist
        composeTestRule.onNodeWithText("Evolves from", substring = true).assertExists()
        composeTestRule.onNodeWithText("Bulbasaur", substring = true).assertExists()
        composeTestRule.onNodeWithTag("PrevPokemonImage_1", useUnmergedTree = true).assertExists()
    }

    @Test
    fun tapPrevPokemon_navigatesToPrevPokemonDetailScreen() {
        val testPokemon = Pokemon(2, "ivysaur", "", "", 1, true)
        val prevPokemon = Pokemon(1, "bulbasaur", "", "", null, true)
        val mockViewModel = mockk<DetailViewModel>(relaxed = true)
        every { mockViewModel.pokemon } returns MutableStateFlow(testPokemon)
        every { mockViewModel.types } returns MutableStateFlow(listOf(Type(12, "grass")))
        every { mockViewModel.prevPokemon } returns MutableStateFlow(prevPokemon)

        val mockNavController = mockk<NavController>(relaxed = true)

        composeTestRule.setContent {
            DetailScreen(
                navController = mockNavController,
                pokemonId = 2,
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithTag("PrevPokemon_1").performClick()
        verify { mockNavController.navigate("detail/1") }
    }

    @Test
    fun backButton_navigatesBack() {
        val testPokemon = Pokemon(1, "bulbasaur", "", "", null, true)
        val mockViewModel = mockk<DetailViewModel>(relaxed = true)
        every { mockViewModel.pokemon } returns MutableStateFlow(testPokemon)
        every { mockViewModel.types } returns MutableStateFlow(listOf(Type(12, "Grass")))
        every { mockViewModel.prevPokemon } returns MutableStateFlow(null)

        val mockNavController = mockk<NavController>(relaxed = true)

        composeTestRule.setContent {
            DetailScreen(
                navController = mockNavController,
                pokemonId = 1,
                viewModel = mockViewModel
            )
        }
        composeTestRule.onNodeWithTag("BackButton").performClick()
        verify { mockNavController.navigateUp() }
    }
}
