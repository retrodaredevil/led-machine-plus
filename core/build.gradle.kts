plugins {
    id("buildlogic.kotlin-common-conventions")
    `java-library`
}

dependencies {
    api(project(":utilities"))
    api(project(":token"))

    val jacksonVersion = "2.20.1" // https://github.com/FasterXML/jackson-databind/releases
    val secondaryJacksonVersion = "2.20"
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-annotations:$secondaryJacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // https://github.com/Discord4J/Discord4J/releases
    api("com.discord4j:discord4j-core:3.3.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-reactor
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
}


tasks.withType<Test> {
    failOnNoDiscoveredTests = false // we currently have no tests set up, just a main method to test some stuff manually
}

