/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.zuan.kernelmanager.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Konstanta Warna Terminal
private object TermColors {
    val Background = Color(0xFF000000) // Hitam Pekat Termux
    val TextDefault = Color(0xFFFFFFFF)
    val Green = Color(0xFF00FF00) // Hacker Green
    val Red = Color(0xFFFF5252)
    val Cyan = Color(0xFF18FFFF)
    val Yellow = Color(0xFFFFFF00)
    val Blue = Color(0xFF448AFF)
}

@Composable
fun TerminalScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var command by remember { mutableStateOf("") }
    
    // State untuk menyimpan list output
    val logs = remember { mutableStateListOf<AnnotatedString>() }
    val listState = rememberLazyListState()
    
    // Focus & Keyboard Manager
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Banner Start
    LaunchedEffect(Unit) {
        val isRoot = withContext(Dispatchers.IO) { Shell.rootAccess() }
        
        logs.add(parseAnsi("\u001B[36mWelcome to Zuan Terminal\u001B[0m"))
logs.add(parseAnsi("\u001B[32m" +
        " ______     __  __     ______     __   __    \n" +
        "/\\___  \\   /\\ \\/\\ \\   /\\  __ \\   /\\ \"-.\\ \\   \n" +
        "\\/_/  /__  \\ \\ \\_\\ \\  \\ \\  __ \\  \\ \\ \\-.  \\  \n" +
        "  /\\_____\\  \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_\\\\\"\\_\\ \n" +
        "  \\/_____/   \\/_____/   \\/_/\\/_/   \\/_/ \\/_/ \n" +
        "\u001B[0m"))
logs.add(parseAnsi("Root Access: ${if(isRoot) "\u001B[32mGRANTED\u001B[0m" else "\u001B[31mDENIED\u001B[0m"}"))
logs.add(parseAnsi("Type 'help' or any shell command."))

        // Auto Focus keyboard saat layar dibuka
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Auto Scroll saat ada log baru
    LaunchedEffect(logs.size) {
        if (logs.size > 0) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Terminal", 
                        fontFamily = FontFamily.Monospace, 
                        fontSize = 14.sp,
                        color = TermColors.TextDefault 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TermColors.TextDefault)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TermColors.Background,
                    titleContentColor = TermColors.TextDefault,
                    navigationIconContentColor = TermColors.TextDefault,
                    actionIconContentColor = TermColors.TextDefault
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TermColors.Background)
                .padding(innerPadding)
                .imePadding() // PENTING: Agar input naik saat keyboard muncul
                .padding(8.dp)
                // Klik area kosong mana saja untuk memunculkan keyboard
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
        ) {
            // 1. Output Area
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                items(logs) { logLine ->
                    Text(
                        text = logLine,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // 2. Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tanda Prompt
                Text(
                    text = "$ ",
                    color = TermColors.Green,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                BasicTextField(
                    value = command,
                    onValueChange = { command = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .focusRequester(focusRequester), // Hubungkan FocusRequester
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TermColors.TextDefault,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(TermColors.Green),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        autoCorrect = false // Matikan autocorrect ala Terminal
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        val cmdToRun = command
                        command = "" // Clear input

                        scope.launch {
                            logs.add(parseAnsi("\u001B[32m$ \u001B[0m$cmdToRun"))
                            val output = runLibSuCommand(cmdToRun)
                            logs.add(parseAnsi(output))
                            
                            // Pastikan keyboard tetap muncul setelah Enter
                            focusRequester.requestFocus() 
                        }
                    })
                )
            }
        }
    }
}

/**
 * Fungsi Eksekusi Shell Menggunakan LibSU
 */
suspend fun runLibSuCommand(command: String): String {
    return withContext(Dispatchers.IO) {
        if (command.trim().isEmpty()) return@withContext ""
        if (command.trim() == "clear") return@withContext "\n\n\n\n\n\n\n\n"

        try {
            val result = Shell.cmd(command).exec()
            val output = StringBuilder()
            
            if (result.out.isNotEmpty()) output.append(result.out.joinToString("\n"))
            
            if (result.err.isNotEmpty()) {
                if (output.isNotEmpty()) output.append("\n")
                output.append("\u001B[31m${result.err.joinToString("\n")}\u001B[0m")
            }

            output.toString()
        } catch (e: Exception) {
            "\u001B[31mError: ${e.message}\u001B[0m"
        }
    }
}

/**
 * Parser ANSI Sederhana
 */
fun parseAnsi(text: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = "\u001B\\[[;\\d]*m".toRegex()
        var lastIndex = 0
        var currentColor = TermColors.TextDefault
        var isBold = false

        regex.findAll(text).forEach { matchResult ->
            val textSegment = text.substring(lastIndex, matchResult.range.first)
            withStyle(style = SpanStyle(color = currentColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)) {
                append(textSegment)
            }

            val code = matchResult.value
            val params = code.removePrefix("\u001B[").removeSuffix("m").split(";")

            params.forEach { param ->
                when (param) {
                    "0", "00" -> { currentColor = TermColors.TextDefault; isBold = false }
                    "1" -> isBold = true
                    "30" -> currentColor = Color.Black
                    "31" -> currentColor = TermColors.Red
                    "32" -> currentColor = TermColors.Green
                    "33" -> currentColor = TermColors.Yellow
                    "34" -> currentColor = TermColors.Blue
                    "35" -> currentColor = Color.Magenta
                    "36" -> currentColor = TermColors.Cyan
                    "37" -> currentColor = Color.White
                }
            }
            lastIndex = matchResult.range.last + 1
        }

        if (lastIndex < text.length) {
            withStyle(style = SpanStyle(color = currentColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)) {
                append(text.substring(lastIndex))
            }
        }
    }
}