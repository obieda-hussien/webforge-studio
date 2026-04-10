package com.webforge.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.webforge.studio.ui.navigation.AppNavGraph
import com.webforge.studio.ui.theme.WebForgeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebForgeTheme {
                AppNavGraph(
                    repository = (application as WebForgeApplication).projectRepository,
                )
            }
        }
    }
}
