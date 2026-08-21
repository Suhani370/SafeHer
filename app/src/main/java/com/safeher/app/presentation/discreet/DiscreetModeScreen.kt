package com.safeher.app.presentation.discreet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiscreetModeScreen(
    onExitDiscreetMode: () -> Unit
) {
    var displayValue by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    val secretPin = "9999"

    fun onDigit(d: String) {
        if (displayValue == "0" || displayValue == "Error") {
            displayValue = d
        } else {
            displayValue += d
        }
    }

    fun onOp(op: String) {
        expression = "$displayValue $op "
        displayValue = "0"
    }

    fun onEquals() {
        if (displayValue == secretPin) {
            onExitDiscreetMode()
            return
        }

        try {
            val parts = expression.trim().split(" ")
            if (parts.size == 2) {
                val num1 = parts[0].toDoubleOrNull() ?: 0.0
                val num2 = displayValue.toDoubleOrNull() ?: 0.0
                val res = when (parts[1]) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "×" -> num1 * num2
                    "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                    else -> num2
                }
                displayValue = if (res.isNaN()) "Error" else if (res % 1 == 0.0) res.toInt().toString() else res.toString()
                expression = ""
            }
        } catch (e: Exception) {
            displayValue = "Error"
        }
    }

    fun onClear() {
        displayValue = "0"
        expression = ""
    }

    Scaffold(
        containerColor = Color(0xFF1E1E1E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Secret gesture area: Long press on title to exit as backup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onExitDiscreetMode() }
                        )
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Calculator",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Display Screen
            Text(
                text = expression,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = displayValue,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                color = Color.White,
                fontWeight = FontWeight.Light,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad
            val buttons = listOf(
                listOf("C", "±", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        val isOp = label in listOf("÷", "×", "-", "+", "=")
                        val isSpecial = label in listOf("C", "±", "%")
                        val btnColor = when {
                            isOp -> Color(0xFFFF9500)
                            isSpecial -> Color(0xFFA5A5A5)
                            else -> Color(0xFF333333)
                        }
                        val txtColor = if (isSpecial) Color.Black else Color.White

                        Button(
                            onClick = {
                                when (label) {
                                    "C" -> onClear()
                                    "÷", "×", "-", "+" -> onOp(label)
                                    "=" -> onEquals()
                                    "." -> if (!displayValue.contains(".")) displayValue += "."
                                    "±" -> {
                                        if (displayValue.startsWith("-")) displayValue = displayValue.substring(1)
                                        else if (displayValue != "0") displayValue = "-$displayValue"
                                    }
                                    "%" -> {
                                        val v = displayValue.toDoubleOrNull() ?: 0.0
                                        displayValue = (v / 100).toString()
                                    }
                                    else -> onDigit(label)
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            modifier = if (label == "0") Modifier.weight(2f).height(68.dp) else Modifier.weight(1f).height(68.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleLarge,
                                color = txtColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
