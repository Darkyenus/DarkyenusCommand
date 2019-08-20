@file:Suppress("unused")

import wemi.Archetypes
import wemi.boot.WemiRootFolder
import wemi.compile.JavaCompilerFlags
import wemi.dependency
import wemi.dependency.Jitpack
import wemi.dependency.sonatypeOss
import wemi.util.copyRecursively
import wemi.util.exists
import java.nio.file.StandardCopyOption

val darkyenusCommand by project(Archetypes.JavaProject) {

	projectGroup set { "com.darkyen.minecraft" }
	projectName set { "DarkyenusCommand" }
	projectVersion set { "8.2" }

	repositories add { Repository("spigot-repo", "https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
	repositories add { Jitpack }
	repositories add { sonatypeOss("snapshots") }

	extend(compilingJava) {
		compilerOptions[JavaCompilerFlags.customFlags] += "-Xlint:unchecked"
		compilerOptions[JavaCompilerFlags.customFlags] += "-Xlint:deprecation"

		// Not only for debugging, plugin reflectively loads parameter names!
		// https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html
		compilerOptions[JavaCompilerFlags.customFlags] += "-parameters"
		
		//<"morning"|"day"|"afternoon"|"evening" | "dusk" | "night" | "dawn">
	}

	libraryDependencies add { dependency("org.jetbrains", "annotations", "16.0.2", scope="provided") }
	libraryDependencies add { dependency("org.spigotmc", "spigot-api", "1.14.4-R0.1-SNAPSHOT", scope="provided") }
	libraryDependencies add { dependency("com.github.EsotericSoftware", "jsonbeans", "7306654ed3") }

	assembly modify { assembled ->
		val testServerPlugins = WemiRootFolder / "../TEST SERVER/plugins"
		if (testServerPlugins.exists()) {
			assembled.copyRecursively(testServerPlugins / (projectName.get() + ".jar"), StandardCopyOption.REPLACE_EXISTING)
			println("Copied to test server")
		}

		assembled
	}
}
