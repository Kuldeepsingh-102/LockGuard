package com.lockguard.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Setup : Screen("setup")
    data object Dashboard : Screen("dashboard")
    data object Gallery : Screen("gallery")
    data object PhotoDetail : Screen("detail/{eventId}") {
        fun createRoute(eventId: Long) = "detail/$eventId"
    }
    data object AppLock : Screen("applock?returnRoute={returnRoute}&isChangingPin={isChangingPin}") {
        fun createRoute(returnRoute: String? = null, isChangingPin: Boolean = false): String {
            val encodedReturn = returnRoute ?: ""
            return "applock?returnRoute=$encodedReturn&isChangingPin=$isChangingPin"
        }
    }
    data object Settings : Screen("settings")
    data object Privacy : Screen("privacy")
    data object About : Screen("about")
}
