package com.example.bcngo.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.CabezeraAdmin
import com.example.bcngo.components.SearchBar
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.network.ApiService
import com.example.bcngo.network.InterestPointsListViewModel
import com.example.bcngo.ui.theme.PrincipalRed
import com.example.bcngo.utils.AuthManager
import com.example.bcngo.screens.searchpoints.BuscadorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ListPointsAdmin(
    modifier: Modifier = Modifier,
    buscadorViewModel: BuscadorViewModel = viewModel(),
    navController: NavController,
    auth: AuthManager,
) {
    val searchState = buscadorViewModel.search.observeAsState("")
    val search = searchState.value

    val interestPoints by InterestPointsListViewModel()

    // Filtrar y ordenar los puntos de interés según la búsqueda
    val filteredPoints = interestPoints?.filter {
        it.name.contains(search, ignoreCase = true)
    }?.sortedBy { it.name } ?: emptyList() // Ordenar alfabéticamente

    val context = LocalContext.current

    // Layout principal
    Column(modifier = Modifier.fillMaxSize()) {
        CabezeraAdmin(navController)

        // Botón de crear nuevo punto
        Button(
            onClick = { navController.navigate("create_interest_point_screen") },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrincipalRed,
            ), // Color rojo principal
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(text = stringResource(R.string.create_new_point), color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Barra de búsqueda
        SearchBar(
            value = search,
            onValueChange = { buscadorViewModel.onSearchChange(it) },
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        )

        // Lista de puntos de interés con acciones
        InterestPointsAdminScreen(
            filteredPoints = filteredPoints,
            onModifyClick = { point -> navController.navigate("edit_point/${point.id}") },
            onDeleteClick = { point ->
                CoroutineScope(Dispatchers.IO).launch {
                    val result = ApiService.deletePointOfInterest(point.id, context)
                    withContext(Dispatchers.Main) {
                        if (result) {
                            navController.navigate("list_points_admin_screen")
                        } else {
                            navController.navigate("list_points_admin_screen")
                        }
                    }
                }
            },
        )
    }
}

@Composable
fun InterestPointsAdminScreen(
    filteredPoints: List<InterestPoint>,
    onModifyClick: (InterestPoint) -> Unit,
    onDeleteClick: (InterestPoint) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (filteredPoints.isNotEmpty()) {
            items(filteredPoints) { point ->
                InfoIntPointAdmin(
                    point = point,
                    onModifyClick = { onModifyClick(point) },
                    onDeleteClick = { onDeleteClick(point) },
                )
            }
        } else {
            item {
                Text(
                    text = stringResource(id = R.string.no_interest_points),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}