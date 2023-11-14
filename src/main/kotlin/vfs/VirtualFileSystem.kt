package vfs

import java.nio.file.Path

interface VirtualFileSystem {
    fun getFileByPath(file: Path): VirtualFile

}