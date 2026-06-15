import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion: String by System.getProperties()
  kotlin("jvm").version(kotlinVersion)
  kotlin("plugin.serialization").version(kotlinVersion)

  id("fabric-loom") version "1.16-SNAPSHOT"
  id("maven-publish")
  id("signing")
}

val modVersion: String by project
val mavenGroup: String by project

val minecraftVersion: String by project
val minecraftTargetVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricKotlinVersion: String by project
val fabricVersion: String by project

val ccVersion: String by project
val ccMcVersion: String by project
val ccTargetVersion: String by project

val nightConfigVersion: String by project
val clothConfigVersion: String by project
val clothApiVersion: String by project
val modMenuVersion: String by project

val trinketsVersion: String by project
val cardinalComponentsVersion: String by project

val scLibraryVersion: String by project
val scTextVersion: String by project
val fabricPermissionsApiVersion: String by project

val kotlinSerializationVersion: String by project
val annotationsVersion: String by project

val figuraVersion: String by project

val archivesBaseName = "sc-goodies"
version = modVersion
group = mavenGroup

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
  }
}

repositories {
  mavenLocal {
    content {
      includeModule("io.sc3", "sc-library")
      includeModule("io.sc3", "sc-text")
    }
  }

  maven {
    url = uri("https://maven.reconnected.cc/releases")
    content {
      includeModule("io.sc3", "sc-library")
      includeModule("io.sc3", "sc-text")
    }
  }

  maven("https://maven.squiddev.cc") {
    content {
      includeGroup("cc.tweaked")
      includeModule("org.squiddev", "Cobalt")
    }
  }

  maven("https://maven.shedaniel.me") {
    // cloth-config
    content {
      includeGroup("me.shedaniel.cloth")
      includeGroup("me.shedaniel.cloth.api")
    }
  }

  maven("https://maven.terraformersmc.com") {
    // Trinkets, mod-menu
    content {
      includeModule("dev.emi", "trinkets")
      includeGroup("com.terraformersmc")
    }
  }

  maven("https://maven.nucleoid.xyz") {
    content {
      includeGroup("eu.pb4")
    }
  }

  maven("https://maven.ladysnake.org/releases") {
    // Cardinal Components API (dependency of Trinkets)
    content {
      includeGroup("org.ladysnake.cardinal-components-api")
    }
  }
  mavenCentral()

  maven("https://maven.figuramc.org/releases")
}

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")
  modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
  modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion) {
    exclude("net.fabricmc.fabric-api", "fabric-gametest-api-v1")
  }
  modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)

  modImplementation(include("io.sc3", "sc-library", scLibraryVersion))

  modImplementation("cc.tweaked:cc-tweaked-$ccMcVersion-fabric:$ccVersion") {
    exclude("net.fabricmc.fabric-api", "fabric-gametest-api-v1")
  }

  implementation(include("com.electronwill.night-config", "core", nightConfigVersion))
  implementation(include("com.electronwill.night-config", "toml", nightConfigVersion))

  modImplementation("dev.emi:trinkets:${trinketsVersion}")

  modApi("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
    exclude("net.fabricmc.fabric-api")
  }
  include("me.shedaniel.cloth", "cloth-config-fabric", clothConfigVersion)
  modImplementation(include("me.shedaniel.cloth.api", "cloth-utils-v1", clothApiVersion))

  modImplementation(include("com.terraformersmc", "modmenu", modMenuVersion))

  modImplementation(include("me.lucko", "fabric-permissions-api", fabricPermissionsApiVersion))

  modImplementation(include("org.ladysnake.cardinal-components-api", "cardinal-components-base", cardinalComponentsVersion))
  modImplementation(include("org.ladysnake.cardinal-components-api", "cardinal-components-entity", cardinalComponentsVersion))

  implementation("org.jetbrains", "annotations", annotationsVersion)
  implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", kotlinSerializationVersion)

  modImplementation(include("io.sc3", "sc-text", scTextVersion))
  // Figura
  modCompileOnly("org.figuramc","figura-fabric", figuraVersion)
}

tasks {
  processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") { expand(mutableMapOf(
      "version" to project.version,
      "minecraft_target_version" to minecraftTargetVersion,
      "fabric_kotlin_version" to fabricKotlinVersion,
      "loader_version" to loaderVersion,
      "cc_target_version" to ccTargetVersion,
    )) }
  }

  jar {
    from("LICENSE") {
      rename { "${it}_${archivesBaseName}" }
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
  }

  remapJar {
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
    destinationDirectory.set(file("${rootDir}/build/final"))
  }

  loom {
    accessWidenerPath.set(file("src/main/resources/sc-goodies.accesswidener"))

    sourceSets {
      main {
        resources {
          srcDir("src/generated/resources")
          exclude("src/generated/resources/.cache")
        }
      }
    }

    runs {
      configureEach {
        property("fabric.debug.disableModShuffle")
      }
      create("datagen") {
        client()
        name("Data Generation")
        vmArgs(
          "-Dfabric-api.datagen",
          "-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}",
          "-Dfabric-api.datagen.modid=${archivesBaseName}"
        )
        runDir("build/datagen")
      }

      named("client") {
        property("mixin.debug.export", "true")
      }
    }
  }
}


publishing {
  publications {
    register("mavenJava", MavenPublication::class) {
      from(components["java"])
    }
  }

  repositories {
    maven {
      name = "reconnectedRepo"
      url = uri("https://maven.reconnected.cc/releases")

      if (!System.getenv("MAVEN_USERNAME").isNullOrEmpty()) {
        credentials {
          username = System.getenv("MAVEN_USERNAME")
          password = System.getenv("MAVEN_PASSWORD")
        }
      } else {
        credentials(PasswordCredentials::class)
      }

      authentication {
        create<BasicAuthentication>("basic")
      }
    }
  }
}
kotlin {
    jvmToolchain(21)
}
