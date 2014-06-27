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
package uk.gov.hmrc

import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._

object PlayMicroServiceBuild {

  import play.Project._
  import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

  def apply(appName: String,
            appVersion: String,
            dependencies: Seq[ModuleID],
            extraRoutesPackageImports: Seq[String] = Seq.empty,
            organizationPackage: String = "uk.gov.hmrc",
            applicationResolvers : Seq[Resolver],
            snapshots : MavenRepository,
            releases : MavenRepository): Project = {

    val commonSettings: Seq[Def.Setting[_]] = DefaultBuildSettings(appName, appVersion, organizationPackage, "jvm-1.7")(ShellPrompt.buildShellPrompt(appVersion))

    implicit val p: Project = play.Project(
      appName,
      appVersion, dependencies, file("."),
      settings = commonSettings
        ++ SbtBuildInfo(organizationPackage)
        ++ playScalaSettings
        ++ Repositories.publishingSettings(snapshots, releases)
        ++ Seq(
        resolvers ++= applicationResolvers
      )
    ).settings(publishArtifact := true, Keys.fork in Test := false)
      .settings(inConfig(TemplateTest)(Defaults.testSettings): _*)
      .configs(IntegrationTest)
      .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
      .settings(Keys.fork in IntegrationTest := false)
      .settings(unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")))
      .settings(addTestReportOption(IntegrationTest, "int-test-reports"))
      .settings(testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value))
      .settings(parallelExecution in IntegrationTest := false)

    addExtraRoutes(extraRoutesPackageImports)
  }

  val allPhases = "tt->test;test->test;test->compile;compile->compile"
  val allItPhases = "tit->it;it->it;it->compile;compile->compile"

  lazy val TemplateTest = config("tt") extend Test
  lazy val TemplateItTest = config("tit") extend IntegrationTest


  private def addExtraRoutes(extraRoutesPackageImports: Seq[String])(implicit p: Project): Project = {
    if (extraRoutesPackageImports.nonEmpty)
      p.settings(routesImport ++= extraRoutesPackageImports)
    else
      p
  }

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}

private object Repositories {

  import uk.gov.hmrc.PublishingSettings._

  lazy val dist = com.typesafe.sbt.SbtNativePackager.NativePackagerKeys.dist

  val publishDist = TaskKey[sbt.File]("publish-dist", "publish the dist artifact")


  def publishingSettings(snapshots : MavenRepository, releases : MavenRepository): Seq[sbt.Setting[_]] = sbtrelease.ReleasePlugin.releaseSettings ++ Seq(

    credentials += PublishingSettings.sbtCredentials,

    publishArtifact in(Compile, packageDoc) := false,
    publishArtifact in(Compile, packageSrc) := false,
    publishArtifact in(Compile, packageBin) := true,

    artifact in publishDist ~= {
      (art: Artifact) => art.copy(`type` = "zip", extension = "zip")
    },

    publishDist <<= (target, normalizedName, version) map {
      (targetDir, id, version) =>
        val packageName = "%s-%s" format(id, version)
        targetDir / "universal" / (packageName + ".zip")
    },

    publishLocal <<= publishLocal dependsOn dist

  ) ++
    publishLocation(publishToSettings(snapshots, releases)) ++
    publishAllArtefacts ++
    addArtifact(artifact in publishDist, publishDist)

}