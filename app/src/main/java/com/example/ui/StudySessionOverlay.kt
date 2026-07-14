package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Flashcard
import com.example.data.SrsScheduler

@Composable
fun StudySessionOverlay(
    flashcards: List<Flashcard>,
    onDismiss: () -> Unit,
    onGrade: (Flashcard, SrsScheduler.ReviewGrade) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var reviewedCount by remember { mutableStateOf(0) }

    val totalCards = flashcards.size

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
                // Top header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Smart Flashcard Session",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Session")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentIndex < totalCards) {
                    val currentCard = flashcards[currentIndex]

                    // Session Progress indicators
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Card ${currentIndex + 1} of $totalCards",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${((currentIndex.toFloat() / totalCards) * 100).toInt()}% Done",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = (currentIndex + 1).toFloat() / totalCards,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Card Flip Section
                    // Create an elegant 3D rotation animation
                    val rotation by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12 * density
                            }
                            .clickable { isFlipped = !isFlipped },
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation <= 90f) {
                            // Front of card
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(currentCard.type, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = currentCard.front,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 28.sp
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(
                                        text = "Touch to reveal answer",
                                        fontSize = 12.sp,
                                        color = Color.LightGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // Back of card (Must be rotated back to display right side up)
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 12.dp)
                                    .graphicsLayer { rotationY = 180f },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Answer Guide",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = currentCard.back,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        lineHeight = 26.sp
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(
                                        text = "Touch to flip back",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons/Action bar
                    if (!isFlipped) {
                        Button(
                            onClick = { isFlipped = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Reveal Answer", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Grading SRS Choices
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "How well did you recall this card?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SrsGradeButton(
                                    label = "Again",
                                    color = Color(0xFFEF5350),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onGrade(currentCard, SrsScheduler.ReviewGrade.AGAIN)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                                SrsGradeButton(
                                    label = "Hard",
                                    color = Color(0xFFFF9800),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onGrade(currentCard, SrsScheduler.ReviewGrade.HARD)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                                SrsGradeButton(
                                    label = "Good",
                                    color = Color(0xFF2196F3),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onGrade(currentCard, SrsScheduler.ReviewGrade.GOOD)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                                SrsGradeButton(
                                    label = "Easy",
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onGrade(currentCard, SrsScheduler.ReviewGrade.EASY)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                            }
                        }
                    }
                } else {
                    // Session Completed screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(Color(0xFFE8F5E9), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Complete",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Study Session Completed! 🎉",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You've successfully graded $reviewedCount flashcards. The SM-2 spaced repetition intervals have been recalculated.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Done", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SrsGradeButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.height(48.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
