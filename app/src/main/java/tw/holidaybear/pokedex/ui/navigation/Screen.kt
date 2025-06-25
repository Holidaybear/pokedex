package tw.holidaybear.pokedex.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Detail : Screen("detail/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "detail/$pokemonId"
    }
}
