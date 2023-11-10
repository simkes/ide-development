package editor

import Direction
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import vfs.VirtualFileSystem
import vfs.VirtualFileSystemImpl
import java.nio.file.Path

object EditorViewModel {
    // TODO: context object to receive control objects (Project?)
    private var _currentDocument: Document = DocumentImpl()
    private val documentManager: DocumentManager = DocumentManagerImpl()
    private val virtualFileSystem: VirtualFileSystem = VirtualFileSystemImpl()

    private var caretOffset = 0
    private var caretLine = 0

    private var lineStartOffset = 0
    private var lineEndOffset = 0

    private val lineLength get() = lineEndOffset - lineStartOffset

    fun onCaretMovement(direction: Direction) {
        when (direction) {
            Direction.UP -> {
                val prevLine = caretLine
                val relativeCaretOffset = caretOffset - lineStartOffset
                caretLine = maxOf(0, caretLine - 1)
                if (caretLine != prevLine) {
                    updateLine()
                    caretOffset = minOf(lineStartOffset + relativeCaretOffset, lineEndOffset)
                }
            }

            Direction.DOWN -> {
                val prevLine = caretLine
                val relativeCaretOffset = caretOffset - lineStartOffset
                caretLine = minOf(_currentDocument.getLineCount(), caretLine + 1)
                if (caretLine != prevLine) {
                    updateLine()
                    caretOffset = minOf(lineStartOffset + relativeCaretOffset, lineEndOffset)
                }
            }

            Direction.LEFT -> {
                caretOffset = maxOf(caretOffset - 1, 0)
                if (caretOffset < lineStartOffset) {
                    caretLine = maxOf(0, caretLine - 1)
                    updateLine()
                }
            }

            Direction.RIGHT -> {
                caretOffset = minOf(caretOffset + 1, _currentDocument.text.length)
                if (caretOffset > lineEndOffset) {
                    caretLine = minOf(_currentDocument.getLineCount(), caretLine + 1)
                    updateLine()
                }
            }
        }
    }

    fun onFileOpening(filePath: String) {
        val path = Path.of(filePath)
        val virtualFile = virtualFileSystem.getFileByPath(path)
        _currentDocument = documentManager.openDocument(virtualFile)
        text.update { _currentDocument.observableText }
    }

    fun onCharInsertion(char: Char) {
        _currentDocument.insertChar(char, caretOffset)
        caretOffset++
    }

    fun getCaret(): Pair<Int, Int> = Pair(caretLine, caretOffset - lineStartOffset)

    fun onTextDeletion(step: Int = 1) {
        repeat(step) {
            if (caretOffset != 0) {
                _currentDocument.removeChar(caretOffset - 1)
                caretOffset--
            }
        }
    }

    fun onNewline() {
        caretLine++
        updateLine()
    }

    private fun updateLine() {
        val (start, end) = _currentDocument.getLineOffsets(caretLine)
        lineStartOffset = start; lineEndOffset = end
    }

    var text = MutableStateFlow(_currentDocument.observableText)
}