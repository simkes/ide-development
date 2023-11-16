package editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vfs.VirtualFile

class DocumentManagerImpl(private val scope: CoroutineScope) : DocumentManager {
    // TODO: bidi map
    private val fileToDoc: MutableMap<VirtualFile, Document> = emptyMap<VirtualFile, Document>().toMutableMap()
    private val docToFile: MutableMap<Document, VirtualFile> = emptyMap<Document, VirtualFile>().toMutableMap()

    override fun openDocument(virtualFile: VirtualFile): Document {
        if (fileToDoc.keys.contains(virtualFile)) {
            return fileToDoc[virtualFile]!!
        }

        val document = DocumentImpl(virtualFile.getBinaryContent().decodeToString())

        scope.launch(Dispatchers.IO) {
            virtualFile.subscribe(document.observableText)
        }
        scope.launch {
            document.subscribe(virtualFile.contentsFlow)
        }

        fileToDoc[virtualFile] = document
        docToFile[document] = virtualFile

        return document
    }

    override fun saveDocument(document: Document) {
        val virtualFile = docToFile[document] ?: (TODO("File relation is missing?"))
        virtualFile.setBinaryContent(document.observableText.value.toByteArray())
    }
    override fun saveDocuments() {}
    override fun closeDocument() {
        TODO("Not yet implemented")
    }

}