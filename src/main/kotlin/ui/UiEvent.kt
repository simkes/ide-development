package ui

import Direction
import androidx.compose.ui.input.key.Key
import arrowEventToDirection
import editor.EditorViewModel

/**
 * Base interface for all events that are caused by UI and need to update the model.
 */
interface UiEvent {
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