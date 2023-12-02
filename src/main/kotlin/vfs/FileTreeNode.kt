package vfs

import java.nio.file.Path

/**
 * Simple tree representation of filesystem directory content
 *
 * It may be more natural to put [VirtualFile] here, as the [FileTreeNode] is also obtained through VFS,
 * but virtual files seem to be too powerful objects to put in a static file tree.
 */
data class FileTreeNode(val path: Path, val kind: TreeNodeKind, val children: List<FileTreeNode>) {
    enum class TreeNodeKind {
        DIRECTORY,
        FILE
    }
    val fileName: Path = path.fileName
}