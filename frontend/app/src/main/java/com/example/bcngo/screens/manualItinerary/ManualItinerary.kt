package com.example.bcngo.screens.manualItinerary

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.GreyButton
import com.example.bcngo.components.ProgressBar
import com.example.bcngo.components.RedButton
import com.example.bcngo.components.SearchBar
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.sendItineraryToBackend
import com.example.bcngo.network.InterestPointsListViewModel
import com.example.bcngo.screens.calendarItinerary.InfoItineraryViewModel
import com.example.bcngo.screens.previewItinerary.PreviewItineraryViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun ManualItinerary(
    navController: NavController,
    selectedPointsViewModel: SelectedPointsViewModel = viewModel(),
    infoItineraryViewModel: InfoItineraryViewModel,
    previewItineraryViewModel: PreviewItineraryViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val searchState = remember { mutableStateOf("") }
    val interestPoints by InterestPointsListViewModel()

    val name by infoItineraryViewModel.itineraryName.observeAsState("Unnamed")
    val startDate by infoItineraryViewModel.startDate.observeAsState()
    val endDate by infoItineraryViewModel.endDate.observeAsState()

    val nonNullStartDate = startDate ?: LocalDate(1970, 1, 1)
    val nonNullEndDate = endDate ?: LocalDate(1970, 1, 1)

    val filteredPointsByCategory =
        interestPoints
            ?.filter { it.name.contains(searchState.value, ignoreCase = true) }
            ?.groupBy { it.category }
            ?: emptyMap()

    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Cabezera(navController)
            ProgressBar(modifier = Modifier.padding(top = 16.dp), highlightedIndex = 2)
            SearchBar(
                value = searchState.value,
                onValueChange = { searchState.value = it },
                modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            )

            InterestPointsScreenByCategory(filteredPointsByCategory, selectedPointsViewModel)
        }

        Row(
            modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GreyButton(onClick = { navController.popBackStack() })
            RedButton(
                onClick = {
                    if (selectedPointsViewModel.selectedPoints.isNotEmpty()) {
                        showDialog = true
                    }
                    else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.select_one_point), Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                text = stringResource(id = R.string.continue_button_text),
            )
        }

        if (showDialog) {
            ConfirmationDialog(
                onConfirm = {
                    showDialog = false
                    scope.launch {
                        crearItinerarioManual(
                            context,
                            name,
                            nonNullStartDate,
                            nonNullEndDate,
                            selectedPointsViewModel.selectedPoints,
                            navController,
                            previewItineraryViewModel,
                            selectedPointsViewModel,
                        )
                    }
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun ConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.confirmation_title)) },
        text = { Text(stringResource(R.string.confirmation_message)) }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InterestPointsScreenByCategory(
    pointsByCategory: Map<String, List<InterestPoint>>,
    selectedPointsViewModel: SelectedPointsViewModel,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        pointsByCategory.toSortedMap().forEach { (category, points) ->
            stickyHeader {
                // Encabezado de categoría
                Text(
                    text = category,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.background),
                )
            }
            items(points.sortedBy { it.name }) { point ->
                val isChecked = selectedPointsViewModel.selectedPoints.contains(point.id)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .clickable {
                            if (isChecked) {
                                selectedPointsViewModel.removePoint(point.id)
                            } else {
                                selectedPointsViewModel.addPoint(point.id)
                            }
                        },
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                selectedPointsViewModel.addPoint(point.id)
                            } else {
                                selectedPointsViewModel.removePoint(point.id)
                            }
                        },
                    )
                    Text(
                        text = point.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

suspend fun crearItinerarioManual(
    context: Context,
    name: String,
    startDate: LocalDate,
    endDate: LocalDate,
    selectedPoints: List<Int>,
    navController: NavController,
    previewItineraryViewModel: PreviewItineraryViewModel,
    selectedPointsViewModel: SelectedPointsViewModel,
) {
    val itineraryResponse =
        sendItineraryToBackend(context, name, startDate, endDate, selectedPoints)

    if (itineraryResponse != null) {
        previewItineraryViewModel.setItinerary(itineraryResponse)
        selectedPointsViewModel.clearPoints()
        navController.navigate(Routes.PreviewItinerary.route)
    } else {
        Log.e("crearItinerarioManual", "Error creando el itinerario.")
    }
}
