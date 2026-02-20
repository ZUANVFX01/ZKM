package com.github.capntrips.kernelflasher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@ExperimentalUnitApi
@Composable
fun ColumnScope.FlashList(
    cardTitle: String,
    output: List<String>,
    content: @Composable ColumnScope.() -> Unit
) {
    val listState = rememberLazyListState()
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    
    // Auto scroll logic
    LaunchedEffect(output.size) {
        if (!isDragged) {
             listState.animateScrollToItem(output.size)
        }
    }

    DataCard(cardTitle)
    Spacer(Modifier.height(8.dp))
    
    // Terminal Box Style
    Box(
        modifier = Modifier
            .weight(1.0f)
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .scrollbar(listState)
        ) {
            items(output) { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    content()
}

fun Modifier.scrollbar(
    state: LazyListState,
    width: Dp = 6.dp,
    color: Color = Color.Gray // Bisa diganti MaterialTheme.colorScheme.primary
): Modifier = composed {
    // ... (Logic scrollbar sama, hanya perlu memastikan imports aman)
    var visibleItemsCountChanged = false
    var visibleItemsCount by remember { mutableIntStateOf(state.layoutInfo.visibleItemsInfo.size) }
    if (visibleItemsCount != state.layoutInfo.visibleItemsInfo.size) {
        visibleItemsCountChanged = true
        visibleItemsCount = state.layoutInfo.visibleItemsInfo.size
    }

    val hidden = state.layoutInfo.visibleItemsInfo.size == state.layoutInfo.totalItemsCount
    val targetAlpha = if (!hidden && (state.isScrollInProgress || visibleItemsCountChanged)) 0.5f else 0f
    val delay = if (!hidden && (state.isScrollInProgress || visibleItemsCountChanged)) 0 else 250
    val duration = if (hidden || visibleItemsCountChanged) 0 else if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(delayMillis = delay, durationMillis = duration)
    )

    drawWithContent {
        drawContent()
        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        if (alpha > 0.0f && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRoundRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                cornerRadius = CornerRadius(width.toPx(), width.toPx()),
                alpha = alpha
            )
        }
    }
}
