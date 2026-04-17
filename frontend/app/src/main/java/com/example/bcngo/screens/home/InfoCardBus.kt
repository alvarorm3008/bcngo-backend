package com.example.bcngo.screens.home

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
import androidx.compose.ui.res.stringResource
import com.example.bcngo.R
import com.example.bcngo.model.BusStop

@Composable
fun InfoCardBus(
    busStop: BusStop,
    onClose: () -> Unit,
    isTransportCardVisible: Boolean,
    setTransportCardVisible: (Boolean) -> Unit,
    selectedTransport: MutableState<Any?>
) {
    val details = listOf(
        stringResource(R.string.bus_stop_name) to busStop.nomParada,
        stringResource(R.string.bus_stop_code) to busStop.codiParada.toString(),
        stringResource(R.string.bus_stop_lines) to busStop.linies.joinToString(", ")
    )
    InfoCardTransport(
        title = stringResource(id = R.string.bus_stop),
        details = details,
        onClose = onClose,
        isTransportCardVisible = isTransportCardVisible,
        setTransportCardVisible = setTransportCardVisible,
        selectedTransport = selectedTransport
    )
}