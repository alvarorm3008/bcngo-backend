package com.example.bcngo.screens.admin

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.CabezeraAdmin
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.network.ApiService
import com.example.bcngo.ui.theme.PrincipalRed
import com.example.bcngo.utils.AuthManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInterestPoint(
    navController: NavController,
    auth: AuthManager,
) {
    var name by remember { mutableStateOf("") }
    var addressName by remember { mutableStateOf("") }
    var streetNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var webUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var isEssential by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Lista de categorías
    val categories = listOf(
        stringResource(id = R.string.architecture),
        stringResource(id = R.string.parks),
        stringResource(id = R.string.art),
        stringResource(id = R.string.shopping),
        stringResource(id = R.string.leisure),
        stringResource(id = R.string.nightlife),
        stringResource(id = R.string.museums),
    )

    // Estado para el menú desplegable de categoría
    var expanded by remember { mutableStateOf(false) }

    // Verificar si el formulario está completo y es válido
    val isFormValid = remember(name, category, latitude, longitude, phone, webUrl) {
        name.isNotBlank() && category.isNotBlank() &&
                latitude.isNotBlank() && longitude.isNotBlank() &&
                (phone.isBlank() || isValidPhone(phone)) &&
                (webUrl.isBlank() || isValidUrl(webUrl))
    }

    // ID fijo (10)
    val id = 10

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera
        CabezeraAdmin(navController)

        // Título
        Text(
            text = stringResource(id = R.string.create_interest_point),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )

        // Formulario
        Column(modifier = Modifier.padding(16.dp)) {
            // Name
            CustomTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(id = R.string.name),
            )

            // Address Name (opcional)
            CustomTextField(
                value = addressName,
                onValueChange = { addressName = it },
                label = stringResource(id = R.string.address_name_optional),
            )

            // Street Number (opcional)
            CustomTextField(
                value = streetNumber,
                onValueChange = { streetNumber = it },
                label = stringResource(id = R.string.street_number_optional),
                keyboardType = KeyboardType.Number,
            )

            // Phone (opcional)
            CustomTextField(
                value = phone,
                onValueChange = { phone = it },
                label = stringResource(id = R.string.phone_optional),
                keyboardType = KeyboardType.Phone,
            )

            // Web URL (opcional)
            CustomTextField(
                value = webUrl,
                onValueChange = { webUrl = it },
                label = stringResource(id = R.string.website_url_optional),
                keyboardType = KeyboardType.Uri,
            )

            // Category (desplegable con DropdownMenu)
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(id = R.string.category)) },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Mostrar el menú desplegable cuando `expanded` sea true
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            expanded = false
                        },
                    )
                }
            }

            // Latitude
            CustomTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = stringResource(id = R.string.latitude),
                keyboardType = KeyboardType.Number,
            )

            // Longitude
            CustomTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = stringResource(id = R.string.longitude),
                keyboardType = KeyboardType.Number,
            )

            // Esencial (checkbox)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.essential))
                Checkbox(
                    checked = isEssential,
                    onCheckedChange = { isEssential = it },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Save
            Button(
                onClick = {
                    saveInterestPoint(
                        id, name, addressName, streetNumber, phone, webUrl, category, latitude, longitude, isEssential, context,
                        onSuccess = {
                            navController.popBackStack() // Volver atrás después de guardar
                        },
                        onError = { errorMessage = it })
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) PrincipalRed else Color.Gray,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
            ) {
                Text(text = stringResource(id = R.string.save), color = Color.White)
            }
        }
        // Mostrar Snackbar con el mensaje de error
        errorMessage?.let { message ->
            Snackbar(
                action = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text(text = stringResource(R.string.save))
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = message)
            }
        }
    }
}


// Componente reutilizable de un campo de texto
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    )
}

// Validación de teléfono
fun isValidPhone(phone: String): Boolean {
    return phone.length >= 10 && Patterns.PHONE.matcher(phone).matches()
}

// Validación de URL
fun isValidUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}

// Función para guardar el punto de interés

fun saveInterestPoint(
    id: Int,
    name: String,
    addressName: String?,
    streetNumber: String?,
    phone: String?,
    webUrl: String?,
    category: String,
    latitude: String,
    longitude: String,
    isEssential: Boolean,
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val newPoint = InterestPoint(
        id = id,
        name = name,
        address_name = addressName,
        street_number = streetNumber,
        latitude = latitude.toFloat(),
        longitude = longitude.toFloat(),
        phone = phone,
        web_url = webUrl,
        category = category,
        esencial = isEssential,
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = ApiService.createPointOfInterest(newPoint, context)
            withContext(Dispatchers.Main) {
                if (result != null) {
                    onSuccess()
                } else {
                    val errorMessage = "Failed to create point of interest"
                    Log.e("CreateInterestPoint", errorMessage)
                    onError(errorMessage)
                }
            }
        } catch (e: Exception) {
            val errorMessage = "Exception: ${e.message}"
            Log.e("CreateInterestPoint", errorMessage)
            withContext(Dispatchers.Main) {
                onError(errorMessage)
            }
        }
    }
}