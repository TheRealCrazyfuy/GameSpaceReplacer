package com.abeja.gamecenterreplacer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MainUI(modifier: Modifier = Modifier, viewModel: ViewModel) {
    val context = LocalContext.current
    val mainSwitchStatus = remember { mutableStateOf(viewModel.checkifServiceisRunning(context, BackgroundService::class.java)) }

    Column(modifier = modifier.padding(top = 56.dp).fillMaxWidth() ) {
        //StandardText("Turn on the switch below and we'll open the app you want instead of the Game Center.")
        if (!viewModel.isUsageStatsPermissionGranted(context)) {
            StandardText("Usage access is required")
            StandardButton("Allow usage access", Icons.Default.Warning) { viewModel.requestUsageStatsPermission(context) }
        }
        if (!viewModel.isOnTopPermissionGranted(context)) {
            StandardText("On top permission is required")
            StandardButton("Allow on top permission", Icons.Default.Warning) { viewModel.requestOnTopPermission(context) }
        }

        StandardSwitch("Replace Game Space", mainSwitchStatus.value, enabled = viewModel.isUsageStatsPermissionGranted(context) && viewModel.isOnTopPermissionGranted(context)) {
            mainSwitchStatus.value = it
            viewModel.setServiceStatus(context, mainSwitchStatus.value)
        }

        StandardText("Currently selected app: ${viewModel.getServiceData().appTargetName}")

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
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