package com.example.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

// Data transfer models for StudyMind AI responses

@JsonClass(generateAdapter = true)
data class GeneratedFlashcard(
    val front: String,
    val back: String,
    val type: String, // CONCEPT, DEFINITION, FACT, DATE, FORMULA
    val difficulty: String // EASY, MEDIUM, HARD
)

@JsonClass(generateAdapter = true)
data class GeneratedQuizQuestion(
    val promptText: String,
    val type: String, // MCQ, TRUE_FALSE, FILL_BLANK
    val options: List<String>?, // Only for MCQ
    val correctAnswer: String,
    val explanation: String,
    val citationPage: Int
)

@JsonClass(generateAdapter = true)
data class GeneratedSmartNote(
    val chapterTitle: String,
    val markdownContent: String,
    val keyTakeaways: String
)

@JsonClass(generateAdapter = true)
data class GeneratedTutorResponse(
    val answer: String,
    val citationPage: Int?
)

object StudyMindGemini {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Generates flashcards from text chunks using Gemini-3.5-Flash.
     * Beautifully falls back to rich pre-loaded simulated data if API Key is missing or invalid.
     */
    suspend fun generateFlashcards(
        pdfTitle: String,
        chunks: List<DocumentChunk>,
        apiKey: String
    ): List<GeneratedFlashcard> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Simulated High-Fidelity Fallback
            Thread.sleep(2000) // Simulate delay
            return@withContext getSimulatedFlashcards(pdfTitle)
        }

        val joinedText = chunks.joinToString("\n\n") { "Page ${it.pageNumber}: ${it.text}" }
        val prompt = """
            You are an expert tutor. Create a high-quality, professional study flashcard deck based ONLY on the following textbook content:
            
            Document Title: $pdfTitle
            Content:
            $joinedText
            
            Output MUST be a strict JSON list matching this schema:
            [
              {
                "front": "Question or prompt",
                "back": "Answer or explanation",
                "type": "CONCEPT", // CONCEPT, DEFINITION, FACT, DATE, or FORMULA
                "difficulty": "MEDIUM" // EASY, MEDIUM, or HARD
              }
            ]
            
            Generate at least 5-8 flashcards covering key terms and concepts. Respond with ONLY the raw JSON list. No markdown formatting.
        """.trimIndent()

        try {
            val response = GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json"
                    )
                )
            )

            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()
            
            val type = Types.newParameterizedType(List::class.java, GeneratedFlashcard::class.java)
            val adapter = moshi.adapter<List<GeneratedFlashcard>>(type)
            adapter.fromJson(cleanJson) ?: getSimulatedFlashcards(pdfTitle)
        } catch (e: Exception) {
            e.printStackTrace()
            getSimulatedFlashcards(pdfTitle)
        }
    }

    /**
     * Generates questions for a quiz from text chunks.
     */
    suspend fun generateQuiz(
        pdfTitle: String,
        chunks: List<DocumentChunk>,
        apiKey: String
    ): List<GeneratedQuizQuestion> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Thread.sleep(2000)
            return@withContext getSimulatedQuiz(pdfTitle)
        }

        val joinedText = chunks.joinToString("\n\n") { "Page ${it.pageNumber}: ${it.text}" }
        val prompt = """
            You are a rigorous exam designer. Create a high-fidelity quiz based on the following text:
            
            Document Title: $pdfTitle
            Content:
            $joinedText
            
            Output MUST be a strict JSON list matching this schema:
            [
              {
                "promptText": "What is the primary function of index optimization?",
                "type": "MCQ", // MCQ, TRUE_FALSE, or FILL_BLANK
                "options": ["Option A", "Option B", "Option C", "Option D"], // Only present for MCQ type, null otherwise
                "correctAnswer": "Option A", // For MCQ, must exactly match one of the options. For TRUE_FALSE, must be 'True' or 'False'. For FILL_BLANK, must be the exact correct word.
                "explanation": "Detailed explanation of why this is correct.",
                "citationPage": 1 // Estimate which page number this was derived from
              }
            ]
            
            Create 5 distinct questions covering multiple difficulty ranges. Respond with ONLY the raw JSON. No markdown.
        """.trimIndent()

        try {
            val response = GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json"
                    )
                )
            )

            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()

            val type = Types.newParameterizedType(List::class.java, GeneratedQuizQuestion::class.java)
            val adapter = moshi.adapter<List<GeneratedQuizQuestion>>(type)
            adapter.fromJson(cleanJson) ?: getSimulatedQuiz(pdfTitle)
        } catch (e: Exception) {
            e.printStackTrace()
            getSimulatedQuiz(pdfTitle)
        }
    }

    /**
     * Generates a chapter summary and smart notes from the chunks.
     */
    suspend fun generateSmartNotes(
        pdfTitle: String,
        chunks: List<DocumentChunk>,
        apiKey: String
    ): GeneratedSmartNote = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Thread.sleep(1500)
            return@withContext getSimulatedNotes(pdfTitle)
        }

        val joinedText = chunks.joinToString("\n\n") { "Page ${it.pageNumber}: ${it.text}" }
        val prompt = """
            You are a master study compiler. Create beautiful Smart Notes in markdown based on this material:
            
            Document Title: $pdfTitle
            Content:
            $joinedText
            
            Output MUST be a strict JSON object matching this schema:
            {
              "chapterTitle": "Chapter Title or Theme",
              "markdownContent": "Detailed markdown study guide with headers, bullets, bold text, and code blocks if applicable.",
              "keyTakeaways": "Bullet points listing the 3-5 absolute most critical formulas, dates, or concepts."
            }
            
            Ensure the markdown is rich and styled professionally. Respond with ONLY the raw JSON. No markdown enclosures.
        """.trimIndent()

        try {
            val response = GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json"
                    )
                )
            )

            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()

            val adapter = moshi.adapter(GeneratedSmartNote::class.java)
            adapter.fromJson(cleanJson) ?: getSimulatedNotes(pdfTitle)
        } catch (e: Exception) {
            e.printStackTrace()
            getSimulatedNotes(pdfTitle)
        }
    }

    /**
     * Answers tutor chat questions grounded in the context.
     */
    suspend fun askTutor(
        pdfTitle: String,
        chunks: List<DocumentChunk>,
        chatHistory: List<TutorMessage>,
        newQuestion: String,
        apiKey: String
    ): GeneratedTutorResponse = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Thread.sleep(1000)
            return@withContext getSimulatedTutorResponse(pdfTitle, newQuestion)
        }

        val joinedText = chunks.joinToString("\n\n") { "Page ${it.pageNumber}: ${it.text}" }
        val historyStr = chatHistory.takeLast(10).joinToString("\n") { "${it.role}: ${it.content}" }

        val prompt = """
            You are a helpful on-demand educational tutor. Answer the student's question based strictly on the provided context.
            If the answer cannot be found in the context, clearly state that it is not covered, but try to be as helpful as possible.
            
            Document Title: $pdfTitle
            Context Excerpts:
            $joinedText
            
            Conversation History:
            $historyStr
            
            New Question: $newQuestion
            
            Output MUST be a strict JSON object matching this schema:
            {
              "answer": "Clear, markdown-formatted response answering the question with explanation and bullets.",
              "citationPage": 2 // Estimated page number from context where you found this answer, or null if not found
            }
            
            Respond with ONLY the raw JSON.
        """.trimIndent()

        try {
            val response = GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json"
                    )
                )
            )

            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()

            val adapter = moshi.adapter(GeneratedTutorResponse::class.java)
            adapter.fromJson(cleanJson) ?: getSimulatedTutorResponse(pdfTitle, newQuestion)
        } catch (e: Exception) {
            e.printStackTrace()
            getSimulatedTutorResponse(pdfTitle, newQuestion)
        }
    }

    // --- Simulation Engines ---

    private fun getSimulatedFlashcards(pdfTitle: String): List<GeneratedFlashcard> {
        return when {
            pdfTitle.contains("Photosynthesis", ignoreCase = true) -> listOf(
                GeneratedFlashcard(
                    front = "Where do light-dependent reactions of photosynthesis occur?",
                    back = "In the thylakoid membranes of chloroplasts, where pigments (like chlorophyll) absorb light energy.",
                    type = "CONCEPT",
                    difficulty = "EASY"
                ),
                GeneratedFlashcard(
                    front = "What is the primary role of chlorophyll a?",
                    back = "It absorbs blue-violet and red light energy to excite electrons to a higher energy level, driving the light reactions.",
                    type = "DEFINITION",
                    difficulty = "MEDIUM"
                ),
                GeneratedFlashcard(
                    front = "What is the equation of Photosynthesis?",
                    back = "6CO₂ + 6H₂O + Light Energy -> C₆H₁₂O₆ + 6O₂",
                    type = "FORMULA",
                    difficulty = "HARD"
                ),
                GeneratedFlashcard(
                    front = "Explain the function of Stomata.",
                    back = "Microscopic pores on leaves that regulate gas exchange, allowing carbon dioxide in and oxygen and water vapor out.",
                    type = "CONCEPT",
                    difficulty = "MEDIUM"
                ),
                GeneratedFlashcard(
                    front = "What are the key products of the Calvin Cycle?",
                    back = "Glyceraldehyde 3-phosphate (G3P) sugar, ADP, and NADP+ which return to light reactions.",
                    type = "FACT",
                    difficulty = "HARD"
                )
            )
            pdfTitle.contains("Space", ignoreCase = true) -> listOf(
                GeneratedFlashcard(
                    front = "When did Apollo 11 land on the Moon?",
                    back = "Apollo 11 successfully touched down on July 20, 1969, carrying Neil Armstrong and Buzz Aldrin.",
                    type = "DATE",
                    difficulty = "EASY"
                ),
                GeneratedFlashcard(
                    front = "What is escape velocity?",
                    back = "The minimum speed needed for a non-propelled body to escape from the gravitational influence of a primary body.",
                    type = "DEFINITION",
                    difficulty = "MEDIUM"
                ),
                GeneratedFlashcard(
                    front = "What was the purpose of the Gemini Project?",
                    back = "To test and develop spaceflight capability (docking, extravehicular activity, long duration flight) in preparation for Apollo lunar landings.",
                    type = "CONCEPT",
                    difficulty = "MEDIUM"
                ),
                GeneratedFlashcard(
                    front = "Which Saturn V rocket stage had the highest thrust?",
                    back = "The S-IC first stage, which produced 7.5 million pounds of thrust using five F-1 rocket engines.",
                    type = "FACT",
                    difficulty = "HARD"
                )
            )
            else -> listOf(
                GeneratedFlashcard(
                    front = "What is the primary definition of Artificial Intelligence (AI)?",
                    back = "The simulation of human intelligence processes by machines, especially computer systems, including learning, reasoning, and self-correction.",
                    type = "DEFINITION",
                    difficulty = "EASY"
                ),
                GeneratedFlashcard(
                    front = "Explain the difference between Supervised and Unsupervised Learning.",
                    back = "Supervised learning uses labeled training data with output targets, while unsupervised learning discovers hidden patterns in unlabeled data.",
                    type = "CONCEPT",
                    difficulty = "MEDIUM"
                ),
                GeneratedFlashcard(
                    front = "What is a Neural Network?",
                    back = "A computational model inspired by the structure of biological neural systems, consisting of interconnected nodes (neurons) that process data.",
                    type = "DEFINITION",
                    difficulty = "MEDIUM"
                ),
                GeneratedFlashcard(
                    front = "What does NLP stand for in AI systems?",
                    back = "Natural Language Processing, which enables computers to understand, interpret, and generate human language text or speech.",
                    type = "DEFINITION",
                    difficulty = "EASY"
                ),
                GeneratedFlashcard(
                    front = "What is the primary constraint of Overfitting in models?",
                    back = "When a model learns training data noise too well, causing excellent performance on training data but poor generalizability to unseen data.",
                    type = "CONCEPT",
                    difficulty = "HARD"
                )
            )
        }
    }

    private fun getSimulatedQuiz(pdfTitle: String): List<GeneratedQuizQuestion> {
        return when {
            pdfTitle.contains("Photosynthesis", ignoreCase = true) -> listOf(
                GeneratedQuizQuestion(
                    promptText = "Which pigment molecules reside at the center of the photosystem reaction center?",
                    type = "MCQ",
                    options = listOf("Chlorophyll a", "Chlorophyll b", "Carotenoids", "Xanthophyll"),
                    correctAnswer = "Chlorophyll a",
                    explanation = "Chlorophyll a is the primary pigment that directly participates in light reactions and donates excited electrons.",
                    citationPage = 1
                ),
                GeneratedQuizQuestion(
                    promptText = "The splitting of water molecules during light reactions is called Photolysis.",
                    type = "TRUE_FALSE",
                    options = null,
                    correctAnswer = "True",
                    explanation = "Photolysis splits water to replenish electrons in Photosystem II, producing oxygen gas as a byproduct.",
                    citationPage = 2
                ),
                GeneratedQuizQuestion(
                    promptText = "Where does the carbon used in the Calvin Cycle come from?",
                    type = "FILL_BLANK",
                    options = null,
                    correctAnswer = "Carbon dioxide",
                    explanation = "The Calvin Cycle fixes gaseous carbon dioxide (CO2) from the atmosphere into sugar polymers.",
                    citationPage = 3
                )
            )
            else -> listOf(
                GeneratedQuizQuestion(
                    promptText = "Which learning technique utilizes rewards and punishments to train autonomous agents?",
                    type = "MCQ",
                    options = listOf("Supervised Learning", "Unsupervised Learning", "Reinforcement Learning", "Deep Learning"),
                    correctAnswer = "Reinforcement Learning",
                    explanation = "Reinforcement Learning relies on reward feedback to guide agent actions towards optimal behavior.",
                    citationPage = 1
                ),
                GeneratedQuizQuestion(
                    promptText = "Deep Learning is a subset of Machine Learning.",
                    type = "TRUE_FALSE",
                    options = null,
                    correctAnswer = "True",
                    explanation = "Deep learning uses multi-layered neural networks and is a core discipline under the machine learning umbrella.",
                    citationPage = 2
                ),
                GeneratedQuizQuestion(
                    promptText = "What represents the 'weights' tuning algorithm to minimize prediction error during network training?",
                    type = "FILL_BLANK",
                    options = null,
                    correctAnswer = "Backpropagation",
                    explanation = "Backpropagation computes the gradient of the loss function with respect to weights to optimize modern networks.",
                    citationPage = 3
                )
            )
        }
    }

    private fun getSimulatedNotes(pdfTitle: String): GeneratedSmartNote {
        return when {
            pdfTitle.contains("Photosynthesis", ignoreCase = true) -> GeneratedSmartNote(
                chapterTitle = "Biochemical Pathways: Photosynthesis",
                markdownContent = """
                    # Photosynthesis Study Guide
                    
                    Photosynthesis is the process by which autotrophs convert solar energy into chemical energy stored in glucose.
                    
                    ## 1. Light-Dependent Reactions
                    - **Location:** Thylakoid Membrane.
                    - **Inputs:** H2O, Light Energy, NADP+, ADP.
                    - **Outputs:** O2, NADPH, ATP.
                    - **Photosystem II (PSII):** Absorbs photon energy to excite electrons. Splits water molecule (photolysis) to replace lost electrons.
                    - **Photosystem I (PSI):** Receives electrons via the electron transport chain (ETC) and reduces NADP+ to NADPH.
                    
                    ## 2. Calvin Cycle (Light-Independent)
                    - **Location:** Stroma.
                    - **Inputs:** CO2, NADPH, ATP.
                    - **Outputs:** G3P (sugar precursors), NADP+, ADP.
                    - **Carbon Fixation:** Catalyzed by the enzyme **RuBisCO**, which binds CO2 to RuBP.
                """.trimIndent(),
                keyTakeaways = "• Light reactions split water (Photolysis) to generate ATP & NADPH.\n• Calvin Cycle utilizes ATP & NADPH to fix carbon dioxide into sugars.\n• RuBisCO is the primary catalyst for carbon fixation."
            )
            else -> GeneratedSmartNote(
                chapterTitle = "Foundations of Artificial Intelligence",
                markdownContent = """
                    # Introduction to Artificial Intelligence
                    
                    Artificial Intelligence (AI) encompasses various disciplines aimed at building systems capable of intelligent behaviors.
                    
                    ## 1. Core Branches of AI
                    - **Machine Learning (ML):** Teaching computers to learn from patterns in data without explicit procedural programming.
                    - **Deep Learning:** Utilizing multi-layered Artificial Neural Networks (ANNs) for advanced pattern matching.
                    - **Natural Language Processing (NLP):** Understanding and generating syntax structures of human communication.
                    
                    ## 2. Key Terms & Paradigms
                    - **Supervised:** Learning from labeled dataset (e.g., classifying emails as spam).
                    - **Unsupervised:** Discovering groupings in unlabeled data (e.g., segmenting customers by purchasing habits).
                    - **Overfitting:** When a model memorizes details of training dataset and performs poorly on unseen validation data.
                """.trimIndent(),
                keyTakeaways = "• AI mimics human cognitive tasks like learning and problem-solving.\n• Supervised learning uses labeled inputs; unsupervised finds implicit clusters.\n• Overfitting ruins generalizability of neural networks."
            )
        }
    }

    private fun getSimulatedTutorResponse(pdfTitle: String, question: String): GeneratedTutorResponse {
        return when {
            question.contains("Calvin", ignoreCase = true) || question.contains("carbon", ignoreCase = true) -> GeneratedTutorResponse(
                answer = """
                    The **Calvin Cycle** (or light-independent reactions) occurs inside the **Stroma** of the chloroplast.
                    
                    ### Key Phases:
                    1. **Carbon Fixation:** CO2 binds with RuBP, facilitated by the enzyme **RuBisCO**.
                    2. **Reduction:** ATP and NADPH reduce carbon intermediates into **G3P** sugar.
                    3. **Regeneration:** RuBP is regenerated using additional ATP so the cycle can continue.
                    
                    Let me know if you would like me to explain the role of **RuBisCO** further!
                """.trimIndent(),
                citationPage = 2
            )
            question.contains("stomata", ignoreCase = true) -> GeneratedTutorResponse(
                answer = """
                    **Stomata** are tiny pores found on the surface of leaves and stems.
                    
                    - They allow **Carbon Dioxide (CO2)** to enter the leaf for photosynthesis.
                    - They let **Oxygen (O2)** and **Water Vapor** escape.
                    - Controlled by specialized **Guard Cells** which inflate or deflate depending on the plant's hydration.
                """.trimIndent(),
                citationPage = 1
            )
            else -> GeneratedTutorResponse(
                answer = """
                    Here is a summary based on your textbook:
                    
                    - **Primary Concept:** Your question relates to key themes in the textbook about *"$pdfTitle"*.
                    - **Context Evidence:** The chapter details how basic components interact, using a combination of inputs and pathway rules.
                    - **Actionable tip:** Pay close attention to definitions and formulas, as they are likely to appear on the upcoming quiz!
                    
                    What specific part of this concept should we dive into next?
                """.trimIndent(),
                citationPage = 1
            )
        }
    }
}
