package com.example.bcngo.screens.login

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.sendUserCredentialsGoogle
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LogInGoogleViewModel : ViewModel() {
    fun onLogInWithGoogle(
        context: Context,
        credential: Credential,
        navController: NavController,
    ) {
        viewModelScope.launch {
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null,
                )
                Log.d("LOGINGOOGLE", "Firebase Credential = $firebaseCredential")
                Log.d("LOGINGOOGLE", "Google Id Token = ${googleIdTokenCredential.idToken}")

                loginWithFirebaseCredential(context, firebaseCredential, navController)
            } else {
                Log.e("LOGINGOOGLE", "Error = Invalid credential")
            }
        }
    }

    private suspend fun loginWithFirebaseCredential(
        context: Context,
        firebaseCredential: AuthCredential,
        navController: NavController,
    ) {
        val task = Firebase.auth.signInWithCredential(firebaseCredential)
        task.await()

        if (task.isSuccessful) {
            val user = task.result.user
            val idToken = user?.getIdToken(false)?.await()?.token // Obtiene el token de Firebase
            Log.i("LOGINGOOGLE", "Login Success: User = ${user?.displayName}, Token = $idToken")

            if (idToken != null) {
                sendUserCredentialsGoogle(context, idToken)
            } else {
                Log.e("LOGINGOOGLE", "Error = idToken is null")
            }

            if (isAdmin(context)) {
                Log.d("LogIn", "El usuario es administrador.")
                navController.navigate(Routes.HomeAdmin.route)
            } else {
                Log.d("LogIn", "El usuario no es administrador.")
                navController.navigate(Routes.Home.route)
            }
        } else {
            Log.e("LOGINGOOGLE", "Error = ${task.exception?.stackTraceToString()}")
        }
    }
}
