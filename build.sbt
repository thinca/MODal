lazy val root = project
  .in(file("."))
  .settings(
    name := "MODal",
    version := "0.1.0",

    scalaVersion := "3.0.0",

    resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
    libraryDependencies += "org.spigotmc" % "spigot-api" % "1.16.5-R0.1-SNAPSHOT",
  )
