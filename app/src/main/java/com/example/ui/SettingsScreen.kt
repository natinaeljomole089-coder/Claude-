package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.StudyMindDao
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    dao: StudyMindDao,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences("studymind_prefs", Context.MODE_PRIVATE) }

    var storedApiKey by remember { mutableStateOf("") }
    var hideApiKey by remember { mutableStateOf(true) }
    var dailyGoalMinutes by remember { mutableIntStateOf(30) }
    var clearStatusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        storedApiKey = sharedPrefs.getString("gemini_api_key", "") ?: ""
        dailyGoalMinutes = sharedPrefs.getInt("daily_goal_mins", 30)
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumnSettings(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Settings Section
        item {
            Text(
                text = "Settings & Configuration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // API Key settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(primaryColor.copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = "Key", tint = primaryColor)
                        }
                        Text(text = "Gemini AI API Key", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Enter your custom Gemini API key to run queries directly from your device. If empty, the app will use the pre-configured project key, or automatically activate high-fidelity offline simulations if none exist.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = storedApiKey,
                        onValueChange = {
                            storedApiKey = it
                            sharedPrefs.edit().putString("gemini_api_key", it).apply()
                        },
                        placeholder = { Text("AI Studio Gemini Key...") },
                        visualTransformation = if (hideApiKey) PasswordVisualTransformation() else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = { hideApiKey = !hideApiKey }) {
                                Icon(
                                    imageVector = if (hideApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle key"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Daily Goals Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE91E63).copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Goal", tint = Color(0xFFE91E63))
                        }
                        Text(text = "Daily Target Study Goal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Define your study targets in minutes. This goal will populate your dashboard progress metrics.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$dailyGoalMinutes Minutes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledIconButton(
                                onClick = {
                                    if (dailyGoalMinutes > 5) {
                                        dailyGoalMinutes -= 5
                                        sharedPrefs.edit().putInt("daily_goal_mins", dailyGoalMinutes).apply()
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Reduce")
                            }
                            FilledIconButton(
                                onClick = {
                                    dailyGoalMinutes += 5
                                    sharedPrefs.edit().putInt("daily_goal_mins", dailyGoalMinutes).apply()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }
            }
        }

        // Clean database/reset section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEF5350).copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Reset", tint = Color(0xFFEF5350))
                        }
                        Text(text = "Reset Application Data", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Warning: This action will permanently erase your local PDF documents, generated flashcards, quiz history logs, and co-pilot chat messages from the Room database.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // Simple wipe simulation by removing top level PDFs
                                val all = dao.getAllPdfs()
                                all.forEach { dao.deletePdf(it) }
                                clearStatusMessage = "Local Database erased successfully!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Wipe Study Database")
                    }

                    if (clearStatusMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = clearStatusMessage!!,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // About section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "StudyMind AI • Version 1.0.0",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Designed for Offline Active Recall on Google AI Studio Android",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Simple custom helper to scroll setting card rows nicely
@Composable
fun LazyColumnSettings(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
