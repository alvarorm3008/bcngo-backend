package com.example.bcngo.screens.home

import android.provider.Settings.Global.getString
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.res.stringResource
import com.example.bcngo.R
import com.example.bcngo.model.BicingStop

@Composable
fun InfoCardBike(
    bikeStop: BicingStop,
    onClose: () -> Unit,
    isTransportCardVisible: Boolean,
    setTransportCardVisible: (Boolean) -> Unit,
    selectedTransport: MutableState<Any?>
) {
    val details = listOf(
        stringResource(R.string.bike_stop_name) to bikeStop.nom,
        stringResource(R.string.bike_stop_address) to bikeStop.adreca,
        stringResource(R.string.bike_stop_capacity) to bikeStop.capacitat.toString(),
        stringResource(R.string.bike_stop_available) to "${bikeStop.nDisponibles} ${stringResource(R.string.bike_stop_mechanical_available)}, ${bikeStop.nElectriquesDisponibles} ${stringResource(R.string.bike_stop_electric_available)}",
        stringResource(R.string.bike_stop_docks_available) to bikeStop.nDocksDisponibles.toString()
    )
    InfoCardTransport(
        title = stringResource(id = R.string.bike_stop),
        details = details,
        onClose = onClose,
        isTransportCardVisible = isTransportCardVisible,
        setTransportCardVisible = setTransportCardVisible,
        selectedTransport = selectedTransport
    )
}

