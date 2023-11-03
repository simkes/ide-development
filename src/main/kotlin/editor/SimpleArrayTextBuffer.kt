package editor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy

class SimpleArrayTextBuffer : TextBuffer {
    override val text: MutableState<String> = mutableStateOf("")

    val _text = ArrayList<Char>()

    override fun add(c: Char, pos: Int) {
        if (pos < 0 || pos > _text.size) throw IllegalArgumentException("add: pos $pos.")
        _text.add(pos, c)
        text.value = getText()
    }

    override fun delete(pos: Int) {
        if (pos < 0 || pos >= _text.size) throw IllegalArgumentException("delete: pos $pos.")
        _text.removeAt(pos)
        text.value = getText()
    }

    override fun getText(): String = _text.joinToString("")

    override fun getCharAtPos(pos: Int): Char {
        if (pos < 0 || pos > _text.size) throw IllegalArgumentException("get: pos $pos.")
        return _text[pos]
    }
}