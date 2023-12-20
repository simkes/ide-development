package editor

import OPENED_DOCUMENTS_LIMIT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vfs.VirtualFile

class DocumentManager(private val scope: CoroutineScope) {
    private val fileToDoc: MutableMap<VirtualFile, IDocument> = emptyMap<VirtualFile, IDocument>().toMutableMap()
    private val docToFile: MutableMap<IDocument, VirtualFile> = emptyMap<IDocument, VirtualFile>().toMutableMap()

    var currentDocument: IDocument = DummyDocument

    val openedDocuments get() = fileToDoc.values.toList()

    fun openDocument(virtualFile: VirtualFile): IDocument {
        if (fileToDoc.keys.contains(virtualFile)) {
            currentDocument = fileToDoc[virtualFile]!!
            currentDocument.caretModel.updateLine()
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
        currentDocument.caretModel.updateLine()

        return document
    }

    fun saveDocument(document: IDocument) {
        val virtualFile = docToFile[document] ?: (TODO("File relation is missing?"))
        virtualFile.setBinaryContent(document.observableText.value.toByteArray())
    }

    private fun closeDocument(document: IDocument) {
        val correspondingVFile = docToFile[document]!!
        fileToDoc.remove(correspondingVFile)
        docToFile.remove(document)
        if (document == currentDocument) {
            if (fileToDoc.isNotEmpty()) {
                currentDocument = fileToDoc.values.last()
                currentDocument.caretModel.updateLine()
            } else {
                currentDocument = DummyDocument
            }
        }
        // TODO: serialize to some meta file
    }

    fun closeDocument(correspondingVFile: VirtualFile) {
        closeDocument(fileToDoc[correspondingVFile]!!)
    }

}