package com.example.notes.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.notes.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    activity: FragmentActivity,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val biometricManager = remember(activity) { BiometricManager.from(activity) }
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRecoveryPasswordVisible by remember { mutableStateOf(false) }
    var isRecoveryConfirmPasswordVisible by remember { mutableStateOf(false) }

    fun showBiometricPrompt() {
        val biometricStatus = biometricManager.canAuthenticate(authenticators)
        if (biometricStatus != BiometricManager.BIOMETRIC_SUCCESS) {
            viewModel.onBiometricUnavailable(
                when (biometricStatus) {
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Отпечаток пальца не настроен на устройстве"
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "На устройстве нет биометрического датчика"
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Биометрия временно недоступна"
                    else -> "Биометрический вход недоступен"
                }
            )
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        viewModel.onBiometricUnavailable(errString.toString())
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Вход по отпечатку")
            .setSubtitle("Приложите палец к сканеру")
            .setNegativeButtonText("Отмена")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Реакция на изменение состояния входа
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
            viewModel.resetLoginState()
        }
    }

    LaunchedEffect(uiState.isBiometricEnabled) {
        val canUseBiometric = biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
        viewModel.updateBiometricAvailability(canUseBiometric)
    }

    // Автоматический вызов при входе на экран, если биометрия включена и доступна
    LaunchedEffect(uiState.isBiometricEnabled, uiState.canUseBiometric) {
        if (uiState.isBiometricEnabled && uiState.canUseBiometric) {
            showBiometricPrompt()
        }
    }

    LaunchedEffect(uiState.recoveryMessage, uiState.recoveryError, uiState.biometricMessage) {
        uiState.recoveryMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearRecoveryMessage()
        }
        uiState.biometricMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearBiometricMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Введите пароль", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Пароль") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null,
                singleLine = true
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти")
            }

            TextButton(onClick = { viewModel.toggleRecoveryDialog(true) }) {
                Text("Забыли пароль?")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isBiometricEnabled && uiState.canUseBiometric) {
                IconButton(
                    onClick = { showBiometricPrompt() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Вход по отпечатку",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (uiState.showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleRecoveryDialog(false) },
            title = { Text("Восстановление пароля") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = uiState.securityQuestion ?: "Контрольный вопрос не задан",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = uiState.recoveryAnswer,
                        onValueChange = { viewModel.onRecoveryAnswerChanged(it) },
                        label = { Text("Ответ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = { viewModel.onNewPasswordChanged(it) },
                        label = { Text("Новый пароль") },
                        visualTransformation = if (isRecoveryPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isRecoveryPasswordVisible = !isRecoveryPasswordVisible }) {
                                Icon(
                                    imageVector = if (isRecoveryPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isRecoveryPasswordVisible) "Скрыть пароль" else "Показать пароль"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.confirmNewPassword,
                        onValueChange = { viewModel.onConfirmNewPasswordChanged(it) },
                        label = { Text("Подтвердите пароль") },
                        visualTransformation = if (isRecoveryConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isRecoveryConfirmPasswordVisible = !isRecoveryConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isRecoveryConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isRecoveryConfirmPasswordVisible) "Скрыть пароль" else "Показать пароль"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.recoveryError != null) {
                        Text(
                            text = uiState.recoveryError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.recoverPassword() }) {
                    Text("Восстановить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleRecoveryDialog(false) }) {
                    Text("Отмена")
                }
            }
        )
    }
}
