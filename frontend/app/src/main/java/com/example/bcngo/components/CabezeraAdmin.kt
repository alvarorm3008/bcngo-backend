package com.example.bcngo.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.removeTokenFromSharedPreferences
import com.example.bcngo.ui.theme.Background

@Composable
fun CabezeraAdmin(navController: NavController,
             onExitAttempt: (() -> Unit)? = null // Parámetro opcional
) {

    val showDialog = remember { mutableStateOf(false) }
    val showMenu = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val onLogoutConfirmed: () -> Unit = {
        removeTokenFromSharedPreferences(context)
        navController.navigate(Routes.Welcome.route) {
            popUpTo(Routes.ListIntPoints.route) { // Cuidado con el popUpTo
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(MaterialTheme.colorScheme.primary), // Color principal definido
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logoblanco),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        navController.navigate(Routes.HomeAdmin.route) // Navegación normal
                    },
            )
            IconButton(
                onClick = {
                    navController.navigate(Routes.EditProfile.route) // Navegación normal
                },
                modifier = Modifier.size(50.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Icon",
                    tint = Color.White, // Color de iconos para el modo oscuro
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }

    if (showMenu.value) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMenu.value = false }
                .background(Background.copy(alpha = 0.5f)), // Fondo ajustado
        ) {
            AnimatedVisibility(
                visible = showMenu.value,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {

            }
        }
    }

}