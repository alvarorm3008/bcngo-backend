package com.example.bcngo.screens.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bcngo.R
import com.example.bcngo.components.GoogleButton
import com.example.bcngo.components.RedButton
import com.example.bcngo.exceptions.*
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.getTokenFromSharedPreferences
import com.example.bcngo.network.ApiService.isAdminApi
import com.example.bcngo.network.ApiService.sendUserCredentialsLogIn
import com.example.bcngo.ui.theme.BCNGOTheme
import com.example.bcngo.ui.theme.Background
import com.example.bcngo.ui.theme.PrincipalRed
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.bcngo.network.ApiService.forgotPassword

@Composable
fun LogIn(
    modifier: Modifier = Modifier,
    logInViewModel: LogInViewModel = viewModel(),
    viewModel: LogInGoogleViewModel = viewModel(),
    navController: NavController,
) {
    val email by logInViewModel.usernameemail.observeAsState("")
    val password by logInViewModel.password.observeAsState("")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LoginScaffold {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(), // Add imePadding to handle keyboard
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 25.dp),
                ) {
                    // Botones de Log in y Sign up
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(topStart = 25.dp, bottomStart = 25.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.log_in_text),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Button(
                        onClick = { navController.navigate(Routes.SignUp.route) },
                        colors = ButtonDefaults.buttonColors(Background),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.sign_up_text),
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            item {
                // Texto de bienvenida
                Text(
                    text = stringResource(R.string.welcome_text),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 25.dp),
                )
            }
            item {
                // Campos de texto
                TextField(
                    value = email,
                    onValueChange = { logInViewModel.onUsernameEmailChange(it) },
                    label = { Text(stringResource(R.string.e_mail_textfield_text)) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .padding(top = 16.dp),
                )
            }
            item {
                TextField(
                    value = password,
                    onValueChange = { logInViewModel.onPasswordChange(it) },
                    label = { Text(stringResource(R.string.password_textfield_text)) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .padding(top = 16.dp),
                )
            }
            item {
                // Botón de Log in
                RedButton(
                    modifier,
                    stringResource(R.string.log_in_text),
                    onClick = {
                        scope.launch {
                            logIn(context, email, password, navController)
                        }
                    },
                )
            }
            item {
                // Texto de "or"
                Text(
                    text = stringResource(R.string.or_text),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                )
            }
            item {
                // Botón de Log in con Google
                GoogleButton(R.string.log_in_with_google_text) { credential ->
                    Log.d("LogIn", "Credential: $credential")
                    viewModel.onLogInWithGoogle(context, credential, navController)
                }
            }
            item {
                // Texto forgot password
                Text(
                    text = stringResource(R.string.forgot_password_text),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = TextDecoration.Underline,
                        fontSize = 18.sp,
                    ),
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .padding(horizontal = 16.dp)
                        .clickable { showForgotPasswordDialog = true },
                )

                ForgotPasswordDialog(
                    showDialog = showForgotPasswordDialog,
                    onDismiss = { showForgotPasswordDialog = false },
                    onSendClick = { email ->
                        showForgotPasswordDialog = false
                        Toast.makeText(context, context.getString(R.string.email_sent, email), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

suspend fun logIn(
    context: Context,
    email: String,
    password: String,
    navController: NavController,
) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        try {
            // Llamamos a sendUserCredentialsLogIn para intentar hacer login
            sendUserCredentialsLogIn(context, email, password)
            val token = getTokenFromSharedPreferences(context)

            if (token != null) {
                Log.d("LogIn", "Token recuperado: $token")
                if (isAdmin(context)) {
                    Log.d("LogIn", "El usuario es administrador.")
                    navController.navigate(Routes.HomeAdmin.route)
                } else {
                    Log.d("LogIn", "El usuario no es administrador.")
                    navController.navigate(Routes.Home.route)
                }
            } else {
                Log.e("LogIn", "No se encontró el token en SharedPreferences.")
                Toast.makeText(
                    context,
                    context.getString(R.string.no_se_pudo_recuperar_la_cuenta_intenta_de_nuevo),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        } catch (e: UserNotFoundException) {
            // Si se lanza la excepción de usuario no encontrado, mostramos un mensaje
            Toast.makeText(
                context,
                context.getString(R.string.usuario_no_encontrado_verifica_las_credenciales),
                Toast.LENGTH_SHORT,
            ).show()
        } catch (e: AccountBlockedException) {
            // Si se lanza la excepción de credenciales inválidas, mostramos un mensaje
            Toast.makeText(
                context,
                context.getString(R.string.account_locked_contact_admin),
                Toast.LENGTH_SHORT,
            ).show()
        } catch (e: Exception) {
            // Manejo general de otras excepciones
            Toast.makeText(
                context,
                context.getString(R.string.error_logging_in),
                Toast.LENGTH_SHORT,
            ).show()
        }
    } else {
            Toast.makeText(context, R.string.empty_fields, Toast.LENGTH_SHORT).show()
        }
}

suspend fun isAdmin(context: Context): Boolean {
    return isAdminApi(context)
}

@Composable
fun ForgotPasswordDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSendClick: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.forgot_password_title)) },
            text = {
                Column {
                    Text(text = stringResource(R.string.enter_email_text))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.e_mail_textfield_text)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    try {
                        validateEmail(email, context)
                        coroutineScope.launch {
                            try {
                                forgotPassword(email, context)
                                Toast.makeText(context, context.getString(R.string.email_sent, email), Toast.LENGTH_SHORT).show()
                                onSendClick(email)
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: context.getString(R.string.error_sending_email), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: InvalidEmailException) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = stringResource(R.string.send_text))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel_text))
                }
            }
        )
    }
}

fun validateEmail(email: String, context: Context) {
    if (email.isBlank()) {
        throw InvalidEmailException(context.getString(R.string.no_blank_email))
    }
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    if (!email.matches(emailPattern.toRegex())) {
        throw InvalidEmailException(context.getString(R.string.no_valid_email))
    }
}
