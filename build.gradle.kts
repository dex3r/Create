plugins {
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.loom)
}

val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .filter(String::isNotEmpty)
    .map { "-build.$it" }
    .orElse("-local")
    .getOrElse("")

val mcVer: String = libs.versions.minecraft.get()
version = "6.0.7.0+mc$mcVer$buildNum"

group = "com.simibubi.create"

repositories {
    fun mavenProviding(url: String, vararg groups: String) {
        exclusiveContent {
            forRepositories(maven(url)).filter {
                for (group in groups) {
                    includeGroupAndSubgroups(group)
                }
            }
        }
    }

    mavenProviding("https://maven.parchmentmc.org", "org.parchmentmc.data")
    mavenProviding("https://maven.createmod.net", "net.createmod", "dev.engine-room")
    mavenProviding("https://mvn.devos.one/snapshots", "com.tterrag.registrate_fabric", "io.github.tropheusj")
    mavenProviding("https://mvn.devos.one/releases", "io.github.fabricators_of_create.Porting-Lib")
    mavenProviding("https://raw.githubusercontent.com/Fuzss/modresources/main/maven", "fuzs.forgeconfigapiport")
    mavenProviding("https://maven.squiddev.cc", "cc.tweaked")
    mavenProviding("https://maven.blamejared.com", "mezz.jei")
    mavenProviding("https://maven.terraformersmc.com", "com.terraformersmc", "dev.emi")
    mavenProviding("https://maven.ladysnake.org/releases", "dev.onyxstudios.cardinal-components-api")
    mavenProviding("https://maven.jamieswhiteshirt.com/libs-release", "com.jamieswhiteshirt")
    mavenProviding("https://jm.gserv.me/repository/maven-public", "info.journeymap")
    mavenProviding("https://maven.ftb.dev/releases", "dev.ftb.mods")
    mavenProviding("https://maven.architectury.dev", "dev.architectury", "me.shedaniel")
    mavenProviding("https://modmaven.dev", "vazkii.botania")
    mavenProviding("https://api.modrinth.com/maven", "maven.modrinth")
}

val modApiInclude: Configuration by configurations.dependencyScope("modApiInclude")
val apiInclude: Configuration by configurations.dependencyScope("apiInclude")

configurations.modApi.configure { extendsFrom(modApiInclude) }
configurations.api.configure { extendsFrom(apiInclude) }
configurations.include.configure { extendsFrom(modApiInclude, apiInclude) }

val recipeViewer = "emi"
val runtimeCc = false
val runtimeSodium = false

dependencies {
    // setup
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment(libs.parchment)
    })
    modImplementation(libs.bundles.fabric)
    modApiInclude(libs.bundles.porting.lib)

    // dependencies
    modApiInclude(libs.registrate) {
        // avoid duplicate Porting Lib
        exclude(group = "io.github.fabricators_of_create.Porting-Lib")
    }

    modApiInclude(libs.flywheel)
    modApiInclude(libs.ponder)
    modApiInclude(libs.bundles.config)
    modApiInclude(libs.rea)
    modApiInclude(libs.milk)
    apiInclude(libs.jsr305)

    // compat
    modCompileOnly(libs.cc.api)
    modCompileOnly(libs.botania) { isTransitive = false }
    modCompileOnly(libs.modmenu)
    modCompileOnly(libs.sandwichable)
    modCompileOnly(libs.sodium)
    modCompileOnly(libs.bundles.trinkets)
    modCompileOnly(libs.bundles.ftb)
    modCompileOnly(libs.bundles.journeymap)
    modCompileOnly(libs.xaeros)

    modCompileOnly(libs.jei) { isTransitive = false }
    modCompileOnly(libs.bundles.rei)
    modCompileOnly(variantOf(libs.emi) {
        classifier("api")
    })

    when (recipeViewer) {
        "jei" -> modLocalRuntime(libs.jei)
        "rei" -> modLocalRuntime(libs.rei.runtime)
        "emi" -> modLocalRuntime(libs.emi)
    }

    // dev env
    modLocalRuntime(libs.modmenu)
    modLocalRuntime(libs.bundles.trinkets)

    // have deprecated modules present at runtime only for some older mods
    modLocalRuntime(libs.fabric.api.deprecated)

    // other mods easily toggleable at runtime for testing

    if (runtimeCc) {
        modLocalRuntime(libs.cc.runtime)
        modLocalRuntime(libs.cloth.config)
    }

    if (runtimeSodium) {
        modLocalRuntime(libs.sodium)
        modLocalRuntime(libs.indium)
    }
}


java {
    withSourcesJar()
    // loom requires java >= 21
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.named<JavaCompile>("compileJava") {
    javaCompiler = javaToolchains.compilerFor {
        // Updated to use Java 21
        languageVersion = JavaLanguageVersion.of(21)
    }

    // this makes it possible to actually count errors after a big merge, since by default only 100 are shown
    options.compilerArgs.add("-Xmaxerrs")
    options.compilerArgs.add("10000")
}


sourceSets.main {
    resources {
        srcDir("src/generated/resources")
        exclude(".cache/")
    }
}

loom {
    accessWidenerPath = file("src/main/resources/create.accesswidener")

    runs {
        register("datagen") {
            client()
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=create")
            vmArg("-Dporting_lib.datagen.existing_resources=${file("src/main/resources")}")
        }

        register("gametestServer") {
            server()
            name("Headlesss GameTests")
            ideConfigGenerated(false) // this run is for CI
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${layout.buildDirectory}/junit.xml")
            runDir("run/gametest")
        }

        named("server") {
            runDir("run/server")
        }

        configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            property("mixin.debug.export", "true")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    exclude("**/*.bbmodel", "**/*.lnk")

    val properties: MutableMap<String, Any> = mutableMapOf(
        "version" to version,
        "minecraft_version" to libs.versions.minecraft.get(),
        "loader_version" to libs.versions.fabric.loader.get(),
        "fabric_version" to libs.versions.fabric.api.get(),
        "forge_config_version" to libs.versions.forge.config.get(),
        "milk_lib_version" to libs.versions.milk.get(),
        "reach_entity_attributes_version" to libs.versions.rea.get()
    )

    for (module: MinimalExternalModuleDependency in libs.bundles.porting.lib.get()) {
        val name = module.module.name
        val version: String = if (name == "tags") { "3.0" } else { libs.versions.porting.lib.get() }
        properties["port_lib_${name}_version"] = version
    }

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        listOf("Releases", "Snapshots").forEach {
            maven("https://mvn.devos.one/${it.lowercase()}") {
                name = "devOs$it"
                credentials(PasswordCredentials::class)
            }
        }
    }
}
