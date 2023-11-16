package vfs

import java.nio.file.Path

class VirtualFileSystemImpl : VirtualFileSystem {
    private val snapshot: MutableSet<VirtualFile> = mutableSetOf()

    override fun getFileByPath(file: Path): VirtualFile {
        // TODO: queue
        return snapshot.find { it == file } ?: run {
            val vFile: VirtualFile = DiskVirtualFile(file, this)
            register(vFile)
            vFile
        }
    }

    // TODO: RWLock
    override fun requestIOWrite(operation: () -> Unit) {
        operation.invoke()
    }

    // TODO: RWLock
    override fun requestIORead(operation: () -> ByteArray): ByteArray {
        return operation.invoke()
    }

    override fun moveFile(virtualFile: VirtualFile, newParent: VirtualFile) {
        TODO("Not yet implemented")
    }

    override fun deleteFile(virtualFile: VirtualFile) {
        TODO("Not yet implemented")
    }

    override fun renameFile(virtualFile: VirtualFile, newName: String) {
        TODO("Not yet implemented")
    }

    override fun createChildFile(parentDir: VirtualFile, childName: String) {
        TODO("Not yet implemented")
    }

    override fun createChildDir(parentDir: VirtualFile, childName: String) {
        TODO("Not yet implemented")
    }

    private fun register(file: VirtualFile) {
        snapshot.add(file)
        // TODO: subscribe for changes from FS
    }
}