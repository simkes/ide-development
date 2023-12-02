package util.watchService

import java.nio.file.Path

data class MyWatchEvent (val file: Path, val kind: Kind) {
    enum class Kind {
        Created,
        Modified,
        Deleted,
        ChannelInitialized
    }
}