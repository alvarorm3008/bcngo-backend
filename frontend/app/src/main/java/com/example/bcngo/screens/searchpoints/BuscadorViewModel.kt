package com.example.bcngo.screens.searchpoints

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BuscadorViewModel : ViewModel() {
    private val _search = MutableLiveData("")
    val search: LiveData<String> = _search

    fun onSearchChange(newSearch: String) {
        _search.value = newSearch
    }
}
