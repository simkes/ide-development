package editor

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the contents of the (virtual) file opened in an editor
 * and provides an interface to interact with it, and with its markup.
 */
interface Document {
    val observableText: StateFlow<String>

    fun insertText(text: String, offset: Int) = text.forEachIndexed { index, c ->
        insertChar(c, offset + index)
    }

    fun insertChar(char: Char, offset: Int)

    fun removeText(startOffset: Int, endOffset: Int) {
        for (offset in startOffset..endOffset) removeChar(offset)
    }

    fun removeChar(offset: Int)

    fun getLineNumber(offset: Int): Int
    fun getLineStartOffset(line: Int): Int
    fun getLineEndOffset(line: Int): Int
    fun getLineOffsets(line: Int): Pair<Int, Int>
    fun getLineCount(): Int
    suspend fun subscribe(flow: StateFlow<ByteArray>)
}