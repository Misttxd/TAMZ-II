package com.example.sokoban.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sokoban.clearSavedGame
import com.example.sokoban.levelHeights
import com.example.sokoban.levelWidths
import com.example.sokoban.levels
import com.example.sokoban.loadSavedGame
import com.example.sokoban.loadSavedMoveCount
import com.example.sokoban.saveBestScore
import com.example.sokoban.saveGame
import kotlin.math.abs

@Composable
fun GameScreen(
    levelIndex: Int,
    onBackToLevelSelection: () -> Unit,
    onLevelCompleted: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val levelWidth = levelWidths[levelIndex]
    val levelHeight = levelHeights[levelIndex]

    val gameMap = remember(levelIndex) {
        val savedLevel = loadSavedGame(context, levelIndex)

        if(savedLevel == null)
            levels[levelIndex].toList().toMutableStateList()
        else
            savedLevel.toMutableStateList()
    }
    val moveCount = remember(levelIndex) {
        mutableStateOf(loadSavedMoveCount(context, levelIndex))
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Level ${levelIndex + 1}",
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Tahy: ${moveCount.value}",
            modifier = Modifier.padding(4.dp)
        )

        GameLevelCanvas(
            levelData = gameMap,
            levelWidth = levelWidth,
            levelHeight = levelHeight,
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .aspectRatio(levelWidth.toFloat() / levelHeight)
                .pointerInput(levelIndex) {
                    var horizontalDragDistance = 0f
                    var verticalDragDistance = 0f

                    detectDragGestures(
                        onDragStart = {
                            horizontalDragDistance = 0f
                            verticalDragDistance = 0f
                        },
                        onDrag = { pointerChange, dragDistance ->
                            pointerChange.consume()
                            horizontalDragDistance += dragDistance.x
                            verticalDragDistance += dragDistance.y
                        },
                        onDragEnd = {
                            var playerMoved = false
                            val horizontalDistance = abs(horizontalDragDistance)
                            val verticalDistance = abs(verticalDragDistance)

                            if(horizontalDistance > verticalDistance)
                            {
                                if(horizontalDragDistance > 0)
                                    playerMoved = movePlayer(gameMap, 1, 0, levelWidth, levelHeight)
                                else
                                    playerMoved = movePlayer(gameMap, -1, 0, levelWidth, levelHeight)
                            }
                            else
                            {
                                if(verticalDragDistance > 0)
                                    playerMoved = movePlayer(gameMap, 0, 1, levelWidth, levelHeight)
                                else
                                    playerMoved = movePlayer(gameMap, 0, -1, levelWidth, levelHeight)
                            }

                            if(playerMoved)
                                moveCount.value++

                            if(isLevelCompleted(gameMap))
                            {
                                saveBestScore(context, levelIndex, moveCount.value)
                                clearSavedGame(context)
                                onLevelCompleted(levelIndex)
                            }
                            else if(playerMoved)
                            {
                                saveGame(context, levelIndex, gameMap, moveCount.value)
                            }

                        }
                    )
                }
        )

        Button(
            onClick = {
                gameMap.clear()
                gameMap.addAll(levels[levelIndex].toList())
                moveCount.value = 0
                clearSavedGame(context)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Restart")
        }

        Button(
            onClick = onBackToLevelSelection,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Výběr levelů")
        }
    }
}
