package com.example.bcngo.screens.review

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bcngo.components.Cabezera

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.network.ApiService
import kotlinx.coroutines.launch

@Composable
fun ReviewForm(
    navController: NavController,
    pointId: Int
) {

    var selectedStars by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var interestPoint by remember { mutableStateOf<InterestPoint?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pointId) {
        interestPoint = ApiService.getInterestPoint(pointId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Cabecera
        Cabezera(navController)

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = stringResource(id = R.string.review_interest_point_name, interestPoint?.name ?: ""),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Selección de estrellas
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                for (i in 1..5) {
                    val starColor = if (i <= selectedStars) Color(0xFF910811) else Color.Gray
                    Icon(
                        imageVector = if (i <= selectedStars) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = starColor,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { selectedStars = i }
                    )
                }
            }

            // Campo de texto para la reseña
            Text(
                text = stringResource(id = R.string.express_your_opinion),
                style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(vertical = 8.dp)
            )

            BasicTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                decorationBox = { innerTextField ->
                    if (reviewText.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.write_your_review),
                            style = TextStyle(fontSize = 16.sp, color = Color.Gray)
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            val errorMessage = stringResource(R.string.could_not_create_review)

            // Botón para enviar la reseña
            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = ApiService.createReview(
                            context = context,
                            pointOfInterestId = pointId,
                            comment = reviewText,
                            rating = selectedStars
                        )
                        if (success) {
                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                context,
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                },
                enabled = selectedStars > 0, // Disable button if no stars are selected
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF910811)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.send_review),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Add some space between buttons

            // Botón para cancelar
            Button(
                onClick = {
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

