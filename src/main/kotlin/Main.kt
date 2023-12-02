@file:OptIn(
    ExperimentalTextApi::class,
)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
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
import editor.EditorViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import ui.*
import vfs.FileTreeNode
import javax.swing.JFileChooser
import kotlin.io.path.absolute
import kotlin.math.max
import kotlin.math.min

class App {
    private val viewModel = EditorViewModel

    @OptIn(DelicateCoroutinesApi::class)
    private val eventProcessor = UiEventProcessor(GlobalScope)
    private val onFileNavigatorFileClick = { treeNode: FileTreeNode ->
        eventProcessor.newEvent(OpenFileInEditorEvent(treeNode.path.absolute().toUri()))
    }
    private var root: MutableState<FileTreeNode?> = mutableStateOf(null)

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
            if (keyEvent.isCtrlPressed && keyEvent.key == Key.S) {
                eventProcessor.newEvent(FileSaveRequestEvent()) // TODO: multiple files
                return true
            }

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
        val fileChooseDialogVisible = remember { mutableStateOf(true) }

        @Composable
        fun fileDialog() {
            val dialog = JFileChooser()
            dialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialog.isVisible = true
            dialog.showOpenDialog(null)
            if (dialog.selectedFile != null) {
                root.value = viewModel.virtualFileSystem.listDirectory(dialog.selectedFile.toPath())
            } else if (root.value == null) {
                // TODO: start dir not chosen, terminate?
            }
            fileChooseDialogVisible.value = false
        }

        if (fileChooseDialogVisible.value) {
            fileDialog()
        }

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
            if (fileChooseDialogVisible.value) {
                fileDialog()
            }
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    FileTree(root.value!!, modifier = Modifier.weight(9f), onFileNavigatorFileClick)
                    Button(modifier = Modifier.weight(1f), onClick = {
                        fileChooseDialogVisible.value = true
                    }) {
                        Text("Choose directory")
                    }
                }
                Box(modifier = Modifier.onPreviewKeyEvent { handleKeyEvent(it) }.weight(2f)) {
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