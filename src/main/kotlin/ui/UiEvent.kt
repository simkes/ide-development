package ui

import App
import Direction
import androidx.compose.ui.input.key.Key
import arrowEventToDirection
import editor.EditorViewModel
import java.net.URI
import java.nio.file.Path

/**
 * Base interface for all events that are caused by UI and need to update the model.
 */
interface UiEvent {
    // TODO: dispatcher accessor
    suspend fun process()
}

class ArrowKeyEvent(event: Key) : UiEvent {
    private val direction: Direction = arrowEventToDirection[event]!!
    override suspend fun process() {
        App.editorViewModel.onCaretMovement(direction)
    }
}

class TextInsertionEvent(private val text: String) : UiEvent {
    override suspend fun process() {
        App.editorViewModel.onTextInsertion(text)
    }
}

class NewlineKeyEvent : UiEvent {
    override suspend fun process() {
        App.editorViewModel.onNewline()
    }
}

class BackspaceKeyEvent : UiEvent {
    override suspend fun process() {
        App.editorViewModel.onTextDeletion()
    }
}

class FileSaveRequestEvent : UiEvent {
    override suspend fun process() {
        App.editorViewModel.onFileSave()
    }
}

class OpenFileInEditorEvent(private val file: URI) : UiEvent {
    override suspend fun process() {
        App.editorViewModel.onFileOpening(file)
    }
}

class CloseFileInEditorEvent(private val file: URI) : UiEvent {
    override suspend fun process() {
        App.editorViewModel.onFileClosing(file)
    }
}

class SetCaretEvent(private val line: Int, private val offset: Int) : UiEvent {
    override suspend fun process() {
        App.editorViewModel.setCaret(line, offset)
    }
}