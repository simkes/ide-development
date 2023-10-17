package editor

interface TextBuffer {
    fun insertChar(lineIndex: Int, pos: Int, c: Char)

    fun deleteChar(lineIndex: Int, pos: Int)

    fun insertLine(lineIndex: Int)

    fun deleteLine(lineIndex: Int)

    fun getLineLength(lineIndex: Int): Int

    fun getSize(): Int

    fun getText(): String

    fun getLine(lineIndex: Int): String
}