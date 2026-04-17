package com.example.bcngo.screens.detailsPoint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.ui.theme.PrincipalRed
import com.example.bcngo.components.Cabezera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bcngo.R
import com.example.bcngo.components.GreyButton
import com.example.bcngo.model.Review
import com.example.bcngo.network.ApiService
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

fun calculateTotalRating(reviews: List<Review>): Double {
    return if (reviews.isNotEmpty()) {
        reviews.sumOf { it.rating }.toDouble() / reviews.size
    } else {
        0.0
    }
}

@Composable
fun DetailsPoint(
    navController: NavController,
    detailsPointViewModel: DetailsPointViewModel = viewModel(),
    pointId: Int
) {
    // Observe states
    val pointDetailsState = detailsPointViewModel.pointDetails.collectAsState()
    val reviewsState = detailsPointViewModel.reviews.collectAsState()

    val context = LocalContext.current

    // Fetch data when the screen opens
    LaunchedEffect(pointId) {
        detailsPointViewModel.fetchPointDetails(pointId)
        detailsPointViewModel.fetchReviews(context, pointId)
    }

    val totalRating = calculateTotalRating(reviewsState.value)

    // Filter and Order State
    val selectedFilter = remember { mutableStateOf("All Ratings") }
    val selectedOrder = remember { mutableStateOf("Newest") }
    val expandedFilter = remember { mutableStateOf(false) }
    val expandedOrder = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Cabezera(navController = navController)

            // Point Details Section
            pointDetailsState.value?.let { pointDetails ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = pointDetails.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )

                        // Star Rating Row
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= totalRating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (i <= totalRating) Color.Yellow else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        val addressDisplay =
                            when {
                                pointDetails.address_name == null -> "-" // Si address_name es null
                                pointDetails.street_number == null -> pointDetails.address_name // Si solo street_number es null
                                else -> "${pointDetails.address_name}, ${pointDetails.street_number}" // Si ambos tienen valor
                            }
                        Text(
                            stringResource(id = R.string.address) + " $addressDisplay",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Text(
                            stringResource(id = R.string.category) + " ${pointDetails.category}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Text(
                            stringResource(id = R.string.phone) + " ${pointDetails.phone ?: stringResource(
                                R.string.no_phone_available)
                            }",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        val context = LocalContext.current
                        Text(
                            buildAnnotatedString {
                                append(stringResource(id = R.string.website)) // Texto normal
                                append(" ")
                                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append(pointDetails.web_url ?: "-") // URL en cursiva
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.clickable(enabled = pointDetails.web_url != null) {
                                pointDetails.web_url?.let { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter and Order Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Filter Dropdown
                Box {
                    Button(
                        onClick = { expandedFilter.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {

                        Text(
                            text = stringResource(id = R.string.filter_label) + " " + when (selectedFilter.value) {
                                "All Ratings" -> stringResource(R.string.all_ratings)
                                "5 Stars" -> stringResource(R.string.rating_5)
                                "4 Stars" -> stringResource(R.string.rating_4)
                                "3 Stars" -> stringResource(R.string.rating_3)
                                "2 Stars" -> stringResource(R.string.rating_2)
                                "1 Star" -> stringResource(R.string.rating_1)
                                else -> ""
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = expandedFilter.value,
                        onDismissRequest = { expandedFilter.value = false }
                    ) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.all_ratings)) }, onClick = {
                            selectedFilter.value = "All Ratings"
                            expandedFilter.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.rating_5)) }, onClick = {
                            selectedFilter.value = "5 Stars"
                            expandedFilter.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.rating_4)) }, onClick = {
                            selectedFilter.value = "4 Stars"
                            expandedFilter.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.rating_3)) }, onClick = {
                            selectedFilter.value = "3 Stars"
                            expandedFilter.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.rating_2)) }, onClick = {
                            selectedFilter.value = "2 Stars"
                            expandedFilter.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.rating_1)) }, onClick = {
                            selectedFilter.value = "1 Star"
                            expandedFilter.value = false
                        })
                    }
                }

                // Order Dropdown
                Box {
                    Button(
                        onClick = { expandedOrder.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            text = stringResource(id = R.string.order_label) + " " + when (selectedOrder.value) {
                                "Newest" -> stringResource(R.string.newest)
                                "Oldest" -> stringResource(R.string.oldest)
                                "Highest Rating" -> stringResource(R.string.highest_rating)
                                "Lowest Rating" -> stringResource(R.string.lowest_rating)
                                else -> ""
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = expandedOrder.value,
                        onDismissRequest = { expandedOrder.value = false }
                    ) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.newest)) }, onClick = {
                            selectedOrder.value = "Newest"
                            expandedOrder.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.oldest)) }, onClick = {
                            selectedOrder.value = "Oldest"
                            expandedOrder.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.highest_rating)) }, onClick = {
                            selectedOrder.value = "Highest Rating"
                            expandedOrder.value = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.lowest_rating)) }, onClick = {
                            selectedOrder.value = "Lowest Rating"
                            expandedOrder.value = false
                        })
                    }
                }

            }

            // Reviews Section
            Text(
                text = stringResource(R.string.reviews),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp),
                color = Color.Black
            )

            val filteredReviews = reviewsState.value
                .filter { review ->
                    selectedFilter.value == "All Ratings" || review.rating == selectedFilter.value.first().digitToIntOrNull()
                }
                .let { reviews ->
                    when (selectedOrder.value) {
                        stringResource(R.string.order_newest) -> reviews.sortedByDescending { review -> review.created_at }
                        stringResource(R.string.order_oldest) -> reviews.sortedBy { review -> review.created_at }
                        stringResource(R.string.order_highest_rating) -> reviews.sortedByDescending { review -> review.rating }
                        stringResource(R.string.order_lowest_rating) -> reviews.sortedBy { review -> review.rating }
                        else -> reviews
                    }
                }

            // Show message if no reviews
            if (filteredReviews.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_reviews),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredReviews) { review ->
                        ReviewCard(
                            review = review,
                            onDelete = { selectedReview ->
                                detailsPointViewModel.deleteReview(context, selectedReview.id) {
                                    detailsPointViewModel.fetchReviews(context, pointId) // Refresh Reviews
                                }
                            },
                            onReport = { selectedReview ->
                                detailsPointViewModel.reportReview(context, selectedReview.id)
                            }
                        )
                    }
                }
            }


        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Asegura la alineación vertical
        ) {
            // Botón gris
            GreyButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .defaultMinSize(minHeight = 56.dp) // Asegura una altura mínima estándar
                    .align(Alignment.CenterVertically) // Alinea verticalmente al centro
            )

            // Floating Action Button (FAB)
            FloatingActionButton(
                onClick = {
                    try {
                        Log.d("DetailsPoint", "Navigating to review_form_screen/$pointId")
                        navController.navigate("review_form_screen/$pointId")
                    } catch (e: Exception) {
                        Log.e("DetailsPoint", "Failed to navigate: ${e.localizedMessage}")
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(top = 30.dp)

            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Review"
                )
            }
        }



    }
}


@Composable
fun ReviewCard(
    review: Review,
    onDelete: (Review) -> Unit,
    onReport: (Review) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var currentUserEmail by remember { mutableStateOf<String?>(null) }

    // Fetch the logged-in user's email
    LaunchedEffect(Unit) {
        currentUserEmail = ApiService.getProfile(context)?.email
    }

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (i <= review.rating) Color.Yellow else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.date_display, review.created_at.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Text(
                        text = review.comment ?: stringResource(id = R.string.no_comment),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Box {
                    IconButton(onClick = { expanded.value = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false },
                        offset = DpOffset(x = (-48).dp, y = 0.dp)
                    ) {
                        if (review.user_email == currentUserEmail) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    expanded.value = false
                                    onDelete(review)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.report)) },
                                onClick = {
                                    expanded.value = false
                                    onReport(review)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}








