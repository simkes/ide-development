package editor

import Direction
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import highlighting.ColoredHighlighter
import highlighting.HighlighterProvider
import highlighting.UnderlinedHighlighter
import language.Level
import vfs.VirtualFileSystem
import vfs.VirtualFileSystemImpl
import java.net.URI

object EditorViewModel {
    // TODO: context object to receive control objects (Project?)
    val text = mutableStateOf("")
    val highlighters = mutableStateOf<Pair<List<ColoredHighlighter>, List<UnderlinedHighlighter>>>(Pair(listOf(), listOf()))

    @OptIn(DelicateCoroutinesApi::class)
    private val scope = GlobalScope
    val virtualFileSystem: VirtualFileSystem = VirtualFileSystemImpl(scope)

    private val documentManager: DocumentManager =
        DocumentManager(virtualFileSystem.getFile(URI("file", "", "/untitled", null)), scope).also {
            scope.launch(Dispatchers.Main) {
                it.currentDocument.observableText.collect {
                    text.value = it
                    highlighters.value = HighlighterProvider.getHighlighters(it, Level.SEMANTIC)
                }
            }
        }

    private val _currentDocument get() = documentManager.currentDocument

    var openedDocuments = mutableStateOf(documentManager.openedDocuments)

    fun onCaretMovement(direction: Direction) {
        _currentDocument.caretModel.moveCaret(direction)
    }

    fun onFileOpening(filePath: URI) {
        val virtualFile = virtualFileSystem.getFile(filePath)
        documentManager.openDocument(virtualFile)
        openedDocuments.value = documentManager.openedDocuments

        scope.launch(Dispatchers.IO) {
            _currentDocument.observableText.collect {
                text.value = it
                highlighters.value = HighlighterProvider.getHighlighters(it, Level.SEMANTIC)
            }
        }
    }

    fun onFileClosing(filePath: URI) {
        val virtualFile = virtualFileSystem.getFile(filePath)
        documentManager.closeDocument(virtualFile)
        openedDocuments.value = documentManager.openedDocuments
        scope.launch(Dispatchers.IO) {
            _currentDocument.observableText.collect {
                text.value = it
                highlighters.value = HighlighterProvider.getHighlighters(it, Level.SEMANTIC)
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

    // TODO: test only
    fun purge() {
        text.value = ""
    }
}