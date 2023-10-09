package editor

class SimpleTextEditor : TextEditor {
    private var text: Array<Char> = Array(1) { Char(0) }
    override fun add(c: Char, pos: Int) {
        if (pos >= text.size) {
            val newBuffer = Array(2 * text.size) { Char(0) }
            for (i in text.indices) {
                newBuffer[i] = text[i]
            }
            text = newBuffer
        }
        text[pos] = c
    }

    override fun delete(pos: Int) {
        text[pos] = Char(0)
    }

    override fun getText(): String = text.filter { it.code != 0 }.fold("") { txt, chr -> txt + chr}
}