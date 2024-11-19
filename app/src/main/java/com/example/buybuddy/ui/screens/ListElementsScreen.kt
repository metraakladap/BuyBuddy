package com.example.buybuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.buybuddy.DestinationScreen
import com.example.buybuddy.viewmodels.ListElementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListElementsScreen(viewModel: ListElementsViewModel, navController: NavController) {

    val userList by viewModel.userList.collectAsState()
    val elementsList by viewModel.listElement.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var listIdToDelete by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    userList?.let { Text(it.name) }
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
                elementsList?.let {
                    items(it.size) { i ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedIconButton(onClick = {
                                    viewModel.completeElement(elementsList!![i].id)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = if (elementsList!![i].completed) "Completed" else "Incomplete",
                                        tint = if (elementsList!![i].completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)
                                    )
                                }
                                Column(
                                    modifier = Modifier,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    elementsList!![i].category?.let { category ->
                                        Text(
                                            category,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Text(
                                        elementsList!![i].name,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                OutlinedIconButton(onClick = {
                                    listIdToDelete = elementsList!![i].id
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete List")
                                }
                            }
                        }



                        if (i == elementsList!!.lastIndex) {
                            Spacer(modifier = Modifier.padding(12.dp))

                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(
                    DestinationScreen.AddElement.createRoute(
                        viewModel.getListId()
                    )
                )
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete item?") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        listIdToDelete?.let { viewModel.deleteUserElement(it) }
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