@file:OptIn(
    ExperimentalTextApi::class,
    ExperimentalTextApi::class,
    ExperimentalTextApi::class,
    ExperimentalTextApi::class,
    ExperimentalTextApi::class,
    ExperimentalTextApi::class,
    ExperimentalTextApi::class
)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import editor.SimpleTextBuffer
import editor.TextBuffer
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

class App {
    private val textBuffer: TextBuffer = SimpleTextBuffer()
    private var text by mutableStateOf(textBuffer.getText())
    private var caret by mutableStateOf(0)

    @OptIn(ExperimentalComposeUiApi::class)
    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown) { // otherwise event is registered two times: up and down
            println(keyEvent.key)
            when (keyEvent.key) {
                Key.DirectionLeft -> {
                    if (caret != 0) {
                        caret--
                    }
                }

                Key.DirectionRight -> {
                    if (caret != text.length) {
                        caret++
                    }
                }

                Key.DirectionUp -> {
                    val lines = text.split("\n")
                    val (caretLine, caretPos) = calculateCaretLineAndPos(lines)
                    if (caretLine > 0) {
                        val prevLine = lines[caretLine - 1]
                        caret -= caretPos + 1 + prevLine.length
                        caret += min(caretPos, prevLine.length)
                    }
                }

                Key.DirectionDown -> {
                    val lines = text.split("\n")
                    val (caretLine, caretPos) = calculateCaretLineAndPos(lines)
                    if (caretLine < lines.size - 1) {
                        val nextLine = lines[caretLine + 1]
                        caret += lines[caretLine].length - caretPos + 1
                        caret += min(caretPos, nextLine.length)
                    }
                }

                Key.Backspace -> {
                    if (caret != 0) {
                        textBuffer.delete(caret - 1)
                        caret--
                        text = textBuffer.getText()
                    }
                }
            }
            when (keyEvent.utf16CodePoint) {
                // TODO: move ranges & constants to separate class
                in 9..126 -> { // ASCII symbols
                    textBuffer.add(keyEvent.utf16CodePoint.toChar(), caret)
                    caret++
                    text = textBuffer.getText()
                    println(text)
                }
            }
        }
        return true
    }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    @Preview
    fun run() {
        val textMeasurer = rememberTextMeasurer()
        val requester = remember { FocusRequester() }
        val caretVisible = remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            while (true) {
                caretVisible.value = !caretVisible.value
                delay(500)
            }
        }
        MaterialTheme {
            Box(modifier = Modifier.onPreviewKeyEvent { handleKeyEvent(it) }) {
                Canvas(modifier = Modifier.focusable(true).clickable { requester.requestFocus() }
                    .focusRequester(requester).fillMaxSize()) {
                    text.let {
                        val textStyle = TextStyle(fontSize = 20.sp)
                        val measuredText = textMeasurer.measure(
                            AnnotatedString(it),
                            style = textStyle
                        )
                        val lines = it.split("\n")
                        val (caretLine, caretPos) = calculateCaretLineAndPos(lines)
                        val charHeight = measuredText.size.height / max(1, lines.size)
                        val caretY = charHeight * caretLine.toFloat()
                        var caretX = 0f
                        if (lines.getOrNull(caretLine) != null) {
                            val lineStr = AnnotatedString(
                                lines[caretLine].substring(
                                    0,
                                    min(caretPos, lines[caretLine].length)
                                )
                            )
                            caretX = textMeasurer.measure(
                                lineStr,
                                style = textStyle
                            ).size.width.toFloat()
                        }

                        translate(50f, 50f) {
                            drawText(measuredText)

                            if (caretVisible.value) {
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(caretX, caretY),
                                    end = Offset(caretX, caretY + charHeight),
                                    strokeWidth = 1f
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun calculateCaretLineAndPos(lines: List<String>): Pair<Int, Int> {
        var caretLine = 0
        var caretPos = 0
        var posCounter = 0
        for ((index, line) in lines.withIndex()) {
            posCounter += line.length + 1
            if (caret < posCounter) {
                caretLine = index
                caretPos = if (index == 0) caret else caret - posCounter + line.length + 1
                break
            }
        }

        return Pair(caretLine, caretPos)
    }
}

fun main() {
    val app = App()
    application {
        Window(onCloseRequest = ::exitApplication) {
            app.run()
        }
    }
}