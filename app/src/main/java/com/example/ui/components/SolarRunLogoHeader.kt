package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SolarRunLogoHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // High-contrast clean styling for SOLAR
        Text(
            text = "SOLAR",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp
        )
        // High-speed energetic Amber coloring for RUN matching the site logo theme
        Text(
            text = "RUN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFB300), // SolarRun Signature Amber
            letterSpacing = 2.sp
        )
    }
}
