package com.example.bcngo.screens.calendarItinerary

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bcngo.R
import com.example.bcngo.components.Cabezera
import com.example.bcngo.components.GreyButton
import com.example.bcngo.components.ProgressBar
import com.example.bcngo.components.RedButton
import com.example.bcngo.navigation.Routes
import com.example.bcngo.ui.theme.PrincipalRed
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import androidx.compose.material3.DatePickerDefaults



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CalendarItinerary(
    navController: NavHostController,
    infoItineraryViewModel: InfoItineraryViewModel,
) {
    val todayInMillis = remember { System.currentTimeMillis() }

    var arrivalDateInMillis by remember { mutableStateOf<Long?>(null) }
    var returnDateInMillis by remember { mutableStateOf<Long?>(null) }
    var itineraryName by remember { mutableStateOf("") }
    var showDateRangePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            //.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
                .background(Color.White)
        ) {
            Cabezera(navController)
            ProgressBar(modifier = Modifier.padding(top = 16.dp), highlightedIndex = 0)

            Column(modifier = Modifier
                .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.name_your_itinerary),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                OutlinedTextField(
                    value = itineraryName,
                    onValueChange = { itineraryName = it },
                    label = { Text(stringResource(id = R.string.enter_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                )

                Text(
                    text = stringResource(id = R.string.select_your_days),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                if (arrivalDateInMillis != null && returnDateInMillis != null) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        OutlinedTextField(
                            value = arrivalDateInMillis?.let { convertMillisToDate(it) } ?: "",
                            onValueChange = {},
                            label = { Text(stringResource(id = R.string.arrival)) },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = stringResource(id = R.string.arrival),
                                )
                            },
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = returnDateInMillis?.let { convertMillisToDate(it) } ?: "",
                            onValueChange = {},
                            label = { Text(stringResource(id = R.string.return_text)) },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = stringResource(id = R.string.return_text),
                                )
                            },
                        )
                    }
                }
                RedButton(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    text = stringResource(id = R.string.select_date_range),
                    onClick = { showDateRangePicker = true },
                )
            }
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
            Button(
                onClick = {
                    val startDate = convertMillisToLocalDate(arrivalDateInMillis!!)
                    val endDate = convertMillisToLocalDate(returnDateInMillis!!)
                    infoItineraryViewModel.setItinerary(
                        name = itineraryName,
                        start = startDate,
                        end = endDate,
                    )
                    navController.navigate(Routes.OptionItinerary.route)
                },
                enabled = itineraryName.isNotBlank() && arrivalDateInMillis != null && returnDateInMillis != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (itineraryName.isNotBlank() && arrivalDateInMillis != null && returnDateInMillis != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 30.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
            ) {
                Text(stringResource(id = R.string.continue_button_text), color = Color.White)
            }
        }
    }

    if (showDateRangePicker) {
        DateRangePickerModal(
            onDateRangeSelected = { selectedRange ->
                arrivalDateInMillis = selectedRange.first
                returnDateInMillis = selectedRange.second
            },
            onDismiss = { showDateRangePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit,
    maxDaysRange: Int = 15 // Número máximo de días entre la llegada y la vuelta

) {
    val dateRangePickerState = rememberDateRangePickerState()
    val todayInMillis = System.currentTimeMillis()
    val context = LocalContext.current // Obtener el contexto para el Toast

    val selectedStartDate = remember { mutableStateOf<Long?>(null) }
    val selectedEndDate = remember { mutableStateOf<Long?>(null) }

    val errorTooEarly = stringResource(R.string.error_date_too_early)
    val errorRangeExceeded = stringResource(R.string.error_date_range_exceeded, maxDaysRange)

    LaunchedEffect(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        val startDate = dateRangePickerState.selectedStartDateMillis
        val endDate = dateRangePickerState.selectedEndDateMillis
        if (startDate != null && startDate < todayInMillis - (1000 * 60 * 60 * 24) ||
            endDate != null && endDate < todayInMillis - (1000 * 60 * 60 * 24)) {
            // Mostrar Toast si la fecha seleccionada es anterior al día actual
            Toast.makeText(
                context,
                errorTooEarly,
                Toast.LENGTH_SHORT
            ).show()
        }
        if (startDate != null && endDate != null) {
            val daysDifference = (endDate - startDate) / (1000 * 60 * 60 * 24) // Diferencia en días
            if (daysDifference > maxDaysRange) {
                // Si la diferencia de días es mayor al máximo permitido, ajustar la fecha de regreso
                Toast.makeText(
                    context,
                    errorRangeExceeded,
                    Toast.LENGTH_SHORT
                ).show()

                // Ajustar la fecha de regreso si excede el límite (actualizamos el estado mutable)
                selectedEndDate.value = startDate?.plus(maxDaysRange * (1000 * 60 * 60 * 24).toLong())
            } else {
                // Si la diferencia de días es válida, actualizamos el estado mutable con las fechas seleccionadas
                selectedStartDate.value = startDate
                selectedEndDate.value = endDate
            }
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startDate = dateRangePickerState.selectedStartDateMillis
                    val endDate = dateRangePickerState.selectedEndDateMillis
                    val todayStartOfDay = todayInMillis - (todayInMillis % (1000 * 60 * 60 * 24)) // Inicio del día actual
                    if (startDate != null && endDate != null && startDate >= todayStartOfDay) {
                        onDateRangeSelected(Pair(startDate, endDate))
                        onDismiss()
                    }
                },
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title={},
            showModeToggle = false,
        )
    }
}



fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun convertMillisToLocalDate(millis: Long): LocalDate {
    val instant = Instant.ofEpochMilli(millis)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return LocalDate(localDate.year, localDate.monthValue, localDate.dayOfMonth)
}

@Preview(showBackground = true)
@Composable
fun CalendarItineraryPreview() {
    val navController = rememberNavController()
    val infoItineraryViewModel = InfoItineraryViewModel()

    CalendarItinerary(
        navController = navController,
        infoItineraryViewModel = infoItineraryViewModel
    )
}