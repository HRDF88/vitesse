package com.example.vitesse.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColor
import com.example.vitesse.R

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@RequiresApi(Build.VERSION_CODES.O)
val Primary = R.color.primary.toColor()
@RequiresApi(Build.VERSION_CODES.O)
val PrimaryVariant = R.color.primary_variant.toColor()
@RequiresApi(Build.VERSION_CODES.O)
val OnPrimary = R.color.on_primary.toColor()
@RequiresApi(Build.VERSION_CODES.O)
val Secondary = R.color.secondary.toColor()
@RequiresApi(Build.VERSION_CODES.O)
val SecondaryVariant = R.color.secondary_variant.toColor()
@RequiresApi(Build.VERSION_CODES.O)
val OnSecondary = R.color.on_secondary.toColor()
