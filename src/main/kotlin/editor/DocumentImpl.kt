package editor

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class DocumentImpl(initialText: String = "") : Document {
    private var _text: TextBuffer = SimpleArrayTextBuffer(initialText)

    private val mutableText = MutableStateFlow(_text.getText())
    override val observableText: StateFlow<String> = mutableText.asStateFlow()

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

    @OptIn(FlowPreview::class)
    override suspend fun subscribe(flow: StateFlow<ByteArray>) {
        flow.debounce(1000).collect { ba ->
            mutableText.update { ba.decodeToString() }
            _text = SimpleArrayTextBuffer(ba.decodeToString())
            println("Updated contents of the document")
        }
    }

    private inline /* TODO: inline meaningful? */ fun modifying(function: () -> Unit) {
        function()
        mutableText.update { _text.getText() }
    }
}