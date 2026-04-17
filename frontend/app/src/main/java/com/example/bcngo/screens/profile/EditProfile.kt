package com.example.bcngo.screens.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.model.ShortUser
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService
import com.example.bcngo.network.ApiService.removeTokenFromSharedPreferences
import kotlinx.coroutines.launch

@Composable
fun EditProfile(navController: NavController) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf<ShortUser?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var isGoogleUser by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    // Lógica para obtener la información del perfil y si es admin
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            profile = ApiService.getProfile(context)
            profile?.let {
                username = it.username
                notificationsEnabled = it.notifications
            }
            isGoogleUser = ApiService.isGoogleUser(context)
            isAdmin = ApiService.isAdminApi(context) // Verifica si es administrador
        }
    }

    val onLogoutConfirmed: () -> Unit = {
        removeTokenFromSharedPreferences(context)
        navController.navigate(Routes.Welcome.route) {
            popUpTo(Routes.ListIntPoints.route) { inclusive = true }
        }
    }

    val onSaveProfile: () -> Unit = {
        coroutineScope.launch {
            profile?.let {
                ApiService.updateProfile(context, username, notificationsEnabled)
                profile = profile?.copy(username = username, notifications = notificationsEnabled)
            }
        }
        Toast.makeText(context, context.getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
    }

    if (profile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Cabezera(navController)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.girsbanner),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back_arrow)
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.blancouser2),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.LightGray, CircleShape)
                    )
                    profile?.let {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(2.dp)
                        ) {
                            Text(
                                text = it.username,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            profile?.let {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            text = stringResource(R.string.username),
                            color = MaterialTheme.colorScheme.surface
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                OutlinedTextField(
                    value = it.email,
                    onValueChange = {},
                    label = {
                        Text(
                            text = stringResource(R.string.email),
                            color = MaterialTheme.colorScheme.surface
                        )
                    },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.notifications))
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Button(
                    onClick = onSaveProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.save_changes), color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                if (!isGoogleUser) {
                    Button(
                        onClick = { navController.navigate("change-password") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(stringResource(R.string.change_password),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Button(
                    onClick = { showDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Button(
                    onClick = { showDeleteDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.delete_account), color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }

    if (showDialog.value) {
        LogoutDialog(
            onConfirmLogout = onLogoutConfirmed,
            onDismiss = { showDialog.value = false }
        )
    }

    if (showDeleteDialog.value) {
        DeleteAccountDialog(
            onConfirmDelete = {
                coroutineScope.launch {
                    try {
                        ApiService.deleteProfile(context)
                        removeTokenFromSharedPreferences(context)
                        Toast.makeText(
                            context,
                            context.getString(R.string.account_deleted), Toast.LENGTH_SHORT,
                        ).show()
                        navController.navigate(Routes.Welcome.route) {
                            popUpTo(Routes.ListIntPoints.route) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("EditProfile", "Failed to delete account")
                        Toast.makeText(
                            context,
                            context.getString(R.string.failed_to_delete_account),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                showDeleteDialog.value = false
            },
            onDismiss = { showDeleteDialog.value = false },
        )
    }
}

@Composable
fun DeleteAccountDialog(onConfirmDelete: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_account)) },
        text = { Text(stringResource(R.string.delete_account_confirm)) },
        confirmButton = {
            Button(onClick = onConfirmDelete) {
                Text(context.getString(R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(context.getString(R.string.no))
            }
        },
    )
}

@Composable
fun LogoutDialog(onConfirmLogout: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.log_out)) },
        text = { Text(stringResource(id = R.string.log_out_confirmation)) },
        confirmButton = {
            Button(onClick = onConfirmLogout) {
                Text(stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.no))
            }
        },
    )
}