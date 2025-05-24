package GameHandler

import Buildings.SauronTower
import Buildings.TowerSystem
import TimedBuildings.*
import java.io.File
import java.util.*

class Game(var player: Player, var AI: Bot){
    var map: Map = Map(0,0)
    private var players: List<String> = File("C:/game/users/users.txt").readLines()
    fun start(){
        var i: Int = 0
        while (true) {
            player.getStatus()
            map.heroes(player, AI)
            player.towerSystem.towers.forEach{tower -> tower.getRadiusAndApply(map, player, AI)}
            AI.towerSystem.towers.forEach{tower -> tower.getRadiusAndApply(map, player, AI)}
            map.displayMap()
            println("Введите свой ход: w - вперёд s - назад a - влево d - вправо")
            val move = readLine()?.trim()?.firstOrNull()?.lowercaseChar()
            player.move(move, map)
            player.checkTowerModeToggle(i)
            player.checkFight(player, AI, i)
            i += 1
            AI.move(map, i)
            AI.checkFight(player, AI, i)
            AI.checkTowerModeToggle(i)
            if (checkWinCondition(player, AI, map)){break}
        }
    }

    fun startMenu(name: String): Map{
        if (name !in this.players){
            println("Добро пожаловать, $name")
            players += name
            File("C:/game/users/users.txt").appendText(name + "\n")
            val userMapsDir = File("C:/game/maps/$name")
            if (!userMapsDir.exists()) {
                userMapsDir.mkdirs()  // Создаем все недостающие директории
                println("Создана папка для ваших карт: ${userMapsDir.path}")
            }
            return map.createMap(name)
        }
        else {
            println("Добро пожаловать обратно, $name")
            return map.editOrNew(name)
            // если с этим ником в базе нет сохранений сразу предложим создать карту/выбрать из созданных
            // считываем строку из файла с нужным ником
        }
    }
}

fun main(){
    println("Введите ник:")
    val name: String = readln().lowercase(Locale.getDefault())
    val creed = CreedBrotherhood()
    val lancelot = LancelotCamp()
    val forge = DwarfForge()
    val buildings = listOf(creed, lancelot, forge)
    val player = Player(50, 20, buildings)
    val AI = Bot(50, 20)

    buildings.forEach {
        it.player = player
        it.bot = AI
    }

    val game = Game(player, AI)
    game.map = game.startMenu(name)
    player.towerSystem = TowerSystem(Owner.PLAYER)
    player.map = game.map
    player.position = Position(game.map.getPosPlayer()[0], game.map.getPosPlayer()[1])
    AI.position = Position(game.map.getPosAI()[0], game.map.getPosAI()[1])
    game.map.placeSpecialBuildings()
    AI.map = game.map
    AI.towerSystem = TowerSystem(Owner.AI)
    game.start()
}

fun checkWinCondition(player: Player, bot: Bot, map: Map): Boolean {
    val playerInAICastle: Boolean = map.grid[player.position.x][player.position.y].terrain == Terrain.CASTLE_AI && player.hasArmy()
    val botInPlayerCastle: Boolean = map.grid[bot.position.x][bot.position.y].terrain == Terrain.CASTLE_PLAYER && bot.hasArmy()

    if (playerInAICastle || botInPlayerCastle) {
        if (playerInAICastle && botInPlayerCastle) {
            // Оба в чужих замках - сравниваем армии
            val playerPower = player.assassins.size + player.spearmen.size + player.knights.size
            val botPower = bot.assassins.size + bot.spearmen.size + bot.knights.size

            if (playerPower > botPower ||
                (playerPower == botPower && player.armydmg > bot.armydmg)) {
                println("Игрок побеждает по силе армии!")
                return true
            } else {
                println("Бот побеждает по силе армии!")
                return true
            }
        } else if (playerInAICastle) {
            println("Игрок достиг замка бота и побеждает!")
            return true
        } else {
            println("Бот достиг замка игрока и побеждает!")
            return true
        }
    }
    return false
}