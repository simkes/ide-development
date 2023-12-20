package ui

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
        EditorViewModel.onCaretMovement(direction)
    }
}

class TextInsertionEvent(private val text: String) : UiEvent {
    override suspend fun process() {
        EditorViewModel.onTextInsertion(text)
    }
}

class NewlineKeyEvent : UiEvent {
    override suspend fun process() {
        EditorViewModel.onNewline()
    }
}

class BackspaceKeyEvent : UiEvent {
    override suspend fun process() {
        EditorViewModel.onTextDeletion()
    }
}

class FileSaveRequestEvent : UiEvent {
    override suspend fun process() {
        EditorViewModel.onFileSave()
    }
}

class OpenFileInEditorEvent(private val file: URI) : UiEvent {
    override suspend fun process() {
        EditorViewModel.onFileOpening(file)
    }
}

class CloseFileInEditorEvent(private val file: URI) : UiEvent {
    override suspend fun process() {
        EditorViewModel.onFileClosing(file)
    }
}

class SetCaretEvent(private val line: Int, private val offset: Int) : UiEvent {
    override suspend fun process() {
        EditorViewModel.setCaret(line, offset)
    }
}