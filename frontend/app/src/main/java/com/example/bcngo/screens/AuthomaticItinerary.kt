package com.example.bcngo.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.GreyButton
import com.example.bcngo.components.ProgressBar
import com.example.bcngo.components.RedButton
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.sendAutomaticItineraryToBackend
import com.example.bcngo.screens.calendarItinerary.InfoItineraryViewModel
import com.example.bcngo.screens.previewItinerary.PreviewItineraryViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate


@Composable
fun AuthomaticItinerary(
    navController: NavController,
    infoItineraryViewModel: InfoItineraryViewModel,
    previewItineraryViewModel: PreviewItineraryViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val name by infoItineraryViewModel.itineraryName.observeAsState("Unnamed")
    val startDate by infoItineraryViewModel.startDate.observeAsState()
    val endDate by infoItineraryViewModel.endDate.observeAsState()

    val nonNullStartDate = startDate ?: LocalDate(1970, 1, 1)
    val nonNullEndDate = endDate ?: LocalDate(1970, 1, 1)

    val categoriesMap = mapOf(
        R.string.architecture to "Arquitectura",
        R.string.parks to "Parques",
        R.string.art to "Arte",
        R.string.shopping to "Compras",
        R.string.leisure to "Ocio",
        R.string.museums to "Museos"
    )

    val localizedCategories = getLocalizedCategories(categoriesMap)

    val selectedCategories = remember { mutableStateOf(listOf<String>()) }
    var tipicidad by remember { mutableStateOf(1f) }
    val labels = listOf(
        stringResource(R.string.atypical),
        stringResource(R.string.typical),
        stringResource(R.string.very_typical),
    )

    var showDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
        ) {
            Cabezera(navController)
            ProgressBar(modifier = Modifier.padding(top = 16.dp), highlightedIndex = 2)

            Text(
                text = stringResource(id = R.string.select_categories),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
            )

            CategoriesList(
                localizedCategories.keys.toList(),
                selectedCategories
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.rarity_text),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.weight(1f) // Use weight to take available space
                    )
                    IconButton(
                        onClick = { showInfoDialog = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.info),
                            tint = Color.Gray,
                            )
                    }
                }
                if (showInfoDialog) {
                    AlertDialog(
                        onDismissRequest = { showInfoDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showInfoDialog = false }) {
                                Text(stringResource(R.string.ok))
                            }
                        },
                        title = { Text(stringResource(R.string.rarity_text)) },
                        text = { Text(stringResource(R.string.rarity_explanation)) }
                    )
                }
                Slider(
                    value = tipicidad,
                    onValueChange = { tipicidad = it },
                    valueRange = 0f..2f,
                    steps = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = labels[tipicidad.toInt()],
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GreyButton(
                onClick = {
                    navController.popBackStack()
                },
            )
            RedButton(
                text = stringResource(id = R.string.continue_button_text),
                onClick = {
                    if (selectedCategories.value.isNotEmpty()) {
                        showDialog = true
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.select_one_category), Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )
        }

        if (showDialog) {
            ConfirmationDialog(
                onConfirm = {
                    showDialog = false
                    scope.launch {
                        val selectedCategoriesInSpanish = selectedCategories.value.mapNotNull {
                            localizedCategories[it]
                        }
                        crearItinerarioAuto(
                            context, name, nonNullStartDate, nonNullEndDate,
                            selectedCategoriesInSpanish, tipicidad, navController,
                            previewItineraryViewModel
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


@Composable
fun CategoriesList(categories: List<String>, selectedCategories: MutableState<List<String>>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(categories) { category ->
            val isChecked = selectedCategories.value.contains(category)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        // Actualiza el estado de las categorías seleccionadas
                        selectedCategories.value = if (checked) {
                            selectedCategories.value + category
                        } else {
                            selectedCategories.value - category
                        }
                    },
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

suspend fun crearItinerarioAuto(
    context: Context,
    name: String,
    startDate: LocalDate,
    endDate: LocalDate,
    selectedCategoriesInSpanish: List<String>,
    tipicidad: Float,
    navController: NavController,
    previewItineraryViewModel: PreviewItineraryViewModel,
) {
    var rarity = "Typical"

    if (tipicidad == 0f) {
        rarity = "Atypical"
    } else if (tipicidad == 2f) {
        rarity = "Very typical"
    }

    val itineraryResponse = sendAutomaticItineraryToBackend(
        context,
        name,
        startDate,
        endDate,
        selectedCategoriesInSpanish,
        rarity
    )
    if (itineraryResponse != null) {
        previewItineraryViewModel.setItinerary(itineraryResponse)
        navController.navigate(Routes.PreviewItinerary.route)
    } else {
        Log.e("crearItinerarioManual", "Error creando el itinerario.")
    }
}

@Composable
fun getLocalizedCategories(categoriesMap: Map<Int, String>): Map<String, String> {
    return categoriesMap.mapKeys { (key, _) -> stringResource(key) }
}

