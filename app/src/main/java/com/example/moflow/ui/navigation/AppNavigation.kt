// ui/navigation/AppNavigation.kt
package com.example.moflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moflow.ui.feature.currency.CurrencyConverterScreen
import com.example.moflow.ui.feature.finance.FinanceScreen
import com.example.moflow.ui.feature.home.HomeScreen
import com.example.moflow.ui.feature.splash.SplashScreen

object Route {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val FINANCE = "finance"
    const val CURRENCY = "currency"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.SPLASH) {
            SplashScreen(onNavigateToHome = {
                // Navigasi langsung ke HOME dan hapus splash dari backstack
                navController.navigate(Route.HOME) {
                    popUpTo(Route.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Route.HOME) {
            HomeScreen(
                onNavigateToFinance = { navController.navigate(Route.FINANCE) },
                onNavigateToCurrency = { navController.navigate(Route.CURRENCY) }
            )
        }

        // Route lainnya tetap sama
        composable(Route.FINANCE) {
            FinanceScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.CURRENCY) {
            CurrencyConverterScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}