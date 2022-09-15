package me.retrodaredevil.led.message

import com.slack.api.Slack
import com.slack.api.socket_mode.SocketModeClient
import com.slack.api.socket_mode.request.EventsApiEnvelope
import com.slack.api.socket_mode.response.AckResponse
import me.retrodaredevil.util.getLogger
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class SlackMessageQueue(
        private val slack: Slack,
        private val appToken: String,
        private val channel: String
) : MessageQueue {

    private val thread: Thread = Thread(this::runThread)

    private val queue: Queue<Message> = ConcurrentLinkedQueue()

    private var client: SocketModeClient? = null

    init {
        thread.isDaemon = true // If for some reason the thread does not stop, don't let that stop the JVM from exiting
        thread.start()
    }


    private fun runThread() {
        initClient()
    }
    private fun initClient() {
        val currentClient = client
        if (currentClient != null) {
            try {
                currentClient.close()
            } catch (ex: IOException) {
                LOGGER.error("Could not close current client", ex)
            }
            this.client = null
        }
        var client: SocketModeClient? = null
        while (client == null) {
            if (Thread.currentThread().isInterrupted) {
                LOGGER.debug("Thread was interrupted.. Slack client never initialized")
                return
            }
            try {
                client = slack.socketMode(appToken, SocketModeClient.Backend.JavaWebSocket)
            } catch (ex: IOException) {
                LOGGER.warn("Error in initial connect to slack. Will try again", ex)
                try {
                    Thread.sleep(5000)
                } catch (ex: InterruptedException) {
                    LOGGER.debug("Interrupted while sleeping. Slack client never initialized")
                    return
                }
            }
        }
        client.isAutoReconnectEnabled = true
        client.addEventsApiEnvelopeListener { eventsApiEnvelope ->
            val ack = AckResponse.builder().envelopeId(eventsApiEnvelope.envelopeId).build()
            client.sendSocketModeResponse(ack)
            handle(eventsApiEnvelope)
        }
        this.client = client
    }
    private fun handle(eventsApiEnvelope: EventsApiEnvelope) {
        if ("events_api" == eventsApiEnvelope.type) {
            val payload = eventsApiEnvelope.payload.asJsonObject
            val eventObject = payload.getAsJsonObject("event")
            if (eventObject["bot_id"] != null) {
                // don't process messages sent by a bot (potentially use in the future)
                return
            }
            if ("message" == eventObject["type"].asString && eventObject["subtype"] == null) {
                val text = eventObject["text"].asString
                val timestamp: Instant = epochSecondsToInstant(eventObject["ts"].asBigDecimal)
                val userId = eventObject["user"].asString
                LOGGER.debug("Got text: $text from $userId at $timestamp")
                val message = Message(text, timestamp)
                queue.add(message)
            }
        }
    }

    override fun popNewMessages(): List<Message> {
        val r = mutableListOf<Message>()
        while (queue.isNotEmpty()) {
            r.add(queue.poll())
        }
        return r
    }

    @Throws(Exception::class)
    override fun close() {
        if (thread.isAlive) {
            thread.interrupt()
        }
        client?.close()
        slack.close()
    }

    companion object {
        private val LOGGER = getLogger()

        private fun epochSecondsToInstant(timestampBigDecimal: BigDecimal): Instant {
            val nanos = timestampBigDecimal.multiply(BigDecimal(1000000)).remainder(BigDecimal(1000)).toLong() * 1000
            // convert epoch millis to milliseconds, then add additional nanoseconds
            return Instant.ofEpochMilli(timestampBigDecimal.multiply(BigDecimal(1000)).toLong())
                    .plusNanos(nanos)
        }
    }
}
