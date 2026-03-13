package com.abeja.gamecenterreplacer

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI(modifier: Modifier = Modifier, viewModel: ViewModel) {
    val context = LocalContext.current
    val mainSwitchStatus = remember {
        mutableStateOf(
            viewModel.checkifServiceisRunning(
                context,
                BackgroundService::class.java
            )
        )
    }
    val startOnBootSwitchStatus = remember { mutableStateOf(viewModel.getStartOnBoot(context)) }
    val showAppsDialog = remember { mutableStateOf(false) }
    val AdvancedOptionssheetState = rememberModalBottomSheetState()
    val showAdvancedOptionsBottomSheet = remember { mutableStateOf(false) }

    // TODO: Figure a better way to update the UI
    val isBatteryOptimizationIgnored by viewModel.isBatteryOptimizationIgnored.observeAsState(false)
    val isUsageStatsPermissionGranted by viewModel.isUsageStatsPermissionGranted.observeAsState(
        false
    )
    val batteryOptimizationDialog = remember { mutableStateOf(false) }
    val usageStatsPermissionDialog = remember { mutableStateOf(false) }
    val isOnTopPermissionGranted by viewModel.isOnTopPermissionGranted.observeAsState(false)
    val displayOverOtherAppsPermissionDialog = remember { mutableStateOf(false) }
    val isNotificationPermissionGranted by viewModel.isNotificationPermissionGranted.observeAsState(
        false
    )
    val notificationPermissionDialog = remember { mutableStateOf(false) }
    val appHasBeenChoosed by viewModel.appHasBeenChoosed.observeAsState(false)
    val appTargetName by viewModel.appTargetName.observeAsState("None")
    val reLaunchTargetApp = remember { mutableStateOf(viewModel.getRelaunchTargetApp(context)) }

    val requestNotificationLauncher: ActivityResultLauncher<String> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
                Log.d("MainUI", "Notification permission granted")
            } else {
                // Permission denied
                Log.d("MainUI", "Notification permission denied")
                viewModel.openNotificationPermissionSettings(context)
            }
        }
    if (batteryOptimizationDialog.value) {
        StandardAlertDialog(
            title = stringResource(R.string.battery_optimization_enabled_alert_title),
            text = stringResource(
                R.string.battery_optimization_enabled_alert_description,
                stringResource(R.string.app_name)
            ),
            onDismiss = { batteryOptimizationDialog.value = false },
            onConfirm = {
                batteryOptimizationDialog.value = false
                viewModel.requestIgnoreBatteryOptimization(context)
            },
            confirmText = stringResource(R.string.grant_dialog_button)
        )
    }
    if (usageStatsPermissionDialog.value) {
        StandardAlertDialog(
            title = stringResource(R.string.usage_access_permission_alert_title),
            text = stringResource(R.string.usage_access_permission_alert_description),
            onDismiss = { usageStatsPermissionDialog.value = false },
            onConfirm = {
                usageStatsPermissionDialog.value = false
                viewModel.requestUsageStatsPermission(context)
            },
            confirmText = stringResource(R.string.grant_dialog_button)
        )
    }
    if (displayOverOtherAppsPermissionDialog.value) {
        StandardAlertDialog(
            title = stringResource(R.string.display_over_other_apps_permission_alert_title),
            text = stringResource(R.string.display_over_other_apps_permission_alert_description),
            onDismiss = { displayOverOtherAppsPermissionDialog.value = false },
            onConfirm = {
                displayOverOtherAppsPermissionDialog.value = false
                viewModel.requestOnTopPermission(context)
            },
            confirmText = stringResource(R.string.grant_dialog_button)
        )
    }
    if (notificationPermissionDialog.value) {
        StandardAlertDialog(
            title = stringResource(R.string.notification_access_permission_alert_title),
            text = stringResource(R.string.notification_access_permission_alert_description),
            onDismiss = { notificationPermissionDialog.value = false },
            onConfirm = {
                notificationPermissionDialog.value = false
                requestNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            confirmText = stringResource(R.string.grant_dialog_button)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
    ) {

        item {
            /**
             * Permissions check
             */
            if (!isBatteryOptimizationIgnored) {
                StandardButton(
                    stringResource(R.string.battery_optimization_enabled),
                    ImageVector.vectorResource(id = R.drawable.warning_24px)
                ) { batteryOptimizationDialog.value = true }
            }
            if (!isUsageStatsPermissionGranted) {
                StandardButton(
                    stringResource(R.string.usage_access_permission_button_text),
                    ImageVector.vectorResource(id = R.drawable.warning_24px)
                ) { usageStatsPermissionDialog.value = true }
            }
            if (!isOnTopPermissionGranted) {
                StandardButton(
                    stringResource(R.string.display_over_other_apps_permission_button_text),
                    ImageVector.vectorResource(id = R.drawable.warning_24px)
                ) { displayOverOtherAppsPermissionDialog.value = true }
            }
            if (!isNotificationPermissionGranted) {
                StandardButton(
                    stringResource(R.string.nortification_permission_button_text),
                    ImageVector.vectorResource(id = R.drawable.warning_24px)
                ) { notificationPermissionDialog.value = true }
            }
        }

        item {
            /**
             * Main UI
             */
            StandardSwitch(
                stringResource(R.string.start_service),
                mainSwitchStatus.value,
                enabled = isUsageStatsPermissionGranted && isOnTopPermissionGranted && isNotificationPermissionGranted && appHasBeenChoosed
            ) {
                mainSwitchStatus.value = it
                viewModel.setServiceStatus(context, mainSwitchStatus.value)
            }
            StandardSwitch(
                stringResource(R.string.start_on_boot),
                startOnBootSwitchStatus.value,
                enabled = isUsageStatsPermissionGranted && isOnTopPermissionGranted && isNotificationPermissionGranted && appHasBeenChoosed
            )
            {
                startOnBootSwitchStatus.value = it
                viewModel.setStartOnBoot(context, startOnBootSwitchStatus.value)
            }

            StandardText(
                stringResource(R.string.selected_app, appTargetName),
            )
            StandardButton(
                stringResource(R.string.choose_an_app),
                ImageVector.vectorResource(id = R.drawable.list_24px)
            ) {
                showAppsDialog.value = true
            }

            StandardButton(
                stringResource(R.string.advanced_options),
                ImageVector.vectorResource(id = R.drawable.build_24px)
            ) {
                showAdvancedOptionsBottomSheet.value = true
            }
        }

        item {
            if (showAdvancedOptionsBottomSheet.value) {
                ModalBottomSheet(
                    onDismissRequest = { showAdvancedOptionsBottomSheet.value = false },
                    sheetState = AdvancedOptionssheetState
                ) {
                    StandardText(
                        stringResource(R.string.advanced_options)
                    )
                    StandardSwitch(
                        stringResource(R.string.relaunch_target_app_option),
                        reLaunchTargetApp.value
                    ) {
                        reLaunchTargetApp.value = it
                        viewModel.setRelaunchTargetApp(context, it)
                    }
                }
            }
            /**
             * App list dialog
             */
            if (showAppsDialog.value) {
                AppListDialog(context, viewModel, { showAppsDialog.value = false }) {
                    viewModel.setApptarget(
                        it.activityInfo.packageName,
                        it.loadLabel(context.packageManager).toString(),
                        context
                    )
                    showAppsDialog.value = false
                }
            }


        }
    }
}

