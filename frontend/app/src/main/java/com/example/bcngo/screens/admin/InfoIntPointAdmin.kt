package com.example.bcngo.screens.admin

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bcngo.R
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.ui.theme.PrincipalRed

@Composable
fun InfoIntPointAdmin(
    point: InterestPoint,
    onModifyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = PrincipalRed,
        ),
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
            ) {
                Text(
                    color = Color.White,
                    text = point.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                )
                val addressDisplay = when {
                    point.address_name == null -> "-" // Si address_name es null
                    point.street_number == null -> point.address_name // Si solo street_number es null
                    else -> "${point.address_name}, ${point.street_number}" // Si ambos tienen valor
                }
                Text(
                    stringResource(id = R.string.address_display, addressDisplay),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                val phone = point.phone ?: "-"
                Text(
                    stringResource(id = R.string.phone_display, phone),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                val web = point.web_url ?: "-"
                Text(
                    stringResource(id = R.string.website_display, web),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    stringResource(id = R.string.category_display, point.category),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }

            // Botones de acción
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete, // Asegúrate de usar el icono adecuado
                        tint = Color.White,
                        contentDescription = "Delete Point",
                    )
                }
            }
        }
    }
}
