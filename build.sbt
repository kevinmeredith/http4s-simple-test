scalaVersion := "2.11.8" // Also supports 2.10.x

lazy val http4sVersion = "0.14.2"

// Only necessary for SNAPSHOT releases
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s"  %% "http4s-dsl"          % http4sVersion,
  "org.http4s"  %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"  %% "http4s-blaze-client" % http4sVersion,
  "com.chuusai" %% "shapeless"           % "2.3.2"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)