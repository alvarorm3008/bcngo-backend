package com.example.bcngo.screens.searchpoints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.SearchBar
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.network.InterestPointsListViewModel

@Composable
fun ListIntPoints(
    modifier: Modifier = Modifier,
    buscadorViewModel: BuscadorViewModel = viewModel(),
    navController: NavController,
) {

    val searchState = buscadorViewModel.search.observeAsState("")
    val search = searchState.value
    val interestPoints by InterestPointsListViewModel()

    // Las categorías en español, como están en el backend
    val categories = listOf(
        "Todos", "Arquitectura", "Parques", "Arte", "Compras", "Museos", "OcioNocturno"
    )

    // Este mapa se usará solo para mostrar las categorías traducidas en la UI
    val categoryMap = mapOf(
        "Todos" to stringResource(R.string.category_all),
        "Arquitectura" to stringResource(R.string.architecture),
        "Parques" to stringResource(R.string.parks),
        "Arte" to stringResource(R.string.art),
        "Compras" to stringResource(R.string.shopping),
        "Museos" to stringResource(R.string.museums),
        "Ocio" to stringResource(R.string.leisure),
        "OcioNocturno" to stringResource(R.string.nightlife)
    )

    var selectedCategory by remember { mutableStateOf("Todos") }

    // Filtrar los puntos de interés en base al nombre y la categoría (usando las categorías en español del backend)
    val filteredPoints = interestPoints?.filter { point ->
        val categoryMatches = selectedCategory == "Todos" || point.category.equals(selectedCategory, ignoreCase = true)
        point.name.contains(search, ignoreCase = true) && categoryMatches
    }?.sortedBy { it.name } ?: emptyList()

    Column {
        Cabezera(navController)

        // Barra de búsqueda y filtro de categorías
        Row(modifier = Modifier.padding(8.dp)) {
            // Barra de búsqueda
            SearchBar(
                value = search,
                onValueChange = { buscadorViewModel.onSearchChange(it) },
                modifier = modifier
                    .padding(8.dp)
                    .weight(1f)
            )

            // Filtro de categorías con dropdown
            CategoryFilterDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category },
                categoryMap = categoryMap // Pasar el mapa de traducciones
            )
        }

        // Mostrar los puntos de interés
        InterestPointsScreen(filteredPoints, modifier, navController)
    }
}

@Composable
fun CategoryFilterDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categoryMap: Map<String, String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize()) {
        // Botón del dropdown con tamaño ajustado al contenido
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .wrapContentWidth() // Ajusta el ancho al contenido
                .height(56.dp) // Mantener altura constante
                .padding(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        ) {
            // Establecer un tamaño constante para el texto
            Text(
                text = stringResource(R.string.filter) + " " + (categoryMap[selectedCategory] ?: selectedCategory),  // Mostrar "Filter:" + la traducción
                style = MaterialTheme.typography.bodyMedium, // Estilo fijo para el texto
                maxLines = 1, // Asegurar que el texto no ocupe más de una línea
                overflow = TextOverflow.Ellipsis // Si el texto es muy largo, usar "..."
            )
        }

        // Menú desplegable
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        // Mostrar la traducción en el menú desplegable
                        Text(
                            text = categoryMap[category] ?: category,  // Traducir categoría
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (category == selectedCategory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun InterestPointsScreen(filteredPoints: List<InterestPoint>, modifier: Modifier, navController: NavController) {
    val interestPoints by InterestPointsListViewModel()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (interestPoints == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (filteredPoints.isNotEmpty()) {
            items(filteredPoints) { point ->
                InfoIntPoint(
                    point,
                    modifier,
                    navController
                )
            }
        } else {
            item {
                Text(
                    stringResource(id = R.string.no_interest_points_found),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
