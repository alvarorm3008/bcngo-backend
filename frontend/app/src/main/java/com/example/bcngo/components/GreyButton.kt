package com.example.bcngo.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bcngo.R
import com.example.bcngo.ui.theme.Background
import com.example.bcngo.ui.theme.TextColor

@Composable
fun GreyButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 30.dp)
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
    ) {
        Text(
            text = stringResource(id = R.string.back_button_text),
            color = MaterialTheme.colorScheme.onSecondary,
        )
    }
}