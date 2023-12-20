package editor

import Direction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URI

object DummyDocument : IDocument {
    override val fileURI: URI = URI("")
    override val observableText: StateFlow<String>
        get() = MutableStateFlow("")

    override val caretModel: ICaretModel = object : ICaretModel {
        override val absoluteOffset: Int = 0
        override val caretOffset: Int = 0
        override val caretLine: Int = 0

        override fun moveCaret(direction: Direction, acrossLine: Boolean) {}
        override fun newline() {}
        override fun updateLine() {}
        override fun setCaret(line: Int, offset: Int) {}
    }


    override fun insertChar(char: Char) {}

    override fun removeChar() {}

    override fun getLineNumber(offset: Int): Int = 0

    override fun getLineStartOffset(line: Int): Int = 0
    override fun getLineEndOffset(line: Int): Int = 0

    override fun getLineOffsets(line: Int): Pair<Int, Int> = Pair(0, 0)

    override suspend fun subscribe(flow: StateFlow<ByteArray>) {
    }
}