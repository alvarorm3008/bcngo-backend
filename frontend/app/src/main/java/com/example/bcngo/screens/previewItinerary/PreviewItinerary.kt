package com.example.bcngo.screens.previewItinerary

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.ProgressBar
import com.example.bcngo.model.Item
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.deleteItineraryApi
import com.example.bcngo.network.ApiService.updateAllItemsOfItineraryApi
import com.example.bcngo.ui.theme.Background
import com.example.bcngo.utils.AuthManager
import com.example.bcngo.screens.manualItinerary.SelectedPointsViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp

@Composable
fun PreviewItinerary(
    navController: NavController,
    selectedPointsViewModel: SelectedPointsViewModel,
    auth: AuthManager,
    previewItineraryViewModel: PreviewItineraryViewModel,
) {
    val itinerary = previewItineraryViewModel.itinerary.value
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    // Usamos un Box para mantener los botones en la parte inferior
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Espacio reservado para los botones
        ) {
            Cabezera(
                navController = navController,
                onExitAttempt = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.save_itinerary_first),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            ProgressBar(modifier = Modifier.padding(top = 16.dp), highlightedIndex = 3)

            Text(
                text = stringResource(id = R.string.itinerary_preview),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )

            // Comprobamos si el itinerario está disponible y mostramos su contenido
            itinerary?.let {
                Text(
                    text =  it.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Lista expandible de días y puntos de interés
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .padding(bottom = 80.dp)

                    ) {
                        items(itinerary.items) { day ->
                            ItineraryDayItem(
                                day = day,
                                dayIndex = itinerary.items.indexOf(day),
                                previewItineraryViewModel = previewItineraryViewModel
                            )
                        }
                    }
                }

            } ?: Text(
                text = stringResource(R.string.loading),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .systemBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f), // Ensures both buttons share space equally
                shape = RoundedCornerShape(25.dp),
            ) {
                Text(
                    text = stringResource(R.string.dont_save),
                    color = MaterialTheme.colorScheme.onSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            itinerary?.let { updatedItinerary ->
                                updateAllItemsOfItineraryApi(context, updatedItinerary)
                            }
                            navController.navigate(Routes.Home.route)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_al_guardar_el_itinerario),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary), // Ensure consistent red color
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f), // Ensures both buttons share space equally
                shape = RoundedCornerShape(25.dp),
            ) {
                Text(
                    text = stringResource(R.string.save),
                    color = Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }


    }

    // Diálogo de confirmación
    if (showDialog) {
        ConfirmDeleteDialog(
            onConfirm = {
                showDialog = false
                scope.launch {
                    try {
                        itinerary?.id?.let { deleteItinerary(context, it) }
                        Toast.makeText(
                            context,
                            context.getString(R.string.itinerario_eliminado_con_exito),
                            Toast.LENGTH_SHORT,
                        ).show()
                        navController.navigate(Routes.Home.route)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_al_eliminar_el_itinerario),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                navController.navigate(Routes.Home.route)
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
fun ItineraryDayItem(
    day: Item,
    dayIndex: Int, // Asegúrate de recibir el índice del día
    previewItineraryViewModel: PreviewItineraryViewModel, // Recibimos el ViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var points by remember { mutableStateOf(day.points.toMutableList()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF800000), shape = RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.day, day.day),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                    contentDescription = "Toggle Day",
                    tint = Color.White,
                )
            }
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE5E5E5))
                    .padding(8.dp),
            ) {
                points.forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp) // Padding around the row
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = point.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            color = Color.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                        IconButton(
                            onClick = {
                                previewItineraryViewModel.removePointFromDay(dayIndex, point.toString())
                                points = points.filter { it != point }.toMutableList()
                                Log.d("Removed Point", "Punto eliminado: $point")
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Point",
                                tint = Color.Red // Ensures the icon is red
                            )
                        }

                    }
                }


            }
        }
    }
}

suspend fun deleteItinerary(context: Context, idItinerary: Int) {
    try {
        deleteItineraryApi(context, idItinerary)
    } catch (e: Exception) {
        Log.e("DeleteItinerary", "Error al eliminar el itinerario: ${e.localizedMessage}")
        throw e // O maneja el error de manera adecuada
    }
}

@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.confirm_delete), fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = stringResource(R.string.delete_confirmation))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.yes), color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.no), color = MaterialTheme.colorScheme.secondary)
            }
        },
    )
}


