package me.retrodaredevil.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory


inline fun <reified T> T.getLogger(): Logger {
    val clazz = T::class.java
    val enclosingClass = clazz.enclosingClass
    return LoggerFactory.getLogger(enclosingClass ?: clazz)
}
