plugins {
    id("buildlogic.kotlin-common-conventions")
    java
    id("com.gradleup.shadow") version "8.3.5"
}

version "0.0.1"

dependencies {
    api(project(":core"))

    val diozero = "1.4.1" // https://www.diozero.com/releases.html
    implementation("com.diozero:diozero-core:$diozero")
    implementation("com.diozero:diozero-ws281x-java:$diozero")

    val log4jVersion = "2.18.0" // https://logging.apache.org/log4j/2.x/javadoc.html
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
}

tasks.shadowJar {
    manifest {
        attributes("Main-Class" to "me.retrodaredevil.led.LedMain")
    }
}
