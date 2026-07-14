package com.example.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Base64

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Dashboard(
    audioRecorder: AudioRecorder,
    onRequestRecordPermission: (onGranted: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var salesAnalysis by remember { mutableStateOf<SalesAnalysis?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingStatus by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Coaching Card, 1: Transcript, 2: Sentiment Graph

    // Audio file launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                processAudioUri(uri, context, { isLoad -> isLoading = isLoad }, { status -> loadingStatus = status }) { analysis, err ->
                    salesAnalysis = analysis
                    errorMessage = err
                }
            }
        }
    }

    // Colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val darkBg = Color(0xFFF8FAFC)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(primaryColor, Color(0xFF4F46E5))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sales Coaching Intelligence",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                salesAnalysis = null
                                errorMessage = null
                            },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Dashboard",
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Upload or record sales calls for instant diarization, sentiment insights, and actionable coaching feedback.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Upload / Setup Hub (Show when no analysis loaded and not loading)
            if (salesAnalysis == null && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Instruction Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "info",
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ready to start?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select one of the options below to get a complete performance review of your conversation.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Large Upload Audio Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { filePickerLauncher.launch("audio/*") },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(primaryColor.copy(alpha = 0.1f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload Audio",
                                    tint = primaryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Upload Call Audio File",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Supports MP3, WAV, M4A, or AAC files",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Recording Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRecording) Color(0xFFFEE2E2) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (isRecording) Color.Red.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = "Record microphone",
                                        tint = if (isRecording) Color.Red else primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (isRecording) "Recording Sales Call..." else "Record Call in Real-time",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isRecording) "Tap stop to begin transcription and coaching" else "Use device mic to record active pitch",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isRecording) {
                                        // Stop recording and process
                                        isRecording = false
                                        val base64Data = audioRecorder.stopRecording()
                                        if (base64Data != null) {
                                            coroutineScope.launch {
                                                processAudioBase64(
                                                    base64Data = base64Data,
                                                    mimeType = "audio/m4a",
                                                    onLoading = { isLoading = it },
                                                    onStatus = { loadingStatus = it }
                                                ) { analysis, err ->
                                                    salesAnalysis = analysis
                                                    errorMessage = err
                                                }
                                            }
                                        } else {
                                            errorMessage = "Failed to stop recording or retrieve file"
                                        }
                                    } else {
                                        // Request permission and start
                                        onRequestRecordPermission {
                                            val file = audioRecorder.startRecording()
                                            if (file != null) {
                                                isRecording = true
                                            } else {
                                                errorMessage = "Could not initialize device microphone"
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecording) Color.Red else primaryColor
                                )
                            ) {
                                Text(if (isRecording) "Stop" else "Record")
                            }
                        }
                    }

                    // Try Sample Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💡 No sales audio file on hand?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instantly explore preloaded analysis dashboards with realistic SaaS and Enterprise calls.",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { salesAnalysis = SampleCalls.SaaS_Demo_Call },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                ) {
                                    Text("Try SaaS Demo Call")
                                }
                                Button(
                                    onClick = { salesAnalysis = SampleCalls.Enterprise_Security_Call },
                                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                                ) {
                                    Text("Try Enterprise Security Call")
                                }
                            }
                        }
                    }
                }
            }

            // Error Display
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error icon",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Analysis Error",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Red
                            )
                            Text(
                                text = errorMessage ?: "Unknown error has occurred",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }

            // Loading state
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 5.dp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Analyzing Call...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loadingStatus,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    // Step progress indicators for delightful feedback
                    StepProgressIndicator(loadingStatus)
                }
            }

            // Analysis Dashboard Content (Tabs & stats)
            if (salesAnalysis != null && !isLoading) {
                val analysis = salesAnalysis!!
                val lastItem = analysis.transcript.lastOrNull()
                val durationStr = lastItem?.time ?: "0:00"

                // Statistics Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Duration",
                        value = durationStr,
                        icon = Icons.Default.Timer,
                        color = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Sales Energy",
                        value = "${analysis.sentimentTimeline.map { it.salespersonEngagement }.average().toInt()}%",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1.05f)
                    )
                    StatMetricCard(
                        title = "Prospect Int.",
                        value = "${analysis.sentimentTimeline.map { it.prospectEngagement }.average().toInt()}%",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Overall Sent.",
                        value = "${analysis.sentimentTimeline.map { it.sentiment }.average().toInt()}%",
                        icon = Icons.Default.Recommend,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1.05f)
                    )
                }

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    contentColor = primaryColor
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Coaching Card", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "coaching") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Transcript", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "transcript") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Sentiment Graph", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.QueryStats, contentDescription = "sentiment") }
                    )
                }

                // Tab Content with elegant transitions
                Crossfade(
                    targetState = selectedTab,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) { tab ->
                    when (tab) {
                        0 -> CoachingCardTab(coachingCard = analysis.coachingCard)
                        1 -> TranscriptTab(transcript = analysis.transcript)
                        2 -> SentimentChart(timeline = analysis.sentimentTimeline)
                    }
                }
            }
        }
    }
}

@Composable
fun StepProgressIndicator(status: String) {
    val steps = listOf(
        "Preparing Audio File" to (status.contains("Preparing") || status.contains("Reading")),
        "Transcribing Audio" to (status.contains("Transcrib") || status.contains("Sending") || status.contains("Preparing")),
        "Diarizing Speakers" to (status.contains("Diariz") || status.contains("Parsing") || status.contains("Transcrib")),
        "Generating Insights" to (status.contains("Insights") || status.contains("Coaching") || status.contains("Parsing"))
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        steps.forEachIndexed { index, (label, isActiveOrPast) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (isActiveOrPast) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isActiveOrPast) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActiveOrPast) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CoachingCardTab(coachingCard: CoachingCard) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)), // Light emerald
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF10B981), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Positive feedback",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "3 Key Positives (What was done well)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    coachingCard.positives.forEachIndexed { index, positive ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "${index + 1}. ",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                fontSize = 14.sp
                            )
                            Text(
                                text = positive,
                                color = Color(0xFF065F46),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Light amber
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF59E0B), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Missed opportunity",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "3 Missed Opportunities",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    coachingCard.missedOpportunities.forEachIndexed { index, opportunity ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "${index + 1}. ",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                fontSize = 14.sp
                            )
                            Text(
                                text = opportunity,
                                color = Color(0xFF92400E),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranscriptTab(transcript: List<TranscriptItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transcript) { item ->
            val isSalesperson = item.speaker.contains("Salesperson", ignoreCase = true) || item.speaker.contains("A", ignoreCase = true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isSalesperson) Arrangement.End else Arrangement.Start
            ) {
                if (!isSalesperson) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF3B82F6), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Card(
                    modifier = Modifier.weight(1f, fill = false).widthIn(max = 280.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSalesperson) Color(0xFFECFDF5) else Color(0xFFEFF6FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSalesperson) "Salesperson" else "Prospect",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSalesperson) Color(0xFF065F46) else Color(0xFF1E40AF)
                            )
                            Text(
                                text = item.time,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.text,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (isSalesperson) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// Processing utilities
private suspend fun processAudioUri(
    uri: Uri,
    context: Context,
    onLoading: (Boolean) -> Unit,
    onStatus: (String) -> Unit,
    onCompleted: (SalesAnalysis?, String?) -> Unit
) {
    onLoading(true)
    onStatus("Reading selected file...")

    val base64Data = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getEncoder().encodeToString(bytes)
                } else {
                    android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    if (base64Data == null) {
        onLoading(false)
        onCompleted(null, "Could not load or parse the selected audio file.")
        return
    }

    val type = context.contentResolver.getType(uri) ?: "audio/mp3"
    processAudioBase64(base64Data, type, onLoading, onStatus, onCompleted)
}

private suspend fun processAudioBase64(
    base64Data: String,
    mimeType: String,
    onLoading: (Boolean) -> Unit,
    onStatus: (String) -> Unit,
    onCompleted: (SalesAnalysis?, String?) -> Unit
) {
    onLoading(true)
    onStatus("Uploading audio data to Gemini...")

    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        // Run demo mode when API Key is placeholder/empty
        onStatus("Simulation: Transcription & Diarization...")
        withContext(Dispatchers.IO) { Thread.sleep(1500) }
        onStatus("Simulation: Analyzing Sentiment Dynamics...")
        withContext(Dispatchers.IO) { Thread.sleep(1500) }
        onStatus("Simulation: Assembling Coaching Scorecard...")
        withContext(Dispatchers.IO) { Thread.sleep(1000) }
        onLoading(false)
        onCompleted(
            SampleCalls.SaaS_Demo_Call,
            "Notice: Running in high-fidelity demonstration mode because no valid Gemini API Key is configured in the environment variables."
        )
        return
    }

    try {
        onStatus("Transcribing & analyzing with Gemini-3.5-Flash...")

        val promptText = """
        Analyze the attached sales call audio. Produce a detailed diarized transcription, engagement timelines, and coaching analysis in a single structured JSON response.

        Your JSON output MUST match this exact schema:
        {
          "transcript": [
            {
              "speaker": "Speaker A (Salesperson)",
              "time": "M:SS",
              "text": "spoken text content here"
            }
          ],
          "sentimentTimeline": [
            {
              "time": "M:SS",
              "salespersonEngagement": 85.0,
              "prospectEngagement": 75.0,
              "sentiment": 80.0
            }
          ],
          "coachingCard": {
            "positives": [
              "positive point 1",
              "positive point 2",
              "positive point 3"
            ],
            "missedOpportunities": [
              "missed point 1",
              "missed point 2",
              "missed point 3"
            ]
          }
        }

        Guidelines for analysis:
        - Diarization: Strictly distinguish between the Salesperson (Speaker A) and the Prospect (Speaker B). Map every single dialogue exchange with a clean timestamp format like '0:15'.
        - Sentiment Graph Points: Provide a point in the timeline roughly every 15-30 seconds or whenever there's a shift in conversation dynamics.
        - Coaching: Be highly objective. The 3 positives and 3 missed opportunities should be specific, actionable, and refer directly to events in the dialogue transcript.
        """.trimIndent()

        val response = withContext(Dispatchers.IO) {
            GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Data)),
                                GeminiPart(text = promptText)
                            )
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json"
                    )
                )
            )
        }

        val jsonResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (jsonResponse == null) {
            onLoading(false)
            onCompleted(null, "The AI did not return a valid response text part.")
            return
        }

        onStatus("Parsing analysis results...")
        val cleanJson = jsonResponse.replace("```json", "").replace("```", "").trim()
        val analysis = GeminiClient.parseResponseJson(cleanJson)

        if (analysis != null) {
            onLoading(false)
            onCompleted(analysis, null)
        } else {
            onLoading(false)
            onCompleted(null, "Failed to parse the structured sales analysis JSON returned by the model.")
        }

    } catch (e: Exception) {
        e.printStackTrace()
        onLoading(false)
        onCompleted(null, "Network or API failure: ${e.localizedMessage ?: "Unknown Error"}")
    }
}
