package com.abeja.gamecenterreplacer

import android.content.Context
import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

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
    val showAppsDialog = remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier
        .padding(top = 56.dp)
        .fillMaxWidth()) {

        item {
            /**
             * Permissions check
             */
            if (!viewModel.isUsageStatsPermissionGranted(context)) {
                StandardText("Usage access is required")
                StandardButton(
                    "Allow usage access",
                    Icons.Default.Warning
                ) { viewModel.requestUsageStatsPermission(context) }
            }
            if (!viewModel.isOnTopPermissionGranted(context)) {
                StandardText("On top permission is required")
                StandardButton(
                    "Allow on top permission",
                    Icons.Default.Warning
                ) { viewModel.requestOnTopPermission(context) }
            }
        }

        item {
            /**
             * Main UI
             */
            StandardText("Turn on the switch below and we'll open the app you want instead of the Game Center.")
            StandardSwitch(
                "Replace Game Space",
                mainSwitchStatus.value,
                enabled = viewModel.isUsageStatsPermissionGranted(context) && viewModel.isOnTopPermissionGranted(
                    context
                ) && serviceData.appTargetPackage != null
            ) {
                mainSwitchStatus.value = it
                viewModel.setServiceStatus(context, mainSwitchStatus.value)
            }

            StandardText(
                "Choosen app: ${
                    if (viewModel.getServiceData().appTargetName != null) {
                        viewModel.getServiceData().appTargetName
                    } else {
                        "None"
                    }
                }"
            )
            StandardButton("Choose a different app ", Icons.AutoMirrored.Filled.List) {
                showAppsDialog.value = true
            }
        }

        item {
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

            StandardText("Version ${BuildConfig.VERSION_NAME}")

            StandardLinkIcon(
                onClickGitHub = { viewModel.openGitHubRepository(context, "https://www.github.com/therealcrazyfuy/GameSpaceReplacer") },
                onClickDiscord = { viewModel.openGitHubRepository(context, "https://discord.gg/Hc4UPXqc4j") }
            )
        }
    }
}

@Composable
fun StandardText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun StandardSwitch(
    text: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = {
                onCheckedChange(it)
            }
        )
    }
}

@Composable
fun StandardButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun StandardLinkIcon(
    onClickGitHub: () -> Unit,
    onClickDiscord: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id= R.drawable.github_mark),
            contentDescription = null,
            modifier = Modifier.size(42.dp).padding(end = 8.dp).clickable(onClick = onClickGitHub)
        )
        Icon(
            painter = painterResource(id= R.drawable.discord_mark),
            contentDescription = null,
            modifier = Modifier.size(42.dp).padding(end = 8.dp).clickable(onClick = onClickDiscord)
        )

    }
}

/**
 * Dialog to choose a different app
 */
@Composable
fun AppListDialog(
    context: Context,
    viewModel: ViewModel,
    onDismiss: () -> Unit,
    onAppSelected: (ResolveInfo) -> Unit
) {
    val launchableApps = remember {
        viewModel.getLaunchableApps(context)
            .sortedBy { it.loadLabel(context.packageManager).toString() }
    }
    val packageManager = context.packageManager

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Choose an app to launch")
        },
        text = {
            LazyColumn {
                items(launchableApps) { appInfo ->
                    val appName = appInfo.loadLabel(packageManager).toString()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(appInfo) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = appInfo.loadIcon(packageManager).toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = appName)
                            Text(text = appInfo.activityInfo.packageName)
                        }
                    }
                }
            }

        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
