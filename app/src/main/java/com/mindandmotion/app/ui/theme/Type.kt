package com.mindandmotion.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography() vine deja cu valori M3 sensibile pentru toate stilurile;
 * suprascriem doar headlineSmall, folosit pe titlurile ecranelor principale
 * (ex: AboutScreen, headerul calendarului din Journal).
 */
val MindAndMotionTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    )
)
