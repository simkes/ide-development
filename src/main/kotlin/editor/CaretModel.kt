package editor

import Direction

interface ICaretModel {
    val absoluteOffset: Int
    val caretOffset: Int
    val caretLine: Int

    fun moveCaret(direction: Direction, acrossLine: Boolean = true)
    fun newline()
}