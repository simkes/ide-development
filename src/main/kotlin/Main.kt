@file:OptIn(
    ExperimentalTextApi::class,
)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import editor.*
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min


class App {
    private val viewModel = EditorViewModel

    @OptIn(ExperimentalComposeUiApi::class)
    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown) { // otherwise event is registered two times: up and down
            println(keyEvent.key)
            when (keyEvent.key) {
                in arrowEventToDirection.keys -> {
                    viewModel.onCaretMovement(arrowEventToDirection[keyEvent.key]!!)
                }

                Key.Backspace -> {
                    viewModel.onTextDeletion()
                }

                Key.Enter -> {
                    viewModel.onNewline()
                }
            }

            when (keyEvent.utf16CodePoint) {
                in ASCII_RANGE -> {
                    viewModel.onCharInsertion(keyEvent.utf16CodePoint.toChar())
                }
            }
        }
        return true
    }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    @Preview
    fun run() {
        val text by viewModel.text
        val textMeasurer = rememberTextMeasurer()
        val requester = remember { FocusRequester() }
        val caretVisible = remember { mutableStateOf(true) }

        val horizontalScrollState = rememberScrollState(0)
        val verticalScrollState = rememberScrollState(0)

        LaunchedEffect(Unit) {
            while (true) {
                caretVisible.value = !caretVisible.value
                delay(500)
            }
        }
        MaterialTheme {
            Box(modifier = Modifier.onPreviewKeyEvent { handleKeyEvent(it) }) {
                Canvas(modifier = Modifier.focusable(true)
                    .clickable { requester.requestFocus() }
                    .focusRequester(requester)
                    .fillMaxSize()
                    .scrollable(verticalScrollState, Orientation.Vertical)
                    .scrollable(horizontalScrollState, Orientation.Horizontal)
                ) {
                    text.let {
                        val textStyle = TextStyle(fontSize = 20.sp)
                        val measuredText = textMeasurer.measure(
                            AnnotatedString(it),
                            style = textStyle
                        )
                        val lines = it.split("\n")
                        val (caretLine, caretPos) = viewModel.getCaret()
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

                        translate(
                            60f - horizontalScrollState.value,
                            60f - verticalScrollState.value
                        ) {
                            // drawing line numbers
                            for ((index, line) in lines.withIndex()) {
                                val lineNumberString = AnnotatedString((index + 1).toString())
                                val lineNumberLayout = textMeasurer.measure(
                                    lineNumberString,
                                    style = textStyle.copy(color = Color.Gray)
                                )
                                drawText(
                                    lineNumberLayout,
                                    topLeft = Offset(-55f, (index * charHeight).toFloat())
                                )
                            }

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