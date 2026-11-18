package com.example.sokoban

import org.junit.Assert.assertEquals
import org.junit.Test

class MovementTest {
    @Test
    fun playerMovesInAllFourDirections() {
        val gameMap = MutableList(100) { 0 }
        gameMap[44] = 4

        movePlayer(gameMap, 1, 0)
        assertEquals(4, gameMap[45])
        movePlayer(gameMap, 0, 1)
        assertEquals(4, gameMap[55])
        movePlayer(gameMap, -1, 0)
        assertEquals(4, gameMap[54])
        movePlayer(gameMap, 0, -1)
        assertEquals(4, gameMap[44])
        assertEquals(0, gameMap[45])
        assertEquals(0, gameMap[55])
        assertEquals(0, gameMap[54])
    }

    @Test
    fun wallBlocksPlayer() {
        val gameMap = MutableList(100) { 0 }
        gameMap[44] = 4
        gameMap[45] = 1
        val originalMap = gameMap.toList()

        movePlayer(gameMap, 1, 0)

        assertEquals(originalMap, gameMap)
    }

    @Test
    fun playerCannotCrossMapEdges() {
        val gameMap = MutableList(100) { 0 }
        gameMap[9] = 4
        val originalMap = gameMap.toList()

        movePlayer(gameMap, 1, 0)
        movePlayer(gameMap, 0, -1)

        assertEquals(originalMap, gameMap)
    }

    @Test
    fun goalRemainsAfterPlayerLeaves() {
        val gameMap = MutableList(100) { 0 }
        gameMap[44] = 4
        gameMap[45] = 3

        movePlayer(gameMap, 1, 0)
        assertEquals(6, gameMap[45])
        movePlayer(gameMap, -1, 0)
        assertEquals(3, gameMap[45])
        assertEquals(4, gameMap[44])
    }

    @Test
    fun boxCanEnterAndLeaveGoal() {
        val gameMap = MutableList(100) { 0 }
        gameMap[44] = 4
        gameMap[45] = 2
        gameMap[46] = 3

        movePlayer(gameMap, 1, 0)
        assertEquals(5, gameMap[46])
        movePlayer(gameMap, 1, 0)
        assertEquals(6, gameMap[46])
        assertEquals(2, gameMap[47])
        movePlayer(gameMap, -1, 0)
        assertEquals(3, gameMap[46])
    }

    @Test
    fun boxCannotBePushedIntoAnotherBoxOrWall() {
        val gameMap = MutableList(100) { 0 }
        gameMap[44] = 4
        gameMap[45] = 2
        gameMap[46] = 2
        val originalMap = gameMap.toList()

        movePlayer(gameMap, 1, 0)
        assertEquals(originalMap, gameMap)

        gameMap[46] = 1
        movePlayer(gameMap, 1, 0)
        assertEquals(4, gameMap[44])
        assertEquals(2, gameMap[45])
        assertEquals(1, gameMap[46])
    }

    @Test
    fun boxCannotBePushedOutsideMap() {
        val gameMap = MutableList(100) { 0 }
        gameMap[8] = 4
        gameMap[9] = 2
        val originalMap = gameMap.toList()

        movePlayer(gameMap, 1, 0)

        assertEquals(originalMap, gameMap)
    }

    @Test
    fun diagonalMovementIsIgnored() {
        val gameMap = MutableList(100) { 0 }
        gameMap[44] = 4
        val originalMap = gameMap.toList()

        movePlayer(gameMap, 1, 1)

        assertEquals(originalMap, gameMap)
    }
}
