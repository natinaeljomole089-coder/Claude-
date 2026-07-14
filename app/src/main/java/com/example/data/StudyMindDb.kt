package com.example.data

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

// Room Entities

@Entity(tableName = "pdf_documents")
data class PdfDocument(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val importedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val processingStatus: String = "READY" // QUEUED, PROCESSING, READY, FAILED
)

@Entity(
    tableName = "document_chunks",
    foreignKeys = [
        ForeignKey(
            entity = PdfDocument::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class DocumentChunk(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pdfId: String,
    val chunkIndex: Int,
    val pageNumber: Int,
    val text: String
)

@Entity(
    tableName = "flashcard_decks",
    foreignKeys = [
        ForeignKey(
            entity = PdfDocument::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class FlashcardDeck(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pdfId: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class Flashcard(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deckId: String,
    val front: String,
    val back: String,
    val type: String = "CONCEPT", // CONCEPT, DEFINITION, FACT, Date, FORMULA
    val difficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val isFavorite: Boolean = false,
    
    // SRS Spaced Repetition fields (SM-2)
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null
)

@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(
            entity = PdfDocument::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class Quiz(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pdfId: String,
    val title: String,
    val difficulty: String = "MEDIUM",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = Quiz::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("quizId")]
)
data class QuizQuestion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val quizId: String,
    val promptText: String,
    val type: String, // MCQ, TRUE_FALSE, FILL_BLANK, SHORT_ANSWER
    val optionsJson: String?, // Serialized JSON string of option list
    val correctAnswer: String,
    val explanation: String,
    val citationPage: Int = 1
)

@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = Quiz::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("quizId")]
)
data class QuizAttempt(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val quizId: String,
    val scorePercent: Float,
    val date: Long = System.currentTimeMillis(),
    val durationSeconds: Int
)

@Entity(
    tableName = "smart_notes",
    foreignKeys = [
        ForeignKey(
            entity = PdfDocument::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class SmartNote(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pdfId: String,
    val chapterTitle: String,
    val markdownContent: String,
    val keyTakeaways: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tutor_messages",
    foreignKeys = [
        ForeignKey(
            entity = PdfDocument::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class TutorMessage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pdfId: String,
    val role: String, // USER, ASSISTANT
    val content: String,
    val citationPage: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// DAOs

@Dao
interface StudyMindDao {
    // PDFs
    @Query("SELECT * FROM pdf_documents ORDER BY importedAt DESC")
    suspend fun getAllPdfs(): List<PdfDocument>

    @Query("SELECT * FROM pdf_documents WHERE id = :pdfId")
    suspend fun getPdfById(pdfId: String): PdfDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfDocument)

    @Delete
    suspend fun deletePdf(pdf: PdfDocument)

    // Chunks
    @Query("SELECT * FROM document_chunks WHERE pdfId = :pdfId ORDER BY chunkIndex ASC")
    suspend fun getChunksForPdf(pdfId: String): List<DocumentChunk>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<DocumentChunk>)

    // Decks
    @Query("SELECT * FROM flashcard_decks WHERE pdfId = :pdfId")
    suspend fun getDecksForPdf(pdfId: String): List<FlashcardDeck>

    @Query("SELECT * FROM flashcard_decks")
    suspend fun getAllDecks(): List<FlashcardDeck>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeck)

    // Flashcards
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    suspend fun getCardsForDeck(deckId: String): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE nextReviewAt <= :nowTime")
    suspend fun getCardsDueForReview(nowTime: Long): List<Flashcard>

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewAt <= :nowTime")
    suspend fun getDueReviewCount(nowTime: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getTotalCardsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<Flashcard>)

    @Update
    suspend fun updateFlashcard(card: Flashcard)

    // Quizzes
    @Query("SELECT * FROM quizzes WHERE pdfId = :pdfId")
    suspend fun getQuizzesForPdf(pdfId: String): List<Quiz>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz)

    // Quiz Questions
    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId")
    suspend fun getQuestionsForQuiz(quizId: String): List<QuizQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestion>)

    // Quiz Attempts
    @Query("SELECT * FROM quiz_attempts ORDER BY date DESC")
    suspend fun getAllQuizAttempts(): List<QuizAttempt>

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId ORDER BY date DESC")
    suspend fun getAttemptsForQuiz(quizId: String): List<QuizAttempt>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttempt)

    // Notes
    @Query("SELECT * FROM smart_notes WHERE pdfId = :pdfId")
    suspend fun getNotesForPdf(pdfId: String): List<SmartNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SmartNote)

    // Tutor Messages
    @Query("SELECT * FROM tutor_messages WHERE pdfId = :pdfId ORDER BY createdAt ASC")
    suspend fun getTutorMessagesForPdf(pdfId: String): List<TutorMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorMessage(msg: TutorMessage)

    @Query("DELETE FROM tutor_messages WHERE pdfId = :pdfId")
    suspend fun clearTutorChat(pdfId: String)
}

// Room Database

@Database(
    entities = [
        PdfDocument::class,
        DocumentChunk::class,
        FlashcardDeck::class,
        Flashcard::class,
        Quiz::class,
        QuizQuestion::class,
        QuizAttempt::class,
        SmartNote::class,
        TutorMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyMindDatabase : RoomDatabase() {
    abstract fun dao(): StudyMindDao

    companion object {
        @Volatile
        private var INSTANCE: StudyMindDatabase? = null

        fun getDatabase(context: android.content.Context): StudyMindDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyMindDatabase::class.java,
                    "studymind_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
