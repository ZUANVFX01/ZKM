/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.services.FpsOverlayService
import com.zuan.kernelmanager.ui.proces.FloatingProcessService
import com.zuan.kernelmanager.ui.terminal.FloatingTerminalService
import com.zuan.kernelmanager.ui.activitylauncher.FloatingActivityService
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ActiveDialog {
    NONE, TOOLS, POWER
}

enum class PowerAction(val titleRes: Int, val command: String, val warningRes: Int) {
    REBOOT(R.string.power_reboot, "svc power reboot", R.string.power_reboot_warning),
    SHUTDOWN(R.string.power_shutdown, "svc power shutdown", R.string.power_shutdown_warning),
    RECOVERY(R.string.power_recovery, "reboot recovery", R.string.power_recovery_warning),
    FASTBOOT(R.string.power_fastboot, "reboot bootloader", R.string.power_fastboot_warning),
    RESTART_UI(R.string.power_restart_ui, "pkill -f com.android.systemui", R.string.power_restart_ui_warning)
}

@Composable
fun generateNavBackgroundColor(primaryColor: Color, isDark: Boolean): Color {
    return if (isDark) {
        primaryColor.copy(
            red = primaryColor.red * 0.08f,
            green = primaryColor.green * 0.08f,
            blue = primaryColor.blue * 0.08f,
            alpha = 1f
        ).compositeOver(Color(0xFF0A0A0A))
    } else {
        val primaryRed = primaryColor.red
        val primaryGreen = primaryColor.green
        val primaryBlue = primaryColor.blue
        
        Color(
            red = 0.97f + (primaryRed * 0.03f),
            green = 0.97f + (primaryGreen * 0.03f),
            blue = 0.97f + (primaryBlue * 0.03f),
            alpha = 1f
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ModernTabNavigationBar(
    navController: NavController,
    hazeState: HazeState,
    onOpenDialog: (ActiveDialog) -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavigationRoute.Overall,    
        NavigationRoute.Dashboard,  
        NavigationRoute.SoC,        
        NavigationRoute.About       
    )

    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemDark
    }
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()
    
    val effectivePrimary = if (isCustomColor) {
        Color(customPrimary)
    } else if (isDynamic) {
        Color.Unspecified
    } else {
        MaterialTheme.colorScheme.primary
    }
    
    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        effectivePrimary
    }

    val tintedBackground = generateNavBackgroundColor(finalPrimary, useDarkTheme)
    
    val accentColor = finalPrimary
    val textColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconColorRight = MaterialTheme.colorScheme.onSurface

    val navBarHazeStyle = HazeStyle(
        backgroundColor = tintedBackground.copy(alpha = 0.5f),
        blurRadius = 24.dp,
        noiseFactor = 0.1f,
        tints = listOf(HazeTint(tintedBackground.copy(alpha = 0.4f)))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = navBarHazeStyle)
            .background(Color.Transparent) 
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.CenterVertically) {
                items.forEach { item ->
                    val isSelected = currentDestination?.route == item.route
                    SceneTopTabItem(
                        item = item, 
                        isSelected = isSelected, 
                        accentColor = accentColor,
                        textColor = textColor,
                        surfaceVariantColor = surfaceVariantColor,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Equalizer, 
                    stringResource(R.string.nav_tools_desc), 
                    tint = iconColorRight, 
                    modifier = Modifier.size(24.dp).clickable { onOpenDialog(ActiveDialog.TOOLS) }
                )
                Icon(
                    Icons.Rounded.PowerSettingsNew, 
                    stringResource(R.string.nav_power_desc), 
                    tint = iconColorRight, 
                    modifier = Modifier.size(24.dp).clickable { onOpenDialog(ActiveDialog.POWER) }
                )
                Icon(
                    Icons.Rounded.Settings, 
                    stringResource(R.string.nav_settings_desc), 
                    tint = iconColorRight, 
                    modifier = Modifier.size(24.dp).clickable { navController.navigate(NavigationRoute.Settings.route) }
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ModernSwipeableNavBar(
    tabs: List<NavigationRoute>,
    pagerState: PagerState,
    hazeState: HazeState,
    onTabSelected: (Int) -> Unit,
    navController: NavController,
    onOpenDialog: (ActiveDialog) -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemDark
    }
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()
    
    val effectivePrimary = if (isCustomColor) {
        Color(customPrimary)
    } else if (isDynamic) {
        Color.Unspecified
    } else {
        MaterialTheme.colorScheme.primary
    }
    
    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        effectivePrimary
    }

    val tintedBackground = generateNavBackgroundColor(finalPrimary, useDarkTheme)
    
    val accentColor = finalPrimary
    val textColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconColorRight = MaterialTheme.colorScheme.onSurface

    val navBarHazeStyle = HazeStyle(
        backgroundColor = tintedBackground.copy(alpha = 0.5f),
        blurRadius = 24.dp,
        noiseFactor = 0.1f,
        tints = listOf(HazeTint(tintedBackground.copy(alpha = 0.4f)))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = navBarHazeStyle)
            .background(Color.Transparent) 
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, item ->
                    val isSelected = pagerState.currentPage == index
                    SceneTopTabItem(
                        item = item,
                        isSelected = isSelected,
                        accentColor = accentColor,
                        textColor = textColor,
                        surfaceVariantColor = surfaceVariantColor,
                        onClick = { onTabSelected(index) }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Equalizer, 
                    stringResource(R.string.nav_tools_desc), 
                    tint = iconColorRight, 
                    modifier = Modifier.size(24.dp).clickable { onOpenDialog(ActiveDialog.TOOLS) }
                )
                Icon(
                    Icons.Rounded.PowerSettingsNew, 
                    stringResource(R.string.nav_power_desc), 
                    tint = iconColorRight, 
                    modifier = Modifier.size(24.dp).clickable { onOpenDialog(ActiveDialog.POWER) }
                )
                Icon(
                    Icons.Rounded.Settings, 
                    stringResource(R.string.nav_settings_desc), 
                    tint = iconColorRight, 
                    modifier = Modifier.size(24.dp).clickable { navController.navigate(NavigationRoute.Settings.route) }
                )
            }
        }
    }
}

@Composable
fun SceneTopTabItem(
    item: NavigationRoute,
    isSelected: Boolean,
    accentColor: Color,
    textColor: Color,
    surfaceVariantColor: Color,
    onClick: () -> Unit
) {
    val activeColor = accentColor
    val inactiveIconColor = surfaceVariantColor
    val inactiveTextColor = surfaceVariantColor
    val activeTextColor = accentColor

    val iconTint by animateColorAsState(targetValue = if (isSelected) activeColor else inactiveIconColor, animationSpec = tween(300), label = "IconColor")
    val animatedTextColor by animateColorAsState(targetValue = if (isSelected) activeTextColor else inactiveTextColor, animationSpec = tween(300), label = "TextColor")

    val label = when(item) {
        NavigationRoute.Overall -> stringResource(R.string.nav_tab_feature)
        NavigationRoute.Dashboard -> stringResource(R.string.nav_tab_overview)
        NavigationRoute.SoC -> stringResource(R.string.nav_tab_adjust)
        NavigationRoute.About -> stringResource(R.string.nav_tab_support)
        else -> ""
    }

    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, 
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp) 
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, 
            color = animatedTextColor, 
            fontSize = 11.sp
        )
    }
}

fun toggleService(context: Context, serviceClass: Class<*>, enable: Boolean) {
    if (enable) {
        if (!Settings.canDrawOverlays(context)) {
            Toast.makeText(context, context.getString(R.string.overlay_permission_required), Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val intent = Intent(context, serviceClass)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    } else {
        context.stopService(Intent(context, serviceClass))
    }
}

@Composable
fun ToolsMenuContent(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var isTerminalEnabled by remember { mutableStateOf(FloatingTerminalService.isRunning) }
    var isProcessEnabled by remember { mutableStateOf(FloatingProcessService.isRunning) } 
    var isActivityEnabled by remember { mutableStateOf(FloatingActivityService.isRunning) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.dialog_tools_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ToolToggleItem(
                icon = Icons.Rounded.Terminal,
                title = stringResource(R.string.tool_terminal_title),
                subtitle = stringResource(R.string.tool_terminal_subtitle),
                isChecked = isTerminalEnabled,
                onCheckedChange = { enable ->
                    toggleService(context, FloatingTerminalService::class.java, enable)
                    isTerminalEnabled = enable
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            FpsMonitorItem()

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            ToolToggleItem(
                icon = Icons.Rounded.Memory,
                title = stringResource(R.string.tool_process_title),
                subtitle = stringResource(R.string.tool_process_subtitle),
                isChecked = isProcessEnabled,
                onCheckedChange = { enable ->
                    toggleService(context, FloatingProcessService::class.java, enable)
                    isProcessEnabled = enable
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            ToolToggleItem(
                icon = Icons.Rounded.Extension, 
                title = stringResource(R.string.tool_activity_title),
                subtitle = stringResource(R.string.tool_activity_subtitle),
                isChecked = isActivityEnabled,
                onCheckedChange = { enable ->
                    toggleService(context, FloatingActivityService::class.java, enable)
                    isActivityEnabled = enable
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}

@Composable
fun PowerMenuContent(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedAction by remember { mutableStateOf<PowerAction?>(null) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(24.dp)
    ) {
        AnimatedContent(
            targetState = selectedAction,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "PowerMenuTransition"
        ) { action ->
            if (action == null) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dialog_power_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    val actions = listOf(
                        PowerAction.REBOOT to Icons.Rounded.RestartAlt,
                        PowerAction.SHUTDOWN to Icons.Rounded.PowerSettingsNew,
                        PowerAction.RECOVERY to Icons.Rounded.Build,
                        PowerAction.FASTBOOT to Icons.Rounded.Adb,
                        PowerAction.RESTART_UI to Icons.Rounded.Layers
                    )

                    actions.forEach { (powerAction, icon) ->
                        PowerActionItem(
                            label = stringResource(powerAction.titleRes), 
                            icon = icon, 
                            onClick = { selectedAction = powerAction }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                     Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.confirm_action_title, stringResource(action.titleRes)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = stringResource(action.warningRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { selectedAction = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
                        
                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        Shell.cmd(action.command).exec()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.action_yes_do_it))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolToggleItem(
    icon: ImageVector, 
    title: String, 
    subtitle: String, 
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange, modifier = Modifier.scale(0.8f))
    }
}

@Composable
fun FpsMonitorItem() {
    val context = LocalContext.current
    val options = listOf(
        stringResource(R.string.fps_style_android),
        stringResource(R.string.fps_style_mini),
        stringResource(R.string.fps_style_pc)
    )
    var selectedOption by remember { mutableStateOf(options[1]) }
    var isEnabled by remember { mutableStateOf(FpsOverlayService.isRunning) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.tool_fps_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = stringResource(R.string.tool_fps_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isEnabled, 
                onCheckedChange = { enable ->
                    isEnabled = enable
                    if (enable) {
                        val styleIndex = when(selectedOption) {
                            options[0] -> 0
                            options[2] -> 1
                            else -> 2
                        }
                        if (!Settings.canDrawOverlays(context)) {
                            Toast.makeText(context, context.getString(R.string.overlay_permission_required), Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                            isEnabled = false
                        } else {
                            val intent = Intent(context, FpsOverlayService::class.java).apply {
                                putExtra("STYLE", styleIndex)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        }
                    } else {
                        context.stopService(Intent(context, FpsOverlayService::class.java))
                    }
                }, 
                modifier = Modifier.scale(0.8f)
            )
        }

        if (isEnabled) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = selectedOption == option
                    OutlinedButton(
                        onClick = { 
                            selectedOption = option 
                            if (isEnabled) {
                                val styleIndex = when(option) { 
                                    options[0] -> 0
                                    options[2] -> 1
                                    else -> 2 
                                }
                                val intent = Intent(context, FpsOverlayService::class.java).apply { putExtra("STYLE", styleIndex) }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
                            }
                        },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(0.dp),
                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) else ButtonDefaults.outlinedButtonColors(),
                        border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text(text = option, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
fun PowerActionItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
