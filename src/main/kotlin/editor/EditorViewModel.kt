package editor

import Direction
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import highlighting.ColoredHighlighter
import highlighting.HighlighterProvider
import highlighting.UnderlinedHighlighter
import language.Level
import java.net.URI

class EditorViewModel(private val scope: CoroutineScope) {
    val text = mutableStateOf("")
    var highlighters: Pair<List<ColoredHighlighter>, List<UnderlinedHighlighter>> = (Pair(listOf(), listOf()))
    private val virtualFileSystem get() = App.vfs

    private val documentManager: DocumentManager = DocumentManager(scope)

    private val _currentDocument get() = documentManager.currentDocument

    var openedDocuments = mutableStateOf(documentManager.openedDocuments)

    fun onCaretMovement(direction: Direction) {
        _currentDocument.caretModel.moveCaret(direction)
    }

    fun onFileOpening(filePath: URI) {
        val virtualFile = virtualFileSystem.getFile(filePath)
        documentManager.openDocument(virtualFile)
        openedDocuments.value = documentManager.openedDocuments

        scope.launch(Dispatchers.Default) {
            _currentDocument.observableText.collect {
                highlighters = HighlighterProvider.getHighlighters(it, Level.SEMANTIC)
                text.value = it
            }
        }
    }

    fun onFileClosing(filePath: URI) {
        val virtualFile = virtualFileSystem.getFile(filePath)
        documentManager.closeDocument(virtualFile)
        openedDocuments.value = documentManager.openedDocuments
        if (_currentDocument != null) {
            scope.launch(Dispatchers.Default) {
                _currentDocument.observableText.collect {
                    highlighters = HighlighterProvider.getHighlighters(it, Level.SEMANTIC)
                    text.value = it
                }
            }
        }
    }

    fun onCharInsertion(char: Char) {
        _currentDocument.insertChar(char)
    }

    fun onTextInsertion(text: String) {
        _currentDocument.insertText(text)
    }

    fun getCaret(): Pair<Int, Int> = with(_currentDocument.caretModel) {
        Pair(this.caretLine, this.caretOffset)
    }

    fun setCaret(line: Int, offset: Int) {
        _currentDocument.caretModel.setCaret(line, offset)
    }

    fun onTextDeletion(step: Int = 1) {
        repeat(step) {
            _currentDocument.removeChar()
        }
    }

    fun onNewline() {
        onCharInsertion('\n')
        _currentDocument.caretModel.newline()
    }

    fun onFileSave() {
        documentManager.saveDocument(_currentDocument)
    }

    fun getLineStart(line: Int) = _currentDocument.getLineStartOffset(line)
    fun getLineNumber(offset: Int) = _currentDocument.getLineNumber(offset)

}