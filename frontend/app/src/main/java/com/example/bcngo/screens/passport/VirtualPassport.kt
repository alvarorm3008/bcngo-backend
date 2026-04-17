package com.example.bcngo.screens.passport

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bcngo.components.SearchBar
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.RedButton
import com.example.bcngo.model.PassportPointWrapper
import com.example.bcngo.screens.home.PermissionRationale
import com.example.bcngo.ui.theme.PrincipalRed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VirtualPassport(navController: NavController, passportViewModel: PassportViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Usamos rememberPermissionState para el permiso de ubicación
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    // Variable para controlar si la ubicación está habilitada
    var isLocationEnabled by remember { mutableStateOf(false) }

    // Comprobamos si la ubicación está habilitada en el dispositivo
    LaunchedEffect(Unit) {
        isLocationEnabled = isLocationPermissionGranted(context)
    }

    // Cuando el permiso es concedido o se intenta solicitar
    when {
        locationPermissionState.status.isGranted -> {
            LaunchedEffect(Unit) {
                passportViewModel.updatePassportPoints(context)
            }
        }

        locationPermissionState.status.shouldShowRationale -> {
            PermissionRationale(
                onRequestPermission = { locationPermissionState.launchPermissionRequest() }
            )
            isLocationEnabled = false
        }

        else -> {
            passportViewModel.updatePassportPoints(context)
            Text(stringResource(id = R.string.location_permission_required))
            isLocationEnabled = false
        }
    }
    val passportPoints by passportViewModel.passportpoints.observeAsState(emptyList())
    val wrappedPoints =
        passportPoints?.map { it.copy(point_of_interest = it.point_of_interest) }

    val nameMappings = mapOf(
        "Temple Expiatori de la Sagrada Família - Basílica" to "Sagrada Família",
        "Park Güell" to "Parc Güell",
        "Casa Batlló" to "Casa Batlló",
        "Monument Arc de Triomf de Barcelona" to "Arc de Triomf",
        "Espai Gaudí: Pis, Golfes i Terrat" to "Espai Gaudí",
        "Parc de la Ciutadella" to "Parc de la Ciutadella",
        "Santa Església Catedral Basílica de Barcelona" to "Catedral de Barcelona",
        "Castell de Montjuïc" to "Castell de Montjuïc",
        "Mercat Boqueria - Sant Josep" to "Mercat de la Boqueria",
        "La Rambla" to "La Rambla",
        "La Plaça de Catalunya" to "Plaça de Catalunya",
        "El Gòtic" to "El Gòtic",
        "Parc del Laberint d'Horta" to "Parc del Laberint d'Horta",
        "Poble Espanyol de Barcelona" to "Poble Espanyol",
        "La plaça d'Espanya" to "Plaça d'Espanya",
        "CosmoCaixa Barcelona" to "CosmoCaixa",
        "L'Anella Olímpica" to "Anella Olímpica",
        "Platja de la Barceloneta" to "Platja de la Barceloneta",
        "Hotel W Barcelona - HB-004411 *Edifici Vela" to "Hotel W Barcelona",
        "Museu Nacional d'Art de Catalunya" to "MNAC",
        "Museu d'Art Contemporani de Barcelona" to "MACBA",
        "Centre Comercial L'Illa Diagonal" to "L'Illa Diagonal",
        "Spotify Camp Nou * Tancat per remodelació" to "Camp Nou",
        "El Portal de l'Àngel" to "Portal de l'Àngel",
        "El Passeig de Gràcia" to "Passeig de Gràcia",
        "Teatre Nacional de Catalunya" to "Teatre Nacional",
        "Monument a Cristòfol Colom" to "Monument a Colom",
        "Sant Pau Recinte Modernista" to "Sant Pau",
        "Palau de la Música Catalana" to "Palau de la Música",
        "Parròquia de Santa Maria del Mar - Basílica" to "Santa Maria del Mar",
        "Moll de la Fusta" to "Moll de la Fusta",
        "Parc d'Atraccions del Tibidabo" to "Tibidabo",
        "Torre agbar" to "Torre Glòries"
    )

    // Mapea los puntos con los nombres modificados
    val filteredPoints = passportPoints?.map { point ->
        val newName =
            nameMappings[point.point_of_interest.name] ?: point.point_of_interest.name
        point.copy(point_of_interest = point.point_of_interest.copy(name = newName))
    }?.filter {
        it.point_of_interest.name.contains(
            searchQuery,
            ignoreCase = true
        ) || searchQuery.isBlank()
    }

    val buttonLabel = stringResource(id = R.string.share_passport)
    val headerText = stringResource(id = R.string.check_virtual_passport)
    val visitedHeader = stringResource(id = R.string.places_explored)
    val unvisitedHeader = stringResource(id = R.string.places_to_explore)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 50.dp)) {
            Cabezera(navController = navController)
            SearchBar(
                value = searchQuery,
                onValueChange = { query -> searchQuery = query },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            // Botón para compartir el pasaporte virtual
            RedButton(
                text = stringResource(id = R.string.share_passport),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val visitedPlaces = passportPoints?.filter { it.is_marked }
                val unvisitedPlaces = passportPoints?.filter { !it.is_marked }

                val sharedText = buildString {
                    append("🌍 $headerText\n")
                    append("📍 $visitedHeader:\n")
                    visitedPlaces?.forEach {
                        append("✅ ${it.point_of_interest.name}\n")
                    }
                    append("\n🗺️ $unvisitedHeader:\n")
                    unvisitedPlaces?.forEach {
                        append("❌ ${it.point_of_interest.name}\n")
                    }
                }

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, sharedText)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
            if (filteredPoints != null) {
                PassportGrid(points = filteredPoints, passportViewModel, isLocationEnabled = isLocationEnabled)
            }

        }
    }
}


@Composable
fun PermissionRationale(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.need_permission),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(id = R.string.request_permission))
        }
    }
}

@Composable
fun StyledSearchBar(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        label = { Text(stringResource(R.string.search_places)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp).fillMaxWidth()
    )
}

@Composable
fun PassportGrid(points: List<PassportPointWrapper>, passportViewModel: PassportViewModel, isLocationEnabled: Boolean) {
    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.padding(8.dp).fillMaxSize()) {
        items(points.size) { index ->
            val point = points[index]
            PassportItem(point, LocalContext.current, passportViewModel,isLocationEnabled = isLocationEnabled)
        }
    }
}

@Composable
fun PassportItem(point: PassportPointWrapper, context: Context, passportViewModel: PassportViewModel, isLocationEnabled: Boolean) {
    val coroutineScope = rememberCoroutineScope() // Inicia un scope de coroutines
    var currentLocation by remember { mutableStateOf<Location?>(null) }  // Obtiene la ubicación del usuario
    var isWithinRange by remember { mutableStateOf(false) }
    val message = stringResource(id = R.string.too_far)
    val locationNotEnabledMessage = stringResource(id = R.string.need_permission)

    // Obtenemos la ubicación del usuario y calculamos la distancia
    LaunchedEffect(isLocationEnabled) {
        if (isLocationEnabled) {
            val location = getCurrentLocation(context)
            currentLocation = location
            location?.let {
                val distance = calculateDistance(
                    it.latitude,
                    it.longitude,
                    point.point_of_interest.latitude,
                    point.point_of_interest.longitude
                )
                isWithinRange = distance <= 100f
            }
        } else {
            isWithinRange = false // Si la ubicación no está activada, deshabilitamos la acción
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box {
            Surface(
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                color = Color.LightGray, // Color de fondo para el rectángulo
                modifier = Modifier
                    .width(120.dp) // Ancho fijo
                    .height(150.dp) // Alto mayor para formar un rectángulo vertical
            ) {
                point.point_of_interest.imageResId?.let { imageResId ->
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = point.point_of_interest.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .padding(4.dp)
                            .clickable {
                                // Solo habilitamos la acción si el usuario está dentro del radio
                                if (!isLocationEnabled) {
                                    Toast.makeText(context, locationNotEnabledMessage, Toast.LENGTH_SHORT).show()
                                }else if (isLocationEnabled && isWithinRange) {
                                    passportViewModel.markPointAsVisited(context, point.point_of_interest.id)
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                }
            }

            // Círculo en la esquina superior derecha
            Box(
                modifier = Modifier
                    .size(34.dp) // Tamaño del círculo
                    .align(Alignment.TopEnd) // Posiciona el círculo en la esquina superior derecha
                    .offset(x = 8.dp, y = (-8).dp)  // Ajuste para que quede bien posicionado
                    .clip(CircleShape)
                    .background(Color.Gray), // Color gris para el círculo
                contentAlignment = Alignment.Center
            ) {
                if (point.is_marked) {
                    Image(
                        painter = painterResource(id = R.drawable.sitio_visitado), // Recurso de la cruz
                        contentDescription = "Cross",
                        modifier = Modifier.size(34.dp) // Tamaño de la cruz
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(80.dp)
                .background( color = if (point.is_marked) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp))
                .padding(2.dp) // Márgenes internos
        ) {
            Text(
                text = point.point_of_interest.name,
                color = Color.White, // Color blanco para el texto
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, // Centrado
                maxLines = 2
            )
        }
    }
}

suspend fun getCurrentLocation(context: Context): Location? {
    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        return null
    }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return try {
        fusedLocationClient.lastLocation.await()
    } catch (e: SecurityException) {
        null
    }
}

// Función para calcular la distancia entre dos puntos geográficos
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double?, lon2: Double?): Float {
    val location1 = Location("point1").apply {
        latitude = lat1
        longitude = lon1
    }
    val location2 = Location("point2").apply {
        if (lat2 != null) {
            latitude = lat2
        }
        if (lon2 != null) {
            longitude = lon2
        }
    }
    return location1.distanceTo(location2) // Devuelve la distancia en metros
}

fun isLocationPermissionGranted(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
}

@Preview(showBackground = true)
@Composable
fun PreviewVirtualPassport() {
    VirtualPassport(navController = rememberNavController())
}