package com.example.bcngo.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bcngo.R
import com.example.bcngo.components.RedButton
import com.example.bcngo.navigation.Routes
import com.example.bcngo.ui.theme.BCNGOTheme

@Composable
fun Welcome(navController: NavController) {
    BCNGOTheme(darkIcons = true) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondowelcome),
                contentDescription = "BACKGROUND IMAGE",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 1.5f),
                                Color.Transparent,
                            ),
                        ),
                    )
                    .blur(10.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 1.5f),
                            ),
                        ),
                    )
                    .blur(10.dp),
            )

            // Imagen del logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "LOGO BCNGO",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize(0.8f)
                    .offset(y = (-80).dp),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp), // Increased bottom padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Botón de Login
                RedButton(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    text = stringResource(id = R.string.log_in_text_cap),
                    onClick = { navController.navigate(Routes.LogIn.route) },
                )

                // Texto con enlace a "Sign Up"
                Row(
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = stringResource(id = R.string.dont_have_account), color = Color.Black)
                    val annotatedString = buildAnnotatedString {
                        append(stringResource(id = R.string.sign_up_text))
                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            ),
                            start = 0,
                            end = length
                        )
                        addStringAnnotation(
                            tag = "URL",
                            annotation = "sign_up",
                            start = 0,
                            end = length
                        )
                    }
                    Text(
                        text = annotatedString,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable {
                                navController.navigate(Routes.SignUp.route)
                            },
                        style = LocalTextStyle.current
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePreview() {
    val navController = rememberNavController()
    BCNGOTheme(darkIcons = true) {
        Welcome(navController)
    }
}