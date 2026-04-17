package com.example.bcngo.screens.searchpoints

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bcngo.R
import com.example.bcngo.components.ViewMoreButton
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.ui.theme.PrincipalRed
import com.example.bcngo.navigation.Routes
import com.google.maps.android.compose.MapEffect

@Composable
fun InfoIntPoint(
    point: InterestPoint,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        modifier =
            modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth(),
    ) {
        Log.d("ListIntPoints", "point: $point")
        CardContent(point, navController)
    }
}

@Composable
fun CardContent(point: InterestPoint, navController: NavController) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .padding(12.dp)
                .animateContentSize(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(12.dp),
        ) {
            Text(
                color = Color.White,
                text = point.name,
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
            )
            if (expanded) {
                val addressDisplay =
                    when {
                        point.address_name == null -> "-" // Si address_name es null
                        point.street_number == null -> point.address_name // Si solo street_number es null
                        else -> "${point.address_name}, ${point.street_number}" // Si ambos tienen valor
                    }
                Text(
                    stringResource(id = R.string.address) + " $addressDisplay",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    stringResource(id = R.string.category) + " ${point.category}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    stringResource(id = R.string.phone) + " ${point.phone ?: stringResource(R.string.no_phone_available)}",
                    style = MaterialTheme.typography.titleMedium,
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
                ViewMoreButton(onClick = {
                    navController.navigate("details_point_screen/${point.id}")
                })

            }
        }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Filled.ExpandLess else Filled.ExpandMore,
                tint = Color.White,
                contentDescription =
                    if (expanded) {
                        stringResource(id = R.string.show_less)
                    } else {
                        stringResource(id = R.string.show_more)
                    },
            )
        }
    }
}
