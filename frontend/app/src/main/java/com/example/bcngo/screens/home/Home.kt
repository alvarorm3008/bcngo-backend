package com.example.bcngo.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.network.InterestPointsListViewModel
import com.example.bcngo.screens.searchpoints.BuscadorViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.bcngo.model.BicingStop
import com.example.bcngo.model.BusStop
import com.example.bcngo.model.MetroStop
import com.example.bcngo.navigation.Routes
import com.example.bcngo.ui.theme.BCNGOTheme
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import com.example.bcngo.components.ViewMoreButton
import com.google.android.gms.maps.model.BitmapDescriptor


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Home(
    navController: NavController,
    buscadorViewModel: BuscadorViewModel = viewModel(),
    pointsViewModel: PointsViewModel = viewModel(),
    pointViewModel: PointViewModel = viewModel()
) {
    BCNGOTheme(darkIcons = false) {
        val locationPermissionState =
            rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

        val hasLocationPermission = locationPermissionState.status.isGranted

        // Solicitamos el permiso solo si no ha sido concedido aún
        LaunchedEffect(Unit) {
            if (!locationPermissionState.status.isGranted && !locationPermissionState.status.shouldShowRationale) {
                locationPermissionState.launchPermissionRequest()
            }
        }

        // En cualquier caso, mostraremos el mapa y dejaremos habilitar la búsqueda de puntos.
        when {
            // Si tenemos el permiso de ubicación
            hasLocationPermission -> {
                HomeContent(
                    navController,
                    buscadorViewModel,
                    pointsViewModel,
                    pointViewModel,
                    enableUserLocation = true
                )
            }

            // Si debe mostrar la justificación del permiso
            locationPermissionState.status.shouldShowRationale -> {
                PermissionRationale(
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() }
                )
            }

            // Si el permiso no está concedido o el usuario ha denegado permanentemente
            else -> {
                HomeContent(
                    navController,
                    buscadorViewModel,
                    pointsViewModel,
                    pointViewModel,
                    enableUserLocation = false
                )
            }
        }
    }
}


@SuppressLint("MissingPermission") // Nos aseguramos de que se verifique el permiso antes de usar
@Composable
fun HomeContent(
    navController: NavController,
    buscadorViewModel: BuscadorViewModel = viewModel(),
    pointsViewModel: PointsViewModel = viewModel(),
    pointViewModel: PointViewModel,
    transportViewModel: TransportViewModel = viewModel(),
    enableUserLocation: Boolean

) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estado para la ubicación del usuario
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentZoom by remember { mutableStateOf(15f) }
    var hasMovedToUserLocation by remember { mutableStateOf(false) }

    // Estado para los puntos de interés filtrados y el seleccionado
    var filteredPoints by remember { mutableStateOf<List<InterestPoint>>(emptyList()) }
    var selectedPoint by remember { mutableStateOf<InterestPoint?>(null) }

    // Observar los puntos cercanos desde el ViewModel
    val nearbyPoints by pointsViewModel.nearbyPoints.observeAsState(emptyList())

    // Obtener puntos de interés desde el ViewModel
    val interestPoints by InterestPointsListViewModel()

    // Estado del buscador
    val searchState = buscadorViewModel.search.observeAsState("")
    val searchQuery = searchState.value


    // Estados para los puntos de transporte
    val busStops by transportViewModel.busStops.observeAsState(emptyList())
    val metroStops by transportViewModel.metroStops.observeAsState(emptyList())
    val bikeStations by transportViewModel.bikeStations.observeAsState(emptyList())

    val selectedTransport = remember { mutableStateOf<Any?>(null) }
    val isTransportCardVisible by transportViewModel.isTransportCardVisible.observeAsState(false)

    val message = stringResource(id = R.string.no_results_found)

    // Filtrar puntos según la búsqueda
    LaunchedEffect(searchQuery) {
        filteredPoints =
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                interestPoints?.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                } ?: emptyList()
            }
        if (searchQuery.isNotBlank() && filteredPoints.isEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        // Deseleccionar el punto de interés actual al realizar una nueva búsqueda
        selectedPoint = null
    }

    // Obtener la ubicación del usuario
    if (enableUserLocation) {
        DisposableEffect(Unit) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50)
                .setMinUpdateIntervalMillis(60000)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)
                        pointsViewModel.updateUserLocation(userLocation!!)
                        transportViewModel.fetchTransportPoints(context, userLocation!!)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

            onDispose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    var hasCenteredOnUserLocation by remember { mutableStateOf(false) }
    var cameraPositionState = rememberCameraPositionState()

    // Inicializar la cámara solo al principio
    val initialPosition = userLocation ?: LatLng(41.3851, 2.1734)  // Si no hay ubicación, usar una ubicación predeterminada
    cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }

    // Cámara se mueve solo al inicio, no depende de la ubicación después de eso
    LaunchedEffect(userLocation) {
        if (userLocation != null && !hasCenteredOnUserLocation) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f))
            hasCenteredOnUserLocation = true
        }
    }


    // Mover la cámara según búsqueda o selección
    LaunchedEffect(userLocation, searchQuery, filteredPoints) {
        if (searchQuery.isNotBlank() && filteredPoints.size == 1) {
            val point = filteredPoints.first()
            val latLng = LatLng(point.latitude!!.toDouble(), point.longitude!!.toDouble())
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom))
            hasMovedToUserLocation = false
        }
    }

    // Contenido principal
    Column(modifier = Modifier.fillMaxSize()) {
        Cabezera(navController)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()) // Solo abajo
        ){
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = enableUserLocation),
                onMapClick = {
                    selectedPoint = null
                    selectedTransport.value = null

                }
            ) {
                val pointsToDisplay = if (searchQuery.isBlank()) nearbyPoints else filteredPoints

                pointsToDisplay.forEach { point ->
                    val latitude = point.latitude
                    val longitude = point.longitude
                    if (latitude != null && longitude != null) {
                        Marker(
                            state = MarkerState(
                                position = LatLng(latitude.toDouble(), longitude.toDouble()),
                            ),
                            icon = getBitmapFromDrawable(context, R.drawable.point_interest_marker, 120, 160),

                            title = point.name,
                            snippet = point.address_name,
                            onClick = {
                                if (selectedPoint == point) {
                                    selectedPoint = null
                                    true
                                } else {
                                    selectedPoint = point
                                    true
                                }
                            },
                        )
                    }
                }
                //Marcadores de paradas de bus
                busStops.forEach { location ->
                    val latitude = location.latitud
                    val longitude = location.longitud
                    Marker(
                        state = MarkerState(position = LatLng(latitude, longitude)),
                        icon = getBitmapFromDrawable(context, R.drawable.bus_stop_marker, 100, 140),
                        title = stringResource(id = R.string.bus_stop),
                        snippet = stringResource(id = R.string.bus_stop),
                        onClick = {
                            if (selectedTransport.value == location) {
                                selectedTransport.value = null
                            } else {
                                selectedPoint = null
                                selectedTransport.value = location
                                transportViewModel.setTransportCardVisible(true)
                            }
                            true
                        }
                    )
                }

                // Marcadores de paradas de metro
                metroStops.forEach { location ->
                    val latitude = location.latitud
                    val longitude = location.longitud
                    Marker(
                        state = MarkerState(position = LatLng(latitude, longitude)),
                        icon = getBitmapFromDrawable(context, R.drawable.metro_stop_marker, 110, 150),
                        title = stringResource(id = R.string.metro_stop),
                        snippet = stringResource(id = R.string.metro_stop),
                        onClick = {
                            if (selectedTransport.value == location) {
                                selectedTransport.value = null
                            } else {
                                selectedPoint = null
                                selectedTransport.value = location
                                transportViewModel.setTransportCardVisible(true)
                            }
                            true
                        }
                    )
                }

                // Marcadores de estaciones de bicis
                bikeStations.forEach { location ->
                    val latitude = location.latitud
                    val longitude = location.longitud
                    Marker(
                        state = MarkerState(position = LatLng(latitude, longitude)),
                        icon = getBitmapFromDrawable(context, R.drawable.bike_stop_marker, 90, 130),
                        title = stringResource(id = R.string.bike_stop),
                        snippet = stringResource(id = R.string.bike_stop),
                        onClick = {
                            if (selectedTransport.value == location) {
                                selectedTransport.value = null
                            } else {
                                selectedPoint = null
                                selectedTransport.value = location
                                transportViewModel.setTransportCardVisible(true)
                            }
                            true
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)  // Margen izquierdo y derecho
            ) {
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchChange = { buscadorViewModel.onSearchChange(it) },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                )
                if(searchQuery == null){
                    selectedTransport.value = false
                    selectedPoint = null
                }
                // Mostrar información del punto seleccionado
                if (selectedPoint != null) {
                    InfoCard(
                        point = selectedPoint!!,
                        onClose = { selectedPoint = null },
                        navController = navController,
                        setTransportCardVisible = transportViewModel::setTransportCardVisible,
                        selectedTransport = selectedTransport,
                        pointViewModel = pointViewModel
                    )
                }
                if (isTransportCardVisible) {
                    selectedTransport.value?.let { transport ->
                        when (transport) {
                            is BusStop -> InfoCardBus(
                                busStop = transport,
                                onClose = {
                                    selectedTransport.value = null
                                    transportViewModel.setTransportCardVisible(false)
                                },
                                isTransportCardVisible = isTransportCardVisible,
                                setTransportCardVisible = transportViewModel::setTransportCardVisible,
                                selectedTransport = selectedTransport
                            )

                            is MetroStop -> InfoCardMetro(
                                metroStop = transport,
                                onClose = {
                                    selectedTransport.value = null
                                    transportViewModel.setTransportCardVisible(false)
                                },
                                isTransportCardVisible = isTransportCardVisible,
                                setTransportCardVisible = transportViewModel::setTransportCardVisible,
                                selectedTransport = selectedTransport
                            )

                            is BicingStop -> InfoCardBike(
                                bikeStop = transport,
                                onClose = {
                                    selectedTransport.value = null
                                    transportViewModel.setTransportCardVisible(false)
                                },
                                isTransportCardVisible = isTransportCardVisible,
                                setTransportCardVisible = transportViewModel::setTransportCardVisible,
                                selectedTransport = selectedTransport
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { navController.navigate("calendar_itinerary_screen") },
                modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 55.dp)
                    .padding(bottom = 16.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(25.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.create_your_itinerary),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}


@Composable
fun InfoCard(
    point: InterestPoint,
    onClose: () -> Unit,
    navController: NavController,
    setTransportCardVisible: (Boolean) -> Unit,
    selectedTransport: MutableState<Any?>,
    pointViewModel: PointViewModel
) {
    setTransportCardVisible(false)
    selectedTransport.value = null
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF910811)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // Botón para cerrar la tarjeta
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = point.name,
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onClose) { // Cerrar la tarjeta al hacer clic
                        Icon(
                            imageVector = Icons.Default.Close, // Cambia a un ícono de cruz
                            contentDescription = stringResource(id = R.string.close),
                            tint = Color.White,
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(8.dp))

                val addressDisplay =
                    when {
                        point.address_name == null -> "-" // Si address_name es null
                        point.street_number == null -> point.address_name // Si solo street_number es null
                        else -> "${point.address_name}, ${point.street_number}" // Si ambos tienen valor
                    }

                Text(
                    stringResource(id = R.string.address) + " $addressDisplay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                Text(
                    stringResource(id = R.string.category) + " ${point.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                Text(
                    stringResource(id = R.string.phone) + " ${point.phone ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                val context = LocalContext.current
                Text(
                    buildAnnotatedString {
                        append(stringResource(id = R.string.website)) // Texto normal
                        append(" ")
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(point.web_url ?: "-") // URL en cursiva
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.clickable(enabled = point.web_url != null) {
                        point.web_url?.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                )

                if (point.esencial) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.essential_point),
                        style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFF9A825),
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
                ViewMoreButton(onClick = {
                    navController.navigate("details_point_screen/${point.id}")
                })
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "clear_search"
                    )
                }
            }
        },
        placeholder = {
            Text(stringResource(id = R.string.search_points_of_interest))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            },
        ),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .heightIn(min = 56.dp)
    )
}


@Composable
fun PermissionRationale(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.location_permission_rationale),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(id = R.string.grant_permission))
        }
    }
}

fun getBitmapFromDrawable(context: Context, drawableId: Int, width: Int, height: Int): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    val bitmap = (drawable as BitmapDrawable).bitmap
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
    return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
}
