package editor

import OPENED_DOCUMENTS_LIMIT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vfs.VirtualFile

class DocumentManager(initialFile: VirtualFile, private val scope: CoroutineScope) {
    private val fileToDoc: MutableMap<VirtualFile, Document> = emptyMap<VirtualFile, Document>().toMutableMap()
    private val docToFile: MutableMap<Document, VirtualFile> = emptyMap<Document, VirtualFile>().toMutableMap()

    var currentDocument: Document = openDocument(initialFile)
        set(value) = run {
            field = value
            field.caretModel.updateLine()
        }

    val openedDocuments get() = fileToDoc.values.toList()

    fun openDocument(virtualFile: VirtualFile): Document {
        if (fileToDoc.keys.contains(virtualFile)) {
            currentDocument = fileToDoc[virtualFile]!!
            return fileToDoc[virtualFile]!!
        }

        val document = Document(virtualFile.getBinaryContent().decodeToString(), virtualFile.uri)

        if (fileToDoc.size == OPENED_DOCUMENTS_LIMIT) {
            saveDocument(currentDocument)
            closeDocument(currentDocument)
        }

        scope.launch(Dispatchers.IO) {
            virtualFile.subscribe(document.observableText)
        }

        // TODO: subscribe document to changes from the source file, prompting user to choose one version

        fileToDoc[virtualFile] = document
        docToFile[document] = virtualFile

        currentDocument = document

        return document
    }

    fun saveDocument(document: Document) {
        val virtualFile = docToFile[document] ?: (TODO("File relation is missing?"))
        virtualFile.setBinaryContent(document.observableText.value.toByteArray())
    }

    private fun closeDocument(document: Document) {
        val correspondingVFile = docToFile[document]!!
        fileToDoc.remove(correspondingVFile)
        docToFile.remove(document)
        if (document == currentDocument) {
            currentDocument = fileToDoc.values.last()
        }
        // TODO: serialize to some meta file
    }

    fun closeDocument(correspondingVFile: VirtualFile) {
        closeDocument(fileToDoc[correspondingVFile]!!)
    }

}