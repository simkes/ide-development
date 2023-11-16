package vfs

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed interface VirtualFile {
    var contents: ByteArray
    val contentsFlow: StateFlow<ByteArray>
        get() = MutableStateFlow(contents).asStateFlow()

    fun getName(): String
    fun getBinaryContent(): ByteArray
    fun setBinaryContent(newContent: ByteArray)
    @OptIn(FlowPreview::class)
    suspend fun subscribe(flow: StateFlow<String>) {
        flow.debounce(500).collect {
            contents = it.encodeToByteArray()
            println("Updated contents of the file")
        }
    }
}