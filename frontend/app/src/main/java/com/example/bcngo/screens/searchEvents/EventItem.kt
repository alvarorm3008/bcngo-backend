package com.example.bcngo.screens.searchEvents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.model.Event
import com.example.bcngo.ui.theme.PrincipalRed

@Composable
fun EventItem(event: Event, navController: NavController, viewModel: SearchEventsViewModel, removeFromList: Boolean) {
    val context = LocalContext.current
    val isFavorite by viewModel.favoriteStates.collectAsState(initial = emptyMap())
    var showDialog by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate("infoEvent/${event.id}")
            }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.start), color = Color.Gray)
            Text(event.start_date.dayOfMonth.toString())
            Text(event.start_date.month.name.take(3))
            Text(event.start_date.year.toString())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(event.name, style = MaterialTheme.typography.titleLarge)

            event.road_name?.let { roadName ->
                Text(
                    text = if (event.street_number.isNullOrEmpty()) roadName else "$roadName, ${event.street_number}",
                    color = Color.Gray
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(event.id, context, removeFromList)
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite[event.id] == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(R.string.favourite),
                    )
                }

                IconButton(
                    onClick = {
                        shareEvent(context, event)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = stringResource(R.string.share),
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.add_to_calendar)) },
            text = { Text(stringResource(R.string.add_to_calendar_question)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        addToCalendar(context, event)
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