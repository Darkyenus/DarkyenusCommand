@file:Suppress("unused")

import wemi.Archetypes
import wemi.dependency.Jitpack
import wemi.*
import wemi.dependency.sonatypeOss

val darkyenusCommand by project(Archetypes.JavaProject) {

	projectGroup set { "com.darkyen.minecraft" }
	projectName set { "DarkyenusCommand" }
	projectVersion set { "8.0-SNAPSHOT" }

	repositories add { Repository("spigot-repo", "https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
	repositories add { Jitpack }
	repositories add { sonatypeOss("snapshots") }

	extend(compiling) {
		libraryDependencies add { dependency("org.spigotmc", "spigot-api", "1.13.2-R0.1-SNAPSHOT") }
	}

	libraryDependencies add { dependency("com.github.EsotericSoftware", "jsonbeans", "7306654ed3") }
	libraryDependencies add { dependency("com.darkyen", "bukkit-nbt-api", "1.0-SNAPSHOT") }
	libraryDependencies add { dependency("org.jetbrains", "annotations", "16.0.2") }
}
