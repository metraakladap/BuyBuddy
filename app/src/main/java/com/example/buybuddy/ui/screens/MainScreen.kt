package com.example.buybuddy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.buybuddy.DestinationScreen
import com.example.buybuddy.responses.UserData
import com.example.buybuddy.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userData: UserData?,
    viewModel: MainViewModel,
    navController: NavController
) {
    val userLists by viewModel.userLists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var listIdToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("BuyBuddy")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(DestinationScreen.Profile.route) }) {
                        Icon(Icons.Filled.AccountBox, contentDescription = "Localized description")
                    }
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userLists.size) { i ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable(onClick = {
                                navController.navigate(
                                    DestinationScreen.ListElements.createRoute(
                                        userLists[i].id
                                    )
                                )
                            }),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(userLists[i].name, style = MaterialTheme.typography.titleLarge)
                            }
                            OutlinedIconButton(onClick = {
                                listIdToDelete = userLists[i].id
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete List")
                            }
                        }
                    }

                    if (i == userLists.lastIndex) {
                        Spacer(modifier = Modifier.padding(12.dp))

                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(DestinationScreen.AddList.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete list?") },
            text = { Text("Are you sure you want to delete this list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        listIdToDelete?.let { viewModel.deleteUserList(it) }
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
