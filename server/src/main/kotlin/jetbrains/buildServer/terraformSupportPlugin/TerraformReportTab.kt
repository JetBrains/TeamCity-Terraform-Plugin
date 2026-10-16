package jetbrains.buildServer.terraformSupportPlugin

import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.serverSide.BuildsManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.web.openapi.BuildTab
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.stream.Collectors


class TerraformReportTab(
    manager: WebControllerManager,
    buildManager: BuildsManager,
    descriptor: PluginDescriptor
) : BuildTab(
    "terraformChangesReport",
    "Terraform Plan",
    manager,
    buildManager,
    descriptor.getPluginResourcesPath("terraformChangesReport.jsp")
) {
    // the report is rendered in a sandboxed iframe, which does not inherit the page styles
    private val reportCss: String by lazy {
        TerraformReportTab::class.java.getResourceAsStream("/buildServerResources/terraformChangesReport.css")
            ?.bufferedReader()?.use { it.readText() }
            ?: ""
    }

    override fun isAvailableFor(build: SBuild): Boolean {
        return build.getBuildFeaturesOfType(TerraformFeatureConstants.FEATURE_TYPE).isNotEmpty() &&
                getTerraformReportFromBuildArtifacts(build) != null
    }

    private fun getTerraformReportFromBuildArtifacts(build: SBuild): BuildArtifact? {
        return try {
            val hiddenArtifacts = build.getArtifacts(
                BuildArtifactsViewMode.VIEW_HIDDEN_ONLY
            )
            hiddenArtifacts.getArtifact(
                "${TerraformFeatureConstants.HIDDEN_ARTIFACT_REPORT_FOLDER}${File.separator}${TerraformFeatureConstants.HIDDEN_ARTIFACT_REPORT_FILENAME}"
            )
        } catch (e: AccessDeniedException) {
            null
        }
    }

    override fun fillModel(model: MutableMap<String, Any>, build: SBuild) {
        val reportArtifact = getTerraformReportFromBuildArtifacts(build)
        try {
            val inputStream = reportArtifact!!.inputStream // isAvailableFor is evaluated before filling the model
            val reportString = BufferedReader(
                InputStreamReader(inputStream)
            ).lines().collect(
                Collectors.joining("\n")
            )
            model["reportString"] = reportString
            model["reportCss"] = reportCss
        } catch (e: Exception) {
            Loggers.SERVER.warnAndDebugDetails(
                "Cannot process Terraform report artifact to show it in the build tab",
                e
            )
        }
    }
}