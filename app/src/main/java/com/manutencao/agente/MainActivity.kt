package com.manutencao.agente

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manutencao.agente.data.model.MaintenanceReport
import com.manutencao.agente.data.remote.GeminiAgentService
import com.manutencao.agente.data.repository.ReportRepository
import com.manutencao.agente.ui.screens.*
import com.manutencao.agente.ui.theme.AgenteManutencaoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: ReportRepository
    private lateinit var geminiService: GeminiAgentService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = ReportRepository(this)
        geminiService = GeminiAgentService(this)

        setContent {
            AgenteManutencaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        repository = repository,
                        geminiService = geminiService,
                        onGenerateReport = { type, assetName, assetTag, tech, company, notes, images, template, onFinished ->
                            lifecycleScope.launch {
                                try {
                                    val apiKey = repository.getApiKey()
                                    val report = geminiService.generateMaintenanceReport(
                                        apiKey = apiKey,
                                        maintenanceType = type,
                                        assetName = assetName,
                                        assetTag = assetTag,
                                        technicianName = tech,
                                        companyName = company,
                                        rawNotes = notes,
                                        imageUris = images,
                                        referenceTemplate = template
                                    )

                                    repository.addReport(report)
                                    onFinished(report)
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "Erro na geração: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    repository: ReportRepository,
    geminiService: GeminiAgentService,
    onGenerateReport: (
        type: com.manutencao.agente.data.model.MaintenanceType,
        assetName: String,
        assetTag: String,
        tech: String,
        company: String,
        notes: String,
        images: List<android.net.Uri>,
        template: com.manutencao.agente.data.model.ReferenceTemplate?,
        onFinished: (MaintenanceReport) -> Unit
    ) -> Unit
) {
    val navController = rememberNavController()
    val reports by repository.reports.collectAsState()
    val templates by repository.templates.collectAsState()

    var selectedReportForDetail by remember { mutableStateOf<MaintenanceReport?>(null) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                reports = reports,
                onCreateReportClick = { navController.navigate("create_report") },
                onReportClick = { report ->
                    selectedReportForDetail = report
                    navController.navigate("report_detail")
                },
                onTemplatesClick = { navController.navigate("templates") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable("create_report") {
            CreateReportScreen(
                templates = templates,
                defaultCompanyName = repository.getCompanyName(),
                defaultTechnicianName = repository.getTechnicianName(),
                onBackClick = { navController.popBackStack() },
                onGenerateReportClick = { type, assetName, assetTag, tech, company, notes, images, template ->
                    onGenerateReport(type, assetName, assetTag, tech, company, notes, images, template) { generatedReport ->
                        selectedReportForDetail = generatedReport
                        navController.navigate("report_detail") {
                            popUpTo("home")
                        }
                    }
                }
            )
        }

        composable("report_detail") {
            selectedReportForDetail?.let { report ->
                ReportDetailScreen(
                    report = report,
                    onReportUpdated = { updatedReport ->
                        repository.updateReport(updatedReport)
                        selectedReportForDetail = updatedReport
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable("templates") {
            TemplatesScreen(
                templates = templates,
                onAddTemplate = { newTpl -> repository.addTemplate(newTpl) },
                onUpdateTemplate = { updatedTpl -> repository.updateTemplate(updatedTpl) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                currentApiKey = repository.getApiKey(),
                currentCompanyName = repository.getCompanyName(),
                currentTechnicianName = repository.getTechnicianName(),
                onSaveSettings = { apiKey, company, tech ->
                    repository.saveApiKey(apiKey)
                    repository.saveCompanyName(company)
                    repository.saveTechnicianName(tech)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
