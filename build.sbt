/*
 * Copyright 2015 Damian McDonald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.heikoseeberger.sbtheader.HeaderPattern
import de.heikoseeberger.sbtheader.license.Apache2_0

lazy val currencytrade = project.in(file(".")).enablePlugins(AutomateHeaderPlugin)

enablePlugins(JavaAppPackaging)

name := "currencytrade-spray"

version       := "1.0.0"

scalaVersion  := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "ivy releases"          at  "http://repo.typesafe.com/typesafe/ivy-releases/, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]",
  "maven releases"        at  "http://repo1.maven.org/maven2/",
  "scalasbt releases"     at  "http://scalasbt.artifactoryonline.com/scalasbt/repo/, [organization]/[module]/scala_[scalaVersion]/sbt_[sbtVersion]/[revision]/[type]s/[artifact].[ext]",
  "bintray releases"      at  "http://dl.bintray.com/scalaz/releases/",
  "sonatype releases"     at  "http://oss.sonatype.org/content/repositories/releases",
  "typesafe releases"     at  "http://dl.bintray.com/typesafe/maven-releases/",
  "scala sbt"             at  "http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases"
)

libraryDependencies ++= {
  val akkaV  = "2.3.9"
  val sprayV = "1.3.2"
  Seq(
    /* Spray web socket support */
    "com.wandoulabs.akka"         %%    "spray-websocket"       % "0.1.4"               withSources() withJavadoc,

      /* Spray */
    "io.spray"                    %%    "spray-json"            % "1.3.1"               withSources() withJavadoc,
    "io.spray"                    %%    "spray-can"             % sprayV                withSources() withJavadoc,
    "io.spray"                    %%    "spray-routing"         % sprayV                withSources() withJavadoc,
    "io.spray"                    %%    "spray-client"          % sprayV                withSources() withJavadoc,

    /* Akka */
    "com.typesafe.akka"           %%    "akka-actor"            % akkaV                 withSources() withJavadoc,
    "com.typesafe.akka"           %%    "akka-slf4j"            % akkaV                 withSources() withJavadoc,

    /* Supporting libs */
    "joda-time"                   %     "joda-time"             % "2.7",
    "ch.qos.logback"              %     "logback-classic"       % "1.1.2",
    "org.scalaz.stream"           %%    "scalaz-stream"         % "0.7a",
    "org.mongodb"                 %%    "casbah"                % "2.8.1",
    "net.liftweb"                 %     "lift-json_2.11"        % "2.6.2",

    /* Testing */
    "com.typesafe.akka"           %%  "akka-testkit"            % akkaV     % "test"     withSources() withJavadoc,
    "io.spray"                    %%  "spray-testkit"           % sprayV    % "test"     withSources() withJavadoc,
    "org.scalatest"               %%  "scalatest"               % "2.2.4"   % "test",
    "junit"                       %   "junit"                   % "4.12"    % "test",
    "org.specs2"                  %%  "specs2"                  % "2.4.17"  % "test"      // until spray-testkit gets compiled against specs 3.3
  )
}

headers := Map(
  "scala" -> Apache2_0("2015", "Damian McDonald")
)

Revolver.settings

scalariformSettings