package com.example.bcngo.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcngo.R
import com.example.bcngo.ui.theme.Background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding

@Composable
fun LoginScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val image = painterResource(R.drawable.background2)
    val image2 = painterResource(R.drawable.logonegro)
    Box(
        modifier =
        modifier
            .fillMaxSize()
            .background(Background)
            //.windowInsetsPadding(WindowInsets.safeDrawing)
            //.windowInsetsPadding(WindowInsets.systemBars),
    ) {
        // Imagen de fondo
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.6F,
            modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
        )
        // Logo
        Image(
            painter = image2,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .scale(1.5f),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier =
            Modifier
                .fillMaxSize()
                .padding(25.dp)
                .systemBarsPadding()
        ) {
            // Rectángulo blanco
            Box(
                modifier =
                Modifier
                    .width(335.dp)
                    .height(550.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White),
            ) {
                content()
            }
        }
    }
}

