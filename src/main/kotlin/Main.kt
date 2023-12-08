@file:OptIn(
    ExperimentalTextApi::class,
)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import editor.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import ui.*
import javax.swing.JFileChooser
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
            val dialog = JFileChooser()
            dialog.fileSelectionMode = JFileChooser.FILES_ONLY
            dialog.isVisible = true
            dialog.showOpenDialog(null)
            if (dialog.selectedFile != null) {
                viewModel.onFileOpening(dialog.selectedFile.path)
            }
            fileChooseDialogVisible.value = false
        }

        val text by viewModel.text.collectAsState()
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
            if (fileChooseDialogVisible.value) {
                fileDialog()
            }

            Column(modifier = Modifier.onPreviewKeyEvent { handleKeyEvent(it) }) {
                Canvas(modifier = Modifier.focusable(true).clickable { requester.requestFocus() }
                    .focusRequester(requester).fillMaxWidth().weight(1f)) {
                    text.let {
                        val textStyle = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                        val (colored, underlined) = viewModel.highlighters
                        val highlighters = colored.map { highlighter ->
                            AnnotatedString.Range(
                                SpanStyle(
                                    color = highlighterColorToComposeColor(highlighter.color)
                                ),
                                highlighter.startOffset,
                                highlighter.endOffset
                            )
                        }
                        val measuredText = textMeasurer.measure(
                            AnnotatedString(it.value, spanStyles = highlighters),
                            style = textStyle
                        )
                        val lines = it.value.split("\n")
                        val (caretLine, caretPos) = viewModel.getCaret()
                        val charHeight = measuredText.size.height / max(1, lines.size)
                        val charWidth = measuredText.size.width / max(1, it.value.length)
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

                            underlined.forEach { highlighter ->
                                if (measuredText.size.width >= highlighter.endOffset) {
                                    if (highlighter.underlinedAfterEndOffset) {
                                        // drawing short red line after end offset
                                        val y =
                                            measuredText.getLineBottom(measuredText.getLineForOffset(highlighter.endOffset))
                                        val x = measuredText.getHorizontalPosition(highlighter.endOffset, true)
                                        val lineLength = 20f
                                        drawLine(
                                            color = Color.Red,
                                            start = Offset(x, y),
                                            end = Offset(x + lineLength, y),
                                            strokeWidth = 2f
                                        )
                                    } else {
                                        val y =
                                            measuredText.getLineBottom(measuredText.getLineForOffset(highlighter.startOffset))
                                        val x1 = measuredText.getHorizontalPosition(highlighter.startOffset, true)
                                        val x2 = measuredText.getHorizontalPosition(highlighter.endOffset, true)
                                        drawLine(
                                            color = Color.Red,
                                            start = Offset(x1, y),
                                            end = Offset(x2, y),
                                            strokeWidth = 2f
                                        )
                                    }
                                }
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