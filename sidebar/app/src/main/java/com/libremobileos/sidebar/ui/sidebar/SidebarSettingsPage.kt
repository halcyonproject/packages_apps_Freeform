@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.libremobileos.sidebar.ui.sidebar

import android.content.Context
import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hlcyn.ui.components.*
import com.libremobileos.sidebar.R
import com.libremobileos.sidebar.bean.SidebarAppInfo
import com.libremobileos.sidebar.service.SidebarMonitorService

import com.android.settingslib.spa.framework.theme.SettingsTheme
import com.android.settingslib.spa.widget.preference.MainSwitchPreference
import com.android.settingslib.spa.widget.preference.Preference
import com.android.settingslib.spa.widget.preference.PreferenceModel
import com.android.settingslib.spa.widget.preference.SwitchPreference
import com.android.settingslib.spa.widget.preference.SwitchPreferenceModel
import com.android.settingslib.spa.widget.ui.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarSettingsPage(
    viewModel: SidebarSettingsViewModel
) {
    SettingsTheme {
        SidebarSettingsContent(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SidebarSettingsContent(
    viewModel: SidebarSettingsViewModel
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var showCustomization by remember { mutableStateOf(false) }

    if (showCustomization) {
        SidebarCustomizationSettingsPage(
            sharedPrefs = sharedPrefs,
            onBack = { showCustomization = false },
            onSettingChanged = {
                // Refresh sidebar if enabled
                if (viewModel.getSidebarEnabled()) {
                    viewModel.setSidebarEnabled(false)
                    viewModel.setSidebarEnabled(true)
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.sidebar_label),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.padding(start = 12.dp)) {
                            FilledTonalIconButton(
                                onClick = { /* Handle back or exit */ },
                                shape = IconButtonDefaults.smallRoundShape,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                Crossfade(targetState = selectedTab, modifier = Modifier.fillMaxSize()) { tab ->
                    when (tab) {
                        0 -> SettingsTab(viewModel, onOpenCustomization = { showCustomization = true })
                        1 -> AppsTab(viewModel)
                    }
                }

                HalcyonFloatingBottomBar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    HalcyonFloatingBottomBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = Icons.Default.Settings
                    )
                    HalcyonFloatingBottomBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = Icons.Default.Apps
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsTab(
    viewModel: SidebarSettingsViewModel,
    onOpenCustomization: () -> Unit
) {
    val context = LocalContext.current
    val sidebarEnabled by viewModel.sidebarEnabledFlow.collectAsState()
    val autoEnableSelectedApps by viewModel.autoEnableSelectedAppsEnabledFlow.collectAsState()
    val predictedAppsEnabled by viewModel.predictedAppsEnabledFlow.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 80.dp)
    ) {
        MainSwitchPreference(
            object : SwitchPreferenceModel {
                override val title = stringResource(R.string.enable_sideline)
                override val checked = { sidebarEnabled }
                override val onCheckedChange: (Boolean) -> Unit = {
                    viewModel.setSidebarEnabled(it)
                    val intent = Intent(context, SidebarMonitorService::class.java)
                    if (it || autoEnableSelectedApps) {
                        context.startService(intent)
                    } else {
                        context.stopService(intent)
                    }
                }
            }
        )

        Category(title = stringResource(R.string.sidebar_options)) {
            SwitchPreference(
                object : SwitchPreferenceModel {
                    override val title = stringResource(R.string.sidebar_auto_enable_selected_apps)
                    override val summary = { context.getString(R.string.sidebar_auto_enable_selected_apps_summary) }
                    override val checked = { autoEnableSelectedApps }
                    override val onCheckedChange: (Boolean) -> Unit = {
                        viewModel.setAutoEnableSelectedAppsEnabled(it)
                        val intent = Intent(context, SidebarMonitorService::class.java)
                        if (it || sidebarEnabled) {
                            context.startService(intent)
                        } else {
                            context.stopService(intent)
                        }
                    }
                }
            )

            SwitchPreference(
                object : SwitchPreferenceModel {
                    override val title = stringResource(R.string.sidebar_predicted_apps)
                    override val summary = { context.getString(R.string.sidebar_predicted_apps_summary) }
                    override val checked = { predictedAppsEnabled }
                    override val onCheckedChange: (Boolean) -> Unit = {
                        viewModel.setPredictedAppsEnabled(it)
                    }
                }
            )
        }

        Category(title = stringResource(R.string.sidebar_customization)) {
            Preference(
                object : PreferenceModel {
                    override val title = stringResource(R.string.sidebar_customization_title)
                    override val onClick = onOpenCustomization
                }
            )
        }
    }
}

@Composable
fun AppsTab(
    viewModel: SidebarSettingsViewModel
) {
    val sidebarApps by viewModel.appListFlow.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp) // Space for floating bar
    ) {
        item {
            Category(title = stringResource(R.string.sidebar_select_apps)) {
                Column {
                    sidebarApps.forEach { appInfo ->
                        SidebarAppListItem(
                            appInfo = appInfo,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.addSidebarApp(appInfo)
                                } else {
                                    viewModel.deleteSidebarApp(appInfo)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarAppListItem(
    appInfo: SidebarAppInfo,
    onCheckedChange: (Boolean) -> Unit
) {
    SwitchPreference(
        object : SwitchPreferenceModel {
            override val title = appInfo.label
            override val icon = @Composable {
                Image(
                    painter = rememberDrawablePainter(appInfo.icon),
                    contentDescription = appInfo.label,
                    modifier = Modifier.size(32.dp)
                )
            }
            override val checked = { appInfo.isSidebarApp }
            override val onCheckedChange: (Boolean) -> Unit = onCheckedChange
        }
    )
}
