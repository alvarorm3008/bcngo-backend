package com.example.bcngo.screens.chats

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.model.Chat
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.accederChat
import com.example.bcngo.network.ApiService.getDeviceToken
import com.example.bcngo.network.ApiService.leaveChat
import kotlinx.coroutines.launch

@Composable
fun ChatItem(
    chat: Chat,
    navController: NavController,
    myChatsViewModel: MyChatsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                scope.launch {
                    if (chat.id != null) {
                        Log.d("ChatItem", "Chat ID: ${chat.id}")
                        val token = getDeviceToken()
                        accederChat(token, chat.event, context)
                        navController.navigate(
                            Routes.GroupChat.createRoute(
                                chat.id,
                                chat.event_name
                            )
                        )
                    } else {
                        Log.e("ChatItem", "Chat ID is null!")
                    }
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = chat.event_name,
            fontSize = 20.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f) // Use weight to take available space
        )
        IconButton(
            onClick = { showDialog = true }
        ) {
            Icon(
                imageVector = Icons.Default.RemoveCircle,
                contentDescription = stringResource(R.string.remove_point),
                tint = Color(0xFF800000),
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = context.getString(R.string.confirm)) },
            text = { Text(text = stringResource(R.string.leave_chat_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle the confirmation action
                        scope.launch {
                            leaveChat(chat.id, context)
                            myChatsViewModel.loadChats(context) // Refresh the screen
                        }
                        showDialog = false
                    }
                ) {
                    Text(text = context.getString(R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = context.getString(R.string.no))
                }
            },
        )
    }
}
