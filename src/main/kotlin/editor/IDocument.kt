package editor

import kotlinx.coroutines.flow.StateFlow
import java.net.URI

interface IDocument {
    val observableText: StateFlow<String>
    val caretModel: ICaretModel
    val fileURI: URI
    fun insertChar(char: Char)
    fun insertText(text: String) = text.forEach { insertChar(it) }
    fun removeChar()
    fun getLineNumber(offset: Int): Int
    fun getLineStartOffset(line: Int): Int
    fun getLineEndOffset(line: Int): Int
    fun getLineOffsets(line: Int): Pair<Int, Int>
    suspend fun subscribe(flow: StateFlow<ByteArray>)
}