package editor

import androidx.compose.runtime.MutableState

interface TextBuffer {
    val text: MutableState<String>

    fun add(c: Char, pos: Int)

    fun delete(pos: Int)

    fun getText(): String

    fun getCharAtPos(pos: Int): Char
}