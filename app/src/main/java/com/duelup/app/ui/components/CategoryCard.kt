package com.duelup.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duelup.app.domain.model.Category
import com.duelup.app.util.toColor
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = category.color?.toColor() ?: MaterialTheme.colorScheme.primary
    // Create a vibrant gradient for the candy-like look
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.8f),
            baseColor
        )
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp), // Taller to fit emoji + text
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Let background box handle gradient
        ),
        shape = MaterialTheme.shapes.extraLarge, // Bubbly shape
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(width = 3.dp, color = Color.White) // Thick white border for pop
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = getCategoryEmoji(category.name),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper function to map categories to fun emojis
private fun getCategoryEmoji(name: String): String {
    return when (name.lowercase()) {
        "science" -> "🧪"
        "history" -> "🦕"
        "geography" -> "🌍"
        "sports" -> "🏀"
        "entertainment" -> "🎬"
        "technology" -> "💻"
        "math" -> "📐"
        "art" -> "🎨"
        "music" -> "🎵"
        else -> "🌟"
    }
}
