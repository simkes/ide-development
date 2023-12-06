package vfs

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.lang.RuntimeException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

class DiskVirtualFile(val path: Path, private val vfs: VirtualFileSystem) :
    VirtualFile {
    private var content: MutableStateFlow<ByteArray>
    private val children: MutableMap<URI, VirtualFile> = mutableMapOf()
    override val contentsFlow: StateFlow<ByteArray>
        get() = content.asStateFlow()

    override val uri: URI = path.toUri()

    override fun isDirectory() = path.isDirectory()

    override fun isValid() = path.exists()

    override fun getName() = path.fileName.toString()

    init {
        content = MutableStateFlow(getBinaryContentFromSource())
    }

    override fun getBinaryContentFromSource(): ByteArray {
        return vfs.runIORead { path.inputStream().readAllBytes() }
    }

    override fun setBinaryContent(newContent: ByteArray) {
        content.update { newContent }
    }

    override fun getBinaryContent(): ByteArray = content.value

    override fun setBinaryContentInSource(newContent: ByteArray) {
        setBinaryContent(newContent)
        vfs.runIOWrite {
            path.outputStream().write(content.value)
        }
    }

    override fun createChildFile(childName: String): VirtualFile {
        if (!isDirectory()) {
            throw FileIsNotDirectoryException(this)
        }
        if (!isValid()) {
            throw FileIsNotInValidStateException(this)
        }
        if (childName.contains('\\')) {
            throw IncorrectFileNameException(childName)
        }
        val childPath = path.toAbsolutePath().resolve(childName)
        if (childPath.exists()) {
            throw FileAlreadyExistsException(childPath.toUri())
        }
        val createdFile = Files.createFile(childPath)
        if (createdFile.exists() && createdFile.isRegularFile()) {
            val createdVirtualFile = DiskVirtualFile(createdFile, vfs)
            children[createdFile.toUri()] = createdVirtualFile
            return createdVirtualFile
        } else {
            throw FileCreationFailedException(createdFile.toUri())
        }
    }

    override fun createChildDir(childName: String): VirtualFile {
        if (!isDirectory()) {
            throw FileIsNotDirectoryException(this)
        }
        if (!isValid()) {
            throw FileIsNotInValidStateException(this)
        }
        if (childName.contains('\\')) {
            throw IncorrectFileNameException(childName)
        }
        val childPath = path.toAbsolutePath().resolve(childName)
        if (childPath.exists()) {
            throw FileAlreadyExistsException(childPath.toUri())
        }
        val createdDir = Files.createDirectory(childPath)
        if (createdDir.exists() && createdDir.isDirectory()) {
            val createdVirtualFile = DiskVirtualFile(createdDir, vfs)
            children[createdDir.toUri()] = createdVirtualFile
            return createdVirtualFile
        } else {
            throw FileCreationFailedException(createdDir.toUri())
        }
    }

    override fun move(newParent: VirtualFile) {
        if (!newParent.isDirectory()) {
            throw FileIsNotDirectoryException(newParent)
        }
        if (!isValid()) {
            throw FileIsNotInValidStateException(this)
        }
        if (newParent !is DiskVirtualFile) {
            throw FileFromWrongFilesystemException(newParent)
        }
        val newPath = newParent.path.resolve(path.fileName)
        if (newPath.exists()) {
            throw FileAlreadyExistsException(newPath.toUri())
        }
        Files.move(path, newPath)
    }

    override fun copy(newParent: VirtualFile, copyName: String) {
        if (!newParent.isDirectory()) {
            throw FileIsNotDirectoryException(newParent)
        }
        if (!isValid()) {
            throw FileIsNotInValidStateException(this)
        }
        if (newParent !is DiskVirtualFile) {
            throw FileFromWrongFilesystemException(newParent)
        }
        val newPath = newParent.path.resolve(copyName)
        if (newPath.exists()) {
            throw FileAlreadyExistsException(newPath.toUri())
        }
        Files.copy(path, newPath)
    }

    override fun delete() {
        if (!isValid()) {
            throw FileIsNotInValidStateException(this)
        }
        Files.delete(path)
    }

    override fun rename(newName: String) {
        val newPath = path.parent.resolve(newName)
        if (newPath.exists()) {
            throw FileAlreadyExistsException(newPath.toUri())
        }
        Files.move(path, newPath)
    }

    @OptIn(FlowPreview::class)
    override suspend fun subscribe(flow: StateFlow<String>) {
        flow.debounce(1000).collect { str ->
            content.update { str.encodeToByteArray() }
            println("Updated contents of the file")
        }
    }
}