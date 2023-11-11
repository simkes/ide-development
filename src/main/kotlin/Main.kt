@file:OptIn(
    ExperimentalTextApi::class,
)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import java.awt.FileDialog
import java.awt.Frame
import ui.*
import kotlin.math.max
import kotlin.math.min


class App {
    private val viewModel = EditorViewModel
    @OptIn(DelicateCoroutinesApi::class)
    private val eventProcessor = UiEventProcessor(GlobalScope)

    init {
        eventProcessor.startEventProcessing()
    }

    fun stopEventProcessor() {
        eventProcessor.stopEventProcessing()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown) { // otherwise event is registered two times: up and down
            println(keyEvent.key)
            when (keyEvent.key) {
                in arrowEventToDirection.keys -> {
                    eventProcessor.newEvent(ArrowKeyEvent(keyEvent.key))
                }

                Key.Backspace -> {
                    eventProcessor.newEvent(BackspaceKeyEvent())
                }

                Key.Enter -> {
                    eventProcessor.newEvent(NewlineKeyEvent())
                }
            }

            when (keyEvent.utf16CodePoint) {
                in ASCII_RANGE -> {
                    eventProcessor.newEvent(TextInsertionEvent(keyEvent.utf16CodePoint.toChar().toString()))
                }
            }
        }
        return true
    }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    @Preview
    fun run() {
        val fileChooseDialogVisible = remember { mutableStateOf(false) }

        @Composable
        fun fileDialog() {
            val dialog = FileDialog(null as Frame?, "Choose a File")
            dialog.isVisible = true
            if (dialog.files.isNotEmpty()) {
                viewModel.onFileOpening(dialog.file)
            }
            fileChooseDialogVisible.value = false
        }

        val text by viewModel.text.collectAsState()
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
            if (fileChooseDialogVisible.value) {
                fileDialog()
            }

            Column(modifier = Modifier.onPreviewKeyEvent { handleKeyEvent(it) }) {
                Canvas(modifier = Modifier.focusable(true).clickable { requester.requestFocus() }
                    .focusRequester(requester).fillMaxWidth().weight(1f)) {
                    text.let {
                        val textStyle = TextStyle(fontSize = 20.sp)
                        val measuredText = textMeasurer.measure(
                            AnnotatedString(it.value),
                            style = textStyle
                        )
                        val lines = it.value.split("\n")
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
                Button(modifier = Modifier, onClick = {
                    fileChooseDialogVisible.value = true
                }) {
                    Text("Choose a File")
                }
            }
        }
    }
}

fun main() {
    val app = App()
    application {
        Window(onCloseRequest = {
            exitApplication()
            app.stopEventProcessor()
        }) {
            app.run()
        }
    }
}