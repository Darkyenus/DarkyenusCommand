name := "DarkyenusCommand"

version := "8.0-SNAPSHOT"

resolvers += Resolver.mavenLocal

resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += ("org.spigotmc" % "spigot-api" % "1.11.2-R0.1-SNAPSHOT" % "provided")

libraryDependencies += "com.esotericsoftware" % "jsonbeans" % "0.7"

libraryDependencies += "com.darkyen" % "bukkit-nbt-api" % "1.0-SNAPSHOT"

autoScalaLibrary := false

crossPaths := false
