/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.setedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PropCategory(val title: String, val command: String) {
    Global("Global Table", "settings list global"),
    Secure("Secure Table", "settings list secure"),
    System("System Table", "settings list system"),
    Android("Android Props", "getprop")
}

data class SetEditItem(val key: String, val value: String)

@Composable
fun SetEditScreen(navController: NavController) {
    val categories = PropCategory.values()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // State Data
    var propList by remember { mutableStateOf<List<SetEditItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // State Search
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // State BottomSheet
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<SetEditItem?>(null) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- LOGIKA UTAMA ---

    fun refreshData() {
        scope.launch(Dispatchers.IO) {
            isLoading = true
            val category = categories[selectedTabIndex]
            
            val output = Shell.cmd(category.command).exec().out
            
            val parsedList = output.mapNotNull { line ->
                if (category == PropCategory.Android) {
                    val parts = line.split("]: [")
                    if (parts.size == 2) {
                        SetEditItem(parts[0].replace("[", "").trim(), parts[1].replace("]", "").trim())
                    } else null
                } else {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        SetEditItem(parts[0].trim(), parts[1].trim())
                    } else null
                }
            }
            
            withContext(Dispatchers.Main) {
                propList = parsedList
                isLoading = false
            }
        }
    }

    // LOGIKA BARU: Hapus 'searchQuery = ""' agar search tetap nempel walau pindah tab
    LaunchedEffect(selectedTabIndex) {
        refreshData()
    }

    // Filter List berdasarkan Search Query (Realtime)
    val filteredList = remember(propList, searchQuery) {
        if (searchQuery.isEmpty()) {
            propList
        } else {
            propList.filter {
                it.key.contains(searchQuery, ignoreCase = true) || 
                it.value.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        // Menggunakan containerColor surface agar status bar blend
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            // Kita bungkus Column TopBar dengan Surface agar warnanya konsisten
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp // Sedikit bayangan biar misah sama konten
            ) {
                Column {
                    // Top Bar Dinamis
                    if (isSearchActive) {
                        SearchBarTop(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClose = { 
                                isSearchActive = false 
                                searchQuery = ""
                            }
                        )
                    } else {
                        TopAppBar(
                            title = { Text("Prop Editor") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        )
                    }

                    // Tabs Menu
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        divider = {} // Hilangkan garis default tabrow biar rapi
                    ) {
                        categories.forEachIndexed { index, category ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(category.title) }
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredList) { item ->
                    ListItem(
                        headlineContent = { 
                            Text(item.key, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) 
                        },
                        supportingContent = { 
                            Text(item.value, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.secondary) 
                        },
                        modifier = Modifier.clickable {
                            selectedItem = item
                            showBottomSheet = true
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                
                if (filteredList.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No properties found",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET ---
    if (showBottomSheet && selectedItem != null) {
        EditPropBottomSheet(
            item = selectedItem!!,
            category = categories[selectedTabIndex],
            onDismiss = { showBottomSheet = false },
            onSave = { key, newValue ->
                scope.launch(Dispatchers.IO) {
                    val cmd = if (categories[selectedTabIndex] == PropCategory.Android) {
                        "setprop $key \"$newValue\""
                    } else {
                        val table = categories[selectedTabIndex].command.split(" ")[2]
                        "settings put $table $key \"$newValue\""
                    }
                    val result = Shell.cmd(cmd).exec()
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Saved: $key")
                            refreshData()
                        } else {
                            snackbarHostState.showSnackbar("Failed to save")
                        }
                        showBottomSheet = false
                    }
                }
            },
            onDelete = { key ->
                scope.launch(Dispatchers.IO) {
                    val cmd = if (categories[selectedTabIndex] == PropCategory.Android) "" 
                    else "settings delete ${categories[selectedTabIndex].command.split(" ")[2]} $key"
                    
                    if (cmd.isNotEmpty()) {
                        Shell.cmd(cmd).exec()
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar("Deleted: $key")
                            refreshData()
                            showBottomSheet = false
                        }
                    } else {
                        withContext(Dispatchers.Main) { snackbarHostState.showSnackbar("Cannot delete system props") }
                    }
                }
            }
        )
    }
}

// --- COMPONENT SEARCH BAR BARU ---
@Composable
fun SearchBarTop(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    // Kita ambil insets status bar
    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues()

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            // INI PENTING: Kita tambahkan padding atas sesuai tinggi status bar
            // Tapi background tetap mengikuti warna container TextField
            .padding(top = statusBarsPadding.calculateTopPadding()), 
        placeholder = { Text("Search property...") },
        leadingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah biar clean
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun EditPropBottomSheet(
    item: SetEditItem,
    category: PropCategory,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var editValue by remember { mutableStateOf(item.value) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Edit Property", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Text("Key", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            OutlinedButton(
                onClick = { clipboardManager.setText(AnnotatedString(item.key)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(item.key, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Text("Original Value", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            OutlinedButton(
                onClick = { clipboardManager.setText(AnnotatedString(item.value)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(item.value, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            OutlinedTextField(
                value = editValue,
                onValueChange = { editValue = it },
                label = { Text("Edit Value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onDelete(item.key) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Remove")
                }

                Button(
                    onClick = { onSave(item.key, editValue) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
    }
}
