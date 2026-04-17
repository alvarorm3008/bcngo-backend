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
import com.example.bcngo.components.SearchBar
import com.example.bcngo.network.ApiService
import com.example.bcngo.ui.theme.PrincipalRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ListUsersAdmin(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var userEmails by remember { mutableStateOf<List<String>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiService.getUserEmails(context) // Implementar esta función en ApiService
            userEmails = response ?: emptyList()
        }
    }

    val filteredEmails = userEmails.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CabezeraAdmin(navController)

        SearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        )

        UsersListScreen(filteredEmails = filteredEmails, context = context)
    }
}

@Composable
fun UsersListScreen(filteredEmails: List<String>, context: android.content.Context) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (filteredEmails.isNotEmpty()) {
            items(filteredEmails) { email ->
                UserListItem(email = email, context = context)
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.no_users_found),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
fun UserListItem(email: String, context: android.content.Context) {
    var isBlocked by remember { mutableStateOf<Boolean?>(null) }
    var isAdmin by remember { mutableStateOf<Boolean?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }
    var currentUserEmail by remember { mutableStateOf<String?>(null) }

    // Obtener el correo del usuario autenticado
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val profile = ApiService.getProfile(context)
            currentUserEmail = profile?.email
        }
    }

    LaunchedEffect(email) {
        CoroutineScope(Dispatchers.IO).launch {
            isBlocked = ApiService.isUserBlocked(context, email)
            isAdmin = ApiService.isUserAdmin(context, email)
        }
    }

    if (showDialog) {
        ConfirmationDialog(
            email = email,
            actionType = actionType,
            onConfirm = {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = when (actionType) {
                        "block" -> ApiService.blockUser(context, email)
                        "unblock" -> ApiService.unblockUser(context, email)
                        "grantAdmin" -> ApiService.grantUserAdmin(context, email)
                        "revokeAdmin" -> ApiService.revokeUserAdmin(context, email)
                        else -> false
                    }
                    if (result) {
                        when (actionType) {
                            "block" -> isBlocked = true
                            "unblock" -> isBlocked = false
                            "grantAdmin" -> isAdmin = true
                            "revokeAdmin" -> isAdmin = false
                        }
                    }
                }
                showDialog = false
            },
            onCancel = { showDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            // Verificar si el correo no es el del usuario autenticado
            if (currentUserEmail != email && email != "prueba3@gmail.com") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            actionType = if (isBlocked == true) "unblock" else "block"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBlocked == true) Color.DarkGray else PrincipalRed
                        )
                    ) {
                        Text(
                            if (isBlocked == true) stringResource(R.string.action_unblock) else stringResource(R.string.action_block),
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            actionType = if (isAdmin == true) "revokeAdmin" else "grantAdmin"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdmin == true) Color.DarkGray else PrincipalRed
                        )
                    ) {
                        Text(
                            if (isAdmin == true) stringResource(R.string.action_revoke_admin) else stringResource(R.string.action_grant_admin),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun ConfirmationDialog(
    email: String,
    actionType: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val actionText = when (actionType) {
        "block" -> stringResource(R.string.action_block)
        "unblock" -> stringResource(R.string.action_unblock)
        "grantAdmin" -> stringResource(R.string.action_grant_admin)
        else -> stringResource(R.string.action_revoke_admin)
    }

    val confirmationText = stringResource(R.string.confirm_action, actionText, email)

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.confirmation)) },
        text = {Text(text = confirmationText)},
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(R.string.no))
            }
        }
    )
}
