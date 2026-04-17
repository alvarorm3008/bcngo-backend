package com.example.bcngo.screens.chats

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.bcngo.model.Event
import com.example.bcngo.network.ApiService.accederChat
import com.example.bcngo.network.ApiService.getDeviceToken
import com.example.bcngo.network.ApiService.getFavouriteEvents
import kotlinx.coroutines.launch

@Composable
fun MyChats(
    navController: NavController,
    myChatsViewModel: MyChatsViewModel = viewModel()
) {
    val context = LocalContext.current
    val chats = myChatsViewModel.chats.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var favoriteEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        myChatsViewModel.loadChats(context)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Cabezera(navController)

            Text(
                text = stringResource(R.string.my_chats),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .padding(10.dp),
            )

            if (chats.value.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_chats_available),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chats.value) { chat ->
                        ChatItem(
                            chat = chat,
                            navController = navController
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val events = getFavouriteEvents(context)
                        if (events != null) {
                            val chatEventIds = chats.value.map { it.event }
                            favoriteEvents = events.filter { it.id !in chatEventIds }
                            showDialog = true
                        }
                    }
                },
                modifier = Modifier.systemBarsPadding(),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.create_chat))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(id = R.string.select_event)) },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    if (favoriteEvents.isEmpty()) {
                        Text(
                            text = stringResource(R.string.favourites_access_chat),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        )
                    } else {
                        LazyColumn {
                            items(favoriteEvents) { event ->
                                Button(
                                    onClick = {
                                        scope.launch {
                                            // Handle event selection
                                            val token = getDeviceToken()
                                            accederChat(token, event.id, context)
                                            showDialog = false
                                            myChatsViewModel.loadChats(context) // Refresh the screen
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(text = event.name)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.close))
                }
            },
        )
    }
}
