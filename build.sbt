scalaVersion := "2.11.8" // Also supports 2.10.x

val http4sVersion = "0.14.5"

val circeVersion = "0.4.1"

libraryDependencies ++= Seq(
  "org.http4s"    %% "http4s-dsl"          % http4sVersion,
  "org.http4s"    %% "http4s-blaze-server" % http4sVersion,
  "com.chuusai"   %% "shapeless"           % "2.2.5",
  "org.tpolecat"  %% "doobie-core"         % "0.2.4",
  "org.typelevel" %% "cats"                % "0.7.0",
  "io.circe"      %% "circe-generic"       % circeVersion,
  "io.argonaut"   %% "argonaut"            % "6.1",
  "postgresql"    % "postgresql"           % "9.1-901-1.jdbc4"
)

// credit: https://tpolecat.github.io/2014/04/11/scalac-flags.html
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"     // 2.11 only
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)