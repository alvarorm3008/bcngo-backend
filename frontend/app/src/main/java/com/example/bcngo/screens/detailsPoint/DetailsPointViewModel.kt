package com.example.bcngo.screens.detailsPoint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.bcngo.network.ApiService
import android.content.Context
import android.util.Log
import com.example.bcngo.model.Review
import com.example.bcngo.model.InterestPoint

class DetailsPointViewModel : ViewModel() {

    // Use InterestPoint directly instead of a custom details class
    private val _pointDetails = MutableStateFlow<InterestPoint?>(null)
    val pointDetails: StateFlow<InterestPoint?> = _pointDetails

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    /**
     * Fetch details of an Interest Point by its ID.
     */
    fun fetchPointDetails(id: Int) {
        viewModelScope.launch {
            try {
                val result = ApiService.getInterestPoint(id)
                if (result != null) {
                    _pointDetails.value = result
                    Log.d("DetailsPointViewModel", "Fetched Point Details: $result")
                } else {
                    Log.e("DetailsPointViewModel", "Failed to fetch point details: Result is null.")
                }
            } catch (e: Exception) {
                Log.e("DetailsPointViewModel", "Exception fetching point details: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Fetch reviews associated with an Interest Point.
     */
    fun fetchReviews(context: Context, id: Int) {
        viewModelScope.launch {
            try {
                val fetchedReviews = ApiService.getAllReviews(context, id)
                fetchedReviews?.let {
                    _reviews.value = it
                    Log.d("DetailsPointViewModel", "Fetched Reviews: $it")
                } ?: Log.d("DetailsPointViewModel", "No reviews fetched.")
            } catch (e: Exception) {
                Log.e("DetailsPointViewModel", "Failed to fetch reviews: ${e.localizedMessage}")
            }
        }
    }

    fun deleteReview(context: Context, reviewId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("DetailsPointViewModel", "Deleting review with ID: $reviewId")
                ApiService.deleteReview(context, reviewId)
                onSuccess() // Trigger refresh after successful delete
            } catch (e: Exception) {
                Log.e("DetailsPointViewModel", "Failed to delete review: ${e.localizedMessage}")
            }
        }
    }


    fun reportReview(context: Context, reviewId: Int) {
        viewModelScope.launch {
            try {
                Log.d("DetailsPointViewModel", "Reporting review with ID: $reviewId")
                ApiService.reportReview(context, reviewId)
            } catch (e: Exception) {
                Log.e("DetailsPointViewModel", "Failed to report review: ${e.localizedMessage}")
            }
        }
    }

}
