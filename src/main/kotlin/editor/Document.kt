package editor

import highlighting.Highlighter
import kotlinx.coroutines.flow.StateFlow
import java.net.URI

/**
 * Represents the contents of the (virtual) file opened in an editor
 * and provides an interface to interact with it, and with its markup.
 */
interface Document {
    val observableText: StateFlow<String>
    val highlighters: List<Highlighter>
    val caretModel: ICaretModel
    val fileURI: URI

    fun insertText(text: String) = text.forEach { c ->
        insertChar(c)
    }

    fun insertChar(char: Char)

    fun removeChar()

    fun getLineNumber(offset: Int): Int
    fun getLineStartOffset(line: Int): Int
    fun getLineEndOffset(line: Int): Int
    fun getLineOffsets(line: Int): Pair<Int, Int>
    fun getLineCount(): Int
    suspend fun subscribe(flow: StateFlow<ByteArray>)
}