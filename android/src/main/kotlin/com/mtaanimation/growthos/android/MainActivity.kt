package com.mtaanimation.growthos.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mtaanimation.growthos.android.ui.navigation.AppNavGraph
import com.mtaanimation.growthos.android.ui.theme.GrowthOSTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.android.ui.navigation.Screen
import com.mtaanimation.growthos.android.ui.theme.BrandOrange
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Single-activity entry point.
 * Edge-to-edge is enabled so the app draws behind the status bar —
 * essential for the immersive dark-mode BI look.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authDataStore: AuthDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowthOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isReady by remember { mutableStateOf(false) }
                    var startDest by remember { mutableStateOf(Screen.Login.route) }

                    LaunchedEffect(Unit) {
                        val token = authDataStore.tokenFlow.first()
                        if (token != null) {
                            startDest = Screen.Dashboard.route
                        }
                        isReady = true
                    }

                    if (isReady) {
                        val navController = rememberNavController()
                        AppNavGraph(navController = navController, startDestination = startDest)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandOrange)
                        }
                    }
                }
            }
        }
    }
}
