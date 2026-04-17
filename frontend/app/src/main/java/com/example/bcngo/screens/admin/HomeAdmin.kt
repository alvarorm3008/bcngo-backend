package com.example.bcngo.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bcngo.R
import com.example.bcngo.components.CabezeraAdmin
import com.example.bcngo.ui.theme.PrincipalRed
import com.example.bcngo.utils.AuthManager

@Composable
fun HomeAdmin(navController: NavHostController, auth: AuthManager) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Cabecera ocupando toda la anchura sin padding
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            CabezeraAdmin(navController = navController)
        }

        // Espaciado entre la cabecera y el contenido
        Spacer(modifier = Modifier.height(20.dp))

        // Título de la pantalla
        Text(
            text = stringResource(R.string.administrator_panel),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de administración
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Botón Administrar Usuarios
            Button(
                onClick = { navController.navigate("list_users_admin_screen") },
                colors = ButtonDefaults.buttonColors(containerColor = PrincipalRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(60.dp),
            ) {
                Text(
                    text = stringResource(R.string.administrate_users),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Administrar Puntos de Interés
            Button(
                onClick = { navController.navigate("list_points_admin_screen") },
                colors = ButtonDefaults.buttonColors(containerColor = PrincipalRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(60.dp),
            ) {
                Text(
                    text = stringResource(R.string.administrate_points_of_interest),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Administrar Puntos de Interés
            Button(
                onClick = { navController.navigate("list_review_admin_screen") },
                colors = ButtonDefaults.buttonColors(containerColor = PrincipalRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(60.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.reported_reviews),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("list_messages_admin_screen") },
                colors = ButtonDefaults.buttonColors(containerColor = PrincipalRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(60.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.reported_messages),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
