package ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Processor of UI events. Uses channel to enqueue events which later will be executed concurrently.
 */
class UiEventProcessor(private val scope: CoroutineScope) {
    private val eventChannel: Channel<UiEvent> = Channel()
    private lateinit var job: Job

    fun newEvent(event: UiEvent) {
        scope.launch {
            eventChannel.send(event)
        }
    }

    fun startEventProcessing() {
        job = scope.launch {
            for (event in eventChannel) {
                event.process()
            }
        }
    }

    fun stopEventProcessing() {
        job.cancel()
        scope.launch {
            eventChannel.close()
        }
    }
}