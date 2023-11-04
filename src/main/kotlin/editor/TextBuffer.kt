package editor

/**
 * Represents the actual plain text with a specific underlying data structure.
 * Supposed to be a part of a [Document]
 */
internal interface TextBuffer {
    fun add(c: Char, pos: Int)

    fun delete(pos: Int)

    fun getText(): String

    fun getCharAtPos(pos: Int): Char

    fun getLineNumber(pos: Int): Int

    fun getLineStartOffset(line: Int): Int

    fun getLineEndOffset(line: Int): Int

    // It may be more efficient to calculate offsets in a single run for some data structures
    fun getLineOffsets(line: Int): Pair<Int, Int> = Pair(getLineStartOffset(line), getLineEndOffset(line))

    val size: Int
    val lineCount: Int

    operator fun get(index: Int): Char = getCharAtPos(index)
}