package com.example.bcngo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.SearchBar
import com.example.bcngo.model.Itinerary
import com.example.bcngo.network.ApiService
import kotlinx.coroutines.launch
import android.content.Intent
import android.content.Context
import com.example.bcngo.components.RedButton


@Composable
fun ListItineraries(
    navController: NavController,
) {
    val context = LocalContext.current
    var itineraries by remember { mutableStateOf<List<Itinerary>?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            itineraries = ApiService.getAllItineraries(context)
        }
    }

    // Estructura principal de la pantalla
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Incluimos la cabecera, que permanecerá estática
        Cabezera(navController)

        // Verifica si no hay itinerarios
        if (itineraries.isNullOrEmpty()) {
            // Mensaje de no itinerarios
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡Todavía no hay itinerarios! No olvides crear uno.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Botón rojo que lleva a la pantalla para crear itinerarios
                RedButton(
                    text = "Crear Itinerario",
                    onClick = { navController.navigate("calendar_itinerary_screen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                )
            }
        } else {
            // Muestra el buscador solo si hay itinerarios
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.padding(16.dp)
            )

            // Lista de itinerarios desplazable
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
            ) {
                items(itineraries?.filter { it.name.contains(searchQuery, ignoreCase = true) } ?: emptyList()) { itinerary ->
                    ItineraryItem(
                        itinerary = itinerary,
                        onDeleteClick = {
                            itineraryToDelete = itinerary
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    // Popup de confirmación
    if (showDialog && itineraryToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(id = R.string.confirm_delete)) },
            text = { Text(text = stringResource(id = R.string.delete_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            ApiService.deleteItineraryApi(context, itineraryToDelete!!.id)
                            itineraries = ApiService.getAllItineraries(context)
                        }
                        showDialog = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }
}


@Composable
fun ItineraryItem(
    itinerary: Itinerary,
    onDeleteClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) } // Dropdown toggle state
    val context = LocalContext.current // Access context for sharing
    var isEditing by remember { mutableStateOf(false) } // Edit Mode toggle
    var editableName by remember { mutableStateOf(itinerary.name) } // Editable name field

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Itinerary Name Button

            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = itinerary.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Action Buttons: Share, Delete
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Share Icon
                IconButton(
                    onClick = { shareItinerary(context, itinerary) }, // Trigger share function
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Itinerary",
                        tint = Color.Black
                    )
                }

                // Delete Icon
                IconButton(
                    onClick = onDeleteClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Itinerary",
                        tint = Color.Black
                    )
                }
            }
        }

        // Dropdown Content for Days and Points
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color(0xFFEFEFEF), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.itinerary_details),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Group items by day
                val groupedByDay = itinerary.items.groupBy { it.day }

                groupedByDay.forEach { (day, items) ->
                    Text(
                        text = stringResource(R.string.day, day),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    items.forEach { item ->
                        item.points.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color = Color(0xFFFAFAFA),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = point,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



/**
 * Shares an itinerary via the system share sheet.
 */
fun shareItinerary(context: Context, itinerary: Itinerary) {
    val shareText = buildShareText(itinerary)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Check out this itinerary: ${itinerary.name}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    val chooser = Intent.createChooser(intent, "Share Itinerary via")
    context.startActivity(chooser)
}

/**
 * Generates a formatted itinerary share text.
 */
fun buildShareText(itinerary: Itinerary): String {
    val builder = StringBuilder()
    builder.append("📍 ${itinerary.name}\n\n")

    itinerary.items.groupBy { it.day }.forEach { (day, items) ->
        builder.append("📅 Day $day:\n")
        items.forEach { item ->
            item.points.forEach { point ->
                builder.append("✅ $point\n")
            }
        }
        builder.append("\n")
    }

    builder.append("🌟 Don't miss out on this itinerary!\n")
    return builder.toString()
}

