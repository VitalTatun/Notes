package com.example.notes.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NotesBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isExpanded: Boolean,
    bottomBarHeight: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val animatedBottomOffset by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else bottomBarHeight,
        label = "bottomBarOffset"
    )

    Surface(
        modifier = modifier
            .offset(y = animatedBottomOffset)
            .alpha(if (isExpanded) 1f else 0f),
        tonalElevation = 3.dp
    ) {
        NavigationBar {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                label = { Text("Заметки") }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                label = { Text("Цитаты") }
            )
        }
    }
}
