plugins {
    id("buildlogic.kotlin-common-conventions")
    `java-library`
}

dependencies {
    api(project(":utilities"))
    api(project(":token"))

    val jacksonVersion = "2.18.2" // https://github.com/FasterXML/jackson-databind/releases
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // https://github.com/Discord4J/Discord4J/releases
    api("com.discord4j:discord4j-core:3.2.7")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-reactor
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.1")
}
