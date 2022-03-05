package me.retrodaredevil.led.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.slack.api.Slack
import com.slack.api.SlackConfig
import com.slack.api.util.http.SlackHttpClient
import me.retrodaredevil.led.message.MessageQueueCreator
import me.retrodaredevil.led.message.SlackMessageQueue
import okhttp3.OkHttpClient
import java.time.Duration

/**
 * A message config represents a configuration that can create a MessageQueue. MessageQueues are for retrieving messages.
 * In the future, this interface may allow for more than just message retrieval.
 */
@JsonSubTypes(
        JsonSubTypes.Type(SlackMessageConfig::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
sealed interface MessageConfig {
    fun toMessageQueueCreator(): MessageQueueCreator
}

@JsonTypeName("slack")
class SlackMessageConfig(
        @JsonProperty("bot_token")
        @get:JsonProperty("bot_token")
        val botToken: String,

        @JsonProperty("app_token")
        @get:JsonProperty("app_token")
        val appToken: String,

        @JsonProperty("channel")
        @get:JsonProperty("channel")
        val channel: String,
) : MessageConfig {
    override fun toMessageQueueCreator(): MessageQueueCreator {
        return MessageQueueCreator {

            // we don't need to use the botToken because we currently don't send anything

            val slack = Slack.getInstance(SlackConfig(), SlackHttpClient(OkHttpClient.Builder()
                    .callTimeout(Duration.ofSeconds(10))
                    .connectTimeout(Duration.ofSeconds(4))
                    .build()))
            SlackMessageQueue(slack, appToken, channel)
        }
    }
}
