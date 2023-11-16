package vfs

import java.nio.file.Path

interface VirtualFileSystem {
    fun getFileByPath(file: Path): VirtualFile

    // TODO: access model to avoid undesired ops?
    fun requestIOWrite(operation: () -> Unit)
    fun requestIORead(operation: () -> ByteArray): ByteArray
    fun moveFile(virtualFile: VirtualFile, newParent: VirtualFile)
    fun deleteFile(virtualFile: VirtualFile)
    fun renameFile(virtualFile: VirtualFile, newName: String)
    fun createChildFile(parentDir: VirtualFile, childName: String)
    fun createChildDir(parentDir: VirtualFile, childName: String)
}