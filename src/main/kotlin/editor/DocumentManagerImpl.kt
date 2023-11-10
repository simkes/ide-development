package editor

import vfs.VirtualFile

class DocumentManagerImpl : DocumentManager {
    // TODO: bidi map
    private val fileToDoc: MutableMap<VirtualFile, Document> = emptyMap<VirtualFile, Document>().toMutableMap()
    private val docToFile: MutableMap<Document, VirtualFile> = emptyMap<Document, VirtualFile>().toMutableMap()

    override fun openDocument(virtualFile: VirtualFile): Document {
        if (fileToDoc.keys.contains(virtualFile)) {
            return fileToDoc[virtualFile]!!
        }

        val document = DocumentImpl(virtualFile.getBinaryContent().toString())
        fileToDoc[virtualFile] = document
        docToFile[document] = virtualFile

        return document
    }

    override fun saveDocument(document: Document) {
        val virtualFile = docToFile[document] ?: (TODO("File relation is missing?"))
        virtualFile.setBinaryContent(document.text.toByteArray())
    }
    override fun saveDocuments() {}
    override fun closeDocument() {
        TODO("Not yet implemented")
    }

}