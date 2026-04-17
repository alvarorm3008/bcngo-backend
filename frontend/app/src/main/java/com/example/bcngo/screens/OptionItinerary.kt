package com.example.bcngo.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.GreyButton
import com.example.bcngo.components.ProgressBar
import com.example.bcngo.components.RedButton
import com.example.bcngo.navigation.Routes
import com.example.bcngo.utils.AuthManager

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OptionItinerary(navController: NavHostController, auth: AuthManager) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
        ) {
            Cabezera(navController)
            ProgressBar(modifier = Modifier.padding(top = 16.dp), highlightedIndex = 1)
            Row {
                Text(
                    text = stringResource(id = R.string.choose_option),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(16.dp),
                )
            }
            Row {
                Text(
                    text = stringResource(id = R.string.choose_each_place),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(30.dp),
                )
            }
            Row {
                RedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.create_itinerary_manually),
                    onClick = { navController.navigate(Routes.ManualItinerary.route) },
                )
            }
            Row {
                Text(
                    text = stringResource(id = R.string.choose_preferences),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(30.dp),
                )
            }
            Row {
                RedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.create_itinerary_automatically),
                    onClick = { navController.navigate(Routes.AuthomaticItinerary.route) },
                )
            }
            Spacer(modifier = Modifier.height(250.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GreyButton(
                onClick = { navController.popBackStack() },
            )
        }
    }
}
