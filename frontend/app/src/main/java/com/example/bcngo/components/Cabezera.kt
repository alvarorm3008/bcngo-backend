
package com.example.bcngo.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.removeTokenFromSharedPreferences
import com.example.bcngo.ui.theme.Background
import com.example.bcngo.ui.theme.TextColor
import com.example.bcngo.ui.theme.TextColorDark

@Composable
fun Cabezera(navController: NavController, onExitAttempt: (() -> Unit)? = null) {
    val showDialog = remember { mutableStateOf(false) }
    val showMenu = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val onLogoutConfirmed: () -> Unit = {
        removeTokenFromSharedPreferences(context)
        navController.navigate(Routes.Welcome.route) {
            popUpTo(Routes.ListIntPoints.route) {
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { showMenu.value = !showMenu.value },
                modifier = Modifier.size(50.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            if (showMenu.value) Background else Color.Transparent,
                            shape = androidx.compose.foundation.shape.CircleShape,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu Icon",
                        tint = if (showMenu.value) TextColor else TextColorDark,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.logoblanco),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        if (onExitAttempt != null) {
                            onExitAttempt()
                        } else {
                            navController.navigate(Routes.Home.route)
                        }
                    },
            )

            IconButton(
                onClick = {
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.EditProfile.route)
                    }
                },
                modifier = Modifier.size(50.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Icon",
                    tint = TextColorDark,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        DropdownMenu(
            expanded = showMenu.value,
            onDismissRequest = { showMenu.value = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.home)) },
                onClick = {
                    showMenu.value = false
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.Home.route)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Home, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.my_itineraries)) },
                onClick = {
                    showMenu.value = false
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.ListItineraries.route)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Star, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.passport)) },
                onClick = {
                    showMenu.value = false
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.VirtualPassport.route)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.ContactMail, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.points_of_interest)) },
                onClick = {
                    showMenu.value = false
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.ListIntPoints.route)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.PinDrop, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.events)) },
                onClick = {
                    showMenu.value = false
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.SearchEvents.route)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Event, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.chats)) },
                onClick = {
                    showMenu.value = false
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.MyChats.route)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Chat, contentDescription = null)
                }
            )
        }
    }

    if (showDialog.value) {
        LogoutDialog(
            onConfirmLogout = {
                onLogoutConfirmed()
                showDialog.value = false
            },
            onDismiss = { showDialog.value = false }
        )
    }
}
@Composable
fun DrawerMenu(modifier: Modifier = Modifier, onDismiss: () -> Unit, navController: NavController, onExitAttempt: (() -> Unit)? = null) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(170.dp)
            .background(Background), // Fondo del menú
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MenuItem(
                icon = Icons.Filled.Home,
                text = stringResource(id = R.string.home),
                onClick = {
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.Home.route)
                    }},
                modifier = Modifier.weight(1f)
            )
            MenuItem(
                icon = Icons.Filled.Star,
                text = stringResource(id = R.string.my_itineraries),
                onClick = {
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    }
                    else { navController.navigate(Routes.ListItineraries.route) }
                },
                modifier = Modifier.weight(1f)
            )
            MenuItem(
                icon = Icons.Filled.Event,
                text = stringResource(id = R.string.my_events),
                onClick = {
                    if (onExitAttempt != null) {
                        onExitAttempt()
                    } else {
                        navController.navigate(Routes.SearchEvents.route)
                    } },
                modifier = Modifier.weight(1f)
            )
            MenuItem(
                icon = Icons.Filled.CalendarToday,
                text = stringResource(id = R.string.calendar),
                onClick = { if (onExitAttempt != null) {
                    onExitAttempt()
                } else {
                    navController.navigate(Routes.ListItineraries.route)
                } },
                modifier = Modifier.weight(1f)
            )
            MenuItem(
                icon = Icons.Filled.Chat,
                text = stringResource(id = R.string.chats),
                onClick = { if (onExitAttempt != null) {
                    onExitAttempt()
                } else {
                    navController.navigate(Routes.MyChats.route)
                } },
                modifier = Modifier.weight(1f)
            )
            MenuItem(
                icon = Icons.Filled.Settings,
                text = stringResource(id = R.string.settings),
                onClick = { if (onExitAttempt != null) {
                    onExitAttempt()
                } else {
                    navController.navigate(Routes.VirtualPassport.route)
                } },
                modifier = Modifier.weight(1f)
            )
            MenuItem(
                icon = Icons.Filled.PinDrop,
                text = stringResource(id = R.string.points_of_interest),
                onClick = { if (onExitAttempt != null) {
                    onExitAttempt()
                } else {
                    navController.navigate(Routes.ListIntPoints.route)
                } },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier) {
    val isSelected = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isSelected.value = !isSelected.value
                onClick()
            }
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        IconButton(
            onClick = { onClick() },
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$text Icon",
                tint = TextColor, // Color de texto
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text,
            color = TextColor, // Color del texto
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
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