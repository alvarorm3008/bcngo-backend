package com.example.bcngo.screens.searchEvents

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.SearchBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.bcngo.navigation.Routes

@Composable
fun SearchEvents(
    navController: NavController,
    searchEventsViewModel: SearchEventsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val events by searchEventsViewModel.events.observeAsState(emptyList())
    val searchQuery by searchEventsViewModel.searchQuery.observeAsState("")
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedMode by searchEventsViewModel.selectedMode.observeAsState(EventFilterMode.All)
    val isLoading by searchEventsViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        searchEventsViewModel.fetchEvents(context)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Cabezera(navController)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.events),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f)
            )
            Button(
                onClick = {
                    navController.navigate(Routes.FavouriteEvents.route)
                },
            ) {
                Text(stringResource(R.string.my_favourites))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SearchBar(
                value = searchQuery,
                onValueChange = { newSearch -> searchEventsViewModel.onSearchQueryChange(newSearch) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    searchEventsViewModel.onModeSelected(EventFilterMode.All, context)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == EventFilterMode.All) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) { Text(stringResource(R.string.All),
                color = if (selectedMode == EventFilterMode.All) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                ) }

            Button(
                onClick = {
                    searchEventsViewModel.onModeSelected(EventFilterMode.Today, context)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == EventFilterMode.Today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) { Text(stringResource(R.string.Today),
                color = if (selectedMode == EventFilterMode.Today) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
            ) }

            Button(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == EventFilterMode.Day) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) { Text(stringResource(R.string.Day),
                color = if (selectedMode == EventFilterMode.Day) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
            ) }

            Button(
                onClick = {
                    searchEventsViewModel.onModeSelected(EventFilterMode.Month, context)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == EventFilterMode.Month) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) { Text(stringResource(R.string.month),
                color = if (selectedMode == EventFilterMode.Month) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
            ) }
        }
        Log.d("SearchEvents", "Selected Mode searchevents: $selectedMode")

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventItem(event, navController, searchEventsViewModel, removeFromList = false)
                }
            }
        }
    }

    if (showDatePicker) {
        searchEventsViewModel.onModeSelected(EventFilterMode.Day, context)
        DatePickerModalInput(
            onDateSelected = { selectedDate ->
                showDatePicker = false
                if (selectedDate != null) {
                    searchEventsViewModel.onDateSelected(selectedDate, context)
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalInput(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}