package editor

import androidx.compose.runtime.mutableStateOf

class DocumentImpl(initialText: String = "") : Document {
    private val _text: TextBuffer = SimpleArrayTextBuffer(initialText)

    override val observableText = mutableStateOf(_text.getText())

    override fun insertChar(char: Char, offset: Int) {
        modifying {
            _text.add(char, offset)
        }
    }

    override fun removeChar(offset: Int) {
        modifying {
            _text.delete(offset)
        }
    }

    override fun getLineNumber(offset: Int) = _text.getLineNumber(offset)
    override fun getLineStartOffset(line: Int) = _text.getLineStartOffset(line)
    override fun getLineEndOffset(line: Int) = _text.getLineEndOffset(line)
    override fun getLineOffsets(line: Int) = _text.getLineOffsets(line)

    override fun getLineCount() = _text.lineCount

    private inline /* TODO: inline meaningful? */ fun modifying(function: () -> Unit) {
        function()
        observableText.value = _text.getText()
    }
}