package com.example.sokoban.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.sokoban.getSavedLevelIndex
import com.example.sokoban.levels

@Composable
fun SokobanApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val selectedLevelIndex = remember {
        mutableStateOf(getSavedLevelIndex(context))
    }

    val currentLevelIndex = selectedLevelIndex.value

    if(currentLevelIndex == null)
    {
        LevelSelection(
            onLevelSelected = { levelIndex ->
                selectedLevelIndex.value = levelIndex
            },
            modifier = modifier
        )
    }
    else
    {
        GameScreen(
            levelIndex = currentLevelIndex,
            onBackToLevelSelection = {
                selectedLevelIndex.value = null
            },
            onLevelCompleted = { completedLevelIndex ->
                if(completedLevelIndex < levels.lastIndex)
                    selectedLevelIndex.value = completedLevelIndex + 1
                else
                    selectedLevelIndex.value = null
            },
            modifier = modifier
        )
    }
}
