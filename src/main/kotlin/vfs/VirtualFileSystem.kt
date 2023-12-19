package vfs

import java.net.URI
import java.nio.file.Path

interface VirtualFileSystem {
    fun getFile(file: URI): VirtualFile

    fun listDirectory(directory: Path): FileTreeNode

    // TODO: access model to avoid undesired ops?
    fun runIOWrite(operation: () -> Unit)
    fun runIORead(operation: () -> ByteArray): ByteArray
    fun moveFile(virtualFile: VirtualFile, newParent: VirtualFile)
    fun deleteFile(virtualFile: VirtualFile)
    fun renameFile(virtualFile: VirtualFile, newName: String)
    fun createChildFile(parentDir: VirtualFile, childName: String)
    fun createChildDir(parentDir: VirtualFile, childName: String)
}