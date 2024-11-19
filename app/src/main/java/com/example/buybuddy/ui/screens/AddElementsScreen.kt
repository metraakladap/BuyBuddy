package com.example.buybuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavHostController
import com.example.buybuddy.DestinationScreen
import com.example.buybuddy.viewmodels.AddElementsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddElementsScreen(
    viewModel: AddElementsViewModel,
    navController: NavHostController
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("BuyBuddy")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        content = { _ ->


            Column(

                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                    
                )
                {
                    Column {
                        DropDownMenuBox(
                            viewModel = viewModel,
                            expanded = state.isCategoryDropDownExpanded,
                            onExpandedChange = { viewModel.toggleCategoryDropDown() },
                            selectedValue = state.selectedCategory,
                            items = state.categories,
                            onItemSelected = {
                                viewModel.updateSelectedCategory(it)
                                viewModel.categoryValidation(it)
                            },
                            label = "Categories",
                            onValueChange = {
                                viewModel.updateSelectedCategory(it)
                                viewModel.categoryValidation(it)
                            },
                            isError = false

                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        DropDownMenuBox(
                            viewModel = viewModel,
                            expanded = state.isProductDropDownExpanded,
                            onExpandedChange = { viewModel.toggleProductDropDown() },
                            selectedValue = state.selectedProduct,
                            items = viewModel.getProductByCategory(state.selectedCategory),
                            onItemSelected = { viewModel.updateSelectedProduct(it) },
                            label = "Products",
                            onValueChange = {
                                viewModel.updateSelectedProduct(it)
                            },
                            isError = state.isCategoryValid
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.onAddListElementClick()
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Text("Add New List")
                    }
                }
            }


        }
    )

}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun DropDownMenuBox(
    viewModel: AddElementsViewModel,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedValue: String?,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    label: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var filteredItems by remember { mutableStateOf(items) }

    // Створюємо StateFlow для дебаунсу пошукового запиту
    val searchQuery = remember { MutableStateFlow(selectedValue ?: "") }

    // Ефект для обробки дебаунсу
    LaunchedEffect(items) {
        searchQuery
            .debounce(300) // 5 секунд для тестування
            .collect { query ->
                filteredItems = if (query.isEmpty()) {
                    items
                } else {
                    items.filter { it.contains(query, ignoreCase = true) }
                }
            }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedValue ?: "",
            onValueChange = { newValue ->
                searchQuery.value = newValue
                onValueChange(newValue)
                if (!expanded) {
                    onExpandedChange(true)
                }


            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            isError = isError,
            readOnly = isError
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        onExpandedChange(false)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}