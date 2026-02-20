package com.github.capntrips.kernelflasher.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.DataValue(
    value: String,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    clickable: Boolean = false,
) {
    // Tidak pakai SelectionContainer agar layout lebih clean, 
    // tapi tetap bisa dicopy jika perlu logic tambahan
    var clicked by remember { mutableStateOf(false) }
    
    val modifier = Modifier
        .weight(1f, fill = false) // Mencegah teks mendorong layout terlalu jauh
        .animateContentSize() // Animasi smooth saat expand
        .then(
            if (clickable) {
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { clicked = !clicked }
            } else Modifier
        )

    Text(
        modifier = modifier,
        text = value,
        color = color,
        style = style,
        maxLines = if (clicked) Int.MAX_VALUE else 1,
        overflow = TextOverflow.Ellipsis
    )
}
