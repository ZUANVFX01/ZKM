package com.zuan.kernelmanager.ui.kernelParameter

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionTitle(title: String, colors: ZkmColors) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = colors.textSub,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
    )
}

@Composable
fun DynamicHeroCard(
    title: String, mainValue: String, subValue: String, icon: ImageVector,
    accentColor: Color, gradientColors: List<Color>, chartValue: String,
    progress: Float = 0.75f, // --- NEW PARAMETER (Default 0.75 just in case)
    colors: ZkmColors, onClick: () -> Unit
) {
    val contentColor = if(accentColor == Color(0xFFFACC15)) Color.Black else Color.White
    val brush = Brush.linearGradient(colors = gradientColors)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color.White.copy(alpha = 0.15f), center = Offset(size.width, 0f), radius = size.height)
            }
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(title, color = contentColor.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(mainValue, color = contentColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = contentColor.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(subValue, color = contentColor.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(72.dp)) {
                        // Background Arc
                        drawArc(color = Color.White.copy(alpha = 0.4f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 12f, cap = StrokeCap.Round))
                        // Dynamic Progress Arc
                        drawArc(color = Color.White, startAngle = -90f, sweepAngle = progress * 360f, useCenter = false, style = Stroke(width = 12f, cap = StrokeCap.Round))
                    }
                    Text(chartValue, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSegmentedControl(pagerState: PagerState, colors: ZkmColors, onTabSelected: (Int) -> Unit) {
    val selectedTab = pagerState.currentPage 

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).background(colors.background).padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).border(1.dp, colors.border, CircleShape).background(colors.cardBg, CircleShape).padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val tabs = listOf("Sched", "Memory", "Network", "Display")
            val tabColors = listOf(Color(0xFF3B82F6), Color(0xFFFACC15), Color(0xFF10B981), Color(0xFFEC4899))
            
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val activeBg = tabColors[index]
                val activeText = if(activeBg == Color(0xFFFACC15)) Color.Black else Color.White
                
                val bgColor by animateColorAsState(if (isSelected) activeBg else Color.Transparent, label = "bg")
                val textColor by animateColorAsState(if (isSelected) activeText else colors.textSub, label = "text")

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(bgColor).clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = title, color = textColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BentoGridToggle(
    modifier: Modifier = Modifier, title: String, status: String, icon: ImageVector, 
    isActive: Boolean, accentColor: Color, colors: ZkmColors, onClick: () -> Unit
) {
    val borderColor by animateColorAsState(if (isActive) accentColor else colors.border, label = "border")
    val iconColor by animateColorAsState(if (isActive) accentColor else colors.textSub, label = "icon")
    val bg = if(isActive) colors.cardActive else colors.cardBg

    Column(
        modifier = modifier.height(110.dp).clip(RoundedCornerShape(24.dp)).background(bg)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .clickable { onClick() }.padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, color = colors.textSub)
            Text(status, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textMain)
        }
        if (isActive) Spacer(modifier = Modifier.height(2.dp).fillMaxWidth().background(accentColor))
    }
}

/* Cari fungsi BentoWideValueCard dan GANTI SEMUANYA dengan ini */

@Composable
fun BentoWideValueCard(
    title: String, value: String, isSlider: Boolean, accentColor: Color, colors: ZkmColors, 
    customMaxRange: Float = 100f, 
    onSliderChange: (Float) -> Unit = {}, onClick: () -> Unit = {}
) {
    // --- FIX: Logic Parse Angka (Handle Koma dan Titik) ---
    // Mengganti koma jadi titik supaya bisa di-convert ke Float dengan aman
    // Contoh: "1,50" -> "1.50" -> 1.5f
    val cleanValue = value.replace(Regex("[^0-9.,]"), "").replace(",", ".")
    val floatVal = cleanValue.toFloatOrNull() ?: 0f
    
    // Safety check supaya slider tidak error jika value melebihi range (visual only)
    val sliderDisplayValue = floatVal.coerceIn(0f, customMaxRange)
    
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.cardBg)
            .border(1.dp, colors.border, RoundedCornerShape(24.dp))
            .clickable(enabled = !isSlider) { onClick() }.padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textSub, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleSmall, color = accentColor, fontWeight = FontWeight.Bold)
        }
        if (isSlider) {
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = sliderDisplayValue, 
                onValueChange = onSliderChange, 
                valueRange = 0f..customMaxRange,
                colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor, inactiveTrackColor = colors.pillBg)
            )
        }
    }
}


@Composable
fun BentoChipGroup(items: List<String>, selectedItem: String, accentColor: Color, colors: ZkmColors, onItemSelected: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            val isSelected = item == selectedItem
            val bg = if(isSelected) accentColor else colors.cardActive
            val contentColor = if(isSelected) {
                if(accentColor == Color(0xFFFACC15)) Color.Black else Color.White
            } else colors.textSub
            
            Box(
                modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(bg)
                    .clickable { onItemSelected(item) }.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(item, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
