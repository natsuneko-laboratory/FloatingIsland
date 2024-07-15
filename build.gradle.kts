import java.util.Date
import java.text.SimpleDateFormat

// declare
val mod_id: String by project
val mod_group_id: String by project
val mod_name: String by project
val mod_version: String by project
val mod_authors: String by project
val mod_license: String by project
val mod_description: String by project

val minecraft_version: String by project
val minecraft_version_range: String by project
val forge_version: String by project
val forge_version_range: String by project
val loader_version_range: String by project
val mapping_channel: String by project
val mapping_version: String by project

buildscript {
    repositories {
        mavenCentral()
        maven("https://files.minecraftforge.net/maven")
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:2.2-SNAPSHOT")
    }
}

apply(plugin = "net.minecraftforge.gradle.forge")

plugins {
    kotlin("jvm") version "2.0.0"
    idea
    `maven-publish`
}

val jar: Jar by tasks

version = mod_version
group = mod_group_id

sourceSets.main {
    java.srcDirs("src/main/java", "src/main/kotlin")
}

base {
    archivesName = mod_id
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
    withSourcesJar()
}

minecraft {
    version = minecraft_version
    // runDir = "run"
    // The mappings can be changed at any time and must be in the following format.
    // Channel:   Version:
    // official   MCVersion  Official field/method names from Mojang mapping files
    // parchment  YYYY.MM.DD-MCVersion  Open community-sourced parameter names and javadocs layered on top of official
    //
    // You must be aware of the Mojang license when using the 'official' or 'parchment' mappings.
    // See more information here: https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md
    //
    // Parchment is an unofficial project maintained by ParchmentMC, separate from MinecraftForge
    // Additional setup is needed to use their mappings: https://github.com/ParchmentMC/Parchment/wiki/Getting-Started
    //
    // Use non-default mappings at your own risk. They may not always work.
    // Simply re-run your setup task after changing the mappings to update your workspace.
    // mappings channel: mapping_channel, version: mapping_version
    // mappings = "${mapping_channel}_${mapping_version}"
    /*
    mappings(
        mutableMapOf<String, String>(
            "channel" to mapping_channel,
            "version" to mapping_version
        )
    )
    */
    // When true, this property will have all Eclipse/IntelliJ IDEA run configurations run the "prepareX" task for the given run configuration before launching the game.
    // In most cases, it is not necessary to enable.
    // enableEclipsePrepareRuns = true
    // enableIdeaPrepareRuns = true

    // This property allows configuring Gradle's ProcessResources task(s) to run on IDE output locations before launching the game.
    // It is REQUIRED to be set to true for this template to function.
    // See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
    // copyIdeResources.set(true)

    // When true, this property will add the folder name of all declared run configurations to generated IDE run configurations.
    // The folder name can be set on a run configuration using the "folderName" property.
    // By default, the folder name of a run configuration is the name of the Gradle project containing it.
    // generateRunFolders = true

    // This property enables access transformers for use in development.
    // They will be applied to the Minecraft artifact.
    // The access transformer file can be anywhere in the project.
    // However, it must be at "META-INF/accesstransformer.cfg" in the final mod jar to be loaded by Forge.
    // This default location is a best practice to automatically put the file in the right place in the final jar.
    // See https://docs.minecraftforge.net/en/latest/advanced/accesstransformers/ for more information.
    // accessTransformer = file('src/main/resources/META-INF/accesstransformer.cfg')

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    /*
    runs {
        // applies to all the run configs below
        configureEach {
            workingDirectory(project.file("run"))

            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            property("forge.logging.markers", "REGISTERES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            property("forge.logging.console.level", "debug")
        }

        create("client") {
            // this block needs to be here for runClient to exist
        }

        create("server") {
            args("--nogui")
        }

        create("data") {
            // example of overriding the workingDirectory set in configureEach above
            workingDirectory(project.file("run-data"))

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            args(
                listOf(
                    "--mod", mod_id,
                    "--all",
                    "--output", file("src/generated/resources/"),
                    "--existing", file("src/main/resources/")
                )
            )
        }
    }
    */
}

repositories {
    mavenCentral()
}

dependencies {
    // Specify the version of Minecraft to use.
    // Any artifact can be supplied so long as it has a "userdev" classifier artifact and is a compatible patcher artifact.
    // The "userdev" classifier will be requested and setup by ForgeGradle.
    // If the group id is "net.minecraft" and the artifact id is one of ["client", "server", "joined"],
    // then special handling is done to allow a setup of a vanilla dependency without the use of an external repository.
    /*
    minecraft("net.minecraftforge:forge:${minecraft_version}-${forge_version}")

    minecraftLibrary(kotlin("stdlib"))
    minecraftLibrary(kotlin("stdlib-common"))
    minecraftLibrary(kotlin("stdlib-jdk8"))
    minecraftLibrary(kotlin("reflect"))
    */
}

tasks.jar {
    enabled = true
}

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error. Properties are expanded using ${} Groovy notation.
// When "copyIdeResources" is enabled, this will also run before the game launches in IDE environments.
// See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
tasks.named<ProcessResources>("processResources").configure {
    val properties: MutableMap<String, Any> = mutableMapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "forge_version" to forge_version,
        "forge_version_range" to forge_version_range,
        "loader_version_range" to loader_version_range,
        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_authors" to mod_authors,
        "mod_description" to mod_description
    )

    inputs.properties(properties)

    filesMatching(mutableSetOf<String>("META-INF/mods.toml", "pack.mcmeta")) {
        properties["project"] = project
    }
}

tasks.named<Jar>("jar").configure {
    manifest {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())

        attributes(
            mapOf(
                "Specification-Title" to mod_id,
                "Specification-Vendor" to mod_authors,
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to jar.archiveVersion,
                "Implementation-Vendor" to mod_authors,
                "Implementation-Timestamp" to timestamp
            )
        )
    }

    finalizedBy("reobfJar")
}

tasks.named("compileJava", JavaCompile::class.java) {
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work
        listOf("--patch-module", "com.natsuneko.floatingisland=${sourceSets["main"].output.asPath}")
    })
}

/*
setOf(sourceSets.main, sourceSets.test)
    .map(Provider<SourceSet>::get)
    .forEach { sourceSet ->
        val mutClassesDirs = sourceSet.output.classesDirs as ConfigurableFileCollection
        val javaClassDir = sourceSet.java.classesDirectory.get()
        val mutClassesFrom = mutClassesDirs.from
            .filter {
                val toCompare = (it as? Provider<*>)?.get()
                return@filter javaClassDir != toCompare
            }
            .toMutableSet()
        mutClassesDirs.setFrom(mutClassesFrom)
    }
 */

publishing {
    publications {
        register<MavenPublication>("maven") {
            artifact(jar)
        }
    }
    repositories {
        maven {
            url = uri("file://${project.projectDir}/mcmodsrepo")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
