package com.safeher.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.safeher.app.core.designsystem.SafeHerTheme
import com.safeher.app.presentation.navigation.SafeHerNavGraph
import com.safeher.app.presentation.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SafeHerTheme {
                val navController = rememberNavController()
                SafeHerNavGraph(
                    navController = navController,
                    startDestination = Screen.Home.route
                )
            }
        }
    }
}
