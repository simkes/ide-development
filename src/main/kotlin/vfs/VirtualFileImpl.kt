package vfs

import java.nio.file.Path
import kotlin.io.path.inputStream

class VirtualFileImpl(override var path: Path) : VirtualFile {
    override fun getName() = path.fileName.toString()

    override fun getBinaryContent(): ByteArray {
        return path.inputStream().readAllBytes()
    }

    override fun setBinaryContent(newContent: ByteArray) {
        TODO("Not yet implemented")
    }
}