package ui

import ASCII_RANGE
import SPACES_IN_TAB
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.ApplicationScope
import arrowEventToDirection
import editor.EditorViewModel
import kotlinx.coroutines.CoroutineScope
import vfs.FileTreeNode
class UiModel(val applicationScope: ApplicationScope, val viewModel: EditorViewModel, coroutineScope: CoroutineScope, ) {
    val eventProcessor = UiEventProcessor(coroutineScope)

    val text by viewModel.text
    val docs get() = viewModel.openedDocuments

    init {
        eventProcessor.startEventProcessing()
    }

    fun close() {
        eventProcessor.stopEventProcessing()
    }

    val fileChooseDialogVisible = mutableStateOf(true)
    val noSourceDirectoryChosenDialogVisible = mutableStateOf(false)
    val fileEntryDialogVisible = mutableStateOf(false)

    var input = mutableStateOf("")

    val requester = FocusRequester()
    val caretVisible = mutableStateOf(true)
    var root: MutableState<FileTreeNode?> = mutableStateOf(null)
    val isEditorOpened get() = docs.value.isNotEmpty()

    fun emit(event: () -> UiEvent) {
        eventProcessor.newEvent(event())
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown) { // otherwise event is registered two times: up and down
            println("Key pressed: ${keyEvent.key}")

            if (keyEvent.isCtrlPressed && keyEvent.key == Key.S) {
                emit { FileSaveRequestEvent() } // TODO: multiple files
                return true
            }

            if (keyEvent.key == Key.Tab) {
                emit { TextInsertionEvent(" ".repeat(SPACES_IN_TAB)) }
                return true
            }

            when (keyEvent.key) {
                in arrowEventToDirection.keys -> {
                    emit { ArrowKeyEvent(keyEvent.key) }
                }

                Key.Backspace -> {
                    emit { BackspaceKeyEvent() }
                }

                Key.Enter -> {
                    emit { NewlineKeyEvent() }
                    return true
                }
            }

            when (keyEvent.utf16CodePoint) {
                in ASCII_RANGE -> {
                    emit {
                        val text = keyEvent.utf16CodePoint.toChar().toString()
                        println("Inserting into editor: $text")
                        TextInsertionEvent(text)
                    }
                }
            }
        }
        return true
    }
}