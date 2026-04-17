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
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.res.stringResource
import com.example.bcngo.R
import com.example.bcngo.model.MetroStop

@Composable
fun InfoCardMetro(
    metroStop: MetroStop,
    onClose: () -> Unit,
    isTransportCardVisible: Boolean,
    setTransportCardVisible: (Boolean) -> Unit,
    selectedTransport: MutableState<Any?>
) {
    val details = listOf(
        stringResource(R.string.metro_stop_name) to metroStop.nom,
        stringResource(R.string.metro_stop_lines) to metroStop.liniesMetro.joinToString(", ")
    )
    InfoCardTransport(
        title = stringResource(id = R.string.metro_stop),
        details = details,
        onClose = onClose,
        isTransportCardVisible = isTransportCardVisible,
        setTransportCardVisible = setTransportCardVisible,
        selectedTransport = selectedTransport
    )
}
