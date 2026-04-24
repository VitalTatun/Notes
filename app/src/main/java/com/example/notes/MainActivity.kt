package com.example.notes

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.notes.ui.components.EditorLoadingScreen
import com.example.notes.ui.navigation.NotesNavGraph
import com.example.notes.ui.screens.LockScreen
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.ui.viewmodel.MainViewModel
import com.example.notes.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            NotesTheme(
                themeMode = uiState.themeMode,
                fontScale = uiState.fontScale,
                useSystemFontSize = uiState.useSystemFontSize,
                dynamicColor = true
            ) {
                SecureWindowEffect(isLocked = uiState.isLocked)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        !uiState.isReady -> EditorLoadingScreen(
                            title = "Загрузка",
                            onBack = {}
                        )

                        uiState.isLocked -> LockScreen(
                            onUnlock = mainViewModel::unlockWithPasscode,
                            onBiometricSuccess = mainViewModel::unlockWithBiometricSuccess,
                            biometricEnabled = uiState.biometricUnlockEnabled
                        )

                        else -> {
                            val navController = rememberNavController()
                            NotesNavGraph(
                                navController = navController,
                                settingsViewModel = settingsViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            mainViewModel.onAppBackgrounded()
        }
    }
}

@Composable
private fun SecureWindowEffect(isLocked: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, isLocked) {
        val window = (view.context as? FragmentActivity)?.window
        if (isLocked) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        onDispose { }
    }
}
