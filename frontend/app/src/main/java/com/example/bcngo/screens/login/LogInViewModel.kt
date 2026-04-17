package com.example.bcngo.screens.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogInViewModel : ViewModel() {
    private val _usernameemail = MutableLiveData("")
    val usernameemail: LiveData<String> = _usernameemail

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _navigateToSign = MutableLiveData(false)
    val navigateToSign: LiveData<Boolean> = _navigateToSign

    fun onUsernameEmailChange(newUsernameEmail: String) {
        _usernameemail.value = newUsernameEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onSignClick() {
        _navigateToSign.value = true
    }

    fun onNavigationDone() {
        _navigateToSign.value = false
    }
}
