/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zuan.kernelmanager.ui.socmenu

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- CONSTANTS UNTUK M3 EXPRESSIVE ---
// Token radius sudut baru untuk tampilan yang lebih modern
private val ShapeExtraLarge = RoundedCornerShape(32.dp)
private val ShapeLarge = RoundedCornerShape(24.dp)
private val ShapeFull = RoundedCornerShape(100.dp) // Stadium/Pill shape

// Definisi Fisika Pegas (Spring Physics)
// Damping 0.4 menciptakan efek "bouncy" (overshoot) khas Expressive
private fun <T> expressiveSpring() = spring<T>(
    dampingRatio = 0.4f,
    stiffness = Spring.StiffnessMediumLow
)

@Composable
fun SectionTitle(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        fontWeight = FontWeight.ExtraBold, // Emphasized Style
        modifier = Modifier.padding(bottom = 16.dp, top = 8.dp, start = 4.dp)
    )
}

@Composable
fun BentoGridToggle(
    modifier: Modifier = Modifier,
    title: String,
    status: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color,
    onClick: () -> Unit
) {
    // State untuk mendeteksi tekanan agar bisa memberikan efek scale
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animasi Scale (Morfing halus saat ditekan)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = expressiveSpring(),
        label = "scale"
    )

    // Animasi Warna dengan fisika Expressive
    val borderColor by animateColorAsState(
        targetValue = if (isActive) activeColor else Color.Transparent,
        animationSpec = expressiveSpring(),
        label = "border"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive) activeColor else subContentColor,
        animationSpec = expressiveSpring(),
        label = "icon"
    )

    val bg = if (isActive) activeColor.copy(alpha = 0.15f) else cardBg

    Column(
        modifier = modifier
            .height(120.dp)
            .scale(scale)
            .clip(ShapeExtraLarge)
            .background(bg)
            .border(2.dp, borderColor.copy(alpha = 0.5f), ShapeExtraLarge)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, color = subContentColor)
            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
        // Indikator aktif opsional
        if (isActive) {
            Spacer(modifier = Modifier.height(3.dp).fillMaxWidth(0.4f).background(activeColor, ShapeFull))
        }
    }
}

@Composable
fun BentoWideValueCard(
    title: String,
    value: String,
    isSlider: Boolean,
    activeColor: Color,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color,
    customMaxRange: Float = 100f,
    onSliderChange: (Float) -> Unit = {},
    onInteractionEnd: ((Float) -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val cleanValue = value.replace(Regex("[^0-9.,]"), "").replace(",", ".")
    val floatVal = cleanValue.toFloatOrNull() ?: 0f
    val sliderDisplayValue = floatVal.coerceIn(0f, customMaxRange)

    val borderColor = if (cardBg == Color.Transparent) Color.White.copy(0.1f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeExtraLarge)
            .background(cardBg)
            .border(1.dp, borderColor, ShapeExtraLarge)
            .clickable(enabled = !isSlider) { onClick() }
            .padding(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = subContentColor, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleMedium, color = activeColor, fontWeight = FontWeight.ExtraBold)
        }
        if (isSlider) {
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = sliderDisplayValue,
                onValueChange = onSliderChange,
                onValueChangeFinished = {
                    onInteractionEnd?.invoke(sliderDisplayValue)
                },
                valueRange = 0f..customMaxRange,
                colors = SliderDefaults.colors(
                    thumbColor = activeColor,
                    activeTrackColor = activeColor,
                    inactiveTrackColor = activeColor.copy(alpha = 0.2f)
                ),
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

@Composable
fun BentoChipGroup(
    items: List<String>,
    selectedItem: String,
    activeColor: Color,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color,
    onItemSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { item ->
            val isSelected = item == selectedItem

            val animatedBg by animateColorAsState(
                targetValue = if (isSelected) activeColor else cardBg,
                animationSpec = expressiveSpring(),
                label = "chipBg"
            )

            val textColor = if (isSelected) Color.White else subContentColor

            Box(
                modifier = Modifier
                    .clip(ShapeFull)
                    .background(animatedBg)
                    .clickable { onItemSelected(item) }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = item,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color
) {
    val borderColor = if (cardBg == Color.Transparent) Color.White.copy(0.1f) else Color.Transparent
    Column(
        modifier = modifier
            .height(110.dp)
            .clip(ShapeLarge)
            .background(cardBg)
            .border(1.dp, borderColor, ShapeLarge)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, null, tint = subContentColor, modifier = Modifier.size(22.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = subContentColor)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BentoWideDetailCard(
    items: List<Pair<String, String>>,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color
) {
    val borderColor = if (cardBg == Color.Transparent) Color.White.copy(0.1f) else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLarge)
            .background(cardBg)
            .border(1.dp, borderColor, ShapeLarge)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = subContentColor, fontWeight = FontWeight.Medium)
                Text(value, style = MaterialTheme.typography.bodyLarge, color = contentColor, fontWeight = FontWeight.Bold)
            }
            if (index < items.size - 1) {
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(subContentColor.copy(alpha = 0.08f)))
            }
        }
    }
}
