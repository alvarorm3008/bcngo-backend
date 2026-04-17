package com.example.bcngo.screens.chats

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcngo.network.ApiService.getMyChats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.bcngo.model.Chat


class MyChatsViewModel : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    fun loadChats(context: Context) {
        viewModelScope.launch {
            val chatList = getMyChats(context)
            if (chatList != null) {
                _chats.value = chatList
            } else {
                Log.e("MyChatsViewModel", "Error al cargar los chats")
            }
        }
    }
}
