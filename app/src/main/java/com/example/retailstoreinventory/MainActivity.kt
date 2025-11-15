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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

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

    val my_items = listOf(
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango",
        "Apple",
        "Banana",
        "Cherry",
        "Orange",
        "Mango"
    )

    val search_bar_state = rememberTextFieldState()

    val filtered_my_items_state = remember { mutableStateOf(my_items) }

    LaunchedEffect(search_bar_state.text) {
        filtered_my_items_state.value = if (search_bar_state.text.isBlank()) my_items
        else my_items.filter {
            it.contains(search_bar_state.text, ignoreCase = true)
        }
    }

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
                        OutlinedTextField(
                            state = search_bar_state,
                            label = { Text("Search") },
                        )

                        Button(
                            onClick = {
                                local_focus_manager.clearFocus()

                                filtered_my_items_state.value = filtered_my_items_state.value.sorted()
                            }) { Text("Sort") }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(filtered_my_items_state.value) { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Button(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .width(280.dp) ,
                                        shape = RectangleShape,
                                            onClick = {
                                            local_focus_manager.clearFocus()

                                            //
                                        },
                                    ) { Text(item) }
                                }
                            }
                        }
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