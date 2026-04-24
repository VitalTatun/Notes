package com.example.notes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.security.BiometricAuthManager
import com.example.notes.ui.components.EditorLoadingScreen
import com.example.notes.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAuthManager = remember { BiometricAuthManager() }
    val biometricAvailable = activity != null && biometricAuthManager.isBiometricAvailable(activity)
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showImportModeDialog by remember { mutableStateOf(false) }
    var importReplaceMode by remember { mutableStateOf(false) }
    var showSetupPasscodeDialog by remember { mutableStateOf(false) }
    var showChangePasscodeDialog by remember { mutableStateOf(false) }
    var showDisablePasscodeDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(context, it, replace = importReplaceMode) }
    }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (uiState.isLoading) {
        EditorLoadingScreen(
            title = "Настройки",
            onBack = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionTitle("Безопасность")

            SettingsSwitchItem(
                title = "Защита приложения",
                checked = uiState.appLockEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showSetupPasscodeDialog = true
                    } else {
                        showDisablePasscodeDialog = true
                    }
                }
            )

            SettingsClickableItem(
                title = if (uiState.hasPasscode) "Изменить пароль" else "Задать пароль",
                enabled = true,
                onClick = {
                    if (uiState.hasPasscode) {
                        showChangePasscodeDialog = true
                    } else {
                        showSetupPasscodeDialog = true
                    }
                }
            )

            SettingsSwitchItem(
                title = "Разблокировка по отпечатку",
                checked = uiState.biometricUnlockEnabled,
                enabled = uiState.hasPasscode && biometricAvailable,
                onCheckedChange = { viewModel.setBiometricUnlockEnabled(it) }
            )

            if (!biometricAvailable) {
                Text(
                    text = "Биометрия недоступна на этом устройстве.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Внешний вид")

            ThemeOption("Системная", "SYSTEM", uiState.themeMode) { viewModel.setThemeMode(it) }
            ThemeOption("Светлая", "LIGHT", uiState.themeMode) { viewModel.setThemeMode(it) }
            ThemeOption("Темная", "DARK", uiState.themeMode) { viewModel.setThemeMode(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Размер шрифта")
            SettingsSwitchItem(
                title = "Использовать системный размер",
                checked = uiState.useSystemFontSize,
                onCheckedChange = { viewModel.setUseSystemFontSize(it) }
            )

            if (!uiState.useSystemFontSize) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Меньше", style = MaterialTheme.typography.bodySmall)
                        Text("Масштаб: ${(uiState.fontScale * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                        Text("Больше", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = uiState.fontScale,
                        onValueChange = { viewModel.setFontScale(it) },
                        valueRange = 0.8f..2.0f,
                        steps = 11
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Данные")

            SettingsClickableItem(
                title = "Экспорт всех записей (JSON)",
                onClick = { viewModel.exportData(context) }
            )

            SettingsClickableItem(
                title = "Импорт из файла",
                onClick = { showImportModeDialog = true }
            )

            SettingsClickableItem(
                title = "Удалить все данные",
                onClick = { showDeleteConfirmDialog = true }
            )

            val packageInfo = remember {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            android.content.pm.PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            val versionText = packageInfo?.versionName ?: "1.0.0"

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Версия $versionText",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    if (showSetupPasscodeDialog) {
        SetupPasscodeDialog(
            biometricAvailable = biometricAvailable,
            onDismiss = { showSetupPasscodeDialog = false },
            onSave = { passcode, enableBiometric ->
                viewModel.setupAppLock(passcode, enableBiometric) { success ->
                    if (success) {
                        showSetupPasscodeDialog = false
                    }
                }
            }
        )
    }

    if (showChangePasscodeDialog) {
        ChangePasscodeDialog(
            onDismiss = { showChangePasscodeDialog = false },
            onSave = { currentPasscode, newPasscode ->
                viewModel.changePasscode(currentPasscode, newPasscode) { success ->
                    if (success) {
                        showChangePasscodeDialog = false
                    }
                }
            }
        )
    }

    if (showDisablePasscodeDialog) {
        DisableAppLockDialog(
            onDismiss = { showDisablePasscodeDialog = false },
            onDisable = { currentPasscode ->
                viewModel.disableAppLock(currentPasscode) { success ->
                    if (success) {
                        showDisablePasscodeDialog = false
                    }
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Удалить все данные?") },
            text = { Text("Это действие нельзя отменить. Все ваши заметки и цитаты будут безвозвратно удалены.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showImportModeDialog) {
        AlertDialog(
            onDismissRequest = { showImportModeDialog = false },
            title = { Text("Импорт данных") },
            text = { Text("Выберите, как импортировать данные из файла.") },
            confirmButton = {
                Button(
                    onClick = {
                        importReplaceMode = false
                        showImportModeDialog = false
                        importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        importReplaceMode = true
                        showImportModeDialog = false
                        importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    }
                ) {
                    Text("Заменить")
                }
            }
        )
    }
}

@Composable
private fun SetupPasscodeDialog(
    biometricAvailable: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var passcode by rememberSaveable { mutableStateOf("") }
    var confirmPasscode by rememberSaveable { mutableStateOf("") }
    var enableBiometric by rememberSaveable { mutableStateOf(biometricAvailable) }
    val passwordsMatch = passcode.isNotBlank() && passcode == confirmPasscode

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Включить защиту") },
        text = {
            Column {
                OutlinedTextField(
                    value = passcode,
                    onValueChange = { passcode = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Новый пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmPasscode,
                    onValueChange = { confirmPasscode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text("Повторите пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (biometricAvailable) {
                    SettingsSwitchItem(
                        title = "Сразу включить отпечаток пальца",
                        checked = enableBiometric,
                        onCheckedChange = { enableBiometric = it }
                    )
                }
                if (confirmPasscode.isNotEmpty() && !passwordsMatch) {
                    Text(
                        text = "Пароли не совпадают",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(passcode, enableBiometric) },
                enabled = passwordsMatch
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun ChangePasscodeDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var currentPasscode by rememberSaveable { mutableStateOf("") }
    var newPasscode by rememberSaveable { mutableStateOf("") }
    var confirmPasscode by rememberSaveable { mutableStateOf("") }
    val passwordsMatch = newPasscode.isNotBlank() && newPasscode == confirmPasscode

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить пароль") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPasscode,
                    onValueChange = { currentPasscode = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Текущий пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = newPasscode,
                    onValueChange = { newPasscode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text("Новый пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmPasscode,
                    onValueChange = { confirmPasscode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text("Повторите новый пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (confirmPasscode.isNotEmpty() && !passwordsMatch) {
                    Text(
                        text = "Новые пароли не совпадают",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(currentPasscode, newPasscode) },
                enabled = currentPasscode.isNotBlank() && passwordsMatch
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun DisableAppLockDialog(
    onDismiss: () -> Unit,
    onDisable: (String) -> Unit
) {
    var currentPasscode by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Отключить защиту") },
        text = {
            Column {
                Text("Введите текущий пароль приложения, чтобы отключить защиту.")
                OutlinedTextField(
                    value = currentPasscode,
                    onValueChange = { currentPasscode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text("Текущий пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDisable(currentPasscode) },
                enabled = currentPasscode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Отключить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 4.dp)
    )
}

@Composable
fun ThemeOption(label: String, value: String, selectedValue: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.RadioButton(selected = (value == selectedValue), onClick = { onSelect(value) })
        Spacer(modifier = Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
