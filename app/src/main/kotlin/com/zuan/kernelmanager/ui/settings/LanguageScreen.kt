/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField  // [IMPORT TAMBAHAN]
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

data class LanguageOption(val code: String, val name: String, val subtitle: String? = null)

@Composable
fun LanguageScreen(
    navController: NavController, 
    viewModel: SettingsViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = rememberHazeState()

    val isDynamic by viewModel.isDynamicColor.collectAsState()
    val selectedThemeColor by viewModel.currentThemeColor.collectAsState()
    
    val accentColor = if (isDynamic) {
        MaterialTheme.colorScheme.primary 
    } else {
        selectedThemeColor.primary
    }

    val currentLanguageCode by viewModel.currentLanguage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val recommendedList = listOf(
        LanguageOption("system", stringResource(R.string.lang_system)), 
        LanguageOption("in", stringResource(R.string.lang_indonesia), "Bahasa Indonesia"),
        LanguageOption("en", stringResource(R.string.lang_english), "English (US)"),
        LanguageOption("ru", stringResource(R.string.lang_russian), "Русский (Россия)"),
        LanguageOption("uk", stringResource(R.string.lang_ukrainian), "Українська"),
        LanguageOption("be", stringResource(R.string.lang_belarusian), "Беларуская"),
        LanguageOption("kk", stringResource(R.string.lang_kazakh), "Қазақша"),
        LanguageOption("ro-MD", stringResource(R.string.lang_moldovan), "Română (Moldova)"),
        LanguageOption("zh-CN", stringResource(R.string.lang_chinese_cn), "简体中文 (中国)"),
        LanguageOption("zh-TW", stringResource(R.string.lang_chinese_tw), "繁體中文 (台灣)"),
        LanguageOption("zh-HK", stringResource(R.string.lang_chinese_hk), "繁體中文 (香港)")
    )

    val allLanguagesList = listOf(
        LanguageOption("zh-SG", stringResource(R.string.lang_chinese_sg), "简体中文 (新加坡)"),
        LanguageOption("zh-MO", stringResource(R.string.lang_chinese_mo), "繁體中文 (澳門)"),
        LanguageOption("ja", stringResource(R.string.lang_japanese), "日本語"),
        LanguageOption("ko", stringResource(R.string.lang_korean), "한국어"),
        LanguageOption("vi", stringResource(R.string.lang_vietnamese), "Tiếng Việt"),
        LanguageOption("de", stringResource(R.string.lang_german), "Deutsch"),
        LanguageOption("fr", stringResource(R.string.lang_french), "Français"),
        LanguageOption("es", stringResource(R.string.lang_spanish), "Español"),
        LanguageOption("pt", stringResource(R.string.lang_portuguese), "Português"),
        LanguageOption("it", stringResource(R.string.lang_italian), "Italiano"),
        LanguageOption("pl", stringResource(R.string.lang_polish), "Polski"),
        LanguageOption("tr", stringResource(R.string.lang_turkish), "Türkçe"),
        LanguageOption("nl", stringResource(R.string.lang_dutch), "Nederlands")
    )

    val filteredRecommended = recommendedList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.subtitle?.contains(searchQuery, ignoreCase = true) == true ||
        it.code.contains(searchQuery, ignoreCase = true)
    }
    
    val filteredAll = allLanguagesList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.subtitle?.contains(searchQuery, ignoreCase = true) == true ||
        it.code.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.pref_language), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentColor)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .padding(paddingValues)
        ) {
            SearchBarFunctional(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                accentColor = accentColor
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (filteredRecommended.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.lang_sect_recommended), accentColor) }
                    
                    items(filteredRecommended) { language ->
                        LanguageItemRow(
                            language = language,
                            isSelected = language.code == currentLanguageCode,
                            activeColor = accentColor,
                            onClick = { 
                                viewModel.setAppLanguage(language.code)
                            }
                        )
                    }
                }

                if (filteredAll.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.lang_sect_all), accentColor) }
                    items(filteredAll) { language ->
                        LanguageItemRow(
                            language = language,
                            isSelected = language.code == currentLanguageCode,
                            activeColor = accentColor,
                            onClick = { viewModel.setAppLanguage(language.code) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarFunctional(
    query: String, 
    onQueryChange: (String) -> Unit, 
    accentColor: Color
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) {
        accentColor.copy(alpha = 0.15f).compositeOver(MaterialTheme.colorScheme.surface)
    } else {
        accentColor.copy(alpha = 0.1f).compositeOver(MaterialTheme.colorScheme.surface)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp)),
        color = containerColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = accentColor)
            Spacer(Modifier.width(12.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.lang_search_hint), 
                                style = MaterialTheme.typography.bodyLarge, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = accentColor.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        title, 
        style = MaterialTheme.typography.labelLarge, 
        color = color, 
        fontWeight = FontWeight.Bold, 
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun LanguageItemRow(
    language: LanguageOption, 
    isSelected: Boolean, 
    activeColor: Color, 
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                language.name, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (language.subtitle != null) {
                Text(
                    language.subtitle, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (isSelected) {
            Icon(Icons.Default.Check, "Selected", tint = activeColor)
        }
    }
}
