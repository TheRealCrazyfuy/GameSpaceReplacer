package com.abeja.gamecenterreplacer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.abeja.gamecenterreplacer.ui.theme.GamecenterreplacerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.onLaunch(this)
        setContent {
            GamecenterreplacerTheme {
                var selectedNavigationItemIndex by rememberSaveable { mutableIntStateOf(0) }
                val navigationBarItems = listOf("Home", "About")
                val unselectedNavigationBarItemIcons = listOf(
                    R.drawable.home_24px,
                    R.drawable.info_24px
                )
                val selectedNavigationBarItemIcons = listOf(
                    R.drawable.home_filled_24px,
                    R.drawable.info_filled_24px
                )
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text(stringResource(R.string.app_name))
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            navigationBarItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(
                                                if (selectedNavigationItemIndex == index)
                                                    selectedNavigationBarItemIcons[index]
                                                else
                                                    unselectedNavigationBarItemIcons[index]
                                            ),
                                            contentDescription = item
                                        )
                                    },
                                    label = { Text(item) },
                                    selected = selectedNavigationItemIndex == index,
                                    onClick = { selectedNavigationItemIndex = index },
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (selectedNavigationItemIndex) {
                        0 -> MainUI(modifier = Modifier.padding(innerPadding), viewModel)
                        1 -> AboutScreen(modifier = Modifier.padding(innerPadding), viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onLaunch(this)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GamecenterreplacerTheme {
        Greeting("Android")
    }
}