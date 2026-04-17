package com.example.bcngo.screens.signup

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bcngo.R
import com.example.bcngo.components.GoogleButton
import com.example.bcngo.exceptions.*
import com.example.bcngo.navigation.Routes
import com.example.bcngo.network.ApiService.sendUserCredentials
import com.example.bcngo.screens.login.LogInGoogleViewModel
import com.example.bcngo.screens.login.LoginScaffold
import com.example.bcngo.ui.theme.BCNGOTheme
import com.example.bcngo.ui.theme.PrincipalRed
import com.example.bcngo.utils.AuthManager
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.VisualTransformation
import com.example.bcngo.network.ApiService.saveTokenToSharedPreferences

@Composable
fun SignUp(
    modifier: Modifier = Modifier,
    signUpViewModel: SignUpViewModel = viewModel(),
    viewModel: LogInGoogleViewModel = viewModel(),
    navController: NavController,
    auth: AuthManager,
) {
    val image = painterResource(R.drawable.background2)
    val image2 = painterResource(R.drawable.logonegro)

    val usernameState = signUpViewModel.username.observeAsState("")
    val emailState = signUpViewModel.email.observeAsState("")
    val passwordState = signUpViewModel.password.observeAsState("")
    val username = usernameState.value
    val email = emailState.value
    val password = passwordState.value
    val navigateToLogin = signUpViewModel.navigateToLogin.observeAsState(false)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(navigateToLogin.value) {
        if (navigateToLogin.value) {
            navController.navigate("login")
            signUpViewModel.onNavigationDone()
        }
    }

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
                        onClick = { navController.navigate(Routes.LogIn.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCCCCC)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(topStart = 25.dp, bottomStart = 25.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.log_in_text),
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Button(
                        onClick = { /* Acción de Sign Up */ },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.sign_up_text),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
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
                    value = username,
                    onValueChange = { signUpViewModel.onUsernameChange(it) },
                    label = { Text(stringResource(R.string.username_textfield_text)) },
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
                    value = email,
                    onValueChange = { signUpViewModel.onEmailChange(it) },
                    label = { Text(stringResource(R.string.e_mail_textfield_text)) },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
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
                    onValueChange = { signUpViewModel.onPasswordChange(it) },
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
                // Botón de Sign Up
                Button(
                    onClick = {
                        scope.launch {
                            signUp(context, username, email, password, navController)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 30.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                ) {
                    Text(text = stringResource(R.string.sign_up_text), color = Color.White)
                }
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
                // Botón de Sign Up con Google
                GoogleButton(R.string.log_in_with_google_text) { credential ->
                    viewModel.onLogInWithGoogle(context, credential, navController)
                }
            }
        }
    }
}

suspend fun signUp(
    context: Context,
    username: String,
    email: String,
    password: String,
    navController: NavController,
) {
    if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
        try {
            // Validar el nombre de usuario antes de continuar
            validateUsername(username, context)

            // Validar la contraseña antes de continuar
            validatePassword(password, context)

            // Llamar a la función de registro si las validaciones son exitosas
            sendUserCredentials(context, username, email, password)

            // Si no ocurre una excepción, navegamos al Home
            navController.navigate(Routes.Home.route)
        } catch (e: InvalidUsernameException) {
            // Si el error es por nombre de usuario inválido, mostramos un mensaje específico
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        } catch (e: InvalidEmailException) {
            // Si el error es por correo electrónico inválido, mostramos un mensaje específico
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        } catch (e: EmailAlreadyExistsException) {
            // Si el error es por correo electrónico ya registrado, mostramos un mensaje específico
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        } catch (e: InvalidPasswordException) {
            // Si el error es por contraseña inválida, mostramos un mensaje específico
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        } catch (e: UsernameAlreadyExistsException) {
            // Si el error es por nombre de usuario ya registrado, mostramos un mensaje específico
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }

        catch (e: Exception) {
            Log.d("SignUpError", e.localizedMessage ?: "Error desconocido")
            // Manejo general de otras excepciones
            Toast.makeText(
                context,
                context.getString(R.string.error_register, e.localizedMessage),
                Toast.LENGTH_SHORT,
            ).show()
        }
    } else {
        Toast.makeText(context, context.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
    }
}

fun validateUsername(username: String, context: Context) {
    // Comprobar longitud mínima de 6 caracteres
    if (username.length <= 6) {
        throw InvalidUsernameException(context.getString(R.string.user_characters))
    }

    // Comprobar que el nombre de usuario solo contenga caracteres alfanuméricos
    val regex = "^[a-zA-Z0-9]*$".toRegex() // Solo permite letras y números
    if (!username.matches(regex)) {
        throw InvalidUsernameException(context.getString(R.string.user_letters_numbers))
    }
}

fun validatePassword(password: String, context: Context) {
    // Comprobar longitud mínima de 6 caracteres
    if (password.length <= 6) {
        throw InvalidPasswordException(context.getString(R.string.password_characters))
    }

    // Comprobar que la contraseña contenga al menos un número
    val regex = ".*\\d.*".toRegex() // Al menos un número en cualquier parte de la contraseña
    if (!password.matches(regex)) {
        throw InvalidPasswordException(context.getString(R.string.password_number))
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    val navController = rememberNavController()
    BCNGOTheme {
        // SignUp(navController = navController)
    }
}

