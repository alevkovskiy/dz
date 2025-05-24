package tests

import GameHandler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tests.LoggedTest
import kotlin.Result.Companion.success

class BotTest : LoggedTest() {
    override fun loggerName() = "BotTest"
    private lateinit var map: GameHandler.Map
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        map = Map(10,10)
        bot = Bot(100,10)
        bot.position = Position(0,0)
        bot.testingStrategyOverride = "full_army"
        map.grid[0][0].terrain = Terrain.CASTLE_AI
        map.grid[0][0].occupant = Occupant.HERO_AI
    }

    @Test
    fun `проверка доступа`() {
        try {
            val method = Bot::class.java.getDeclaredMethod("recruitInitialUnits")
            method.invoke(bot) // ← это должно выбросить IllegalAccessException
            method.isAccessible = false
            logWarning("Отказано в доступе при обращении к приватному методу 'recruitInitialUnits'")
        } catch (e: IllegalAccessException) {
            logError("Попытка обращения к приватному методу 'recruitInitialUnits' ${e.message}")
        }
    }

    @Test
    fun `набор начальной армии`() {
        bot.publicAPI()
        assertTrue(bot.hasArmy() || bot.towerSystem.towers.isNotEmpty())
    }

    @Test
    fun `смена режима башни ботом`() {
        bot.towerSystem.addTower(Position(1,1))
        bot.turnsCount = 3
        bot.checkTowerModeToggle(bot.turnsCount)
    }
}
