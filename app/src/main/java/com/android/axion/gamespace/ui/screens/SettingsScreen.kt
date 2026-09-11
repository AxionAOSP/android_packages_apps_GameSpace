@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.android.axion.gamespace.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Slider
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.compose.scaffold.AxionPinnedTopAppBar
import com.android.axion.gamespace.R
import com.android.axion.gamespace.ui.components.SettingsDropdown
import com.android.axion.gamespace.ui.components.SettingsSection
import com.android.axion.gamespace.ui.components.SettingsSlider
import com.android.axion.gamespace.ui.components.SettingsSwitch
import com.android.axion.gamespace.ui.viewmodel.SettingsViewModel

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val listState = rememberLazyListState()

    var showQuickStartDialog by remember { mutableStateOf(false) }

    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        installedApps = loadInstalledApps(context)
    }

    val callsModeOptions = listOf(
        "0" to stringResource(R.string.in_game_calls_no_action),
        "1" to stringResource(R.string.in_game_calls_auto_answer),
        "2" to stringResource(R.string.in_game_calls_auto_reject)
    )

    val ringerModeOptions = listOf(
        "0" to stringResource(R.string.ringer_mode_silent),
        "1" to stringResource(R.string.ringer_mode_vibrate),
        "2" to stringResource(R.string.ringer_mode_normal),
        "3" to stringResource(R.string.ringer_mode_no_change)
    )

    if (showQuickStartDialog) {
        QuickStartAppsDialog(
            apps = installedApps,
            selectedApps = viewModel.quickStartApps.split(",").filter { it.isNotBlank() }.toSet(),
            onDismiss = { showQuickStartDialog = false },
            onConfirm = { selected ->
                viewModel.updateQuickStartApps(selected.joinToString(","))
                showQuickStartDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AxionPinnedTopAppBar(
                title = stringResource(R.string.settings_title),
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SettingsSection(title = stringResource(R.string.notifications)) {
                    SettingsSwitch(
                        title = stringResource(R.string.auto_dnd_title),
                        summary = stringResource(R.string.auto_dnd_summary),
                        checked = viewModel.autoDnd,
                        onCheckedChange = { viewModel.updateAutoDnd(it) },
                        icon = painterResource(R.drawable.materialsymbols_ic_do_not_disturb_on_rounded_filled)
                    )

                    SettingsSwitch(
                        title = stringResource(R.string.call_overlay_enabled_title),
                        summary = stringResource(R.string.call_overlay_enabled_summary),
                        checked = viewModel.callOverlayEnabled,
                        onCheckedChange = { viewModel.updateCallOverlay(it) },
                        icon = painterResource(R.drawable.materialsymbols_ic_call_rounded_filled)
                    )

                    SettingsSwitch(
                        title = stringResource(R.string.danmaku_notification_mode_title),
                        summary = stringResource(R.string.danmaku_notification_mode_summary),
                        checked = viewModel.danmakuNotification,
                        onCheckedChange = { viewModel.updateDanmakuNotification(it) },
                        icon = Icons.Filled.ChatBubble
                    )

                    SettingsDropdown(
                        title = stringResource(R.string.in_game_calls_title),
                        selectedValue = viewModel.callsMode.toString(),
                        options = callsModeOptions,
                        onValueChange = { viewModel.updateCallsMode(it.toIntOrNull() ?: 0) },
                        icon = painterResource(R.drawable.materialsymbols_ic_phone_in_talk_rounded_filled)
                    )

                    SettingsDropdown(
                        title = stringResource(R.string.ringer_mode_title),
                        selectedValue = viewModel.ringerMode.toString(),
                        options = ringerModeOptions,
                        onValueChange = { viewModel.updateRingerMode(it.toIntOrNull() ?: 3) },
                        icon = painterResource(R.drawable.materialsymbols_ic_volume_up_rounded_filled)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.display_gestures)) {
                    SettingsSwitch(
                        title = stringResource(R.string.auto_brightness_disabled_title),
                        summary = stringResource(R.string.auto_brightness_disabled_summary),
                        checked = viewModel.noAutoBrightness,
                        onCheckedChange = { viewModel.updateNoAutoBrightness(it) },
                        icon = painterResource(R.drawable.materialsymbols_ic_brightness_6_rounded_filled)
                    )

                    SettingsSwitch(
                        title = stringResource(R.string.three_screenshot_disabled_title),
                        summary = stringResource(R.string.three_screenshot_disabled_summary),
                        checked = viewModel.noThreeScreenshot,
                        onCheckedChange = { viewModel.updateNoThreeScreenshot(it) },
                        icon = painterResource(R.drawable.materialsymbols_ic_gesture_rounded_filled)
                    )

                    SettingsSwitch(
                        title = stringResource(R.string.stay_awake_title),
                        summary = stringResource(R.string.stay_awake_summary),
                        checked = viewModel.stayAwake,
                        onCheckedChange = { viewModel.updateStayAwake(it) },
                        icon = painterResource(R.drawable.materialsymbols_ic_visibility_rounded_filled)
                    )

                    SettingsSlider(
                        title = stringResource(R.string.icon_idle_alpha_title),
                        value = viewModel.iconIdleAlpha,
                        onValueChange = { viewModel.updateIconIdleAlpha(it) },
                        valueRange = 5f..100f,
                        valueLabel = "${viewModel.iconIdleAlpha.toInt()}%",
                        icon = painterResource(R.drawable.materialsymbols_ic_opacity_rounded_filled)
                    )

                    SettingsSlider(
                        title = stringResource(R.string.gamespace_menu_opacity_title),
                        value = viewModel.menuOpacity,
                        onValueChange = { viewModel.updateMenuOpacity(it) },
                        valueRange = 0f..100f,
                        valueLabel = "${viewModel.menuOpacity.toInt()}%",
                        icon = painterResource(R.drawable.materialsymbols_ic_opacity_rounded_filled)
                    )

                    SettingsSwitch(
                        title = stringResource(R.string.music_player_enabled_title),
                        summary = stringResource(R.string.music_player_enabled_summary),
                        checked = viewModel.musicPlayerEnabled,
                        onCheckedChange = { viewModel.updateMusicPlayerEnabled(it) },
                        icon = painterResource(R.drawable.materialsymbols_ic_volume_up_rounded_filled)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.crosshair_settings_title)) {
                    SettingsSwitch(
                        title = "Enable Crosshair Overlay",
                        summary = stringResource(R.string.crosshair_settings_summary),
                        checked = viewModel.crosshairEnabled,
                        onCheckedChange = { viewModel.updateCrosshairEnabled(it) },
                        icon = painterResource(R.drawable.materialsymbols_ic_adjust_rounded_filled)
                    )

                    if (viewModel.crosshairEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Crosshair Style",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val styles = listOf(
                                1 to R.drawable.crosshair_style_1,
                                2 to R.drawable.crosshair_style_2,
                                3 to R.drawable.crosshair_style_3,
                                4 to R.drawable.crosshair_style_4,
                                5 to R.drawable.crosshair_style_5,
                                6 to R.drawable.crosshair_style_6,
                                7 to R.drawable.crosshair_style_7,
                                8 to R.drawable.crosshair_style_8
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                styles.chunked(4).forEach { rowStyles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowStyles.forEach { (index, resId) ->
                                            val isSelected = viewModel.crosshairStyle == index
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .border(
                                                        width = 2.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        viewModel.updateCrosshairStyle(index)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(resId),
                                                    contentDescription = "Style $index",
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Crosshair Color",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val presetColors = listOf(
                                0xFF00FF00.toInt() to Color(0xFF00FF00), // Green
                                0xFFFF0000.toInt() to Color(0xFFFF0000), // Red
                                0xFF00FFFF.toInt() to Color(0xFF00FFFF), // Cyan
                                0xFFFFFF00.toInt() to Color(0xFFFFFF00), // Yellow
                                0xFFFFFFFF.toInt() to Color(0xFFFFFFFF), // White
                                0xFF2196F3.toInt() to Color(0xFF2196F3)  // Blue
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                presetColors.forEach { (colorVal, composeColor) ->
                                    val isSelected = viewModel.crosshairColor == colorVal
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(composeColor)
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.updateCrosshairColor(colorVal)
                                            }
                                    )
                                }
                            }
                        }

                        SettingsSlider(
                            title = "Size",
                            value = viewModel.crosshairSize.toFloat(),
                            onValueChange = { viewModel.updateCrosshairSize(it.roundToInt()) },
                            valueRange = 24f..64f,
                            valueLabel = "${viewModel.crosshairSize}dp",
                            icon = painterResource(R.drawable.ic_panel)
                        )

                        SettingsSlider(
                            title = "Opacity",
                            value = viewModel.crosshairOpacity,
                            onValueChange = { viewModel.updateCrosshairOpacity(it) },
                            valueRange = 0.2f..1f,
                            valueLabel = "${(viewModel.crosshairOpacity * 100).roundToInt()}%",
                            icon = painterResource(R.drawable.materialsymbols_ic_opacity_rounded_filled)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Position Tuning",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (viewModel.crosshairOffsetX != 0 || viewModel.crosshairOffsetY != 0) {
                                Text(
                                    text = "Reset Position",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        viewModel.updateCrosshairOffsets(0, 0)
                                    }
                                )
                            }
                        }

                        SettingsSlider(
                            title = "X Offset",
                            value = viewModel.crosshairOffsetX.toFloat(),
                            onValueChange = { viewModel.updateCrosshairOffsets(it.roundToInt(), viewModel.crosshairOffsetY) },
                            valueRange = -100f..100f,
                            valueLabel = "${if (viewModel.crosshairOffsetX > 0) "+" else ""}${viewModel.crosshairOffsetX}dp",
                            icon = painterResource(R.drawable.ic_drag)
                        )

                        SettingsSlider(
                            title = "Y Offset",
                            value = viewModel.crosshairOffsetY.toFloat(),
                            onValueChange = { viewModel.updateCrosshairOffsets(viewModel.crosshairOffsetX, it.roundToInt()) },
                            valueRange = -100f..100f,
                            valueLabel = "${if (viewModel.crosshairOffsetY > 0) "+" else ""}${viewModel.crosshairOffsetY}dp",
                            icon = painterResource(R.drawable.ic_drag)
                        )
                    }
                }
            }

            item {
                if (viewModel.isBypassSupported) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSection(title = "Power") {
                        SettingsSwitch(
                            title = stringResource(R.string.bypass_charge_enabled_title),
                            summary = stringResource(R.string.bypass_charge_enabled_summary),
                            checked = viewModel.bypassChargeEnabled,
                            onCheckedChange = { viewModel.updateBypassChargeEnabled(it) },
                            icon = Icons.Rounded.BatteryChargingFull
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun QuickStartAppsDialog(
    apps: List<AppInfo>,
    selectedApps: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val toggleButtonShapes = ToggleButtonShapes(
        shape = ToggleButtonDefaults.squareShape,
        pressedShape = ToggleButtonDefaults.pressedShape,
        checkedShape = ToggleButtonDefaults.roundShape,
    )

    val selected = remember { mutableStateListOf<String>().apply { addAll(selectedApps) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.quick_start_apps_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps) { app ->
                    val isSelected = selected.contains(app.packageName)
                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                selected.add(app.packageName)
                            } else {
                                selected.remove(app.packageName)
                            }
                        },
                        shapes = toggleButtonShapes,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (app.icon != null) {
                                Icon(
                                    painter = BitmapPainter(
                                        app.icon.toBitmap(48, 48).asImageBitmap()
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.Unspecified
                                )
                            }
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toSet()) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun loadInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    
    return pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        .mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val packageName = activityInfo.packageName
            try {
                val appInfo = pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
                AppInfo(
                    packageName = packageName,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
        .sortedBy { it.label.lowercase() }
}
