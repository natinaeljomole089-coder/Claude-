package com.example.data

object SrsScheduler {
    
    enum class ReviewGrade {
        AGAIN,  // Incorrect / completely forgot (quality = 1)
        HARD,   // Correct after serious effort (quality = 3)
        GOOD,   // Correct after some hesitation (quality = 4)
        EASY    // Perfect response (quality = 5)
    }

    /**
     * Calculates the updated spaced repetition values for a flashcard using the SM-2 algorithm.
     * Returns a Triple of (updatedEaseFactor, updatedIntervalDays, updatedRepetitions).
     */
    fun calculateNextReview(
        grade: ReviewGrade,
        currentEaseFactor: Float,
        currentIntervalDays: Int,
        currentRepetitions: Int
    ): Triple<Float, Int, Int> {
        val quality = when (grade) {
            ReviewGrade.AGAIN -> 1
            ReviewGrade.HARD -> 3
            ReviewGrade.GOOD -> 4
            ReviewGrade.EASY -> 5
        }

        val nextRepetitions: Int
        val nextIntervalDays: Int

        if (quality < 3) {
            // Card forgotten, restart intervals
            nextRepetitions = 0
            nextIntervalDays = 1
        } else {
            nextRepetitions = currentRepetitions + 1
            nextIntervalDays = when (nextRepetitions) {
                1 -> 1
                2 -> 6
                else -> (currentIntervalDays * currentEaseFactor).toInt().coerceAtLeast(1)
            }
        }

        // Adjust Ease Factor (EF)
        var nextEaseFactor = currentEaseFactor + (0.1f - (5f - quality) * (0.08f + (5f - quality) * 0.02f))
        if (nextEaseFactor < 1.3f) {
            nextEaseFactor = 1.3f
        }

        return Triple(nextEaseFactor, nextIntervalDays, nextRepetitions)
    }
}
