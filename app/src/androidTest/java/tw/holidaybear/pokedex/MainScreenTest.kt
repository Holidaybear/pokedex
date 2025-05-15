package tw.holidaybear.pokedex

import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import tw.holidaybear.pokedex.data.local.Pokemon
import tw.holidaybear.pokedex.data.model.CapturedPokemon
import tw.holidaybear.pokedex.data.model.TypeWithCount
import tw.holidaybear.pokedex.data.local.Type
import tw.holidaybear.pokedex.ui.screen.MainScreen
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
        every { mockViewModel.typesWithCount } returns MutableStateFlow(emptyList())
        every { mockViewModel.error } returns MutableStateFlow(null)
        every { mockViewModel.capturedPokemon } returns MutableStateFlow(emptyList())
        every { mockViewModel.pokemonByType } returns MutableStateFlow(emptyMap())

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
        every { mockViewModel.typesWithCount } returns MutableStateFlow(emptyList())
        every { mockViewModel.error } returns MutableStateFlow("Network error")
        every { mockViewModel.capturedPokemon } returns MutableStateFlow(emptyList())
        every { mockViewModel.pokemonByType } returns MutableStateFlow(emptyMap())

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
        val testPokemon = Pokemon(3, "venusaur", "", "", null, true)
        val typeWithCount = TypeWithCount(type = Type(12, "grass"), count = 1)
        every { mockViewModel.typesWithCount } returns MutableStateFlow(listOf(typeWithCount))
        every { mockViewModel.pokemonByType } returns MutableStateFlow(mapOf(12 to listOf(testPokemon)))
        every { mockViewModel.capturedPokemon } returns MutableStateFlow(emptyList())
        every { mockViewModel.error } returns MutableStateFlow(null)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "main"
            ) {
                composable("main") {
                    MainScreen(navController = navController, viewModel = mockViewModel)
                }
                composable("detail/{pokemonId}") { backStackEntry ->
                    Text("Detail for ${backStackEntry.arguments?.getString("pokemonId")}")
                }
            }
        }
        composeTestRule.onAllNodes(hasTestTagPrefix("PokemonCard_")).onFirst().performClick()
        composeTestRule.onNodeWithText("Detail for 3").assertExists()
    }

    @Test
    fun capturedPokemonList_displayAndRelease() {
        val testPokemon = Pokemon(1, "bulbasaur", "", "", null, true)
        val captured = CapturedPokemon(testPokemon, 100L, 123456L, "grass")
        val capturedFlow = MutableStateFlow(listOf(captured))
        val typeWithCount = TypeWithCount(type = Type(12, "Grass"), count = 1)
        val typesWithCountFlow = MutableStateFlow(listOf(typeWithCount))
        val pokemonByTypeFlow = MutableStateFlow(mapOf(12 to listOf(testPokemon)))
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.capturedPokemon } returns capturedFlow
        every { mockViewModel.typesWithCount } returns typesWithCountFlow
        every { mockViewModel.pokemonByType } returns pokemonByTypeFlow
        every { mockViewModel.error } returns MutableStateFlow(null)
        every { mockViewModel.releasePokemon(any()) } answers { capturedFlow.value = emptyList() }

        composeTestRule.setContent {
            MainScreen(navController = rememberNavController(), viewModel = mockViewModel)
        }
        // Should be 1 captured Pokemon card
        composeTestRule.onAllNodes(hasTestTagPrefix("CapturedPokemonCard_")).assertCountEquals(1)
        // Click the release button
        composeTestRule.onAllNodes(hasTestTagPrefix("ReleasePokeball_")).onFirst().performClick()
        // The card should be removed after release
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasTestTagPrefix("CapturedPokemonCard_")).assertCountEquals(0)
    }

    @Test
    fun typeList_capturePokemonAndAllowDuplicates() {
        val testPokemon = Pokemon(2, "ivysaur", "", "", null, true)
        val typeWithCount = TypeWithCount(type = Type(12, "Grass"), count = 1)
        val capturedList = mutableListOf<CapturedPokemon>()
        val capturedFlow = MutableStateFlow<List<CapturedPokemon>>(emptyList())
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.typesWithCount } returns MutableStateFlow(listOf(typeWithCount))
        every { mockViewModel.pokemonByType } returns MutableStateFlow(mapOf(12 to listOf(testPokemon)))
        every { mockViewModel.capturedPokemon } returns capturedFlow
        every { mockViewModel.error } returns MutableStateFlow(null)
        every { mockViewModel.capturePokemon(any(), any()) } answers {
            // Allow capturing the same Pokemon multiple times
            val newCapture = CapturedPokemon(testPokemon, (100L..999L).random(), System.currentTimeMillis(), "grass")
            capturedList.add(newCapture)
            capturedFlow.value = capturedList.toList()
        }

        composeTestRule.setContent {
            MainScreen(navController = rememberNavController(), viewModel = mockViewModel)
        }
        // The captured list is empty in the beginning
        composeTestRule.onAllNodes(hasTestTagPrefix("CapturedPokemonCard_")).assertCountEquals(0)
        // Click the capture button 3 times
        repeat(3) {
            composeTestRule.onAllNodes(hasTestTagPrefix("CapturePokeball_")).onFirst().performClick()
            composeTestRule.waitForIdle()
        }
        // All captured Pokemon cards should be displayed
        composeTestRule.onAllNodes(hasTestTagPrefix("CapturedPokemonCard_")).assertCountEquals(3)
    }
}
