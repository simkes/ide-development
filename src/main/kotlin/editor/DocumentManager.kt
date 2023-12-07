package editor

import vfs.VirtualFile

interface DocumentManager {
    fun openDocument(virtualFile: VirtualFile): Document
    fun saveDocument(document: Document)
    fun saveDocuments()
    fun closeDocument(document: Document)
    fun closeDocument(correspondingVFile: VirtualFile)

    val openedDocuments: List<Document>
    var currentDocument: Document

}