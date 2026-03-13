package com.abeja.gamecenterreplacer

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(enabled) { onCheckedChange(!checked) },
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
            painter = painterResource(id = R.drawable.github_mark),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .padding(end = 16.dp)
                .clickable(onClick = onClickGitHub)
        )
        Icon(
            painter = painterResource(id = R.drawable.discord_mark),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .padding(end = 16.dp)
                .clickable(onClick = onClickDiscord)
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
    val packageManager = context.packageManager
    val launchableApps = remember {
        viewModel.getLaunchableApps(context)
            .sortedBy { it.loadLabel(packageManager).toString() }
    }
    val searchText = remember { mutableStateOf("") }
    val searchResults = remember(searchText.value) {
        if (searchText.value.isEmpty()) {
            launchableApps
        } else {
            launchableApps.filter {
                it.loadLabel(packageManager).toString()
                    .contains(searchText.value, ignoreCase = true) ||
                        it.activityInfo.packageName
                            .contains(searchText.value, ignoreCase = true)
            }
        }
    }

    val appInfoCache = remember {
        mutableMapOf<String, Pair<String, Bitmap>>() // packageName to (appName, appIcon)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.select_an_app))
        },
        text = {
            Column {
                TextField(
                    value = searchText.value,
                    onValueChange = { searchText.value = it },
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true
                )

                LazyColumn {
                    items(searchResults) { appInfo ->
                        val packageName = appInfo.activityInfo.packageName
                        val appName = appInfoCache[packageName]?.first
                            ?: appInfo.loadLabel(packageManager).toString()
                        val appIcon = appInfoCache[packageName]?.second
                            ?: appInfo.loadIcon(packageManager).toBitmap().also {
                                appInfoCache[packageName] = appName to it
                            }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(appInfo) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = appIcon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = appName)
                                Text(text = packageName)
                            }
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

/**
 * Standard AlertDialog
 */
@Composable
fun StandardAlertDialog(
    title: String,
    text: String,
    dismissText: String = "Cancel",
    confirmText: String = "OK",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit = onDismiss
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        }
    )
}