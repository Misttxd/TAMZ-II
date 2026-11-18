package com.example.sokoban.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sokoban.getBestScore
import com.example.sokoban.levelHeights
import com.example.sokoban.levelWidths
import com.example.sokoban.levels

@Composable
fun LevelSelection(
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(levels.size) { levelIndex ->
            Button(
                onClick = {
                    onLevelSelected(levelIndex)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameLevelCanvas(
                        levelData = levels[levelIndex].toList(),
                        levelWidth = levelWidths[levelIndex],
                        levelHeight = levelHeights[levelIndex],
                        modifier = Modifier.size(80.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Level ${levelIndex + 1}")

                        val bestScore = getBestScore(context, levelIndex)

                        if(bestScore == null)
                            Text("Nejlepší skóre: -")
                        else
                            Text("Nejlepší skóre: $bestScore tahů")
                    }
                }
            }
        }
    }
}
