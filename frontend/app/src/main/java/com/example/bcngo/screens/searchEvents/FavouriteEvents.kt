package com.example.bcngo.screens.searchEvents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.GreyButton
import com.example.bcngo.components.SearchBar

@Composable
fun FavouriteEvents(
    navController: NavController,
    searchEventsViewModel: SearchEventsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val searchQuery by searchEventsViewModel.searchQuery.observeAsState("") // Obtener la consulta de búsqueda actual
    val isLoading by searchEventsViewModel.isLoading.collectAsState()
    val events by searchEventsViewModel.events.observeAsState(emptyList()) // Observar cambios en la lista de eventos

    LaunchedEffect(Unit) {
        searchEventsViewModel.fetchFavoriteEvents(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Cabezera(navController)

            Text(
                text = stringResource(R.string.my_favourite_Events),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(start = 16.dp),
            )

            // Barra de búsqueda
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SearchBar(
                    value = searchQuery,
                    onValueChange = { newSearch ->
                        searchEventsViewModel.onSearchQueryChange(newSearch)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Lista de eventos
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (events.isEmpty()) { // Verificamos si la lista de eventos está vacía
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_favourite_events),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 56.dp), // Add padding to the bottom
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(events) { event ->
                        EventItem(event, navController, searchEventsViewModel, removeFromList = true)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GreyButton(onClick = { navController.popBackStack() })
        }
    }
}