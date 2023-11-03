package editor

import Direction


interface EditorViewModel {
    val _model: TextBuffer

    fun onCaretMovement(direction: Direction, step: Int = 1)

    fun onCharInsertion(char: Char)

    fun onTextInsertion(text: String) {
        text.forEach {
            onCharInsertion(it)
        }
    }

    fun getCaret(): Pair<Int, Int>

    fun onTextDeletion(step: Int = 1)
}