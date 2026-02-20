package com.github.capntrips.kernelflasher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Card(
    shape: Shape = RoundedCornerShape(24.dp), // Lebih bulat (Expressive)
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer, // Warna M3 modern
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    tonalElevation: Dp = 0.dp, // Flat style dengan kontras warna
    shadowElevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = shadowElevation
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Sedikit spasi antar kartu
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // Padding internal lebih lega
            content = content
        )
    }
}
