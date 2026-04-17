package com.example.bcngo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.bcngo.ui.theme.PrincipalRed
import com.example.bcngo.ui.theme.Background

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    highlightedIndex: Int, // Índice que se desea resaltar
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (i in 0..3) {
            Box(
                modifier =
                    Modifier
                        .weight(1f) // Todos los bloques tienen el mismo ancho
                        .height(15.dp) // Altura del bloque
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            if (i == highlightedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        ),
            )
        }
    }
}
