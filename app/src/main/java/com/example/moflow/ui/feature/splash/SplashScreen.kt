// ui/feature/splash/SplashScreen.kt
package com.example.moflow.ui.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.moflow.R

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    // Gunakan R.drawable langsung
    val logoResource = if (isSystemInDarkTheme()) {
        R.drawable.logo_dark // Pastikan logo_white ada di res/drawable
    } else {
        R.drawable.logo_white  // Pastikan logo_dark ada di res/drawable
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = logoResource),
            contentDescription = "MoFlow Logo",
            modifier = Modifier.size(200.dp)
        )
    }

    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToHome()
    }
}
