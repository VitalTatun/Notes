package com.example.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.notes.ui.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved, uiState.isSkipped) {
        if (uiState.isSaved || uiState.isSkipped) {
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Настройка безопасности",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Новый пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChanged(it) },
            label = { Text("Подтвердите пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Text(
            text = "Контрольный вопрос для восстановления",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = uiState.securityQuestion,
            onValueChange = { viewModel.onQuestionChanged(it) },
            label = { Text("Ваш вопрос (напр. Девичья фамилия матери)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = uiState.securityAnswer,
            onValueChange = { viewModel.onAnswerChanged(it) },
            label = { Text("Ответ") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Использовать отпечаток пальца",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = uiState.isBiometricEnabled,
                onCheckedChange = { viewModel.onBiometricToggled(it) }
            )
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { viewModel.savePassword() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Установить пароль")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.skipSetup() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Напомнить позже")
        }
    }
}
