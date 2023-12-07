package editor

import androidx.compose.runtime.MutableState
import highlighting.Highlighter
import highlighting.HighlighterProvider
import language.Level

/**
 * Represents the contents of the (virtual) file opened in an editor
 * and provides an interface to interact with it, and with its markup.
 */
interface Document {
    val observableText: MutableState<String> // TODO: mutable public field
    val text get() = observableText.value
    val highlighters: Pair<List<Highlighter>, List<Highlighter>> get() = HighlighterProvider.getHighlighters(text, Level.SEMANTIC)

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
}