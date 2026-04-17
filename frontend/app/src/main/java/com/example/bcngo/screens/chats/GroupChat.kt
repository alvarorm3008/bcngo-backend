package com.example.bcngo.screens.chats

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.ChatBubble
import com.example.bcngo.model.Message
import com.example.bcngo.model.ShortUser
import com.example.bcngo.network.ApiService
import com.example.bcngo.network.ApiService.getChatMessages
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bcngo.R

@Composable
fun GroupChat(
    navController: NavController,
    chatId: Int,
    event_name: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<Message>?>(null) }
    var profile by remember { mutableStateOf<ShortUser?>(null) }
    var newMessage by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var showDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<Message?>(null) }
    var messageToReport by remember { mutableStateOf<Message?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        scope.launch {
            val fetchedMessages = getChatMessages(context, chatId)
            if (fetchedMessages != null) {
                messages = fetchedMessages.sortedBy { it.created_at }
                if (messages!!.isNotEmpty()) {
                    listState.scrollToItem(messages!!.size - 1)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            profile = ApiService.getProfile(context)
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val receivedChatId = intent?.getIntExtra("chatId", -1)
                if (receivedChatId == chatId) {
                    scope.launch {
                        val fetchedMessages = getChatMessages(context!!, chatId)
                        if (fetchedMessages != null) {
                            messages = fetchedMessages.sortedBy { it.created_at }
                            if (messages!!.isNotEmpty()) {
                                listState.scrollToItem(messages!!.size - 1)
                            }
                        }
                    }
                }
            }
        }
        val filter = IntentFilter("com.example.bcngo.NEW_MESSAGE")
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Cabezera(navController)

        Row {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = stringResource(R.string.back_arrow),
                )
            }
            Text(
                text = event_name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(10.dp),
            )
        }

        if (messages == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (messages!!.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_messages),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(10.dp)
            ) {
                val groupedMessages = messages!!.groupBy { message ->
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val messageDate = message.created_at.date
                    when {
                        messageDate == today -> context.getString(R.string.today)
                        messageDate == today.minus(1, DateTimeUnit.DAY) -> context.getString(R.string.yesterday)
                        else -> messageDate.toString()
                    }
                }

                groupedMessages.forEach { (date, messages) ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    itemsIndexed(messages) { _, message ->
                        ChatBubble(
                            message = message,
                            isCurrentUser = message.creator_name == profile?.username,
                            onDeleteClick = {
                                messageToDelete = message
                                showDialog = true
                            },
                            onReportClick = {
                                messageToReport = message
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .systemBarsPadding()
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.write_message)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (newMessage.isNotBlank()) {
                            scope.launch {
                                ApiService.addMessage(chatId, newMessage, context)
                                newMessage = ""
                                val fetchedMessages = getChatMessages(context, chatId)
                                if (fetchedMessages != null) {
                                    messages = fetchedMessages.sortedBy { it.created_at }
                                    if (messages!!.isNotEmpty()) {
                                        listState.scrollToItem(messages!!.size - 1)
                                    }
                                }
                            }
                        }
                    }
                )
            )
            Button(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        scope.launch {
                            ApiService.addMessage(chatId, newMessage, context)
                            newMessage = ""
                            val fetchedMessages = getChatMessages(context, chatId)
                            if (fetchedMessages != null) {
                                messages = fetchedMessages.sortedBy { it.created_at }
                                if (messages!!.isNotEmpty()) {
                                    listState.scrollToItem(messages!!.size - 1)
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send, // Usa un ícono de flecha
                    contentDescription = stringResource(R.string.send),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = context.getString(R.string.confirm)) },
            text = {
                if (messageToDelete != null) {
                    Text(text = stringResource(R.string.delete_message_confirmation))
                } else {
                    Text(text = stringResource(R.string.report_message_confirmation))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            messageToDelete?.let {
                                ApiService.deleteMessage(it.id, context)
                                val fetchedMessages = getChatMessages(context, chatId)
                                if (fetchedMessages != null) {
                                    messages = fetchedMessages.sortedBy { it.created_at }
                                    if (messages!!.isNotEmpty()) {
                                        listState.scrollToItem(messages!!.size - 1)
                                    }
                                }
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.deleted_message),
                                    Toast.LENGTH_SHORT,
                                ).show()
                                messageToDelete = null
                            }
                            messageToReport?.let {
                                ApiService.reportMessage(it.id, context)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.report_message), Toast.LENGTH_SHORT,
                                ).show()
                                messageToReport = null
                            }
                        }
                        showDialog = false
                    },
                ) {
                    Text(context.getString(R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(context.getString(R.string.no))
                }
            },
        )
    }
}