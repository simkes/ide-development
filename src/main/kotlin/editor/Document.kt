package editor

import Direction
import dataStructures.GapBuffer
import highlighting.HighlighterProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import language.Level
import java.net.URI

class Document(initialText: String = "", val fileURI: URI) {
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

    val highlighters
        get() = HighlighterProvider.getHighlighters(observableText.value, Level.SEMANTIC)

    val caretModel = CaretModel()
    private var _text: TextBuffer = GapBuffer(initialText)

    private val mutableText = MutableStateFlow(_text.getText())
    val observableText: StateFlow<String> = mutableText.asStateFlow()

    fun insertChar(char: Char) {
        modifying {
            _text.add(char, caretModel.absoluteOffset)
        }
        caretModel.updateLine()
        caretModel.moveCaret(Direction.RIGHT, acrossLine = false)
    }

    fun insertText(text: String) = text.forEach { insertChar(it) }

    fun removeChar() {
        if (caretModel.absoluteOffset != 0) {
            caretModel.moveCaret(Direction.LEFT)
            modifying {
                _text.delete(caretModel.absoluteOffset)
            }
        }
    }

    fun getLineNumber(offset: Int) = _text.getLineNumber(offset)
    fun getLineStartOffset(line: Int) = _text.getLineStartOffset(line)
    fun getLineEndOffset(line: Int) = _text.getLineEndOffset(line)
    fun getLineOffsets(line: Int) = _text.getLineOffsets(line)

    fun getLineCount() = _text.lineCount

    @OptIn(FlowPreview::class)
    suspend fun subscribe(flow: StateFlow<ByteArray>) {
        flow.debounce(1000).collect { ba ->
            mutableText.update { ba.decodeToString() }
            _text = GapBuffer(ba.decodeToString())
            println("Updated contents of the document")
        }
    }

    private inline /* TODO: inline meaningful? */ fun modifying(function: () -> Unit) {
        function()
        mutableText.update { _text.getText() }
    }
}