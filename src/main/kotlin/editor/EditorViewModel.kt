package editor

import Direction
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import vfs.VirtualFileSystem
import vfs.VirtualFileSystemImpl
import java.nio.file.Path

object EditorViewModel {
    // TODO: context object to receive control objects (Project?)
    var text = mutableStateOf("")

    @OptIn(DelicateCoroutinesApi::class)
    private val scope = GlobalScope

    private var _currentDocument: Document = DocumentImpl().also {
        scope.launch(Dispatchers.IO) {
            it.observableText.collect {
                text.value = it
            }
        }
    }

    private val documentManager: DocumentManager = DocumentManagerImpl(scope)
    private val virtualFileSystem: VirtualFileSystem = VirtualFileSystemImpl()

    private var caretLine = 0
    private var lineStartOffset = 0

    private var lineEndOffset = 0
    private val lineLength get() = lineEndOffset - lineStartOffset

    private val caretOffset get() = minOf(lineStartOffset + rememberedOffset, lineEndOffset)
    private var rememberedOffset: Int = 0
        set(offset) {
            field = relative(offset)
        }

    private fun relative(offset: Int) = offset - lineStartOffset

    fun onCaretMovement(direction: Direction) {
        when (direction) {
            Direction.UP -> {
                val prevLine = caretLine
                caretLine = maxOf(0, caretLine - 1)
                if (caretLine != prevLine) {
                    updateLine()
                }
            }

            Direction.DOWN -> {
                val prevLine = caretLine
                caretLine = minOf(_currentDocument.getLineCount(), caretLine + 1)
                if (caretLine != prevLine) {
                    updateLine()
                }
            }

            Direction.LEFT -> {
                rememberedOffset = maxOf(0, caretOffset - 1)
                if (lineStartOffset + rememberedOffset < lineStartOffset) {
                    caretLine = maxOf(0, caretLine - 1)
                    updateLine()
                    rememberedOffset = lineEndOffset
                }
            }

            Direction.RIGHT -> {
                rememberedOffset = minOf(text.value.length, caretOffset + 1)
                if (lineStartOffset + rememberedOffset > lineEndOffset) {
                    caretLine = minOf(_currentDocument.getLineCount(), caretLine + 1)
                    updateLine()
                    rememberedOffset = lineStartOffset
                }
            }
        }
    }

    fun onFileOpening(filePath: String) {
        val path = Path.of(filePath)
        val virtualFile = virtualFileSystem.getFileByPath(path)
        _currentDocument = documentManager.openDocument(virtualFile)

        scope.launch(Dispatchers.IO) {
            _currentDocument.observableText.collect {
                text.value = it
            }
        }
    }

    fun onCharInsertion(char: Char) {
        _currentDocument.insertChar(char, caretOffset)
        updateLine()
        rememberedOffset = minOf(lineEndOffset, caretOffset) + 1
    }

    fun onTextInsertion(text: String) {
        _currentDocument.insertText(text, caretOffset)
        updateLine()
        rememberedOffset = minOf(lineEndOffset, caretOffset) + text.length
    }

    fun getCaret(): Pair<Int, Int> = Pair(caretLine, relative(caretOffset))

    fun onTextDeletion(step: Int = 1) {
        repeat(step) {
            if (caretOffset != 0) {
                _currentDocument.removeChar(caretOffset - 1)
                rememberedOffset = maxOf(lineStartOffset, caretOffset) - 1
                if (rememberedOffset == -1) {
                    val prevLine = caretLine
                    caretLine = maxOf(0, caretLine - 1)
                    rememberedOffset = if (caretLine != prevLine) {
                        updateLine()
                        lineEndOffset
                    } else {
                        0
                    }
                }
            }
        }
    }

    fun onNewline() {
        caretLine++
        updateLine()
        rememberedOffset = lineStartOffset
    }

    fun onFileSave() {
        documentManager.saveDocument(_currentDocument)
    }

    private fun updateLine() {
        val (start, end) = _currentDocument.getLineOffsets(caretLine)
        lineStartOffset = start; lineEndOffset = end
    }

    // TODO: test only
    fun purge() {
        _currentDocument = DocumentImpl()
        caretLine = 0
        lineStartOffset = 0
        lineEndOffset = 0
        rememberedOffset = 0
        text.value = ""
    }
}