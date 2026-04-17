package com.example.bcngo.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.CabezeraAdmin
import com.example.bcngo.model.ReportedMessage
import com.example.bcngo.network.ApiService
import com.example.bcngo.ui.theme.PrincipalRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ListMessagesAdmin(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var reportedMessages by remember { mutableStateOf<List<ReportedMessage>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<ReportedMessage?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiService.getReportedMessages(context)
            reportedMessages = response ?: emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CabezeraAdmin(navController)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (reportedMessages.isNotEmpty()) {
                items(reportedMessages) { message ->
                    ReportedMessageItem(message = message) {
                        selectedMessage = message
                        showDialog = true
                    }
                }
            } else {
                item {
                    Text(
                        text = stringResource(id = R.string.no_messages),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        if (showDialog && selectedMessage != null) {
            ConfirmationDialog(
                message = selectedMessage!!,
                onConfirm = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val success = ApiService.deleteMessage(context, selectedMessage!!.id)
                        if (success) {
                            reportedMessages = reportedMessages.filter { it.id != selectedMessage!!.id }
                        }
                    }
                    showDialog = false
                },
                onCancel = { showDialog = false }
            )
        }
    }
}

@Composable
fun ReportedMessageItem(message: ReportedMessage, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.user_display, message.creatorName),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(stringResource(id = R.string.message_display, message.message))
            Text(stringResource(id = R.string.reports_display) +  message.reportsCount)
            Text(stringResource(id = R.string.created_at_display) +  message.createdAt)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrincipalRed)
            ) {
                Text(stringResource(id = R.string.delete_message), color = Color.White)
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    message: ReportedMessage,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(id = R.string.delete_message)) },
        text = { Text(stringResource(id = R.string.sure_deleting)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(id = R.string.no))
            }
        }
    )
}
