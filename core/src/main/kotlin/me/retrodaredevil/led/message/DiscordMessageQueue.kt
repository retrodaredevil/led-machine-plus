package me.retrodaredevil.led.message

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.reaction.ReactionEmoji
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import me.retrodaredevil.util.getLogger
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class DiscordMessageQueue(
        botToken: String,
        private val channelId: Long
) : MessageQueue {
    companion object {
        private val LOGGER = getLogger()
    }

    private val thread: Thread = Thread(this::runThread)

    private val queue: Queue<Message> = ConcurrentLinkedQueue()
    private var client: DiscordClient = DiscordClientBuilder.create(botToken).build()

    init {
        thread.isDaemon = true
        thread.start()
    }

    private fun runThread() {
        initClient()
    }
    private fun initClient() {
        client.withGateway { client ->
            mono {
                client.on(MessageCreateEvent::class.java)
                        .asFlow()
                        .collect {
                            val discordMessage = it.message
                            if (discordMessage.channelId.asLong() == channelId) {
                                val message = Message(discordMessage.content, discordMessage.timestamp)
                                queue.add(message)

                                val heartReaction = ReactionEmoji.unicode("\u2764")
                                try {
                                    discordMessage.addReaction(heartReaction).awaitSingleOrNull()
                                } catch (e: Exception) {
                                    // The type of this exception is likely a ClientException, but for now catch everything just in case
                                    LOGGER.warn("Was unable to add reaction", e)
                                }
                            }
                        }
            }
        }.block()
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
        thread.interrupt()
    }
}
