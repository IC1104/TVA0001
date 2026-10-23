package com.example.tva0001

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tva0001.ui.theme.TVA0001Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TVA0001Theme {
                HomeScreen()
            }
        }
    }
}
