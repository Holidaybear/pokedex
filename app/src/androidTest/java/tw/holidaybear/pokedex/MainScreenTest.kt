package tw.holidaybear.pokedex

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
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
import tw.holidaybear.pokedex.data.model.TypeWithCount
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.ui.navigation.Screen
import tw.holidaybear.pokedex.ui.screen.MainScreen
import tw.holidaybear.pokedex.ui.viewmodel.MainScreenUiState
import tw.holidaybear.pokedex.ui.viewmodel.MainViewModel

@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun hasTestTagPrefix(prefix: String): SemanticsMatcher = SemanticsMatcher("TestTag starts with $prefix") { node ->
        val tag = node.config.getOrNull(SemanticsProperties.TestTag)
        tag != null && tag.startsWith(prefix)
    }

    @Test
    fun progressIndicator_showsDuringLoading() {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(MainScreenUiState(isLoading = true))

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                viewModel = mockViewModel
            )
        }
        composeTestRule.onNodeWithTag("ProgressIndicator").assertExists()
    }

    @Test
    fun errorMessageAndRetryButton_shownOnError() {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(MainScreenUiState(isLoading = false, error = "Network error"))

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                viewModel = mockViewModel
            )
        }
        composeTestRule.onNodeWithTag("ErrorMessage").assertExists()
        composeTestRule.onNodeWithTag("RetryButton").assertExists()
    }

    @Test
    fun tapPokemonCard_navigatesToDetailScreen() {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val mockNavController = mockk<NavController>(relaxed = true)
        val testPokemon = Pokemon(3, "venusaur", "", "", null, true)
        val typeWithCount = TypeWithCount(type = Type(12, "grass"), count = 1)
        val initialState = MainScreenUiState(
            isLoading = false,
            typesWithCount = listOf(typeWithCount),
            pokemonByType = mapOf(12 to listOf(testPokemon))
        )
        every { mockViewModel.uiState } returns MutableStateFlow(initialState)

        composeTestRule.setContent {
            MainScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        composeTestRule.onAllNodes(hasTestTagPrefix("PokemonCard_")).onFirst().performClick()

        verify { mockNavController.navigate(Screen.Detail.createRoute(3)) }
    }
}
