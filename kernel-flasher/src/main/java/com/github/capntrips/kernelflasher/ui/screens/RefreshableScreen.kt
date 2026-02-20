package com.github.capntrips.kernelflasher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.capntrips.kernelflasher.R
import com.github.capntrips.kernelflasher.ui.screens.main.MainViewModel
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalSerializationApi
@Composable
fun RefreshableScreen(
    viewModel: MainViewModel,
    navController: NavController,
    swipeEnabled: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val statusBar = WindowInsets.statusBars.only(WindowInsetsSides.Top).asPaddingValues()
    val navigationBars = WindowInsets.navigationBars.asPaddingValues()
    val context = LocalContext.current
    
    // M3 PullToRefresh State
    val state = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(statusBar)
            ) {
                if (navController.previousBackStackEntry != null) {
                    AnimatedVisibility(
                        !viewModel.isRefreshing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(16.dp, 8.dp, 0.dp, 8.dp)
                        ) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    ) { paddingValues ->
        if (swipeEnabled) {
            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.refresh(context) },
                state = state,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                indicator = {
                     PullToRefreshDefaults.Indicator(
                        state = state,
                        isRefreshing = viewModel.isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = MaterialTheme.colorScheme.background,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp, 0.dp, 16.dp, 16.dp + navigationBars.calculateBottomPadding())
                        .verticalScroll(rememberScrollState()),
                    content = content
                )
            }
        } else {
            // Jika swipe tidak di-enable, langsung render kolom
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp, 0.dp, 16.dp, 16.dp + navigationBars.calculateBottomPadding())
                        .verticalScroll(rememberScrollState()),
                    content = content
                )
            }
        }
    }
}
