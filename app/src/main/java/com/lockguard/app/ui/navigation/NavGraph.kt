package com.lockguard.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.ui.screens.about.AboutScreen
import com.lockguard.app.ui.screens.applock.AppLockScreen
import com.lockguard.app.ui.screens.applock.AppLockViewModel
import com.lockguard.app.ui.screens.dashboard.DashboardScreen
import com.lockguard.app.ui.screens.dashboard.DashboardViewModel
import com.lockguard.app.ui.screens.detail.PhotoDetailScreen
import com.lockguard.app.ui.screens.detail.PhotoDetailViewModel
import com.lockguard.app.ui.screens.gallery.GalleryScreen
import com.lockguard.app.ui.screens.gallery.GalleryViewModel
import com.lockguard.app.ui.screens.onboarding.OnboardingScreen
import com.lockguard.app.ui.screens.privacy.PrivacyScreen
import com.lockguard.app.ui.screens.settings.SettingsScreen
import com.lockguard.app.ui.screens.settings.SettingsViewModel
import com.lockguard.app.ui.screens.setup.SetupScreen
import com.lockguard.app.ui.screens.splash.SplashScreen

@Composable
fun LockGuardNavHost(
    navController: NavHostController,
    settingsRepository: SettingsRepository,
    pinManager: PinManager,
    initialTargetRoute: String? = null,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(280)) },
        exitTransition = { fadeOut(animationSpec = tween(280)) },
        popEnterTransition = { fadeIn(animationSpec = tween(280)) },
        popExitTransition = { fadeOut(animationSpec = tween(280)) },
        modifier = modifier
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                settingsRepository = settingsRepository,
                pinManager = pinManager,
                onNavigate = { destination ->
                    val resolvedDestination = if (destination == Screen.Dashboard.route && !initialTargetRoute.isNullOrEmpty()) {
                        initialTargetRoute
                    } else {
                        destination
                    }
                    navController.navigate(resolvedDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Permissions & Setup Screen
        composable(Screen.Setup.route) {
            SetupScreen(
                settingsRepository = settingsRepository,
                onNavigateToPinSetup = {
                    navController.navigate(Screen.AppLock.createRoute(returnRoute = Screen.Setup.route, isChangingPin = true))
                },
                onFinishSetup = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Main Security Dashboard
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSetup = { navController.navigate(Screen.Setup.route) },
                onNavigateToDetail = { eventId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(eventId))
                }
            )
        }

        // 5. Intruder Evidence Gallery
        composable(Screen.Gallery.route) {
            val galleryViewModel: GalleryViewModel = hiltViewModel()
            GalleryScreen(
                viewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { eventId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(eventId))
                }
            )
        }

        // 6. Photo Detail Screen
        composable(
            route = Screen.PhotoDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            val detailViewModel: PhotoDetailViewModel = hiltViewModel()
            PhotoDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. App Lock / PIN Screen
        composable(
            route = Screen.AppLock.route,
            arguments = listOf(
                navArgument("returnRoute") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("isChangingPin") {
                    type = NavType.StringType
                    defaultValue = "false"
                }
            )
        ) {
            val appLockViewModel: AppLockViewModel = hiltViewModel()
            AppLockScreen(
                viewModel = appLockViewModel,
                onUnlockSuccess = { returnRoute ->
                    if (!returnRoute.isNullOrEmpty()) {
                        navController.navigate(returnRoute) {
                            popUpTo(Screen.AppLock.route) { inclusive = true }
                        }
                    } else {
                        val popped = navController.popBackStack()
                        if (!popped) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // 8. Settings Screen
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onChangePin = {
                    navController.navigate(Screen.AppLock.createRoute(returnRoute = Screen.Settings.route, isChangingPin = true))
                },
                onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        // 9. Privacy Policy Screen
        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. About Screen
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
