name := "DarkyenusCommand"

version := "8.0-SNAPSHOT"

resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += ("org.spigotmc" % "spigot-api" % "1.10.2-R0.1-SNAPSHOT" % "provided")

libraryDependencies += "com.esotericsoftware" % "jsonbeans" % "0.7"

autoScalaLibrary := false

crossPaths := false
