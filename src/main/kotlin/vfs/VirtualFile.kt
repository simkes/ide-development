package vfs

import kotlinx.coroutines.flow.*

sealed interface VirtualFile {
    val contentsFlow: StateFlow<ByteArray>

    fun isDirectory(): Boolean
    fun isValid(): Boolean

    fun getName(): String
    fun getBinaryContent(): ByteArray
    fun getBinaryContentFromSource(): ByteArray
    fun setBinaryContent(newContent: ByteArray)
    fun setBinaryContentInSource(newContent: ByteArray)
    fun createChildFile(childName: String): VirtualFile
    fun createChildDir(childName: String): VirtualFile
    fun move(newParent: VirtualFile)
    fun copy(newParent: VirtualFile, copyName: String)
    fun delete()
    fun rename(newName: String)
    suspend fun subscribe(flow: StateFlow<String>)
}