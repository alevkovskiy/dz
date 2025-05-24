package tests

import Characters.*
import GameHandler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier
import javax.xml.stream.events.Characters

class GameEndTest : LoggedTest() {
    override fun loggerName() = "GameEndTest"

    private lateinit var map: GameHandler.Map
    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        map = Map(5,5)
        player = Player(0, 20, emptyList())
        player.position = Position(0, 0)
        bot = Bot(0, 20)
        bot.position = Position(4,4)
        map.grid[0][0].terrain = Terrain.CASTLE_PLAYER
        map.grid[4][4].terrain = Terrain.CASTLE_AI

    }

    @Test
    fun `игрок с ненулевой армией в замке бота`() {
        player.ops.add(OP())
        player.position = Position(4,4)
        assertTrue(checkWinCondition(player, bot, map))
        logWarning("Проверка армии игрока: ${player.ops.size} OP юнитов")
    }

    @Test
    fun `бот с ненулевой армией в замке игрока`() {
        bot.assassins.add(Assassin())
        bot.position = Position(0,0)
        assertTrue(checkWinCondition(player, bot, map))
        logWarning("Проверка армии бота: ${bot.assassins.size} Assassin юнитов")
    }

    @Test
    fun `оба в замках, игрок сильнее`() {
        player.position = Position(4,4)
        bot.position    = Position(0,0)
        // даём игроку больше армии
        player.assassins.add(Assassin()); player.updateArmyStats()
        assertTrue(checkWinCondition(player, bot, map))
    }

    @Test
    fun `оба в замках, бот сильнее`() {
        player.position = Position(4,4)
        bot.position    = Position(0,0)
        bot.spearmen.add(Spearman()); bot.updateArmyStats()
        assertTrue(checkWinCondition(player, bot, map))
    }
}