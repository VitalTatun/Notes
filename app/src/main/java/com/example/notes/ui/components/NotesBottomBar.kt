package com.example.notes.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notes.R

@Composable
fun NotesBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val translationProgress by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        // animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "BottomBar Translation"
    )

    NavigationBar(
        tonalElevation = 3.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.graphicsLayer {
            translationY = translationProgress * (80.dp.toPx() + 48.dp.toPx()) // Смещение вниз
        }
    ) {
        val items = listOf(
            Triple(0, Icons.AutoMirrored.Filled.StickyNote2, R.string.notes),
            Triple(1, Icons.Default.FormatQuote, R.string.quotes)
        )

        items.forEach { (index, icon, labelRes) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                },
                label = { 
                    Text(
                        stringResource(labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}
