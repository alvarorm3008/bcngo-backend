package com.example.bcngo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bcngo.R
import com.example.bcngo.model.Message
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun ChatBubble(
    message: Message,
    isCurrentUser: Boolean,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit
) {
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    var expanded by remember { mutableStateOf(false) }

    // Define rounded shape with shadow
    val bubbleShape = RoundedCornerShape(16.dp)

    // Apply a drop shadow and slight padding for a floating effect
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { expanded = !expanded }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = bubbleShape,
            color = backgroundColor,
            modifier = Modifier
                .padding(8.dp)
                .widthIn(min = 100.dp, max = 250.dp)
                .shadow(8.dp, shape = bubbleShape)  // Shadow for a floating effect
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // User name with enhanced typography
                Text(
                    text = message.creator_name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                // Message content with adjusted font size and line height
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                // Timestamp (relative date and exact time)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Hour of the message (normal time format)
                    Text(
                        text = message.created_at.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 10.sp,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
        // Show options (Delete/Report) only when expanded
        if (expanded) {
            if (isCurrentUser) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable {
                            onDeleteClick()
                            expanded = false
                        }
                        .padding(start = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.report),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable {
                            onReportClick()
                            expanded = false
                        }
                        .padding(start = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                )
            }
        }
    }
}

@Composable
fun getRelativeTimeString(createdAt: LocalDateTime): String {
    val now = LocalDateTime.now()
    val daysBetween = ChronoUnit.DAYS.between(createdAt, now)

    val currentLocale = Locale.getDefault()

    return when {
        daysBetween == 0L -> stringResource(id = R.string.today) // Hoy
        daysBetween == 1L -> stringResource(id = R.string.yesterday) // Ayer
        daysBetween < 7L -> stringResource(id = R.string.days_ago, daysBetween.toInt()) // X días atrás
        else -> {
            // Obtener el formato de la fecha desde los recursos
            val dateFormat = stringResource(id = R.string.date_format)
            val formatter = DateTimeFormatter.ofPattern(dateFormat, currentLocale)
            createdAt.format(formatter) // Fecha con localización
        }
    }
}
