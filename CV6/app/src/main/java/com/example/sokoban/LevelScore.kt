package com.example.sokoban

import android.content.Context
import androidx.core.content.edit

private const val SCORE_STORAGE_NAME = "level_scores"

fun getBestScore(context: Context, levelIndex: Int): Int? {
    val storage = context.getSharedPreferences(SCORE_STORAGE_NAME, Context.MODE_PRIVATE)
    val score = storage.getInt("level_$levelIndex", -1)

    return if (score == -1) null else score
}

fun saveBestScore(context: Context, levelIndex: Int, score: Int) {
    val currentBestScore = getBestScore(context, levelIndex)

    if (currentBestScore == null || score < currentBestScore) {
        context.getSharedPreferences(SCORE_STORAGE_NAME, Context.MODE_PRIVATE)
            .edit {
                putInt("level_$levelIndex", score)
            }
    }
}

