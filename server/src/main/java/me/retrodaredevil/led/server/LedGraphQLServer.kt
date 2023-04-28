package me.retrodaredevil.led.server

import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.federation.FederatedSchemaGeneratorConfig
import com.expediagroup.graphql.generator.federation.FederatedSchemaGeneratorHooks
import com.expediagroup.graphql.generator.federation.toFederatedSchema
import com.expediagroup.graphql.federation.execution.FederatedTypeRegistry
import graphql.schema.GraphQLSchema

class LedGraphQLServer {
// https://github.com/ExpediaGroup/graphql-kotlin/blob/master/examples/server/ktor-server/src/main/kotlin/com/expediagroup/graphql/examples/server/ktor/ktorGraphQLSchema.kt

    private val config = FederatedSchemaGeneratorConfig(
            supportedPackages = listOf("de.hello"),
            hooks = FederatedSchemaGeneratorHooks(FederatedTypeRegistry(emptyMap()))
    )
    private val queries = listOf(TopLevelObject(PersonService()))
    private val mutations = listOf(TopLevelObject(PersonUpdater()))

    fun getSchema(): GraphQLSchema {
        return toFederatedSchema(config, queries, mutations)
    }
}
