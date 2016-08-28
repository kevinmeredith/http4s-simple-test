scalaVersion := "2.11.8" // Also supports 2.10.x

val http4sVersion = "0.14.2"

val circeVersion = "0.4.1"

libraryDependencies ++= Seq(
  "org.http4s"    %% "http4s-dsl"          % http4sVersion,
  "org.http4s"    %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"    %% "http4s-blaze-client" % http4sVersion,
  "com.chuusai"   %% "shapeless"           % "2.3.2",
  "org.tpolecat"  %% "doobie-core"         % "0.3.0",
  "org.typelevel" %% "cats"                % "0.7.0",
  "io.circe"      %% "circe-core"          % circeVersion,
  "io.circe"      %% "circe-generic"       % circeVersion,
  "io.circe"      %% "circe-parser"        % circeVersion
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)