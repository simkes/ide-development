package editor

class SimpleTextBuffer : TextBuffer {
    private var text = ArrayList<Char>()
    override fun add(c: Char, pos: Int) {
        if (pos < 0 || pos > text.size) throw IllegalArgumentException("add: pos $pos.")
        text.add(pos, c)
    }

    override fun delete(pos: Int) {
        if (pos < 0 || pos >= text.size) throw IllegalArgumentException("delete: pos $pos.")
        text.removeAt(pos)
    }

    override fun getText(): String = text.joinToString("")
}