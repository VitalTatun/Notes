package com.example.notes

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.notes.ui.navigation.NotesNavGraph
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.ui.viewmodel.*
import com.example.notes.ui.components.EditorLoadingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val themeMode by mainViewModel.themeMode.collectAsState(initial = "SYSTEM")
            val fontScale by mainViewModel.fontScale.collectAsState(initial = 1.0f)
            val useSystemFontSize by mainViewModel.useSystemFontSize.collectAsState(initial = true)
            
            NotesTheme(
                themeMode = themeMode,
                fontScale = fontScale,
                useSystemFontSize = useSystemFontSize,
                dynamicColor = true
            ) {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesNavGraph(navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditorLoadingScreenPreview() {
    NotesTheme {
        EditorLoadingScreen(
            title = "Загрузка",
            onBack = {}
        )
    }
}
