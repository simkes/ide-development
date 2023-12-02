package vfs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import util.watchService.MyWatchEvent
import util.watchService.MyWatchService
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

class VirtualFileSystemImpl(scope: CoroutineScope) : VirtualFileSystem {
    private val snapshot: MutableMap<URI, VirtualFile> = mutableMapOf()
    private val watchService = MyWatchService(scope)

    private fun getFileByPath(file: Path): VirtualFile {
        // TODO: queue
        return snapshot[file.toUri()] ?: run {
            val vFile: VirtualFile = DiskVirtualFile(file, this)
            register(vFile)
            vFile
        }
    }

    override fun getFile(file: URI): VirtualFile {
        return when (file.scheme) {
            "file" -> getFileByPath(Path.of(file.path))
            else -> {
                throw UnsupportedResourceSchemaException(file.scheme)
            }
        }
    }

    override fun listDirectory(directory: Path): FileTreeNode {
        val children = mutableListOf<FileTreeNode>()
        directory.listDirectoryEntries().forEach {
            if (it.isRegularFile()) {
                children.add(FileTreeNode(it, FileTreeNode.TreeNodeKind.FILE, listOf()))
            } else {
                children.add(listDirectory(it))
            }
        }
        return FileTreeNode(directory, FileTreeNode.TreeNodeKind.DIRECTORY, children)
    }

    init {
        scope.launch(Dispatchers.IO) {
            for (event in watchService) {
                when (event.kind) {
                    MyWatchEvent.Kind.Deleted -> {}
                    MyWatchEvent.Kind.Created -> {}
                    MyWatchEvent.Kind.Modified -> {
                        snapshot[event.file.toUri()]?.setBinaryContent(
                            runIORead {
                                event.file.inputStream().readAllBytes()
                            }
                        )// TODO: java allows to subscribe only to dir changes?
                    }

                    MyWatchEvent.Kind.ChannelInitialized -> {
                        println("Channel initialized successfully!")
                    }
                }
            }
        }
    }

    // TODO: RWLock
    override fun runIOWrite(operation: () -> Unit) {
        operation.invoke()
    }

    // TODO: RWLock
    override fun runIORead(operation: () -> ByteArray): ByteArray {
        return operation.invoke()
    }

    override fun moveFile(virtualFile: VirtualFile, newParent: VirtualFile) {
        runIOWrite {
            virtualFile.move(newParent)
        }
    }

    override fun deleteFile(virtualFile: VirtualFile) {
        runIOWrite {
            virtualFile.delete()
        }
    }

    override fun renameFile(virtualFile: VirtualFile, newName: String) {
        runIOWrite {
            virtualFile.rename(newName)
        }
    }

    override fun createChildFile(parentDir: VirtualFile, childName: String) {
        runIOWrite {
            parentDir.createChildFile(childName)
        }
    }

    override fun createChildDir(parentDir: VirtualFile, childName: String) {
        runIOWrite {
            parentDir.createChildDir(childName)
        }
    }

    private fun register(file: VirtualFile) {
        if (file is DiskVirtualFile) {
            snapshot[file.path.toUri()] = file
            val filePath = file.path
            watchService.subscribe(filePath, false)
        }
    }
}