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
    private var caretLine by mutableStateOf(0) // index of line
    private var caretPos by mutableStateOf(0)  // index of position in line

    @OptIn(ExperimentalComposeUiApi::class)
    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown) { // otherwise event is registered two times: up and down
            println(keyEvent.key)
            when (keyEvent.key) {
                Key.Enter -> {
                    caretLine++
                    textBuffer.insertLine(caretLine)
                    caretPos = 0
                    text = textBuffer.getText()
                }

                Key.DirectionLeft -> {
                    if (caretPos != 0) {
                        caretPos--
                    } else if (caretLine != 0) {
                        caretLine--
                        caretPos = textBuffer.getLineLength(caretLine)
                    }
                }

                Key.DirectionRight -> {
                    if (caretPos != textBuffer.getLineLength(caretLine)) {
                        caretPos++
                    } else {
                        caretLine++
                        caretPos = 0
                        if (caretLine == textBuffer.getSize()) {
                            textBuffer.insertLine(caretLine)
                            text = textBuffer.getText()
                        }
                    }
                }

                Key.DirectionUp -> {
                    if (caretLine != 0) {
                        caretLine--
                        caretPos = min(caretPos, textBuffer.getLineLength(caretLine))
                    }
                }

                Key.DirectionDown -> {
                    if (caretLine < textBuffer.getSize()) {
                        caretLine++
                        if (caretLine == textBuffer.getSize()) {
                            textBuffer.insertLine(caretLine)
                            text = textBuffer.getText()
                        }
                        caretPos = min(caretPos, textBuffer.getLineLength(caretLine))
                    }
                }

                Key.Backspace -> {
                    if (caretPos != 0) {
                        textBuffer.deleteChar(caretLine, caretPos - 1)
                        caretPos--
                        text = textBuffer.getText()
                    } else if (caretLine != 0) {
                        textBuffer.deleteLine(caretLine)
                        caretLine--
                        caretPos = textBuffer.getLineLength(caretLine)
                    }
                }
            }
            when (keyEvent.utf16CodePoint) {
                // TODO: move ranges & constants to separate class
                in 32..126 -> { // ASCII symbols
                    textBuffer.insertChar(caretLine, caretPos, keyEvent.utf16CodePoint.toChar())
                    caretPos++
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
                        val lines = it.split("\n");
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
}

fun main() {
    val app = App()
    application {
        Window(onCloseRequest = ::exitApplication) {
            app.run()
        }
    }
}