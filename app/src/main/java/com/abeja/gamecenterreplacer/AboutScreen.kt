package com.abeja.gamecenterreplacer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(modifier: Modifier = Modifier, viewModel: ViewModel) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
    ) {
        item {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "App Icon",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .size(128.dp)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            StandardText(
                stringResource(
                    R.string.version_number,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.BUILD_TYPE
                )
            )

            StandardLinkIcon(
                onClickGitHub = {
                    viewModel.openGitHubRepository(
                        context,
                        "https://www.github.com/therealcrazyfuy/GameSpaceReplacer"
                    )
                },
                onClickDiscord = {
                    viewModel.openGitHubRepository(
                        context,
                        "https://discord.gg/Hc4UPXqc4j"
                    )
                }
            )
        }
    }
}
