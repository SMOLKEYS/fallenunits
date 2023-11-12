//stolen straight from yellow-java

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.Base64

version = "1.0"

val mindustryVersion = "v146"

val windows = System.getProperty("os.name").lowercase().contains("windows")

plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
    maven("https://www.jitpack.io")
}

sourceSets {
    main {
        java.srcDirs("src")
    }
    test {
        java.srcDir("test")
    }
}

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.github.Anuken.Arc") {
            useVersion(mindustryVersion)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.register("jarAndroid") {
    group = "build"
    description = "Compiles an android-only jar."
    dependsOn("jar")
    
    fun hash(data: ByteArray): String =
		MessageDigest.getInstance("MD5")
		.digest(data)
		.let { Base64.getEncoder().encodeToString(it) }
		.replace('/', '_')
	
	doLast {
		val sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
		
		if(sdkRoot == null || sdkRoot.isEmpty() || !File(sdkRoot).exists()) {
			throw GradleException("""
				No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.
				Note: if the gradle daemon has been started before ANDROID_HOME env variable was defined, it won't be able to read this variable.
				In this case you have to run "./gradlew --stop" and try again
			""".trimIndent());
		}
		
		println("searching for an android sdk... ")
		val platformRoot = File("$sdkRoot/platforms/").listFiles().filter { 
			val fi = File(it, "android.jar")
			val valid = fi.exists() && it.name.startsWith("android-")
			
			if (valid) {
				print(it)
				println(" — OK.")
			}
			return@filter valid
		}.maxByOrNull {
			it.name.substring("android-".length).toIntOrNull() ?: -1
		}
		
		if (platformRoot == null) {
			throw GradleException("No android.jar found. Ensure that you have an Android platform installed. (platformRoot = $platformRoot)")
		} else {
			println("using ${platformRoot.absolutePath}")
		}
		
		//collect dependencies needed to translate java 8 bytecode code to android-compatible bytecode (yeah, android's dvm and art do be sucking)
		val dependencies = 
			(configurations.runtimeClasspath.get().files)
			.map { it.path }
		
		val dexRoot = File("${layout.buildDirectory.get()}/dex/").also { it.mkdirs() }
		val dexCacheRoot = dexRoot.resolve("cache").also { it.mkdirs() }

		// read the dex cache map (path-to-hash)
		val dexCacheHashes = dexRoot.resolve("listing.txt")
			.takeIf { it.exists() }
			?.readText()
			?.lineSequence()
			?.map { it.split(" ") }
			?.filter { it.size == 2 }
			?.associate { it[0] to it[1] }
			.orEmpty()
			.toMutableMap()

		// calculate hashes for all dependencies
		val hashes = dependencies
			.associate {
				it to hash(File(it).readBytes())
			}

		// determime which dependencies can have their cached dex files reused and which can not
		val reusable = ArrayList<String>()
		val needReDex = HashMap<String, String>() // path-to-hash
		hashes.forEach { (path, hash) ->
			if (dexCacheHashes.getOrDefault(path, null) == hash) {
				reusable += path
			} else {
				needReDex[path] = hash
			}
		}

		println("${reusable.size} dependencies are already desugared and can be reused.")
		if (needReDex.isNotEmpty()) println("Desugaring ${needReDex.size} dependencies.")

		// for every non-reusable dependency, invoke d8 (d8.bat for windows) and save the new hash

		val d8 = if (windows) "d8.bat" else "d8"

		var index = 1
		needReDex.forEach { (dependency, hash) ->
			println("Processing ${index++}/${needReDex.size} ($dependency)")

			val outputDir = dexCacheRoot.resolve(hash(dependency.toByteArray()).replace("==", "")).also { it.mkdir() }
			exec {
				errorOutput = OutputStream.nullOutputStream()
				commandLine(
					d8,
					"--intermediate",
					"--classpath", "${platformRoot.absolutePath}/android.jar",
					"--min-api", "14", 
					"--output", outputDir.absolutePath, 
					dependency
				)
			}
			println()
			dexCacheHashes[dependency] = hash
		}

		// write the updated hash map to the file
		dexCacheHashes.asSequence()
			.map { (k, v) -> "$k $v" }
			.joinToString("\n")
			.let { dexRoot.resolve("listing.txt").writeText(it) }

		if (needReDex.isNotEmpty()) println("Done.")
		println("Preparing to desugar the project and merge dex files.")

		val dexPathes = dependencies.map { 
			dexCacheRoot.resolve(hash(it.toByteArray())).also { it.mkdir() }
		}
		// assemble the list of classpath arguments for project dexing
		val dependenciesStr = Array<String>(dependencies.size * 2) {
			if (it % 2 == 0) "--classpath" else dexPathes[it / 2].absolutePath
		}
		
		// now, compile the project
		exec {
			val output = dexCacheRoot.resolve("project").also { it.mkdirs() }
			commandLine(
				d8,
				*dependenciesStr,
				"--classpath", "${platformRoot.absolutePath}/android.jar",
				"--min-api", "14",
				"--output", "$output",
				"${layout.buildDirectory.get()}/libs/${project.name}Desktop.jar"
			)

			errorOutput = ByteArrayOutputStream()
		}

		// finally, merge all dex files
		exec {
			val depDexes = dexPathes
				.map { it.resolve("classes.dex") }.toTypedArray()
				.filter { it.exists() } // some are empty
				.map { it.absolutePath }
				.toTypedArray()

			commandLine(
				d8,
				*depDexes,
				dexCacheRoot.resolve("project/classes.dex").absolutePath,
				"--output", "${layout.buildDirectory.get()}/libs/${project.name}Android.jar"
			)
		}
	}
}


tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${project.name}Desktop.jar")

    from(*configurations.runtimeClasspath.get().files.map { if (it.isDirectory) it else zipTree(it) }.toTypedArray())

    from(rootDir) {
        include("mod.hjson")
        include("icon.png")
    }

    from("$rootDir/assets/") { include("**") }

}

task<Jar>("deploy") {
    group = "build"
    description = "Compiles a multiplatform jar."
    dependsOn("jarAndroid")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${project.name}.jar")
    from(
        zipTree("${layout.buildDirectory.get()}/libs/${project.name}Desktop.jar"),
        zipTree("${layout.buildDirectory.get()}/libs/${project.name}Android.jar")
    )
    doLast {
        delete { delete("${layout.buildDirectory.get()}/libs/${project.name}Desktop.jar") }
        delete { delete("${layout.buildDirectory.get()}/libs/${project.name}Android.jar") }
    }
}

val dir = if(windows) "${System.getenv("APPDATA")}\\Mindustry\\mods" else "${System.getenv("HOME")}/.local/share/Mindustry/mods"

task("copy") {
    group = "copy"
    description = "Compiles a desktop-only jar and copies it to your mindustry data directory."
    dependsOn("jar")

    doLast {
        println("Copying mod...")
        copy {
            from("${layout.buildDirectory.get()}/libs")
            into(dir)
            include("${project.name}Desktop.jar")
        }
    }
}

task("copyDeploy") {
    group = "copy"
    description = "Compiles a multiplatform jar and copies it to your mindustry data directory."
    dependsOn("deploy")

    doLast {
        println("Copying mod...")
        copy {
            from("${layout.buildDirectory.get()}/libs")
            into(dir)
            include("${project.name}.jar")
        }
    }
}

