package me.retrodaredevil.led

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule

fun buildDefaultMapper(builder: JsonMapper.Builder): JsonMapper.Builder =
        builder.addModule(
                KotlinModule.Builder()
                        .configure(KotlinFeature.StrictNullChecks, true)
                        .configure(KotlinFeature.SingletonSupport, true)
                        .build()
        )

fun createDefaultMapper(): JsonMapper = buildDefaultMapper(JsonMapper.builder()).build()

