package com.zuan.kernelmanager.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object TerminalUtils {
    // Warna Terminal
    val TermColors_Green = Color(0xFF00FF00)
    val TermColors_Red = Color(0xFFFF5252)
    val TermColors_Cyan = Color(0xFF18FFFF)
    val TermColors_Default = Color(0xFFFFFFFF)

    fun parseAnsi(text: String): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")
        
        return buildAnnotatedString {
            // Regex untuk menangkap kode warna ANSI
            val regex = "\u001B\\[[;\\d]*m".toRegex()
            var lastIndex = 0
            var currentColor = TermColors_Default
            var isBold = false

            regex.findAll(text).forEach { matchResult ->
                val textSegment = text.substring(lastIndex, matchResult.range.first)
                if (textSegment.isNotEmpty()) {
                    withStyle(style = SpanStyle(color = currentColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)) {
                        append(textSegment)
                    }
                }

                val code = matchResult.value
                val params = code.removePrefix("\u001B[").removeSuffix("m").split(";")

                params.forEach { param ->
                    when (param) {
                        "0", "00" -> { currentColor = TermColors_Default; isBold = false }
                        "1" -> isBold = true
                        "31" -> currentColor = TermColors_Red
                        "32" -> currentColor = TermColors_Green
                        "36" -> currentColor = TermColors_Cyan
                        else -> { /* Ignore unknown codes */ }
                    }
                }
                lastIndex = matchResult.range.last + 1
            }

            if (lastIndex < text.length) {
                withStyle(style = SpanStyle(color = currentColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)) {
                    append(text.substring(lastIndex))
                }
            }
        }
    }
}
