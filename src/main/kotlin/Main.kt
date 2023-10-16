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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import editor.SimpleTextBuffer
import editor.TextBuffer
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
                    if(caretPos != textBuffer.getLineLength(caretLine)) {
                        caretPos++
                    } else if (caretLine <= textBuffer.getSize()) {
                        caretLine++
                        caretPos = 0
                    } else {
                        textBuffer.insertLine(caretLine)
                        caretLine++
                        caretPos = 0
                        text = textBuffer.getText()
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
        MaterialTheme {
            Box(modifier = Modifier.onPreviewKeyEvent { handleKeyEvent(it) }) {
                Canvas(modifier = Modifier.focusable(true).clickable { requester.requestFocus() }
                    .focusRequester(requester).fillMaxSize()) {
                    text.let {
                        val measuredText = textMeasurer.measure(
                            AnnotatedString(it),
                            style = TextStyle(fontSize = 20.sp)
                        )

                        translate(100f, 100f) {
                            drawText(measuredText)
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