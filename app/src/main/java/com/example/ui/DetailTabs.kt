package com.example.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flashcard
import com.example.data.Quiz
import com.example.data.SmartNote
import com.example.data.TutorMessage

@Composable
fun FlashcardsTab(
    flashcards: List<Flashcard>,
    onStartSession: () -> Unit
) {
    if (flashcards.isEmpty()) {
        EmptyTabPlaceholder(
            title = "No Flashcards Composed",
            description = "Hit the 'AI Hub' tab and compile a study package to automatically extract concept cards from your document chunks.",
            icon = Icons.Default.Style,
            color = Color(0xFFFF5722)
        )
        return
    }

    val dueCount = flashcards.count { it.nextReviewAt <= System.currentTimeMillis() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Study Session Ready",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$dueCount card reviews are currently due for spaced repetition review.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = onStartSession,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                    ) {
                        Text("Review Now")
                    }
                }
            }
        }

        items(flashcards) { card ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(card.type, fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp)
                        )
                        
                        Text(
                            text = "SM-2 Interval: ${card.intervalDays}d",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = card.front, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = card.back, fontSize = 13.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun QuizzesTab(
    quizzes: List<Quiz>,
    onStartQuiz: (String) -> Unit
) {
    if (quizzes.isEmpty()) {
        EmptyTabPlaceholder(
            title = "No Interactive Quizzes",
            description = "Hit the 'AI Hub' tab and compile a study package to automatically extract multi-format exam questions grounded with citations.",
            icon = Icons.Default.Quiz,
            color = Color(0xFF4CAF50)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quizzes) { quiz ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = quiz.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Difficulty: ${quiz.difficulty} • Grounded with PDF page sources", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { onStartQuiz(quiz.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Start Test")
                    }
                }
            }
        }
    }
}

@Composable
fun SmartNotesTab(notes: List<SmartNote>) {
    if (notes.isEmpty()) {
        EmptyTabPlaceholder(
            title = "No Notes Compiled",
            description = "Hit the 'AI Hub' tab and compile a study package to automatically synthesize a comprehensive high-contrast study manual in Markdown.",
            icon = Icons.Default.Book,
            color = Color(0xFF9C27B0)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(notes) { note ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = note.chapterTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Takeaways box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3E5F5), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("🔑 Key Takeaways", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7B1FA2))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = note.keyTakeaways, fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Notes Guide Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Simplified Markdown Viewer
                    val lines = note.markdownContent.split("\n")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        lines.forEach { line ->
                            val cleanLine = line.trim()
                            when {
                                cleanLine.startsWith("# ") -> {
                                    Text(
                                        text = cleanLine.substring(2),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                cleanLine.startsWith("## ") -> {
                                    Text(
                                        text = cleanLine.substring(3),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                                cleanLine.startsWith("### ") -> {
                                    Text(
                                        text = cleanLine.substring(4),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                cleanLine.startsWith("- ") || cleanLine.startsWith("* ") -> {
                                    Row(modifier = Modifier.padding(start = 8.dp)) {
                                        Text("• ", fontWeight = FontWeight.Bold)
                                        Text(text = cleanLine.substring(2), fontSize = 13.sp, color = Color.DarkGray)
                                    }
                                }
                                cleanLine.isNotEmpty() -> {
                                    Text(text = cleanLine, fontSize = 13.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorChatTab(
    messages: List<TutorMessage>,
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onClearChat: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Co-Pilot Study Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            TextButton(onClick = onClearChat) {
                Text("Clear History", color = Color.Red, fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ask your first question!", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                            Text("I am grounded in your document text chunks.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                    val isUser = msg.role == "USER"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.content,
                                    fontSize = 13.sp,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (msg.citationPage != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "📖 Page Citation: ${msg.citationPage}",
                                            fontSize = 10.sp,
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
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = { Text("Ask anything about pages...", fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            IconButton(
                onClick = onSendMessage,
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun EmptyTabPlaceholder(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(color.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
