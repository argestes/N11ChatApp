package tr.yigitunlu.n11chatapp.ui.viewmodel

sealed class Screen {
    data object Chat : Screen()
    data object Feedback : Screen()
}