package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vfs.FileTreeNode

class FileNavigator(private val onFileClick: (FileTreeNode) -> Unit) {
    @Composable
    fun Tree(treeNode: FileTreeNode, modifier: Modifier) {
        val expandedNodes = remember { mutableStateListOf(treeNode) }
        LazyColumn(modifier = modifier) {
            Node(
                treeNode,
                1,
                isExpanded = {
                    expandedNodes.contains(it)
                },
                switchExpanded = {
                    if (expandedNodes.contains(it)) {
                        expandedNodes.remove(it)
                    } else {
                        expandedNodes.add(it)
                    }
                },
            )
        }
    }

    private fun LazyListScope.Nodes(nodes: List<FileTreeNode>, depth: Int, isExpanded: (FileTreeNode) -> Boolean, switchExpanded: (FileTreeNode) -> Unit) {
        nodes.forEach {
            Node(it, depth, isExpanded, switchExpanded)
        }
    }

    private fun LazyListScope.Node(node: FileTreeNode, depth: Int, isExpanded: (FileTreeNode) -> Boolean, switchExpanded: (FileTreeNode) -> Unit) {
        val modifier = if (node.kind == FileTreeNode.TreeNodeKind.DIRECTORY) {
            Modifier.clickable {
                switchExpanded(node)
            }
        } else {
            Modifier.clickable {
                onFileClick(node)
            }
        }
        item {
            Row {
                Spacer(modifier = Modifier.width(8.dp * depth))
                Text(
                    node.fileName.toString(),
                    modifier = modifier
                )
            }
        }
        if (isExpanded(node)) {
            Nodes(
                node.children.sortedBy { it.fileName },
                depth + 1,
                isExpanded,
                switchExpanded
            )
        }
    }
}