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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun PdfDetailScreen(
    pdfId: String,
    dao: StudyMindDao,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var pdf by remember { mutableStateOf<PdfDocument?>(null) }
    var chunks by remember { mutableStateOf<List<DocumentChunk>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoadingAI by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    // Flashcards state
    var decks by remember { mutableStateOf<List<FlashcardDeck>>(emptyList()) }
    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var showStudySession by remember { mutableStateOf(false) }

    // Quiz state
    var quizzes by remember { mutableStateOf<List<Quiz>>(emptyList()) }
    var showQuizSession by remember { mutableStateOf(false) }
    var selectedQuizId by remember { mutableStateOf<String?>(null) }

    // Notes state
    var notes by remember { mutableStateOf<List<SmartNote>>(emptyList()) }

    // Chat state
    var messages by remember { mutableStateOf<List<TutorMessage>>(emptyList()) }
    var userPrompt by remember { mutableStateOf("") }

    fun refreshAll() {
        coroutineScope.launch {
            val retrievedPdf = dao.getPdfById(pdfId)
            if (retrievedPdf != null) {
                pdf = retrievedPdf
                chunks = dao.getChunksForPdf(pdfId)
                
                // Refresh subcomponents
                decks = dao.getDecksForPdf(pdfId)
                if (decks.isNotEmpty()) {
                    flashcards = dao.getCardsForDeck(decks.first().id)
                }
                
                quizzes = dao.getQuizzesForPdf(pdfId)
                notes = dao.getNotesForPdf(pdfId)
                messages = dao.getTutorMessagesForPdf(pdfId)
            }
        }
    }

    LaunchedEffect(pdfId) {
        refreshAll()
    }

    if (pdf == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentPdf = pdf!!
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = currentPdf.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentPdf.pageCount} pages loaded • Local Database",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Tab Navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = primaryColor,
                edgePadding = 12.dp
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Hub", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flashcards (${flashcards.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Interactive Quiz", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Smart Notes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Tutor", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoadingAI) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = statusText, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text(text = "Please wait while StudyMind AI compiles materials...", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    Crossfade(targetState = selectedTab) { tab ->
                        when (tab) {
                            0 -> OverviewTab(
                                pdf = currentPdf,
                                deckCount = decks.size,
                                quizCount = quizzes.size,
                                noteCount = notes.size,
                                onGenerateFlashcards = {
                                    coroutineScope.launch {
                                        isLoadingAI = true
                                        statusText = "Gemini is extracting key concepts..."
                                        
                                        val generated = StudyMindGemini.generateFlashcards(
                                            currentPdf.title,
                                            chunks,
                                            BuildConfig.GEMINI_API_KEY
                                        )
                                        
                                        val deckId = UUID.randomUUID().toString()
                                        dao.insertDeck(FlashcardDeck(id = deckId, pdfId = pdfId, name = "${currentPdf.title} Study Deck"))
                                        
                                        val cards = generated.map {
                                            Flashcard(
                                                deckId = deckId,
                                                front = it.front,
                                                back = it.back,
                                                type = it.type,
                                                difficulty = it.difficulty
                                            )
                                        }
                                        dao.insertFlashcards(cards)
                                        
                                        refreshAll()
                                        isLoadingAI = false
                                        selectedTab = 1
                                    }
                                },
                                onGenerateQuiz = {
                                    coroutineScope.launch {
                                        isLoadingAI = true
                                        statusText = "Gemini is designing interactive quiz..."
                                        
                                        val generated = StudyMindGemini.generateQuiz(
                                            currentPdf.title,
                                            chunks,
                                            BuildConfig.GEMINI_API_KEY
                                        )
                                        
                                        val quizId = UUID.randomUUID().toString()
                                        dao.insertQuiz(Quiz(id = quizId, pdfId = pdfId, title = "${currentPdf.title} Comprehensive Quiz"))
                                        
                                        val questions = generated.map {
                                            val optionsStr = if (it.options != null) {
                                                // Simple serialized format for lists
                                                it.options.joinToString("|||")
                                            } else null
                                            
                                            QuizQuestion(
                                                quizId = quizId,
                                                promptText = it.promptText,
                                                type = it.type,
                                                optionsJson = optionsStr,
                                                correctAnswer = it.correctAnswer,
                                                explanation = it.explanation,
                                                citationPage = it.citationPage
                                            )
                                        }
                                        dao.insertQuestions(questions)
                                        
                                        refreshAll()
                                        isLoadingAI = false
                                        selectedTab = 2
                                    }
                                },
                                onGenerateNotes = {
                                    coroutineScope.launch {
                                        isLoadingAI = true
                                        statusText = "Gemini is writing Markdown summary guide..."
                                        
                                        val generated = StudyMindGemini.generateSmartNotes(
                                            currentPdf.title,
                                            chunks,
                                            BuildConfig.GEMINI_API_KEY
                                        )
                                        
                                        dao.insertNote(
                                            SmartNote(
                                                pdfId = pdfId,
                                                chapterTitle = generated.chapterTitle,
                                                markdownContent = generated.markdownContent,
                                                keyTakeaways = generated.keyTakeaways
                                            )
                                        )
                                        
                                        refreshAll()
                                        isLoadingAI = false
                                        selectedTab = 3
                                    }
                                }
                            )
                            1 -> FlashcardsTab(
                                flashcards = flashcards,
                                onStartSession = { showStudySession = true }
                            )
                            2 -> QuizzesTab(
                                quizzes = quizzes,
                                onStartQuiz = { qId ->
                                    selectedQuizId = qId
                                    showQuizSession = true
                                }
                            )
                            3 -> SmartNotesTab(notes = notes)
                            4 -> TutorChatTab(
                                messages = messages,
                                prompt = userPrompt,
                                onPromptChange = { userPrompt = it },
                                onSendMessage = {
                                    val promptToSend = userPrompt.trim()
                                    if (promptToSend.isNotEmpty()) {
                                        userPrompt = ""
                                        coroutineScope.launch {
                                            val userMsg = TutorMessage(pdfId = pdfId, role = "USER", content = promptToSend)
                                            dao.insertTutorMessage(userMsg)
                                            refreshAll()
                                            
                                            isLoadingAI = true
                                            statusText = "Tutor is thinking..."
                                            
                                            val currentHistory = dao.getTutorMessagesForPdf(pdfId)
                                            val aiResp = StudyMindGemini.askTutor(
                                                currentPdf.title,
                                                chunks,
                                                currentHistory,
                                                promptToSend,
                                                BuildConfig.GEMINI_API_KEY
                                            )
                                            
                                            val assistantMsg = TutorMessage(
                                                pdfId = pdfId,
                                                role = "ASSISTANT",
                                                content = aiResp.answer,
                                                citationPage = aiResp.citationPage
                                            )
                                            dao.insertTutorMessage(assistantMsg)
                                            
                                            refreshAll()
                                            isLoadingAI = false
                                        }
                                    }
                                },
                                onClearChat = {
                                    coroutineScope.launch {
                                        dao.clearTutorChat(pdfId)
                                        refreshAll()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Flashcards Spaced Repetition Overlay
        if (showStudySession && flashcards.isNotEmpty()) {
            StudySessionOverlay(
                flashcards = flashcards,
                onDismiss = {
                    showStudySession = false
                    refreshAll()
                },
                onGrade = { card, grade ->
                    coroutineScope.launch {
                        val results = SrsScheduler.calculateNextReview(
                            grade = grade,
                            currentEaseFactor = card.easeFactor,
                            currentIntervalDays = card.intervalDays,
                            currentRepetitions = card.repetitions
                        )
                        
                        val updated = card.copy(
                            easeFactor = results.first,
                            intervalDays = results.second,
                            repetitions = results.third,
                            lastReviewedAt = System.currentTimeMillis(),
                            nextReviewAt = System.currentTimeMillis() + (results.second * 86400000L) // ms in days
                        )
                        dao.updateFlashcard(updated)
                    }
                }
            )
        }

        // Quiz Screen Overlay
        if (showQuizSession && selectedQuizId != null) {
            QuizSessionOverlay(
                quizId = selectedQuizId!!,
                dao = dao,
                onDismiss = {
                    showQuizSession = false
                    refreshAll()
                }
            )
        }
    }
}

@Composable
fun OverviewTab(
    pdf: PdfDocument,
    deckCount: Int,
    quizCount: Int,
    noteCount: Int,
    onGenerateFlashcards: () -> Unit,
    onGenerateFlashcardsFull: () -> Unit = onGenerateFlashcards,
    onGenerateQuiz: () -> Unit,
    onGenerateNotes: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Welcome to StudyMind AI Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tap the compile buttons below to run our Gemini-3.5-Flash model over the textbook chunks and automatically build custom active-recall card decks, interactive multiple-choice tests, and Markdown guides.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Text("Available Packages", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            StudyPackRow(
                title = "SRS Flashcard Deck",
                description = "Builds cards with vocabulary definitions, key formulas, and facts styled with dual-side flip overlays and SuperMemo-2 interval schedulers.",
                status = if (deckCount > 0) "Composed ($deckCount)" else "Not Composed",
                buttonText = if (deckCount > 0) "Regenerate" else "Compile with AI",
                icon = Icons.Default.Style,
                color = Color(0xFFFF5722),
                onAction = onGenerateFlashcards
            )
        }

        item {
            StudyPackRow(
                title = "Interactive Evaluation Quiz",
                description = "Builds full interactive multiple choice and text input quizzes, carrying custom citation markers linked directly back to your textbook sources.",
                status = if (quizCount > 0) "Composed ($quizCount)" else "Not Composed",
                buttonText = if (quizCount > 0) "Regenerate" else "Compile with AI",
                icon = Icons.Default.Quiz,
                color = Color(0xFF4CAF50),
                onAction = onGenerateQuiz
            )
        }

        item {
            StudyPackRow(
                title = "Smart Notes Compiler",
                description = "Summarizes the complex chemical structures, physics formulas, or historic dates into an elegant high-contrast study manual.",
                status = if (noteCount > 0) "Composed ($noteCount)" else "Not Composed",
                buttonText = if (noteCount > 0) "Regenerate" else "Compile with AI",
                icon = Icons.Default.Book,
                color = Color(0xFF9C27B0),
                onAction = onGenerateNotes
            )
        }
    }
}

@Composable
fun StudyPackRow(
    title: String,
    description: String,
    status: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = status, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}
