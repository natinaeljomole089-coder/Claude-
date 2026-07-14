package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import kotlinx.coroutines.launch

@Composable
fun QuizSessionOverlay(
    quizId: String,
    dao: StudyMindDao,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var blankInputAnswer by remember { mutableStateOf("") }
    var isGraded by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isQuizCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(quizId) {
        questions = dao.getQuestionsForQuiz(quizId)
        startTime = System.currentTimeMillis()
    }

    if (questions.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading quiz questions...", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val totalQuestions = questions.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            color = Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive Evaluation Quiz",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Quiz")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isQuizCompleted) {
                    val currentQuestion = questions[currentIndex]

                    // Progress indicators
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Question ${currentIndex + 1} of $totalQuestions",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Score: $correctCount / ${currentIndex}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = (currentIndex + 1).toFloat() / totalQuestions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Prompt
                            item {
                                Text(
                                    text = currentQuestion.promptText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp
                                )
                            }

                            // MCQ Options or TextInput
                            if (currentQuestion.type == "MCQ" || currentQuestion.type == "TRUE_FALSE") {
                                // Parse optionsJson (joined by "|||")
                                val options = currentQuestion.optionsJson?.split("|||") ?: listOf("True", "False")
                                
                                items(options) { opt ->
                                    val isSelected = selectedAnswer == opt
                                    val isCorrectOpt = opt == currentQuestion.correctAnswer
                                    
                                    val cardColor = when {
                                        isGraded && isCorrectOpt -> Color(0xFFE8F5E9) // correct
                                        isGraded && isSelected && !isCorrectOpt -> Color(0xFFFFEBEE) // incorrect
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.White
                                    }

                                    val borderColor = when {
                                        isGraded && isCorrectOpt -> Color(0xFF4CAF50)
                                        isGraded && isSelected && !isCorrectOpt -> Color(0xFFEF5350)
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> Color.LightGray.copy(alpha = 0.5f)
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isGraded) { selectedAnswer = opt }
                                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = cardColor),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedAnswer = opt },
                                                enabled = !isGraded
                                            )
                                            Text(
                                                text = opt,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            } else {
                                // FILL_BLANK (text input)
                                item {
                                    OutlinedTextField(
                                        value = blankInputAnswer,
                                        onValueChange = { if (!isGraded) blankInputAnswer = it },
                                        placeholder = { Text("Type correct answer...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        enabled = !isGraded,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                if (isGraded) {
                                    item {
                                        val isCorrect = blankInputAnswer.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)
                                        val cardColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                        val labelText = if (isCorrect) "Correct answer typed!" else "Incorrect! Correct answer: ${currentQuestion.correctAnswer}"
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(cardColor, shape = RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(text = labelText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            // Graded feedback box with explanation & citation
                            if (isGraded) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Explanation",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Explanation & Citations",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = currentQuestion.explanation, fontSize = 13.sp, color = Color.DarkGray)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "📖 Grounded Source: Page ${currentQuestion.citationPage}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom controls
                    if (!isGraded) {
                        Button(
                            onClick = {
                                val isCorrect = if (currentQuestion.type == "FILL_BLANK") {
                                    blankInputAnswer.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)
                                } else {
                                    selectedAnswer == currentQuestion.correctAnswer
                                }
                                if (isCorrect) correctCount++
                                isGraded = true
                            },
                            enabled = (selectedAnswer != null || blankInputAnswer.isNotBlank()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Submit Answer", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentIndex + 1 < totalQuestions) {
                                    currentIndex++
                                    selectedAnswer = null
                                    blankInputAnswer = ""
                                    isGraded = false
                                } else {
                                    // Quiz Finished!
                                    isQuizCompleted = true
                                    coroutineScope.launch {
                                        val percent = (correctCount.toFloat() / totalQuestions) * 100
                                        dao.insertQuizAttempt(
                                            QuizAttempt(
                                                quizId = quizId,
                                                scorePercent = percent,
                                                durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                                            )
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            val nextLabel = if (currentIndex + 1 < totalQuestions) "Next Question" else "See Results"
                            Text(nextLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Completion score dashboard
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = "Finished",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Evaluation Completed! 📝",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        val percent = ((correctCount.toFloat() / totalQuestions) * 100).toInt()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$percent%",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Score: $correctCount / $totalQuestions Correct",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Duration: ${((System.currentTimeMillis() - startTime) / 1000)} seconds",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Finish Test", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
