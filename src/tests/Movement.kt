package tests

import GameHandler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tests.LoggedTest

class Movement: LoggedTest() {

    override fun loggerName() = "MovementTest"

    private lateinit var map: GameHandler.Map
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        map = Map(3, 3)
        player = Player(10, 10, emptyList())
        player.position = Position(1, 1)
    }

    @Test
    fun `выход за карту влево`() {
        player.position = Position(0, 1)
        player.move('a', map)
        assertEquals(0, player.position.x)
    }

    @Test
    fun `выход за карту вверх`() {
        player.position = Position(2, 0)
        player.move('w', map)
        assertEquals(0, player.position.y)
    }

    @Test
    fun `проход в горы`() {
        map.grid[1][0].terrain = Terrain.MOUNTAIN
        player.position = Position(1, 1)
        player.move('w', map)
        player.position = Position(1, 0)
        if (player.position.y == 0) logWarning("Игрок прошёл на гору, что запрещено")

    }

    @Test
    fun `корректность штрафов`() {
        map.grid[1][2].terrain = Terrain.NORMAL  // штраф 4, восстановится 2
        player.position = Position(1, 1)
        player.energy = 10
        player.move('s', map)
        assertEquals(8, player.energy)
    }

    @Test
    fun `проверка доступа к приватному полю`() {
        try {
            val field = Player::class.java.getDeclaredField("test_en")
            field.isAccessible = false
            if (!field.canAccess(player)) {
                logError("Попытка доступа к приватному полю 'test_en'")
                throw IllegalAccessException("Запрещено обращаться к приватному полю")
            }
        } catch (e: IllegalAccessException) {
            logWarning("Отказано в доступе к test_en: ${e.message}")
        }
    }
}