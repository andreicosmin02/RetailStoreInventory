package com.example.retailstoreinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.retailstoreinventory.ui.theme.RetailStoreInventoryTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            RetailStoreInventoryTheme(darkTheme = true) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val local_focus_manager = LocalFocusManager.current

    Scaffold(
        content = { innerPadding ->
            Box(
                modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    local_focus_manager.clearFocus()
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val search_bar_state = rememberTextFieldState()

                        OutlinedTextField(
                            state = search_bar_state,
                            label = { Text("Search") },
                        )

                        Button(
                            onClick = {
                                local_focus_manager.clearFocus()

                                //
                            }) { Text("Sort") }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                    ) {
                        // aici ramane de facut lista cu produsele
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier
                                .align(Alignment.Center),
                            onClick = {
                                local_focus_manager.clearFocus()

                                //
                            },
                        ) { Text("Scan") }
                    }
                }
            }
        }
    )
}