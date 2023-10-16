package editor

class SimpleTextBuffer : TextBuffer {

    private val lines = mutableListOf<ArrayList<Char>>()

    init {
        lines.add(ArrayList())
    }

    override fun insertChar(lineIndex: Int, pos: Int, c: Char) {
        if (lineIndex < 0 || lineIndex >= lines.size) throw IllegalArgumentException("insertChar: lineIndex $lineIndex.")
        val line = lines[lineIndex]
        if (pos < 0 || pos > line.size) throw IllegalArgumentException("insertChar: pos $pos.")
        line.add(pos, c)
    }

    override fun deleteChar(lineIndex: Int, pos: Int) {
        if (lineIndex < 0 || lineIndex >= lines.size) throw IllegalArgumentException("deleteChar: lineIndex $lineIndex.")
        val line = lines[lineIndex]
        if (pos < 0 || pos >= line.size) throw IllegalArgumentException("deleteChar: pos $pos.")
        line.removeAt(pos)
    }

    override fun insertLine(lineIndex: Int) {
        if (lineIndex < 0 || lineIndex > lines.size) throw IllegalArgumentException("insertLine: lineIndex $lineIndex.")
        val newLine = ArrayList<Char>()
        lines.add(lineIndex, newLine)
    }

    override fun deleteLine(lineIndex: Int) {
        if (lineIndex <= 0 || lineIndex >= lines.size) throw IllegalArgumentException("deleteLine: lineIndex $lineIndex.")
        val previousLine = lines[lineIndex - 1]
        val currentLine = lines[lineIndex]
        previousLine.addAll(currentLine)
        lines.removeAt(lineIndex)
    }

    override fun getLineLength(lineIndex: Int): Int {
        if (lineIndex < 0 || lineIndex >= lines.size) throw IllegalArgumentException("getLineLength: lineIndex $lineIndex.")
        return lines[lineIndex].size
    }

    override fun getSize(): Int {
        return lines.size
    }

    override fun getText(): String {
        val result = StringBuilder()
        for (line in lines) {
            result.append(line.joinToString(""))
            result.append('\n')
        }
        return result.toString().trim()
    }
}