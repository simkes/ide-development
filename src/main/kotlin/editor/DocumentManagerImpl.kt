package editor

import OPENED_DOCUMENTS_LIMIT
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vfs.VirtualFile
import java.net.URI

class DocumentManagerImpl(private val scope: CoroutineScope) : DocumentManager {
    private val fileToDoc: MutableMap<VirtualFile, Document> = emptyMap<VirtualFile, Document>().toMutableMap()
    private val docToFile: MutableMap<Document, VirtualFile> = emptyMap<Document, VirtualFile>().toMutableMap()

    var currentDocument: Document = DocumentImpl(fileURI = URI("untitled"))
    override val openedDocuments get() = fileToDoc.values.toList()

    override fun openDocument(virtualFile: VirtualFile): Document {
        if (fileToDoc.keys.contains(virtualFile)) {
            currentDocument = fileToDoc[virtualFile]!!
            return fileToDoc[virtualFile]!!
        }

        val document = DocumentImpl(virtualFile.getBinaryContent().decodeToString(), virtualFile.uri)

        if (fileToDoc.size == OPENED_DOCUMENTS_LIMIT) {
            val virtualFileOfCurrentDocument = docToFile[currentDocument]!!
            saveDocument(currentDocument)
            closeDocument(currentDocument)

            fileToDoc.remove(virtualFileOfCurrentDocument)
            docToFile.remove(currentDocument)
        }

        scope.launch(Dispatchers.IO) {
            virtualFile.subscribe(document.observableText)
        }

        // TODO: subscribe document to changes from the source file, prompting user to choose one version

        fileToDoc[virtualFile] = document
        docToFile[document] = virtualFile

        currentDocument = document
        currentDocument.caretModel.updateLine()

        return document
    }

    override fun saveDocument(document: Document) {
        val virtualFile = docToFile[document] ?: (TODO("File relation is missing?"))
        virtualFile.setBinaryContent(document.observableText.value.toByteArray())
    }
    override fun saveDocuments() {}
    override fun closeDocument(document: Document) {
        // TODO: serialize to some meta file
    }

}