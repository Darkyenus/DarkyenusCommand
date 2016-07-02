name := "DarkyenusCommand8"

version := "8.0-SNAPSHOT"

resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"

libraryDependencies += ("org.spigotmc" % "spigot-api" % "1.9.4-R0.1-SNAPSHOT" % "provided").exclude("net.md-5","bungeecord-chat")

autoScalaLibrary := false
