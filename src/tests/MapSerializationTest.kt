package tests

import GameHandler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MapSerializationTest : LoggedTest() {
    override fun loggerName() = "MapSerializationTest"

    @Test
    fun `сохранение размеров`() {
        val originalMap = Map(4, 3)
        val serialized = originalMap.serialize()
        val restoredMap = GameHandler.Map.deserialize(serialized)

        assertEquals(originalMap.width, restoredMap.width)
        assertEquals(originalMap.height, restoredMap.height)
        logWarning("Проверка сохранения размеров карты: ${originalMap.width}x${originalMap.height}")
    }

    @Test
    fun `корректность сериализации`() {
        val map = Map(3, 3)
        map.grid[0][0].terrain = Terrain.ROAD
        map.grid[0][0].occupant = Occupant.HERO_PLAYER

        map.grid[1][1].terrain = Terrain.MOUNTAIN
        map.grid[1][1].occupant = Occupant.HERO_AI
        val serialized = map.serialize()
        val deserialized = GameHandler.Map.deserialize(serialized)

        assertEquals(Terrain.ROAD, deserialized.grid[0][0].terrain)
        assertEquals(Occupant.HERO_PLAYER, deserialized.grid[0][0].occupant)
        assertEquals(Terrain.MOUNTAIN, deserialized.grid[1][1].terrain)
        assertEquals(Occupant.HERO_AI, deserialized.grid[1][1].occupant)
        logWarning("Сериализация сохранила ${map.grid.size}x${map.grid[0].size} клеток")
    }

    @Test
    fun `корректность десериализации`() {
        val rawMap = """
            2,2
            NORMAL,NONE;MOUNTAIN,HERO_AI
            ROAD,HERO_PLAYER;CASTLE_AI,NONE
        """.trimIndent()
        val map = GameHandler.Map.deserialize(rawMap)

        assertEquals(2, map.width)
        assertEquals(2, map.height)
        assertEquals(Terrain.NORMAL, map.grid[0][0].terrain)
        assertEquals(Terrain.MOUNTAIN, map.grid[0][1].terrain)
        assertEquals(Terrain.ROAD, map.grid[1][0].terrain)
        assertEquals(Terrain.CASTLE_AI, map.grid[1][1].terrain)

        assertEquals(Occupant.NONE, map.grid[0][0].occupant)
        assertEquals(Occupant.HERO_AI, map.grid[0][1].occupant)
        assertEquals(Occupant.HERO_PLAYER, map.grid[1][0].occupant)
        assertEquals(Occupant.NONE, map.grid[1][1].occupant)
    }

    @Test
    fun `количество строк`() {
        val map = Map(3, 2)
        val lines = map.serialize().lines().filter { it.isNotBlank() }
        assertEquals(1 + map.height, lines.size)
    }
}