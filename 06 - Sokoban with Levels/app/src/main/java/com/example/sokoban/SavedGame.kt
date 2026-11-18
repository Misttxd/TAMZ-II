package com.example.sokoban

import android.content.Context
import androidx.core.content.edit

private const val GAME_STORAGE_NAME = "saved_game"
private const val LEVEL_INDEX_KEY = "level_index"
private const val LEVEL_DATA_KEY = "level_data"
private const val MOVE_COUNT_KEY = "move_count"

fun saveGame(context: Context, levelIndex: Int, levelData: List<Int>, moveCount: Int) {
    context.getSharedPreferences(GAME_STORAGE_NAME, Context.MODE_PRIVATE)
        .edit {
            putInt(LEVEL_INDEX_KEY, levelIndex)
                .putString(LEVEL_DATA_KEY, levelData.joinToString(","))
                .putInt(MOVE_COUNT_KEY, moveCount)
        }
}

fun getSavedLevelIndex(context: Context): Int? {
    val storage = context.getSharedPreferences(GAME_STORAGE_NAME, Context.MODE_PRIVATE)
    val levelIndex = storage.getInt(LEVEL_INDEX_KEY, -1)

    if (levelIndex == -1)
        return null

    return levelIndex
}

fun loadSavedGame(context: Context, levelIndex: Int): List<Int>? {
    val storage = context.getSharedPreferences(GAME_STORAGE_NAME, Context.MODE_PRIVATE)

    if (storage.getInt(LEVEL_INDEX_KEY, -1) != levelIndex)
        return null

    val savedLevelData = storage.getString(LEVEL_DATA_KEY, null)

    if (savedLevelData == null)
        return null

    return savedLevelData.split(",").map { it.toInt() }
}

fun loadSavedMoveCount(context: Context, levelIndex: Int): Int {
    val storage = context.getSharedPreferences(GAME_STORAGE_NAME, Context.MODE_PRIVATE)

    if (storage.getInt(LEVEL_INDEX_KEY, -1) != levelIndex)
        return 0

    return storage.getInt(MOVE_COUNT_KEY, 0)
}

fun clearSavedGame(context: Context) {
    context.getSharedPreferences(GAME_STORAGE_NAME, Context.MODE_PRIVATE)
        .edit {
            clear()
        }
}
