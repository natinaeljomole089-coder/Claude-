package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranscriptItem(
    @Json(name = "speaker") val speaker: String,
    @Json(name = "time") val time: String,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class SentimentPoint(
    @Json(name = "time") val time: String,
    @Json(name = "salespersonEngagement") val salespersonEngagement: Float,
    @Json(name = "prospectEngagement") val prospectEngagement: Float,
    @Json(name = "sentiment") val sentiment: Float // Overall sentiment score (0 to 100)
)

@JsonClass(generateAdapter = true)
data class CoachingCard(
    @Json(name = "positives") val positives: List<String>,
    @Json(name = "missedOpportunities") val missedOpportunities: List<String>
)

@JsonClass(generateAdapter = true)
data class SalesAnalysis(
    @Json(name = "transcript") val transcript: List<TranscriptItem>,
    @Json(name = "sentimentTimeline") val sentimentTimeline: List<SentimentPoint>,
    @Json(name = "coachingCard") val coachingCard: CoachingCard
)

// Gemini API structures
@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "responseSchema") val responseSchema: GeminiSchema? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSchema(
    @Json(name = "type") val type: String,
    @Json(name = "properties") val properties: Map<String, GeminiSchemaProperty>? = null,
    @Json(name = "required") val required: List<String>? = null,
    @Json(name = "items") val items: GeminiSchema? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSchemaProperty(
    @Json(name = "type") val type: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "items") val items: GeminiSchema? = null,
    @Json(name = "properties") val properties: Map<String, GeminiSchemaProperty>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)
