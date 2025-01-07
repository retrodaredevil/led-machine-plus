package me.retrodaredevil.led.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import me.retrodaredevil.led.message.DiscordMessageQueue
import me.retrodaredevil.led.message.MessageQueueCreator

/**
 * A message config represents a configuration that can create a MessageQueue. MessageQueues are for retrieving messages.
 * In the future, this interface may allow for more than just message retrieval.
 */
@JsonSubTypes(
        JsonSubTypes.Type(DiscordMessageConfig::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
sealed interface MessageConfig {
    fun toMessageQueueCreator(): MessageQueueCreator
}

// NOTE: SlackMessageConfig used to be here in previous versions.
// The setup of this file should be self-explanatory for how to add additional message config types

@JsonTypeName("discord")
class DiscordMessageConfig(
        @JsonProperty("bot_token")
        @get:JsonProperty("bot_token")
        val botToken: String,
        @JsonProperty("channel_id")
        @get:JsonProperty("channel_id")
        val channelId: Long,
) : MessageConfig {
    override fun toMessageQueueCreator(): MessageQueueCreator {
        return MessageQueueCreator {
            DiscordMessageQueue(botToken, channelId)
        }
    }
}
