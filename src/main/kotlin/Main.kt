@file:OptIn(ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class,
    ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class
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
import editor.SimpleTextEditor
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    val textMeasurer = rememberTextMeasurer()
    val textEditor = SimpleTextEditor()
    var text by remember { mutableStateOf(textEditor.getText()) }
    var caret by remember { mutableStateOf(0) }

    val requester = remember { FocusRequester() }

    MaterialTheme {
        Box(modifier = Modifier.onKeyEvent {
            if (it.type == KeyEventType.KeyDown) { // otherwise event is registered two times: up and down
                println(it.key.keyCode)
                when (it.key.keyCode) {
                    Key.DirectionLeft.keyCode -> {
                        caret = max(0, caret - 1)
                    }
                    Key.DirectionRight.keyCode -> {
                        caret = min(caret + 1, text.length)
                    }
                }
                when (it.utf16CodePoint) {
                    // TODO: move ranges & constants to separate class
                    in 9 .. 126 -> { // ASCII symbols
                        caret++
                        textEditor.add(it.utf16CodePoint.toChar(), caret)
                        text = textEditor.getText()
                    }
                    8 -> { // backspace
                        textEditor.delete(caret)
                        caret--
                        if (caret < 0) caret = 0
                        text = textEditor.getText()
                    }
                }
            }
            true
        }) {
            Canvas(modifier = Modifier.focusable(true).clickable { requester.requestFocus() }.focusRequester(requester).fillMaxSize()) {
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

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
