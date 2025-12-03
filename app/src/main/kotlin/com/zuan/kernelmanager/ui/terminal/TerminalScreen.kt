package com.zuan.kernelmanager.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.utils.TerminalUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    navController: NavController,
    viewModel: TerminalViewModel = viewModel()
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(viewModel.shouldScrollToBottom) {
        if (viewModel.shouldScrollToBottom && viewModel.logs.isNotEmpty()) {
            listState.scrollToItem(viewModel.logs.lastIndex)
            viewModel.onScrolledToBottom()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Zuan Shell", fontFamily = FontFamily.Monospace, fontSize = 16.sp) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            // 1. Output Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = viewModel.logs,
                    key = { it.id } 
                ) { logItem ->
                    // UPDATE: Bungkus Text dengan SelectionContainer biar bisa di-copy
                    SelectionContainer {
                        Text(
                            text = logItem.content,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // 2. Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    color = TerminalUtils.TermColors_Green,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                BasicTextField(
                    value = viewModel.commandInput,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(TerminalUtils.TermColors_Green),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        autoCorrect = false
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.runCommand()
                    })
                )
                
                // UPDATE: Tombol Aksi (Send / Stop)
                IconButton(
                    onClick = {
                        if (viewModel.isRunning) {
                            viewModel.stopCommand() // Stop jika sedang jalan
                        } else {
                            viewModel.runCommand() // Run jika idle
                        }
                    }
                ) {
                    if (viewModel.isRunning) {
                        // Icon Merah Silang (Stop)
                        Icon(Icons.Default.Close, contentDescription = "Stop", tint = TerminalUtils.TermColors_Red)
                    } else {
                        // Icon Hijau Panah (Send)
                        Icon(Icons.Default.Send, contentDescription = "Run", tint = TerminalUtils.TermColors_Green)
                    }
                }
            }
        }
    }
}
