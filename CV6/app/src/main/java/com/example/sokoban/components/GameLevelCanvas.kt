package com.example.sokoban.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.sokoban.R
import kotlin.math.roundToInt

@Composable
fun GameLevelCanvas(
    levelData: List<Int>,
    levelWidth: Int,
    levelHeight: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val tiles = remember {
        arrayOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.empty).asImageBitmap(),
            BitmapFactory.decodeResource(context.resources, R.drawable.wall).asImageBitmap(),
            BitmapFactory.decodeResource(context.resources, R.drawable.box).asImageBitmap(),
            BitmapFactory.decodeResource(context.resources, R.drawable.goal).asImageBitmap(),
            BitmapFactory.decodeResource(context.resources, R.drawable.hero).asImageBitmap(),
            BitmapFactory.decodeResource(context.resources, R.drawable.boxok).asImageBitmap(),
            BitmapFactory.decodeResource(context.resources, R.drawable.hero).asImageBitmap()
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val tileWidth = size.width / levelWidth
        val tileHeight = size.height / levelHeight
        val tileSize = minOf(tileWidth, tileHeight)

        for (row in 0 until levelHeight) {
            for (column in 0 until levelWidth) {
                val tileIndex = levelData[row * levelWidth + column]
                val tile = tiles[tileIndex]

                drawImage(
                    image = tile,
                    dstOffset = IntOffset(
                        x = (column * tileSize).roundToInt(),
                        y = (row * tileSize).roundToInt()
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
