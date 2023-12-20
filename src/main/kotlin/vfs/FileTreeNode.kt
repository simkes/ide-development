package vfs

import java.nio.file.Path

/**
 * Simple tree representation of filesystem directory content
 *
 * It may be more natural to put [VirtualFile] here, as the [FileTreeNode] is also obtained through VFS,
 * but virtual files seem to be too powerful objects to put in a static file tree.
 */
data class FileTreeNode(
    val path: Path,
    var kind: TreeNodeKind,
    var children: List<FileTreeNode>,
    val gatherContent: FileTreeNode.() -> Unit
) {
    enum class TreeNodeKind {
        DIRECTORY,
        UNINITIALIZED,
        FILE
    }

    val fileName: Path = path.fileName
}