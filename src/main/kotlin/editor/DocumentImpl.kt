package editor

import Direction
import highlighting.Highlighter
import highlighting.HighlighterProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import language.Level
import java.net.URI

class DocumentImpl(initialText: String = "", override val fileURI: URI) : Document {
    inner class CaretModel : ICaretModel {
        private var _caretLine = 0
        override val caretLine get() = _caretLine
        private var _lineStartOffset = 0

        private var _lineEndOffset = 0
        private val lineLength get() = _lineEndOffset - _lineStartOffset

        private val _caretOffset get() = minOf(_lineStartOffset + rememberedOffset, _lineEndOffset)
        override val caretOffset get() = relative(_caretOffset)
        override val absoluteOffset get() = _caretOffset
        private var rememberedOffset: Int = 0
            set(offset) {
                field = relative(offset)
            }

        private fun relative(offset: Int) = offset - _lineStartOffset

        override fun moveCaret(direction: Direction, acrossLine: Boolean) {
            when (direction) {
                Direction.UP -> {
                    val prevLine = _caretLine
                    _caretLine = maxOf(0, _caretLine - 1)
                    if (_caretLine != prevLine) {
                        updateLine()
                    }
                }

                Direction.DOWN -> {
                    val prevLine = _caretLine
                    _caretLine = minOf(getLineCount(), _caretLine + 1)
                    if (_caretLine != prevLine) {
                        updateLine()
                    }
                }

                Direction.LEFT -> {
                    val leftLimit = if (acrossLine) 0 else _lineStartOffset
                    rememberedOffset = maxOf(leftLimit, _caretOffset - 1)
                    if (_lineStartOffset + rememberedOffset < _lineStartOffset) {
                        _caretLine = maxOf(0, _caretLine - 1)
                        updateLine()
                        rememberedOffset = _lineEndOffset
                    }
                }

                Direction.RIGHT -> {
                    val rightLimit = if (acrossLine) observableText.value.length else _lineEndOffset
                    rememberedOffset = minOf(rightLimit, _caretOffset + 1)
                    if (_lineStartOffset + rememberedOffset > _lineEndOffset) {
                        _caretLine = minOf(getLineCount(), _caretLine + 1)
                        updateLine()
                        rememberedOffset = _lineStartOffset
                    }
                }
            }
        }

        override fun updateLine() {
            val (start, end) = getLineOffsets(_caretLine)
            _lineStartOffset = start; _lineEndOffset = end
        }

        override fun newline() {
            _caretLine++
            updateLine()
            rememberedOffset = _lineStartOffset
        }
    }

    override val highlighters
        get() = HighlighterProvider.getHighlighters(observableText.value, Level.SEMANTIC)

    override val caretModel = CaretModel()
    private var _text: TextBuffer = SimpleArrayTextBuffer(initialText)

    private val mutableText = MutableStateFlow(_text.getText())
    override val observableText: StateFlow<String> = mutableText.asStateFlow()

    override fun insertChar(char: Char) {
        modifying {
            _text.add(char, caretModel.absoluteOffset)
        }
        caretModel.updateLine()
        caretModel.moveCaret(Direction.RIGHT, acrossLine = false)
    }

    override fun removeChar() {
        if (caretModel.absoluteOffset != 0) {
            caretModel.moveCaret(Direction.LEFT)
            modifying {
                _text.delete(caretModel.absoluteOffset)
            }
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