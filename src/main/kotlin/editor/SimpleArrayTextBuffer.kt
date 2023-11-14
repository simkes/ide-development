package editor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy

class SimpleArrayTextBuffer(initialText: String = "") : TextBuffer {
    private val _text: ArrayList<Char> = initialText.toCollection(ArrayList())

    override val lineCount: Int get() = _text.count { it == '\n'}
    override val size: Int get() = _text.size

    override fun add(c: Char, pos: Int) {
        if (pos < 0 || pos > _text.size) throw IllegalArgumentException("add: pos $pos.")
        _text.add(pos, c)
    }

    override fun delete(pos: Int) {
        if (pos < 0 || pos >= _text.size) throw IllegalArgumentException("delete: pos $pos.")
        _text.removeAt(pos)
    }

    override fun getText(): String = _text.joinToString("")

    override fun getCharAtPos(pos: Int): Char {
        if (pos < 0 || pos > _text.size) throw IllegalArgumentException("get: pos $pos.")
        return _text[pos]
    }

    override fun getLineNumber(pos: Int): Int {
        var number = 0
        for (offset in 0 .. pos) {
            if (get(offset) == '\n') number++
        }
        return number
    }

    override fun getLineStartOffset(line: Int): Int {
        var number = 0
        var offset = 0
        while (offset < size && number < line) {
            if (get(offset) == '\n') number++
            offset++
        }
        return offset
    }

    override fun getLineEndOffset(line: Int): Int {
        var offset = getLineStartOffset(line)
        while (offset < size && get(offset) != '\n') {
            offset++
        }
        return offset
    }
}