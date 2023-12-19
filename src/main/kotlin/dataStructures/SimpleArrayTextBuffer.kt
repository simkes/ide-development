package dataStructures

import editor.TextBuffer

class SimpleArrayTextBuffer(initialText: String = "") : TextBuffer {
    private val _text: ArrayList<Char> = initialText.toCollection(ArrayList())

    override val lineCount: Int get() = _text.count { it == '\n' }
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
}