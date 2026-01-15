package com.example.reportebarreras.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

    // 1. Obtenemos los colores del tema actual
    val azulInstitucional = MaterialTheme.colorScheme.secondary
    val colorTextoBoton = Color.White // Mantenemos blanco para contraste sobre azul

    // 2. Definimos el fondo dinámico
    val backgroundColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) // Gris neutro del tema
        isPressed -> azulInstitucional.copy(alpha = 0.7f)
        else -> azulInstitucional
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            contentColor = colorTextoBoton,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        // El borde ahora también es dinámico según el estado
        border = BorderStroke(1.5.dp, if (enabled) azulInstitucional else Color.Transparent),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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

    // Colores adaptables
    val textColor = MaterialTheme.colorScheme.onBackground
    val borderColor = textColor.copy(alpha = 0.5f)
    val cursorColor = MaterialTheme.colorScheme.primary

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
        cursorBrush = SolidColor(cursorColor),
        textStyle = TextStyle(color = textColor, fontSize = 16.sp),
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .drawBehind {
                val stroke = 1.5f * density
                drawLine(
                    color = borderColor,
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
                        tint = borderColor,
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
                            color = textColor.copy(alpha = 0.4f),
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
                        tint = borderColor,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 8.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { passwordVisible = !passwordVisible }
                    )
                }
            }
        }
    )
}