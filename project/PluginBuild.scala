/*
 * Copyright 2014 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt._
import Keys._

object PluginBuild extends Build {

  val pluginName = "sbt-microservice"
  val pluginVersion = "3.1.0-SNAPSHOT"

  lazy val root = Project(pluginName, base = file("."), settings = Project.defaultSettings ++ Seq(

      version := pluginVersion,
      sbtPlugin := true,
      organization := "uk.gov.hmrc",
      name := pluginName,
      scalaVersion := "2.10.4",
      resolvers ++= Seq(
        Opts.resolver.sonatypeReleases,
        Opts.resolver.sonatypeSnapshots
      ),
      addSbtPlugin("uk.gov.hmrc" % "sbt-utils" % "1.4.0"),
      addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3"),
      addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.3"),
      publishArtifact := true,
      publishArtifact in Test := false
    ) ++ SonatypeBuild() 
  )
}

object SonatypeBuild {

  import xerial.sbt.Sonatype._

  def apply() = {
    sonatypeSettings ++ Seq(
      pomExtra := (<url>https://www.gov.uk/government/organisations/hm-revenue-customs</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          </license>
        </licenses>
        <scm>
          <connection>scm:git@github.com:hmrc/sbt-microservice.git</connection>
          <developerConnection>scm:git@github.com:hmrc/sbt-microservice.git</developerConnection>
          <url>git@github.com:hmrc/sbt-microservice.git</url>
        </scm>
        <developers>
        <developer>
          <id>duncancrawford</id>
          <name>Duncan Crawford</name>
          <url>http://www.equalexperts.com</url>
        </developer>
        <developer>
          <id>jakobgrunig</id>
          <name>Jakob Grunig</name>
          <url>http://www.equalexperts.com</url>
        </developer>
      </developers>)
    )
  }
}