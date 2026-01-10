package com.example.reportebarreras.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButtonWithEffect(onClick: () -> Unit, text: String, enabled: Boolean = true) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Definimos el color de fondo basado en si está presionado o deshabilitado
    val backgroundColor = when {
        !enabled -> Color(0xFFBDBDBD) // Gris si está cargando/deshabilitado
        isPressed -> Color(0xFFE6E7E8)
        else -> Color(0xFF6496D1)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color(0xFFBDBDBD)
        ),
        border = BorderStroke(1.5.dp, Color(0xFF6496D1)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            color = if (enabled) Color.Black else Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun BottomBorderInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    isEmail: Boolean = false,
    heightDp: Dp = 56.dp,
    innerPaddingDp: Dp = 8.dp,
    leadingIcon: ImageVector? = null
    ) {
    var passwordVisible by remember { mutableStateOf(false) }
    val visual: VisualTransformation =
        if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None

    BasicTextField(
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                isPassword -> KeyboardType.Password
                isEmail -> KeyboardType.Email
                else -> KeyboardType.Text
            }
        ),
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        visualTransformation = visual,
        cursorBrush = SolidColor(Color.Black),
        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .drawBehind {
                val stroke = 1.5f * density
                drawLine(
                    color = Color.Black.copy(alpha = 0.6f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = stroke
                )
            },
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = innerPaddingDp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }

                if (isPassword && value.isNotEmpty()) {
                    val eyeIcon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = eyeIcon,
                        contentDescription = "Mostrar/ocultar contraseña",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 8.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                }
            }
        }
    )
}