package com.webforge.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.webforge.studio.ui.navigation.AppNavGraph
import com.webforge.studio.ui.theme.WebForgeTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity entry point for WebForge Studio.
 *
 * [@AndroidEntryPoint] allows Hilt to inject members into this Activity and
 * ensures that [hiltViewModel()] works in all hosted Composable destinations.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebForgeTheme {
                AppNavGraph()
            }
        }
    }
}
