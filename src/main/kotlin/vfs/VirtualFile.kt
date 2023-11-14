package vfs

import java.nio.file.Path

interface VirtualFile {
    val path: Path

    fun getName(): String
    fun getBinaryContent(): ByteArray
    fun setBinaryContent(newContent: ByteArray)
}