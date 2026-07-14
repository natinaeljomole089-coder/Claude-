package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.StudyMindDatabase
import com.example.ui.DashboardScreen
import com.example.ui.LibraryScreen
import com.example.ui.PdfDetailScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = StudyMindDatabase.getDatabase(applicationContext)
        val dao = database.dao()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf("MAIN") } // "MAIN", "PDF_DETAIL"
                var activeTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Library, 2: Settings
                var selectedPdfId by remember { mutableStateOf("") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen == "MAIN") {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = activeTab == 0,
                                    onClick = { activeTab = 0 },
                                    label = { Text("Dashboard") },
                                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") }
                                )
                                NavigationBarItem(
                                    selected = activeTab == 1,
                                    onClick = { activeTab = 1 },
                                    label = { Text("Library") },
                                    icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Library") }
                                )
                                NavigationBarItem(
                                    selected = activeTab == 2,
                                    onClick = { activeTab = 2 },
                                    label = { Text("Settings") },
                                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (currentScreen == "MAIN") {
                            when (activeTab) {
                                0 -> DashboardScreen(
                                    dao = dao,
                                    onNavigateToPdf = { pdfId ->
                                        selectedPdfId = pdfId
                                        currentScreen = "PDF_DETAIL"
                                    },
                                    onNavigateToStudy = {
                                        activeTab = 1 // Redirect to Library to select/add textbook
                                    }
                                )
                                1 -> LibraryScreen(
                                    dao = dao,
                                    onNavigateToPdf = { pdfId ->
                                        selectedPdfId = pdfId
                                        currentScreen = "PDF_DETAIL"
                                    }
                                )
                                2 -> SettingsScreen(
                                    dao = dao
                                )
                            }
                        } else if (currentScreen == "PDF_DETAIL") {
                            PdfDetailScreen(
                                pdfId = selectedPdfId,
                                dao = dao,
                                onBack = {
                                    currentScreen = "MAIN"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
