plugins {
    id("io.papermc.paperweight.userdev") version "1.5.9"

    id("xyz.jpenilla.run-paper") version "2.2.3"

    `java-library`
}

base {
    archivesName.set(rootProject.name)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven/")
        }
        filter { includeGroup("maven.modrinth") }
    }
}

rootProject.version = if (System.getenv("BUILD_NUMBER") != null) "${rootProject.version}-${System.getenv("BUILD_NUMBER")}" else rootProject.version

val mcVersion = providers.gradleProperty("minecraftVersion").get()

dependencies {
    paperweight.paperDevBundle("$mcVersion-R0.1-SNAPSHOT")

    compileOnly("maven.modrinth", "pl3xmap", providers.gradleProperty("pl3xmapVersion").get())
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of("17"))
}

tasks {
    javadoc {
        options.encoding = "UTF-8"
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion(mcVersion)

        downloadPlugins {
            hangar("Chunky", "1.3.136")

            modrinth("pl3xmap", providers.gradleProperty("pl3xmapVersion").get())

            url("https://ci.lucko.me/job/spark/401/artifact/spark-bukkit/build/libs/spark-1.10.60-bukkit.jar")
        }
    }

    val jarsDir = File("$rootDir/jars")

    assemble {
        doFirst {
            delete(jarsDir)

            jarsDir.mkdirs()
        }

        dependsOn(reobfJar)

        doLast {
            copy {
                from(rootProject.layout.buildDirectory.files("libs/${rootProject.name}-${rootProject.version}.jar"))
                into(jarsDir)
            }
        }
    }

    processResources {
        val props = mapOf(
            "name" to rootProject.name,
            "group" to rootProject.group,
            "version" to rootProject.version,
            "description" to rootProject.description,
            "authors" to rootProject.properties["authors"],
            "website" to rootProject.properties["website"],
            "apiVersion" to rootProject.properties["apiVersion"],
        )

        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}