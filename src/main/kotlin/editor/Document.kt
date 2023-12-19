package editor

import highlighting.Highlighter
import kotlinx.coroutines.flow.StateFlow
import java.net.URI
import androidx.compose.runtime.MutableState
import highlighting.ColoredHighlighter
import highlighting.HighlighterProvider
import highlighting.UnderlinedHighlighter
import language.Level

/**
 * Represents the contents of the (virtual) file opened in an editor
 * and provides an interface to interact with it, and with its markup.
 */
interface Document {
    val observableText: StateFlow<String>
    val highlighters: Pair<List<ColoredHighlighter>, List<UnderlinedHighlighter>> get() = HighlighterProvider.getHighlighters(observableText.value, Level.SEMANTIC)
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