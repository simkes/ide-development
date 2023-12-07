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

    private var _currentDocument: Document = DocumentImpl(fileURI = URI("")).also {
        scope.launch(Dispatchers.Main) {
            it.observableText.collect {
                text.value = it
            }
        }
    }

    private val documentManager: DocumentManager = DocumentManagerImpl(scope)
    val virtualFileSystem: VirtualFileSystem = VirtualFileSystemImpl(scope)

    var openedDocuments = mutableStateOf(documentManager.openedDocuments)

    fun onCaretMovement(direction: Direction) {
        _currentDocument.caretModel.moveCaret(direction)
    }

    fun onFileOpening(filePath: URI) {
        val virtualFile = virtualFileSystem.getFile(filePath)
        _currentDocument = documentManager.openDocument(virtualFile)
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
        _currentDocument = DocumentImpl(fileURI = URI(""))
        text.value = ""
    }
}