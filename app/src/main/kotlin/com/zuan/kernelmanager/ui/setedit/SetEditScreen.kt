/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
/*
* Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
* All Rights Reserved.
*/
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class,
    ExperimentalFoundationApi::class
)

package com.zuan.kernelmanager.ui.setedit

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation.NavController
import com.topjohnwu.superuser.Shell
// PENTING: Import Haze Materials agar style 'thin' bisa dipakai
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
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
    
    // PAGER STATE: Menggantikan state manual selectedTabIndex
    // Kita inisialisasi pager dengan jumlah halaman sesuai jumlah kategori
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { categories.size })
    
    // Mengambil index halaman saat ini dari pagerState
    val currentTabIndex = pagerState.currentPage

    // State Data
    var propList by remember { mutableStateOf<List<SetEditItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // State Search
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // State BottomSheet
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<SetEditItem?>(null) }

    // Haze State untuk efek kaca
    val hazeState = rememberHazeState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- LOGIKA UTAMA ---
    // Fungsi refreshData sekarang menerima index kategori secara eksplisit
    fun refreshData(index: Int) {
        scope.launch(Dispatchers.IO) {
            isLoading = true
            val category = categories[index]

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

    // Trigger refresh setiap kali halaman (pager) berubah
    LaunchedEffect(currentTabIndex) {
        refreshData(currentTabIndex)
    }

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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        // Kita gunakan Box sebagai root agar TopBar bisa "mengambang" di atas List
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. PAGER CONTENT (Di Lapis Bawah)
            // HorizontalPager memungkinkan swipe kiri/kanan
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                
                // Pastikan list hanya dirender jika halaman aktif (untuk efisiensi ringan)
                // Atau biarkan render semua tapi logic data tetap terpusat
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState), // Sumber efek haze (Source)
                    contentPadding = PaddingValues(
                        // Padding atas disesuaikan agar item pertama tidak ketutup Toolbar kaca
                        top = 140.dp,
                        bottom = innerPadding.calculateBottomPadding() + 80.dp
                    )
                ) {
                    // Tampilkan list hanya jika tidak loading, atau tampilkan kosong
                    if (isLoading) {
                        // Saat loading di swipe, kita bisa kosongkan dulu atau tampilkan skeleton
                        // Di sini kita biarkan kosong agar indikator loading terlihat jelas
                    } else {
                        items(filteredList) { item ->
                            ListItem(
                                headlineContent = {
                                    Text(item.key, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                supportingContent = {
                                    Text(item.value, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.secondary)
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.clickable {
                                    selectedItem = item
                                    showBottomSheet = true
                                }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }

                    if (filteredList.isEmpty() && !isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No properties found",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            // 2. HEADER / TOP BAR AREA (Di Lapis Atas - Floating)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    // A. Efek Blur (Glass)
                    .hazeChild(
                        state = hazeState,
                        style = HazeMaterials.thin() // Pakai style Thin ala iOS
                    )
                    // B. Background Tint (Warna Tipis)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(bottom = 8.dp)
            ) {
                // Status Bar Spacer
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                // Toolbar Logic
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
                        },
                        // Container harus transparan agar tembus pandang ke Column di belakangnya
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }

                // Tabs Menu (Sekarang tersinkronisasi dengan Pager)
                ScrollableTabRow(
                    selectedTabIndex = currentTabIndex,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent, // Transparan
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[currentTabIndex]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = currentTabIndex == index,
                            onClick = {
                                // Saat tab diklik, animasikan pager ke halaman tersebut
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(category.title) }
                        )
                    }
                }
            }

            // Loading Indicator (Floating)
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }
    }

    // --- BOTTOM SHEET ---
    if (showBottomSheet && selectedItem != null) {
        EditPropBottomSheet(
            item = selectedItem!!,
            category = categories[currentTabIndex], // Gunakan currentTabIndex dari Pager
            onDismiss = { showBottomSheet = false },
            onSave = { key, newValue ->
                scope.launch(Dispatchers.IO) {
                    val cmd = if (categories[currentTabIndex] == PropCategory.Android) {
                        "setprop $key \"$newValue\""
                    } else {
                        val table = categories[currentTabIndex].command.split(" ")[2]
                        "settings put $table $key \"$newValue\""
                    }
                    val result = Shell.cmd(cmd).exec()
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Saved: $key")
                            refreshData(currentTabIndex)
                        } else {
                            snackbarHostState.showSnackbar("Failed to save")
                        }
                        showBottomSheet = false
                    }
                }
            },
            onDelete = { key ->
                scope.launch(Dispatchers.IO) {
                    val cmd = if (categories[currentTabIndex] == PropCategory.Android) ""
                    else "settings delete ${categories[currentTabIndex].command.split(" ")[2]} $key"

                    if (cmd.isNotEmpty()) {
                        Shell.cmd(cmd).exec()
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar("Deleted: $key")
                            refreshData(currentTabIndex)
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

// --- SEARCH BAR ---
@Composable
fun SearchBarTop(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
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
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

// --- EDIT PROP BOTTOM SHEET (Fixed IME/Keyboard & Build Error) ---
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

    // State untuk kontrol BottomSheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // State scroll untuk konten agar bisa digulir saat keyboard muncul
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = Color.Black.copy(alpha = 0.35f),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // --- LOGIKA BLUR BACKGROUND NATIVE ---
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect {
            if (dialogWindowProvider != null) {
                val window = dialogWindowProvider.window
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window.attributes.blurBehindRadius = 64
                    window.setDimAmount(0.3f)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // FIXED: Logic untuk handle keyboard
                .navigationBarsPadding() // 1. Hindari navigasi bar bawah
                .imePadding() // 2. Push layout ke atas sesuai tinggi keyboard
                .verticalScroll(scrollState) // 3. Izinkan scroll jika layout terhimpit
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
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
