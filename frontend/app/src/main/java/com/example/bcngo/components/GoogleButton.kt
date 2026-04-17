package com.example.bcngo.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.example.bcngo.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

@Composable
fun GoogleButton(
    buttonText: Int,
    onCredentialResponse: (Credential) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    Button(
        onClick = {
            val googleIdOptions =
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()

            val request =
                GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOptions)
                    .build()

            coroutineScope.launch {
                try {
                    Log.d("GoogleButtonLogin", "Request = $request")
                    Log.d("GoogleButtonLogin", "Context = $context")
                    val result =
                        credentialManager.getCredential(
                            request = request,
                            context = context,
                        )
                    Log.d("GoogleButtonLogin", "Credential = ${result.credential}")

                    onCredentialResponse(result.credential)
                } catch (e: NoCredentialException) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.no_credentials), Toast.LENGTH_SHORT,
                    ).show()
                    Log.e("GoogleButtonLogin", "No credentials available: ${e.message}, ${e.stackTraceToString()}")
                } catch (e: Exception) {
                    Log.e("GoogleButtonLogin", "Error: ${e.stackTraceToString()}, ${e.message}")
                }
            }
        },
        colors = ButtonDefaults.buttonColors(Color.LightGray),
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono de Google
            Image(
                painter = painterResource(id = R.drawable.logo_google),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(buttonText),
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

