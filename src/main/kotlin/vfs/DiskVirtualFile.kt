package vfs

import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class PhysicalDiskFile(private val path: Path, private val vfs: VirtualFileSystem) : VirtualFile {
    override lateinit var contents: ByteArray
    override fun getName() = path.fileName.toString()

    init {
        contents = getBinaryContent()
    }

    override fun getBinaryContent(): ByteArray {
        return vfs.requestIORead { path.inputStream().readAllBytes() }
    }

    override fun setBinaryContent(newContent: ByteArray) {
        vfs.requestIOWrite {
            path.outputStream().write(newContent)
        }
    }
}