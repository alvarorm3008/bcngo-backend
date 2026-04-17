package com.example.bcngo.screens.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bcngo.model.InterestPoint

class PointViewModel : ViewModel() {
    val selectedInterestPoint = MutableLiveData<InterestPoint?>(null)
}