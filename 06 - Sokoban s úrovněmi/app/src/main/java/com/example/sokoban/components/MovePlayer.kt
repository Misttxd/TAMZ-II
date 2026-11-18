package com.example.sokoban.components

import kotlin.math.abs

// 0 = podlaha, 1 = zeď, 2 = bedna, 3 = cíl,
// 4 = hráč, 5 = bedna na cíli, 6 = hráč na cíli.
internal fun movePlayer(
    gameMap: MutableList<Int>,
    horizontalStep: Int,
    verticalStep: Int,
    levelWidth: Int,
    levelHeight: Int
): Boolean {
    if(abs(horizontalStep) + abs(verticalStep) != 1)
        return false

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
        return false

    val playerColumn = playerIndex % levelWidth
    val playerRow = playerIndex / levelWidth
    val targetColumn = playerColumn + horizontalStep
    val targetRow = playerRow + verticalStep

    if(targetColumn < 0 || targetColumn >= levelWidth || targetRow < 0 || targetRow >= levelHeight)
        return false

    val targetIndex = targetRow * levelWidth + targetColumn
    var targetTile = gameMap[targetIndex]

    if(targetTile == 1)
        return false

    if(targetTile == 2 || targetTile == 5)
    {
        val boxDestinationColumn = targetColumn + horizontalStep
        val boxDestinationRow = targetRow + verticalStep

        if(boxDestinationColumn < 0 || boxDestinationColumn >= levelWidth ||
            boxDestinationRow < 0 || boxDestinationRow >= levelHeight)
            return false

        val boxDestinationIndex = boxDestinationRow * levelWidth + boxDestinationColumn
        val boxDestinationTile = gameMap[boxDestinationIndex]

        if(boxDestinationTile != 0 && boxDestinationTile != 3)
            return false

        if(boxDestinationTile == 3)
            gameMap[boxDestinationIndex] = 5
        else
            gameMap[boxDestinationIndex] = 2

        if(targetTile == 5)
            targetTile = 3
        else
            targetTile = 0
    }

    if(targetTile != 0 && targetTile != 3)
        return false

    if(gameMap[playerIndex] == 6)
        gameMap[playerIndex] = 3
    else
        gameMap[playerIndex] = 0

    if(targetTile == 3)
        gameMap[targetIndex] = 6
    else
        gameMap[targetIndex] = 4

    return true
}

internal fun isLevelCompleted(gameMap: List<Int>): Boolean
{
    for(tile in gameMap)
    {
        if(tile == 2)
            return false
    }

    return true
}
