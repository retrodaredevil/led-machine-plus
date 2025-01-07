plugins {
    id("buildlogic.kotlin-common-conventions")
    `java-library`
}

dependencies {
    api(project(":utilities"))
    api(project(":token"))

    val slackVersion = "1.25.1" // https://github.com/slackapi/java-slack-sdk/releases
    implementation("com.slack.api:slack-api-client:$slackVersion")
    // https://mvnrepository.com/artifact/org.java-websocket/Java-WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.3")



    val jacksonVersion = "2.13.4" // https://github.com/FasterXML/jackson-databind/releases
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    api("com.discord4j:discord4j-core:3.2.3")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-reactor
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
}
