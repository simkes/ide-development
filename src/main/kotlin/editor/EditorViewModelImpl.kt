package editor

import Direction
import kotlin.math.min


class EditorViewModelImpl : EditorViewModel {
    override val _model = SimpleArrayTextBuffer()

    private var caret = 0

    override fun onCaretMovement(direction: Direction, step: Int) {
        when (direction) {
            Direction.UP -> {
                val lines = _model.getText().split("\n")
                val (caretLine, caretPos) = calculateCaretLineAndPos(lines)
                if (caretLine > 0) {
                    val prevLine = lines[caretLine - 1]
                    caret -= caretPos + 1 + prevLine.length
                    caret += min(caretPos, prevLine.length)
                }
            }

            Direction.DOWN -> {
                val lines = _model.getText().split("\n")
                val (caretLine, caretPos) = calculateCaretLineAndPos(lines)
                if (caretLine < lines.size - 1) {
                    val nextLine = lines[caretLine + 1]
                    caret += lines[caretLine].length - caretPos + 1
                    caret += min(caretPos, nextLine.length)
                }
            }

            Direction.LEFT -> caret = maxOf(caret - 1, 0)
            Direction.RIGHT -> caret = minOf(caret + 1, _model.getText().length)
        }
    }

    override fun onCharInsertion(char: Char) {
        _model.add(char, caret)
        caret++
    }

    override fun getCaret(): Pair<Int, Int> {
        return calculateCaretLineAndPos(_model.getText().split("\n"))
    }

    override fun onTextDeletion(step: Int) {
        repeat(step) {
            if (caret != 0) {
                _model.delete(caret - 1)
                caret--
            }
        }
    }

    private fun calculateCaretLineAndPos(lines: List<String>): Pair<Int, Int> {
        var caretLine = 0
        var caretPos = 0
        var posCounter = 0
        for ((index, line) in lines.withIndex()) {
            posCounter += line.length + 1
            if (caret < posCounter) {
                caretLine = index
                caretPos = if (index == 0) caret else caret - posCounter + line.length + 1
                break
            }
        }

        return Pair(caretLine, caretPos)
    }
}