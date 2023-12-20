package util.watchService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolute
import kotlin.io.path.isDirectory

/**
 * Wrapper channel for Java's [WatchService] to
 * make it slightly more usable with Kotlin and its coroutines especially
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyWatchService(
    scope: CoroutineScope, private val channel: Channel<MyWatchEvent> = Channel()
) : Channel<MyWatchEvent> by channel {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val watchKeys = mutableListOf<WatchKey>()

    fun subscribe(pathToSubscribe: Path, recursive: Boolean) {
        val path = if (pathToSubscribe.isDirectory()) pathToSubscribe.absolute() else pathToSubscribe.absolute().parent ?: Path.of(".")
        if (recursive) {
            Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    watchKeys.add(
                        dir.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY
                        )
                    )
                    return super.preVisitDirectory(dir, attrs)
                }
            })
        } else {
            watchKeys.add(
                path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )
            )
        }
    }

    init {
        scope.launch(Dispatchers.IO) {
            channel.send(MyWatchEvent(Path.of(""), MyWatchEvent.Kind.ChannelInitialized))
            while (!isClosedForSend) {
                val watchKey = watchService.take()
                val dirPath = watchKey.watchable() as? Path ?: break
                watchKey.pollEvents().forEach {
                    val eventPath = dirPath.resolve(it.context() as Path)
                    val eventType = when (it.kind()) {
                        StandardWatchEventKinds.ENTRY_CREATE -> MyWatchEvent.Kind.Created
                        StandardWatchEventKinds.ENTRY_DELETE -> MyWatchEvent.Kind.Deleted
                        StandardWatchEventKinds.ENTRY_MODIFY -> MyWatchEvent.Kind.Modified
                        else -> MyWatchEvent.Kind.Modified
                    }

                    channel.send(MyWatchEvent(eventPath, eventType))
                }

                if (!watchKey.reset()) {
                    watchKey.cancel()
                    close()
                    break
                }
            }
        }
    }

    override fun close(cause: Throwable?): Boolean {
        watchKeys.onEach { it.cancel() }
        watchService.close()

        return channel.close(cause)
    }
}