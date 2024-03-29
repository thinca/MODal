val scala3Version = "3.3.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "MODal",
    version := "0.1.0",

    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-deprecation",
      "-Wunused:all",
    ),

    resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
    libraryDependencies += "org.spigotmc" % "spigot-api" % "1.20.1-R0.1-SNAPSHOT",
  )
