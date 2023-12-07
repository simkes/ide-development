package editor

import Direction
import OPENED_DOCUMENTS_LIMIT
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import vfs.VirtualFileSystem
import vfs.VirtualFileSystemImpl
import java.net.URI
import javax.print.Doc

object EditorViewModel {
    // TODO: context object to receive control objects (Project?)
    val text = mutableStateOf("")
    val highlighters get() = _currentDocument.highlighters


    @OptIn(DelicateCoroutinesApi::class)
    private val scope = GlobalScope

    private val documentManager: DocumentManager = DocumentManagerImpl(scope).also {
        scope.launch(Dispatchers.Main) {
            it.currentDocument.observableText.collect {
                text.value = it
            }
        }
    }

    val virtualFileSystem: VirtualFileSystem = VirtualFileSystemImpl(scope)
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

    fun onTextDeletion(step: Int = 1) {
        repeat(step) {
            _currentDocument.removeChar()
        }
    }

    fun onNewline() {
        _currentDocument.caretModel.newline()
    }

    fun onFileSave() {
        documentManager.saveDocument(_currentDocument)
    }

    // TODO: test only
    fun purge() {
        text.value = ""
    }
}