package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfDocument
import com.example.data.StudyMindDao
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    dao: StudyMindDao,
    onNavigateToPdf: (String) -> Unit,
    onNavigateToStudy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var pdfList by remember { mutableStateOf<List<PdfDocument>>(emptyList()) }
    var totalCards by remember { mutableStateOf(0) }
    var dueCards by remember { mutableStateOf(0) }
    var quizAttemptsCount by remember { mutableStateOf(0) }
    var averageScore by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        pdfList = dao.getAllPdfs()
        totalCards = dao.getTotalCardsCount()
        dueCards = dao.getDueReviewCount(System.currentTimeMillis())
        val attempts = dao.getAllQuizAttempts()
        quizAttemptsCount = attempts.size
        averageScore = if (attempts.isNotEmpty()) {
            attempts.map { it.scorePercent }.average().toFloat()
        } else {
            0f
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val cardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Streak Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, Scholar! 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ready to master your textbooks? Let's generate some active recall cards today.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Streak flame badge
                    Surface(
                        color = Color(0xFFFFECE5),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "3 Days",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Daily Study Goal Progress Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Goal Circular Progress
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier.size(84.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Circle Background
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    style = Stroke(width = 8.dp.toPx())
                                )
                            }
                            // Progress Arc
                            val progress = if (totalCards > 0) 0.75f else 0f
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = primaryColor,
                                    startAngle = -90f,
                                    sweepAngle = progress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "75%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                               )
                                Text(
                                    text = "Goal",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Daily Progress",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Spent 15 mins reviewing. 15 more to reach target goal.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Cards Count Summary
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Flashcards Logo",
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "$dueCards Due Cards",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dueCards > 0) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total size: $totalCards cards",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricItemCard(
                    label = "Quizzes Done",
                    value = quizAttemptsCount.toString(),
                    icon = Icons.Default.Quiz,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                MetricItemCard(
                    label = "Average Score",
                    value = "${averageScore.toInt()}%",
                    icon = Icons.Default.Leaderboard,
                    color = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Materials Horizontal Row
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Study Materials",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "See All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.clickable { onNavigateToStudy() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (pdfList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(cardBg, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study documents loaded yet.\nHead over to Library to add some!",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pdfList) { pdf ->
                            RecentMaterialCard(pdf = pdf) {
                                onNavigateToPdf(pdf.id)
                            }
                        }
                    }
                }
            }
        }

        // Study Tips / Weak Topics Hub
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Weekly Study Insight",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Spaced repetition (SRS) is proven to increase long-term memory retention by up to 200%. Click on the Study tab or any of your material's card decks to start reviewing.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToStudy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Smart Study Session")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItemCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RecentMaterialCard(
    pdf: PdfDocument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(110.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "PDF File",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "PDF Info",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column {
                Text(
                    text = pdf.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${pdf.pageCount} Pages",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
