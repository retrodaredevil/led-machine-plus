package me.retrodaredevil.led.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory


fun startServer() {
    val logger = LoggerFactory.getLogger("me.retrodaredevil.led.server.LedServer")
    val server = embeddedServer(Netty, 7001) {
        install(DefaultHeaders)
        install(Compression)
        install(CallLogging)

        routing {
            get("/") {
                call.respondText("Hello there", ContentType.Text.Html)
            }
            post("/graphql") {
                val query = call.receive(GraphQLRequest::class).query
                logger.info("Query is: $query\n")
                val schema = PersonStorage.getPersonSchema()
                logger.info("Schema is\n" + schema.print() + "\n")
                /* If _service is null because SDL creation failed, maybe sending the broken sdl from schema.print to Gateway API might provide
                information about validation problems. E.g.
                 call.respond(GraphQLResponse.kt(ServiceSDL(_Service(schema.print(includeDefaultSchemaDefinition = false, includeDirectives = false)))))
                */
                val builder = GraphQL.newGraphQL(schema).build()
                val result = builder.execute(query).toGraphQLResponse()
                call.respond(result)
            }
        }
    }
    server.start(wait = true)
}
