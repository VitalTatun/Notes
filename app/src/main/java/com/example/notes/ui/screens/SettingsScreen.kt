package com.example.notes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.notes.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showImportModeDialog by remember { mutableStateOf(false) }
    var importReplaceMode by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                        steps = 11 // (2.0 - 0.8) / 0.1 - 1 = 11 steps for 0.1 increments
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Опасная зона")

            SettingsClickableItem(
                title = "Удалить все данные",
                onClick = { showDeleteConfirmDialog = true }
            )

            SettingsClickableItem(
                title = "Добавить тестовые данные (50+50)",
                onClick = { viewModel.addSampleData() }
            )
        }
    }

    // Диалоги
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
        RadioButton(selected = (value == selectedValue), onClick = { onSelect(value) })
        Spacer(modifier = Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
fun SettingsClickableItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
