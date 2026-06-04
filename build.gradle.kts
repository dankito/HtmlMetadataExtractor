plugins {
    kotlin("jvm") version "2.3.20"
}

group = "net.dankito.web"
version = "1.0.0-SNAPSHOT"


kotlin {
    jvmToolchain(11)
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.22.2")

    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}