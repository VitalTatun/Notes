package com.example.notes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.notes.R

@Composable
fun NotesBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        NavigationBar(
            tonalElevation = 3.dp
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                label = { Text(stringResource(R.string.notes)) }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = { Icon(Icons.Default.FormatQuote, null) },
                label = { Text(stringResource(R.string.quotes)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreenPreview() {
    var selectedTab by remember { mutableStateOf(0) }
    var isExpanded by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") }
            )
        },
        bottomBar = {
            NotesBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                isExpanded = isExpanded
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Button(
                onClick = { isExpanded = !isExpanded }
            ) {
                Text("Toggle BottomBar")
            }
            Text("Current tab: $selectedTab")
        }
    }
}
