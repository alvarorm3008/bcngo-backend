package com.example.bcngo.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bcngo.R

@Composable
fun ViewMoreButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary, // El mismo color rojo de tu tema
            contentColor = Color.White // Color blanco para el texto
        ),
        shape = RoundedCornerShape(50), // Bordes redondeados
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .height(48.dp), // Aumentar la altura para hacerlo más accesible
    ) {
        Text(
            text = stringResource(id = R.string.view_more), // Texto del botón
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp, // Tamaño de texto adecuado
            modifier = Modifier.padding(start = 16.dp, end = 16.dp) // Agregar un poco de espacio en los lados
        )
    }
}