/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.composables.core.rememberDialogState
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeStyle
import kotlin.math.roundToInt

@Composable
fun generateThemedBackgroundColor(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
        val darkTintedBg = primaryColor.copy(
            red = primaryColor.red * 0.08f,
            green = primaryColor.green * 0.08f,
            blue = primaryColor.blue * 0.08f,
            alpha = 1f
        ).compositeOver(Color(0xFF0A0A0A))
        
        val cardColor = primaryColor.copy(
            red = primaryColor.red * 0.15f,
            green = primaryColor.green * 0.15f,
            blue = primaryColor.blue * 0.15f,
            alpha = 1f
        ).compositeOver(Color(0xFF141414))
        
        Pair(darkTintedBg, cardColor)
    } else {
        val primaryRed = primaryColor.red
        val primaryGreen = primaryColor.green
        val primaryBlue = primaryColor.blue
        
        val softBackground = Color(
            red = 0.97f + (primaryRed * 0.03f),
            green = 0.97f + (primaryGreen * 0.03f),
            blue = 0.97f + (primaryBlue * 0.03f),
            alpha = 1f
        )
        
        val cardColor = Color(0xFFFFFFFF)
        
        Pair(softBackground, cardColor)
    }
}

@Composable
fun SettingsScreen(
    navController: NavController,
    hazeState: HazeState, 
    viewModel: SettingsViewModel = viewModel(),
    lifecycleOwner: LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
) {
    val isDynamic by viewModel.isDynamicColor.collectAsState()
    
    MainSettingsContent(
        viewModel = viewModel,
        lifecycleOwner = lifecycleOwner,
        isDynamic = isDynamic,
        onLanguageClick = { navController.navigate(NavigationRoute.Language.route) },
        onBackClick = { navController.popBackStack() }, 
        hazeState = hazeState,
        navController = navController
    )
}

@Composable
fun MainSettingsContent(
    viewModel: SettingsViewModel,
    lifecycleOwner: LifecycleOwner,
    isDynamic: Boolean,
    onLanguageClick: () -> Unit,
    onBackClick: () -> Unit,
    hazeState: HazeState,
    navController: NavController
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val pollingInterval by viewModel.pollingInterval.collectAsState()
    val currentLanguageCode by viewModel.currentLanguage.collectAsState()
    val savedDpi by viewModel.appDpi.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val cardDarkness by viewModel.cardDarkness.collectAsState()
    val bgType by viewModel.bgType.collectAsState()
    val isHazeEnabled by viewModel.isHazeEnabled.collectAsState() 
    
    val themeColorName by viewModel.currentThemeColor.collectAsState()
    val isCustomColor by viewModel.isCustomColor.collectAsState()
    val customPrimary by viewModel.customPrimaryColor.collectAsState()

    var alternateIcon by remember { mutableStateOf(false) }
    val currentDeviceDpi = context.resources.configuration.densityDpi
    var dpiSliderValue by remember { mutableFloatStateOf(if (savedDpi == 0) currentDeviceDpi.toFloat() else savedDpi.toFloat()) }
    LaunchedEffect(savedDpi) { if (savedDpi != 0) dpiSliderValue = savedDpi.toFloat() }

    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }
    
    val effectivePrimary = remember(isDynamic, themeColorName, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color.Unspecified
            else -> themeColorName.primary
        }
    }

    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        effectivePrimary
    }

    val (tintedBackground, cardContainerColor) = generateThemedBackgroundColor(finalPrimary, useDarkTheme)

    val isGlassActive = isHazeEnabled && bgType != BgType.SYSTEM

    val actualCardContainerColor = when {
        isGlassActive -> Color.Transparent
        bgType != BgType.SYSTEM -> MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness)
        else -> cardContainerColor
    }

    val cardShape = RoundedCornerShape(28.dp)
    
    val glassModifier = if (isGlassActive) {
        Modifier
            .clip(cardShape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
    } else Modifier

    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val openPollingDialog = rememberDialogState(initiallyVisible = false)
    val openDpiDialog = rememberDialogState(initiallyVisible = false)
    var pollingValueStr by remember { mutableStateOf((pollingInterval / 1000).toString()) }
    val intervalSeconds = pollingValueStr.toLongOrNull()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadSettingsData(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = if (bgType != BgType.SYSTEM) Color.Transparent else tintedBackground,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            SettingsLargeAppBar(
                text = stringResource(R.string.settings_title), 
                onBack = onBackClick,
                scrollBehavior = scrollBehavior, 
                hazeState = hazeState, 
                containerColor = Color.Transparent
            )
        }
    ) { paddingValues -> 
        
        Box(modifier = Modifier.fillMaxSize()) {
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    Text(
                        text = stringResource(R.string.sect_general), 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            fontSize = 13.sp
                        ), 
                        color = finalPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = actualCardContainerColor), 
                        shape = cardShape, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(glassModifier)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            
                            // [UPDATE] Language display dengan semua bahasa baru
                            val langSubtitle = when(currentLanguageCode) {
                                "in" -> "Bahasa Indonesia"
                                "en" -> "English (US)"
                                // Cyrillic
                                "ru" -> "Русский"
                                "uk" -> "Українська"
                                "be" -> "Беларуская"
                                "kk" -> "Қазақша"
                                "ro-MD" -> "Română (Moldova)"
                                // Chinese
                                "zh-CN" -> "简体中文"
                                "zh-SG" -> "简体中文 (新加坡)"
                                "zh-TW" -> "繁體中文 (台灣)"
                                "zh-HK" -> "繁體中文 (香港)"
                                "zh-MO" -> "繁體中文 (澳門)"
                                else -> stringResource(R.string.lang_system)
                            }
                            
                            SettingsItem(
                                Icons.Outlined.Language, 
                                stringResource(R.string.pref_language), 
                                langSubtitle, 
                                onLanguageClick, 
                                finalPrimary
                            )
                            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = dividerColor, thickness = 0.8.dp)
                            SettingsItem(
                                icon = Icons.Outlined.Palette,
                                title = stringResource(R.string.pref_wallpaper_style),
                                subtitle = stringResource(R.string.pref_wallpaper_style_desc),
                                onClick = { navController.navigate(NavigationRoute.WallpaperStyle.route) },
                                iconTint = finalPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.sect_custom), 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            fontSize = 13.sp
                        ), 
                        color = finalPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = actualCardContainerColor), 
                        shape = cardShape, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(glassModifier)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openDpiDialog.visible = true }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AspectRatio, null, tint = finalPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.label_applied_dpi), 
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ), 
                                        color = onSurfaceColor
                                    )
                                    Text(
                                        text = stringResource(R.string.pref_applied_dpi_desc, savedDpi), 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = onSurfaceVariantColor
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = dividerColor, thickness = 0.8.dp)
                            SettingsSwitchItem(
                                Icons.Outlined.Android, 
                                stringResource(R.string.pref_alt_icon), 
                                stringResource(R.string.pref_alt_icon_desc), 
                                alternateIcon, 
                                { alternateIcon = it }, 
                                finalPrimary, 
                                finalPrimary
                            )
                            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = dividerColor, thickness = 0.8.dp)
                            SettingsItem(
                                Icons.Default.Timer, 
                                stringResource(R.string.pref_polling), 
                                stringResource(R.string.pref_polling_desc, pollingInterval), 
                                { openPollingDialog.visible = true }, 
                                finalPrimary
                            )
                        }
                    }
                } 
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    
    DialogUnstyled(
        state = openDpiDialog, 
        hazeState = hazeState, 
        isCustomBg = (bgType != BgType.SYSTEM), 
        title = stringResource(R.string.dialog_title_dpi), 
        text = { 
            Column {
                Text(
                    stringResource(R.string.dialog_msg_dpi, currentDeviceDpi, dpiSliderValue.roundToInt()), 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = dpiSliderValue, 
                    onValueChange = { dpiSliderValue = it }, 
                    valueRange = 320f..600f, 
                    steps = 0, 
                    colors = SliderDefaults.colors(thumbColor = finalPrimary, activeTrackColor = finalPrimary)
                )
            }
        }, 
        confirmButton = { 
            Button(
                onClick = { 
                    openDpiDialog.visible = false; 
                    viewModel.setAppDpi(dpiSliderValue.roundToInt()); 
                    (context as? Activity)?.recreate() 
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = finalPrimary), 
                shape = RoundedCornerShape(50)
            ) { 
                Text(stringResource(R.string.btn_confirm), color = Color.White) 
            } 
        }, 
        dismissButton = { 
            TextButton(
                onClick = { openDpiDialog.visible = false }, 
                colors = ButtonDefaults.textButtonColors(contentColor = finalPrimary)
            ) { 
                Text(stringResource(R.string.btn_cancel)) 
            } 
        }
    )

    DialogUnstyled(
        state = openPollingDialog, 
        hazeState = hazeState, 
        isCustomBg = (bgType != BgType.SYSTEM), 
        title = stringResource(R.string.pref_polling), 
        text = { 
            Column { 
                Text(
                    stringResource(R.string.dialog_msg_polling), 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = pollingValueStr, 
                    onValueChange = { pollingValueStr = it }, 
                    label = { Text(stringResource(R.string.label_interval_sec)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    singleLine = true, 
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface, 
                        cursorColor = finalPrimary, 
                        focusedBorderColor = finalPrimary, 
                        focusedLabelColor = finalPrimary, 
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ), 
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number, 
                        imeAction = ImeAction.Done
                    ), 
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            if (intervalSeconds != null && intervalSeconds in 1..30) { 
                                viewModel.setPollingInterval(intervalSeconds * 1000)
                                openPollingDialog.visible = false 
                            } 
                        }
                    )
                ) 
            } 
        }, 
        confirmButton = { 
            TextButton(
                onClick = { 
                    if (intervalSeconds != null && intervalSeconds in 1..30) { 
                        viewModel.setPollingInterval(intervalSeconds * 1000)
                        openPollingDialog.visible = false 
                    } 
                }, 
                colors = ButtonDefaults.textButtonColors(contentColor = finalPrimary)
            ) { 
                Text(stringResource(R.string.btn_apply)) 
            } 
        }, 
        dismissButton = { 
            TextButton(
                onClick = { openPollingDialog.visible = false }, 
                colors = ButtonDefaults.textButtonColors(contentColor = finalPrimary)
            ) { 
                Text(stringResource(R.string.btn_cancel)) 
            } 
        }
    )
}

@Composable
private fun SettingsLargeAppBar(
    text: String,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    hazeState: HazeState? = null,
    containerColor: Color
) {
    val glassStyle = HazeMaterials.thin()
    
    val modifier = if (hazeState != null) {
        Modifier.hazeEffect(state = hazeState) {
            style = glassStyle 
            progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f, preferPerformance = true)
        }
    } else Modifier

    LargeTopAppBar(
        title = { 
            Text(
                text = text, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis, 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.headlineMedium
            ) 
        },
        navigationIcon = { 
            IconButton(onClick = onBack) { 
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = stringResource(R.string.btn_cancel)
                ) 
            } 
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor, 
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}
