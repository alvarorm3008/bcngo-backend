package com.example.bcngo.screens.searchEvents

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.GreyButton
import com.example.bcngo.model.Event
import com.example.bcngo.network.ApiService.getEventById
import com.example.bcngo.network.ApiService.doFavouriteEvent
import com.example.bcngo.network.ApiService.doUnfavouriteEvent
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun InfoEvent(navController: NavController, eventId: Int) {
    val context = LocalContext.current
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    var isFavorite by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        scope.launch {
            event = getEventById(context, eventId)
            isLoading = false
            isFavorite = event?.is_favorite ?: false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Cabezera(navController)

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                event?.let {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Text(
                                text = stringResource(
                                    R.string.start_date,
                                    LocalDateTime.parse(it.start_date.toString()).format(formatter),

                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            Text(
                                text = stringResource(
                                    R.string.end_date,
                                    LocalDateTime.parse(it.end_date.toString()).format(formatter),
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            val location = buildString {
                                if (it.road_name.isNullOrEmpty() && it.street_number.isNullOrEmpty()) {
                                    append("-")
                                } else {
                                    append(it.road_name)
                                    if (!it.road_name.isNullOrEmpty() && !it.street_number.isNullOrEmpty()) {
                                        append(", ")
                                    }
                                    append(it.street_number)
                                }
                            }
                            Text(
                                text = stringResource(R.string.location, location),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            val district = it.district.ifEmpty { "-" }
                            Text(
                                text = stringResource(R.string.district, district),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                if (!isFavorite) {
                                                    doFavouriteEvent(context, it.id)
                                                    Log.d("InfoEvent", "Evento marcado como favorito")
                                                    isFavorite = true
                                                } else {
                                                    doUnfavouriteEvent(context, it.id)
                                                    Log.d("InfoEvent", "Evento desmarcado como favorito")
                                                    isFavorite = false
                                                }
                                            } catch (e: Exception) {
                                                Log.e("InfoEvent", "Error al cambiar estado favorito: ${e.localizedMessage}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = stringResource(R.string.favourite)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        shareEvent(context, event!!)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = stringResource(R.string.share)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        showDialog = true
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.google_calendar_icon),
                                        contentDescription = stringResource(R.string.add_to_calendar),
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp) // Adjust the size as needed
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.event_not_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
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
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.add_to_calendar)) },
            text = { Text(stringResource(R.string.add_to_calendar_question)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        addToCalendar(context, event!!)
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            },
        )
    }
}

@Preview
@Composable
fun InfoEventPreview() {
    val context = LocalContext.current
    InfoEvent(navController = NavController(context), eventId = 12)
}