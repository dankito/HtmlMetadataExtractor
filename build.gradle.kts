plugins {
    kotlin("jvm") version "2.0.21"
}

group = "net.dankito.web"
version = "1.0.0-SNAPSHOT"

ext["customArtifactId"] = "html-metadata-extractor"

ext["sourceCodeRepositoryBaseUrl"] = "github.com/dankito/HtmlMetadataExtractor"

ext["projectDescription"] = "Extracts web page metadata from HTML"



kotlin {
    jvmToolchain(11)
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.22.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.2")

    testImplementation(kotlin("test"))
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
}


tasks.test {
    useJUnitPlatform()
}


if (file("./gradle/scripts/publish-dankito.gradle.kts").exists()) {
    apply(from = "./gradle/scripts/publish-dankito.gradle.kts")
}