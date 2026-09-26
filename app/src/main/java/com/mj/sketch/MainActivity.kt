package com.mj.sketch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mj.sketch.navigation.AppNavGraph
import com.mj.sketch.ui.theme.SketchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SketchTheme {
                AppNavGraph()
            }
        }
    }
}
