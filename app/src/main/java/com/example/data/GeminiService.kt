package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    fun parseResponseJson(jsonString: String): SalesAnalysis? {
        return try {
            val adapter = moshi.adapter(SalesAnalysis::class.java)
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

object SampleCalls {
    val SaaS_Demo_Call = SalesAnalysis(
        transcript = listOf(
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "0:00",
                text = "Hi Sarah, thank you for taking the time to meet today! I'm Alex from CloudScale. How's your week going?"
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "0:06",
                text = "Hi Alex, it's going well, thanks. We've been pretty busy scaling our infrastructure for our new product launch."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "0:14",
                text = "Ah, that's exciting! Scaling up can be tough though. What's been the biggest bottleneck for your engineering team during this scaling phase?"
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "0:25",
                text = "Honestly, our cloud database keeps hitting performance peaks. We get queries slowing down to 5-10 seconds during heavy traffic, which causes timeouts for our customers."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "0:42",
                text = "Ouch, 5 to 10 seconds is a major issue when customers are actively using the app. So, if we could help optimize those queries and automatically cache heavy load, what would that mean for your launch timeline?"
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "0:56",
                text = "It would be huge. It would easily shave two weeks of debugging off our timeline and give us the peace of mind to launch on schedule."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "1:10",
                text = "That makes total sense. Our database optimization engine is designed specifically to intercept slow queries and index them automatically. Here's a brief look at how it works..."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "1:25",
                text = "That looks very promising. But what is the pricing model? We've looked at other products that charge based on CPU cycles, and it got incredibly expensive very quickly."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "1:38",
                text = "I completely understand that. Nobody likes surprise bills. We charge a flat fee per database cluster, which includes unlimited queries and indexing. This way, your costs remain 100% predictable as you scale."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "1:55",
                text = "Okay, that flat-fee model sounds much more transparent. I'd definitely like to see a deeper tech demo."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "2:05",
                text = "Great! I would love to show you that. I'm actually available this Thursday at 2 PM for a technical deep-dive. Let me send a calendar invitation right away."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "2:18",
                text = "Perfect, Thursday works for me. Looking forward to it!"
            )
        ),
        sentimentTimeline = listOf(
            SentimentPoint("0:00", 80f, 65f, 70f),
            SentimentPoint("0:15", 85f, 70f, 75f),
            SentimentPoint("0:30", 90f, 50f, 60f), // Prospect discussing database issues (low engagement/frustrated)
            SentimentPoint("0:45", 88f, 75f, 80f), // Salesperson shows empathy
            SentimentPoint("1:00", 92f, 85f, 88f), // Prospect gets excited about the launch benefits
            SentimentPoint("1:15", 80f, 70f, 75f),
            SentimentPoint("1:30", 82f, 60f, 65f), // Price objection
            SentimentPoint("1:45", 95f, 88f, 92f), // Excellent handling of objection
            SentimentPoint("2:00", 90f, 90f, 95f), // Closing the deal
            SentimentPoint("2:18", 95f, 95f, 98f)
        ),
        coachingCard = CoachingCard(
            positives = listOf(
                "Empathy-first discovery: Validated the prospect's pain point regarding slow database queries before jumping straight into the solution details.",
                "Objection handling: Addressed the pricing concern perfectly by explaining the flat-fee structure, directly resolving the prospect's fear of unpredictable bills.",
                "Clear next action: Successfully closed the call by securing a technical demo and proposing a specific day and time (Thursday at 2 PM)."
            ),
            missedOpportunities = listOf(
                "Rushed agenda: Did not clearly set an agenda or duration at the beginning of the call, which could have made the start feel smoother.",
                "Missed stakeholder question: Could have asked if there are other decision-makers or team leads (e.g., CTO or Dev Leads) who should attend the Thursday demo.",
                "Feature explanation: Mentioned the database optimizer but could have checked for understanding ('Does this make sense?') before continuing."
            )
        )
    )

    val Enterprise_Security_Call = SalesAnalysis(
        transcript = listOf(
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "0:00",
                text = "Hi David, this is Maria from GuardNet. Appreciate you joining our call today."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "0:05",
                text = "Sure, Maria. We have about 15 minutes. We are currently reviewing our endpoint security protocols."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "0:12",
                text = "Great. Our security suite protects over 10,000 servers globally, features 24/7 automated monitoring, zero-trust network access, and machine learning thread prevention. We also have SOC2 compliance and..."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "0:30",
                text = "Uh, okay. That's a lot of features. But what about integration? Our team is small, and we can't afford to spend months on configuration."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "0:40",
                text = "Ah yes, configuration is very easy. We have a single-agent architecture that can be deployed in under 5 minutes on all major operating systems. It runs in the background and has very low CPU usage."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "0:55",
                text = "That sounds better. What about your pricing for 100 endpoints? Our budget is pretty tight this quarter."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "1:05",
                text = "Well, we charge $15 per endpoint per month. For 100 endpoints, that's $1,500 a month. But if you sign an annual contract, I can ask my manager for a 15% discount."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "1:20",
                text = "Hmm, $1,500 is still slightly above our current allocation. Is there a lower tier with just monitoring and no zero-trust access?"
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "1:32",
                text = "No, unfortunately, we only sell the unified bundle. We believe security cannot be compromised by selecting partial tools."
            ),
            TranscriptItem(
                speaker = "Speaker B (Prospect)",
                time = "1:44",
                text = "I see. Well, let me discuss this with our finance team and we'll get back to you if it makes sense."
            ),
            TranscriptItem(
                speaker = "Speaker A (Salesperson)",
                time = "1:55",
                text = "Sounds good. I'll send you an email with our product datasheet. Thanks, David."
            )
        ),
        sentimentTimeline = listOf(
            SentimentPoint("0:00", 75f, 60f, 65f),
            SentimentPoint("0:12", 80f, 40f, 50f), // Salesperson feature dumps, prospect engagement drops
            SentimentPoint("0:30", 70f, 55f, 60f),
            SentimentPoint("0:40", 85f, 70f, 75f), // Configuration explanation increases prospect's mood
            SentimentPoint("0:55", 80f, 60f, 65f),
            SentimentPoint("1:05", 75f, 50f, 55f), // High price objections
            SentimentPoint("1:20", 70f, 45f, 50f),
            SentimentPoint("1:32", 65f, 40f, 45f), // Salesperson rigid response
            SentimentPoint("1:44", 60f, 50f, 55f),
            SentimentPoint("1:55", 65f, 55f, 60f)
        ),
        coachingCard = CoachingCard(
            positives = listOf(
                "Prompt product details: Answered technical questions about configuration and single-agent architecture accurately and clearly.",
                "Pricing transparency: Shared the pricing details immediately and directly without hesitating or beating around the bush.",
                "Friendly closure: Closed the call with a polite and helpful sign-off, offering to send additional materials."
            ),
            missedOpportunities = listOf(
                "Feature dumping: Rushed straight into listing feature bullets (zero-trust, SOC2, etc.) at the start, without asking the prospect about their specific context or challenges.",
                "Inflexible objection handling: When the prospect asked for a lower tier, the response was slightly defensive ('security cannot be compromised') instead of exploring custom packages or exploring their budget limits.",
                "Weak call-to-action: Ended with a passive 'get back to us if it makes sense' instead of suggesting a short follow-up check-in next week."
            )
        )
    )
}
