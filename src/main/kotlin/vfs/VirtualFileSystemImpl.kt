package vfs

import java.nio.file.Path

class VirtualFileSystemImpl : VirtualFileSystem {
    private val snapshot: MutableMap<Path, VirtualFile> = emptyMap<Path, VirtualFile>().toMutableMap()

    override fun getFileByPath(file: Path): VirtualFile {
        // TODO: queue
        return snapshot[file] ?: run {
            val vFile: VirtualFile = VirtualFileImpl(file)
            register(vFile)
            vFile
        }
    }

    private fun register(file: VirtualFile) {
        snapshot[file.path] = file
        // TODO: subscribe for changes from FS
    }
}