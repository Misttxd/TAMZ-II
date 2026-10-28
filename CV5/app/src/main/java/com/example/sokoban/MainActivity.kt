package com.example.sokoban

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sokoban.ui.theme.SokobanTheme
import kotlin.math.abs
import kotlin.math.roundToInt

import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

import androidx.compose.material3.Button

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokobanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private const val LEVEL_WIDTH = 10
private const val LEVEL_HEIGHT = 10

// Data levelu
private val levelData = intArrayOf(
    1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 0, 2, 3, 3, 2, 1, 0, 1, 0,
    1, 0, 1, 3, 2, 3, 2, 0, 1, 0,
    1, 0, 2, 3, 3, 2, 4, 0, 1, 0,
    1, 0, 1, 3, 2, 3, 2, 0, 1, 0,
    1, 0, 2, 3, 3, 2, 1, 0, 1, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0
)

private val levelDataEasy = intArrayOf(
    1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 0, 0, 0, 2, 0, 3, 0, 1, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 0, 0, 2, 3, 0, 4, 0, 1, 0,
    1, 0, 0, 2, 3, 0, 0, 0, 1, 0,
    1, 0, 0, 2, 3, 0, 0, 0, 1, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0
)

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val gameMap = remember { levelData.toList().toMutableStateList() }
    val levelCompleted = remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameLevelCanvas(
            levelData = gameMap,
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .aspectRatio(LEVEL_WIDTH.toFloat() / LEVEL_HEIGHT)
                .pointerInput(Unit) {
                var horizontalDragDistance = 0f
                var verticalDragDistance = 0f
                val minimumDragDistance = 24.dp.toPx()

                detectDragGestures(
                    onDragStart = {
                        horizontalDragDistance = 0f
                        verticalDragDistance = 0f
                    },
                    onDragCancel = {
                        horizontalDragDistance = 0f
                        verticalDragDistance = 0f
                    },
                    onDrag = { pointerChange, dragDistance ->
                        pointerChange.consume()
                        horizontalDragDistance += dragDistance.x
                        verticalDragDistance += dragDistance.y
                    },
                    onDragEnd = {
                        // Krátké nechtěné pohyby ignorujeme.
                        if(!levelCompleted.value &&
                            (abs(horizontalDragDistance) >= minimumDragDistance ||
                                    abs(verticalDragDistance) >= minimumDragDistance))
                        {
                            if(abs(horizontalDragDistance) > abs(verticalDragDistance))
                            {
                                if(horizontalDragDistance > 0)
                                    movePlayer(gameMap, 1, 0)
                                else
                                    movePlayer(gameMap, -1, 0)
                            }
                            else
                            {
                                if(verticalDragDistance > 0)
                                    movePlayer(gameMap, 0, 1)
                                else
                                    movePlayer(gameMap, 0, -1)
                            }
                        }
                        levelCompleted.value = isLevelCompleted(gameMap)

                        horizontalDragDistance = 0f
                        verticalDragDistance = 0f
                    }
                )
            }
        )

        if(levelCompleted.value)
            Text(
                text = "Vyhrál jsi!",
                fontSize = 32.sp,
                modifier = Modifier
                    .padding(16.dp)
            )

        Button( //restart
            onClick = {
                gameMap.clear()
                gameMap.addAll(levelData.toList())
                levelCompleted.value = false
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Restart")
        }
    }
}

private fun isLevelCompleted(gameMap: List<Int>): Boolean
{
    for(tile in gameMap)
    {
        if(tile == 2)
            return false
    }

    return true
}

// 0 = podlaha, 1 = zeď, 2 = bedna, 3 = cíl,
// 4 = hráč, 5 = bedna na cíli, 6 = hráč na cíli.
internal fun movePlayer(gameMap: MutableList<Int>, horizontalStep: Int, verticalStep: Int) {
    // Povolíme pouze jeden krok vodorovně nebo svisle.
    if(abs(horizontalStep) + abs(verticalStep) != 1)
        return

    var playerIndex = -1
    for(tileIndex in gameMap.indices)
    {
        if(gameMap[tileIndex] == 4 || gameMap[tileIndex] == 6)
        {
            playerIndex = tileIndex
            break
        }
    }

    if(playerIndex == -1)
        return

    val playerColumn = playerIndex % LEVEL_WIDTH
    val playerRow = playerIndex / LEVEL_WIDTH
    val targetColumn = playerColumn + horizontalStep
    val targetRow = playerRow + verticalStep

    if(targetColumn < 0 || targetColumn >= LEVEL_WIDTH ||
        targetRow < 0 || targetRow >= LEVEL_HEIGHT)
        return

    val targetIndex = targetRow * LEVEL_WIDTH + targetColumn
    var targetTile = gameMap[targetIndex]

    if(targetTile == 1)
        return

    if(targetTile == 2 || targetTile == 5)
    {
        val boxDestinationColumn = targetColumn + horizontalStep
        val boxDestinationRow = targetRow + verticalStep

        if(boxDestinationColumn < 0 || boxDestinationColumn >= LEVEL_WIDTH ||
            boxDestinationRow < 0 || boxDestinationRow >= LEVEL_HEIGHT)
            return

        val boxDestinationIndex = boxDestinationRow * LEVEL_WIDTH + boxDestinationColumn
        val boxDestinationTile = gameMap[boxDestinationIndex]

        // Za bednou musí být podlaha nebo cíl.
        if(boxDestinationTile != 0 && boxDestinationTile != 3)
            return

        if(boxDestinationTile == 3)
            gameMap[boxDestinationIndex] = 5
        else
            gameMap[boxDestinationIndex] = 2

        // Zapamatujeme si políčko, které bylo pod posunutou bednou.
        if(targetTile == 5)
            targetTile = 3
        else
            targetTile = 0
    }

    if(targetTile != 0 && targetTile != 3)
        return

    if(gameMap[playerIndex] == 6)
        gameMap[playerIndex] = 3
    else
        gameMap[playerIndex] = 0

    if(targetTile == 3)
        gameMap[targetIndex] = 6
    else
        gameMap[targetIndex] = 4
}

@Composable
fun GameLevelCanvas(levelData: List<Int>, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val tiles = remember {
        arrayOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.empty).asImageBitmap(), // 0
            BitmapFactory.decodeResource(context.resources, R.drawable.wall).asImageBitmap(),  // 1
            BitmapFactory.decodeResource(context.resources, R.drawable.box).asImageBitmap(),   // 2
            BitmapFactory.decodeResource(context.resources, R.drawable.goal).asImageBitmap(),  // 3
            BitmapFactory.decodeResource(context.resources, R.drawable.hero).asImageBitmap(),  // 4
            BitmapFactory.decodeResource(context.resources, R.drawable.boxok).asImageBitmap(), // 5
            BitmapFactory.decodeResource(context.resources, R.drawable.hero).asImageBitmap()   // 6
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val tileWidthPx = size.width / LEVEL_WIDTH
        val tileHeightPx = size.height / LEVEL_HEIGHT
        val tileSize = minOf(tileWidthPx, tileHeightPx)

        for (y in 0 until LEVEL_HEIGHT) {
            for (x in 0 until LEVEL_WIDTH) {
                val tileIndex = levelData[y * LEVEL_WIDTH + x]
                val img: ImageBitmap = tiles[tileIndex.coerceIn(0, tiles.lastIndex)]

                drawImage(
                    image = img,
                    dstOffset = IntOffset(
                        x = (x * tileSize).roundToInt(),
                        y = (y * tileSize).roundToInt()
                    ),
                    dstSize = IntSize(
                        width = tileSize.roundToInt(),
                        height = tileSize.roundToInt()
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameLevelCanvasPreview() {
    SokobanTheme {
        GameScreen()
    }
}
