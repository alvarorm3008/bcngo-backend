package com.example.bcngo.screens

import androidx.compose.foundation.background
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
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bcngo.components.Cabezera
import com.example.bcngo.model.Review
import com.example.bcngo.network.ApiService
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.example.bcngo.R

@Composable
fun EditReviewForm(
    navController: NavController,
    reviewId: Int
) {
    val scope = rememberCoroutineScope()
    val reviewDetails = remember { mutableStateOf<Review?>(null) }

    // Fetch Review Details
    LaunchedEffect(reviewId) {
        try {
            Log.d("EditReviewForm", "Fetching details for reviewId: $reviewId")
            val fetchedReview = ApiService.getReviewById(reviewId)
            reviewDetails.value = fetchedReview
        } catch (e: Exception) {
            Log.e("EditReviewForm", "Failed to fetch review details: ${e.localizedMessage}")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Cabezera(navController)

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.preview_review),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Review Details
            reviewDetails.value?.let { review ->
                Text(
                    text = stringResource(R.string.user_display),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.rating),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Display Stars
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    for (i in 1..5) {
                        val starColor = if (i <= review.rating) Color(0xFF910811) else Color.Gray
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = starColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.comment),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Display Read-Only Comment
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = review.comment ?: stringResource(R.string.no_comment_avilable),
                        style = TextStyle(fontSize = 16.sp, color = Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            } ?: Text(
                text = stringResource(R.string.loading_review_details),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Back Button
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.back_button_text),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
