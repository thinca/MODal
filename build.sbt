val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "MODal",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
    libraryDependencies += "org.spigotmc" % "spigot-api" % "1.19.4-R0.1-SNAPSHOT",
  )
