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
import com.example.bcngo.model.Review
import com.example.bcngo.model.ReviewModelSerializer
import com.example.bcngo.network.ApiService
import com.example.bcngo.ui.theme.PrincipalRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ListReviewAdmin(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var reportedReviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiService.getReportedReviews(context) // Implementar esta función en ApiService
            reportedReviews = response ?: emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CabezeraAdmin(navController)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (reportedReviews.isNotEmpty()) {
                items(reportedReviews) { review ->
                    ReportedReviewItem(review = review) {
                        selectedReview = review
                        showDialog = true
                    }
                }
            } else {
                item {
                    Text(
                        text = stringResource(id = R.string.no_rep_reviews),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        if (showDialog && selectedReview != null) {
            ConfirmationDialog(
                review = selectedReview!!,
                onConfirm = {
                    CoroutineScope(Dispatchers.IO).launch {
                        ApiService.deleteReview(context, selectedReview!!.id) // Implementar esta función en ApiService
                        reportedReviews = reportedReviews.filter { it.id != selectedReview!!.id }
                    }
                    showDialog = false
                },
                onCancel = { showDialog = false }
            )
        }
    }
}

@Composable
fun ReportedReviewItem(review: Review, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.user_display, review.user_email),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            val comment = review.comment ?: "No comment provided"
            Text(text = stringResource(id = R.string.comment_display, comment))
            Text(text = stringResource(id = R.string.reports_display) + review.reports_count)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrincipalRed)
            ) {
                Text(text = stringResource(id = R.string.delete_review), color = Color.White)
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    review: Review,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(id = R.string.delete_review)) },
        text = { Text(stringResource(id = R.string.sure_deleting_review)) },
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
