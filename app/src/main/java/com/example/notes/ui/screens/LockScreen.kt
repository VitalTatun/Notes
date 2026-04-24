package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.notes.security.BiometricAuthManager

@Composable
fun LockScreen(
    onUnlock: (String) -> Boolean,
    onBiometricSuccess: () -> Unit,
    biometricEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as? FragmentActivity
    val biometricAuthManager = rememberBiometricAuthManager()
    val biometricActivity = if (biometricEnabled && activity != null && biometricAuthManager.isBiometricAvailable(activity)) {
        activity
    } else {
        null
    }
    val isBiometricAvailable = biometricActivity != null

    var passcode by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(isBiometricAvailable) {
        biometricActivity?.let {
            biometricAuthManager.authenticate(
                activity = it,
                title = "Разблокировка",
                subtitle = "Подтвердите вход отпечатком пальца",
                description = "Если не получится, используйте пароль приложения.",
                onSuccess = onBiometricSuccess,
                onError = { errorMessage = it }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Приложение заблокировано",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Введите пароль приложения, чтобы продолжить.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = passcode,
            onValueChange = {
                passcode = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val isUnlocked = onUnlock(passcode)
                if (isUnlocked) {
                    passcode = ""
                    errorMessage = null
                } else {
                    errorMessage = "Неверный пароль"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = passcode.isNotBlank()
        ) {
            Text("Разблокировать")
        }

        biometricActivity?.let { activityForPrompt ->
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    biometricAuthManager.authenticate(
                        activity = activityForPrompt,
                        title = "Разблокировка",
                        subtitle = "Подтвердите вход отпечатком пальца",
                        description = "Если не получится, используйте пароль приложения.",
                        onSuccess = onBiometricSuccess,
                        onError = { errorMessage = it }
                    )
                }
            ) {
                Text("Войти по отпечатку")
            }
        }
    }
}

@Composable
private fun rememberBiometricAuthManager(): BiometricAuthManager {
    return androidx.compose.runtime.remember { BiometricAuthManager() }
}
