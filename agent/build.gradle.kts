import java.util.zip.ZipInputStream

plugins {
    kotlin("jvm")
    id("com.github.rodm.teamcity-agent")
}

teamcity {
    version = rootProject.extra["teamcityVersion"] as String

    agent {
        descriptor {
            project.file("teamcity-agent-plugin.xml")
            pluginDeployment {
                useSeparateClassloader = true
            }
        }

        archiveName = "terraform-agent"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("io.pebbletemplates:pebble:3.2.2")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("org.apache.commons:commons-text:1.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
}

configurations.all {
    exclude(group = "org.slf4j")
}

var mainClassName = "jetbrains.buildServer.terraformSupportPlugin.TerraformSupport"

tasks.withType<Jar> {
    archiveBaseName.set("terraform-agent")


}

tasks["agentPlugin"].doLast {
    val zipTask = tasks["agentPlugin"] as Zip
    val zipFile = zipTask.archivePath

    zipFile.inputStream().use { it ->
        ZipInputStream(it).use { z ->
            generateSequence { z.nextEntry }
                .filterNot { it.isDirectory }
                .map { it.name }
                .toList()
                .sorted()
        }
    }
}