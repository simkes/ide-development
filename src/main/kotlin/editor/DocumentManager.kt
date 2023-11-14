package editor

import vfs.VirtualFile

interface DocumentManager {
    fun openDocument(virtualFile: VirtualFile): Document
    fun saveDocument(document: Document)
    fun saveDocuments()
    fun closeDocument()

}