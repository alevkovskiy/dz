package GameHandler

import TimedBuildings.*
import java.io.File
import java.util.Locale
import java.util.Locale.getDefault

class Map(val width: Int, val height: Int){
    var grid = Array(height){Array(width){ Cell(Terrain.NORMAL, Occupant.NONE) }}

    fun heroes(hero: Player, comp: Bot){
        grid[hero.getPos().x][hero.getPos().y].occupant = Occupant.HERO_PLAYER
        grid[comp.getPos().x][comp.getPos().y].occupant = Occupant.HERO_AI
        hero.towerSystem.towers.forEach { tower ->
            grid[tower.position.y][tower.position.x].terrain = Terrain.SAURON_TOWER
        }
        comp.towerSystem.towers.forEach { tower ->
            grid[tower.position.y][tower.position.x].terrain = Terrain.SAURON_TOWER
        }
    }

    fun placeSpecialBuildings(){
        val candidates = mutableListOf<Position>()
        for (x in 0 until this.width) {
            for (y in 0 until this.height) {
                if (this.grid[x][y].terrain == Terrain.NORMAL || this.grid[x][y].terrain == Terrain.ROAD) {
                    candidates.add(Position(x, y))
                }
            }
        }

        val buildings = mutableListOf<TimedBuilding>()
        val shuffled = candidates.shuffled()

        if (shuffled.size >= 3) {
            this.grid[shuffled[0].x][shuffled[0].y].terrain = Terrain.LANCELOT_CAMP
            buildings.add(LancelotCamp())

            this.grid[shuffled[1].x][shuffled[1].y].terrain = Terrain.CREED
            buildings.add(CreedBrotherhood())

            this.grid[shuffled[2].x][shuffled[2].y].terrain = Terrain.DWARF_FORGE
            buildings.add(DwarfForge())
        }

    }


    fun displayMap(){
        for (y in 0 until height) {
            for (x in 0 until width) {
                print(grid[x][y].getSymbol())
            }
            println()
        }
    }

    fun getCastleAI(): List<Int> {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (this.grid[x][y].terrain == Terrain.CASTLE_AI) {
                    return listOf(x, y)
                }
            }
        }
        return emptyList()
    }

    fun getPosAI(): List<Int>{
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (this.grid[x][y].terrain == Terrain.CASTLE_AI) {
                    return when {
                        (x < width - 1 && y == height - 1) -> listOf(x + 1, y - 1)
                        (x == width - 1 && y < height - 1) -> listOf(x - 1, y + 1)
                        else -> listOf(x + 1, y + 1)
                    }
                }
            }
        }
        return emptyList()
    }

    fun getCastlePlayer(): List<Int>{
        for (y in 0 until height){
            for(x in 0 until width){
                if (this.grid[x][y].terrain == Terrain.CASTLE_PLAYER){
                return listOf(x,y)}
            }
        }
        return emptyList()
    }


    fun getPosPlayer(): List<Int>{
        for (y in 0 until height){
            for(x in 0 until width){
                if (this.grid[x][y].terrain == Terrain.CASTLE_PLAYER){
                    return when{
                        (x < width - 1 && y >= height - 1) -> listOf(x + 1,y)
                        (x >= width - 1 && y < height - 1) -> listOf(x,y + 1)
                        (x > 1 && y > 1) -> listOf(x - 1, y - 1)
                        else -> listOf(x + 1, y + 1)
                    }
                }
            }
        }
        return emptyList()
    }

    fun serialize(): String {
        val builder = StringBuilder()
        builder.append("$width,$height\n")
        for (y in 0 until height) {
            builder.append(grid[y].joinToString(";") { it.serialize() }).append("\n")
        }
        return builder.toString()
    }

    fun editOrNew(user: String): Map{
        println("Выберите режим работы:\n 1.Создать карту\n 2.Изменить существующую\n 3.Играть на существующей")
        val ans: Char? = readlnOrNull()?.firstOrNull()
        return when(ans){
            '1' -> createMap(user)
            '2' -> editMap(user)
            else -> loadMap(user)
        }
    }

    fun loadMap(name: String): Map{
        println("Выберите карту, доступны следующие варианты:\n ")

        val dict = mutableMapOf<Int, Map>()
        val dictNames = mutableMapOf<Int, String>()
        val maps = MapLoader.loadMaps("C:/game/maps/$name")
        val mapNames = MapLoader.loadMapNames("C:/game/maps/$name")
        mapNames.forEachIndexed { index, string -> dictNames.put(index+1, string)
            println("${index + 1}. $string")}
        maps.forEachIndexed { index, map ->
            dict.put(index+1, map)
        }
        val ans = readln()
        val map: Map = dict.getValue(ans.toInt())
        println(map.getCastleAI())
        return map
    }

    fun createMap(name: String): Map {
        val width = 10
        val height = 10
        val map = Map(width, height)

        var castles = Array(2) {1;1}
        var first: Int = castles[0]
        var second: Int = castles[1]

        var castlesLeft = first + second
        println("Создание новой карты ${width}x${height}")

        while (true) {
            println("\nТекущая карта:")
            map.displayMap()

            println("\nОсталось разместить замков: $castlesLeft")

            println("\nВыберите действие:")
            println("1. Задать ячейку")
            println("2. Задать диапазон")
            println("3. Сохранить карту")

            when (readlnOrNull()?.toIntOrNull()) {
                1 -> {
                    println("Введите координаты (x,y):")
                    val (x, y) = readCoordinates() ?: continue
                    castles = setCell(map, x, y, castles)
                    castlesLeft = castles[0] + castles[1]
                }
                2 -> {
                    println("Введите начальные координаты (x1,y1):")
                    val (x1, y1) = readCoordinates() ?: continue
                    println("Введите конечные координаты (x2,y2):")
                    val (x2, y2) = readCoordinates() ?: continue

                    val startX = minOf(x1, x2).coerceIn(0 until width)
                    val endX = maxOf(x1, x2).coerceIn(0 until width)
                    val startY = minOf(y1, y2).coerceIn(0 until height)
                    val endY = maxOf(y1, y2).coerceIn(0 until height)

                    setArea(map, startX, endX, startY, endY)
                }
                3 -> {
                    if (castlesLeft != 0) {
                        println("Нужно разместить все замки!")
                        continue
                    }
                    println("Введите имя файла карты:")
                    val fileName = readlnOrNull()?.trim().orEmpty().ifEmpty { "custom" }

                    val saveDir = File("C:/game/maps/$name")
                    if (!saveDir.exists()) saveDir.mkdirs()

                    val file = File(saveDir, "$fileName.map")
                    file.writeText(map.serialize())

                    println("Карта сохранена в ${file.absolutePath}")
                    return map

                }
                else -> println("Некорректный ввод")
            }
        }
    }

    private fun readCoordinates(): Pair<Int, Int>? {
        print("> ")
        return try {
            val (x, y) = readln().split(",").map { it.trim().toInt() }
            if (x in 0..9 && y in 0..9) x to y else {
                println("Координаты должны быть от 0 до 9")
                null
            }
        } catch (e: Exception) {
            println("Некорректный формат. Введите как x,y (например: 3,5)")
            null
        }
    }

    fun setCell(map: Map, x: Int, y: Int, castles: Array<Int>): Array<Int> {
        println("Выберите тип местности:")

        var first = castles[0]
        var second = castles[1]

        val availableTerrains = Terrain.entries.filter {
            when{
                it == Terrain.SAURON_TOWER || it == Terrain.TOWER_RADIUS_ATTACK || it == Terrain.TOWER_RADIUS_HEAL -> false
                it == Terrain.CASTLE_PLAYER && first == 0 -> false
                it == Terrain.CASTLE_AI && second == 0 -> false
                else -> true
            }
        }

        availableTerrains.forEachIndexed { i, terrain ->
            println("${i + 1}. ${terrain.name}")
        }

        val selectedIndex = readln().toIntOrNull()?.minus(1) ?: 0
        val terrain = availableTerrains.getOrElse(selectedIndex) { Terrain.NORMAL }

        val currentCell = map.grid[x][y]
        if (currentCell.terrain == Terrain.CASTLE_PLAYER || currentCell.terrain == Terrain.CASTLE_AI) {
            println("Ошибка: здесь уже есть замок!")
            castles
        }
        map.grid[x][y].terrain = terrain
        map.grid[x][y].occupant = Occupant.NONE

        return when(terrain){
            Terrain.CASTLE_PLAYER -> arrayOf(0,second)
            Terrain.CASTLE_AI -> arrayOf(first,0)
            else -> castles
        }
    }

    private fun setArea(map: Map, startX: Int, endX: Int, startY: Int, endY: Int): Int {
        println("Выберите тип местности для области (замки нельзя ставить в области):")

        val availableTerrains = listOf(
            Terrain.NORMAL,
            Terrain.ROAD,
            Terrain.MOUNTAIN
        )

        availableTerrains.forEachIndexed { i, terrain ->
            println("${i + 1}. ${terrain.name}")
        }

        val selectedIndex = readln().toIntOrNull()?.minus(1) ?: 0
        val terrain = availableTerrains.getOrElse(selectedIndex) { Terrain.NORMAL }

        for (x in startX..endX) {
            for (y in startY..endY) {
                if (!(map.grid[x][y].terrain == Terrain.CASTLE_PLAYER || map.grid[x][y].terrain == Terrain.CASTLE_AI)) {
                    map.grid[x][y].terrain = terrain
                    map.grid[x][y].occupant = Occupant.NONE
                }
            }
        }

        return 0
    }

    fun removeCastles(){
        this.grid[this.getCastleAI()[0]][this.getCastleAI()[1]].terrain = Terrain.NORMAL
        this.grid[this.getCastlePlayer()[0]][this.getCastlePlayer()[1]].terrain = Terrain.NORMAL
    }

    fun editMap(name: String): Map{
        println("Выберите карту для редактирования:")

        println("Выберите карту для редактирования:")

        val dict = mutableMapOf<Int, Map>()
        val dictNames = mutableMapOf<Int, String>()
        val maps = MapLoader.loadMaps("C:/game/maps/$name")
        val mapNames = MapLoader.loadMapNames("C:/game/maps/$name")
        mapNames.forEachIndexed { index, string -> dictNames.put(index+1, string)
            println("${index + 1}. $string")}
        maps.forEachIndexed { index, map ->
            dict.put(index+1, map)
        }


        val ans = readln()

        val map: Map = dict.getValue(ans.toInt())

        val width = 10
        val height = 10

        var castles = Array(2) {0;0}
        var first: Int = castles[0]
        var second: Int = castles[1]
        var castlesLeft = first + second

        println("Редактирование карты ${width}x${height}")

        println("\nТекущая карта:")
        map.displayMap()

        println("\nУдаляем замки?\n 1. Да\n 2. Нет")

        val answer: String? = readln()

        when(answer){
            "1" -> {map.removeCastles(); castles = arrayOf(1,1)}
            else -> castles = arrayOf(0,0)
        }

        while (true) {
            println("\nТекущая карта:")
            map.displayMap()

            println("\nВыберите действие:")
            println("1. Задать ячейку")
            println("2. Задать диапазон")
            println("3. Сохранить карту")

            when (readlnOrNull()?.toIntOrNull()) {
                1 -> {
                    println("Введите координаты (x,y):")
                    val (x, y) = readCoordinates() ?: continue
                    castles = setCell(map, x, y, castles)
                    castlesLeft = castles[0] + castles[1]
                }
                2 -> {
                    println("Введите начальные координаты (x1,y1):")
                    val (x1, y1) = readCoordinates() ?: continue
                    println("Введите конечные координаты (x2,y2):")
                    val (x2, y2) = readCoordinates() ?: continue

                    val startX = minOf(x1, x2).coerceIn(0 until width)
                    val endX = maxOf(x1, x2).coerceIn(0 until width)
                    val startY = minOf(y1, y2).coerceIn(0 until height)
                    val endY = maxOf(y1, y2).coerceIn(0 until height)

                    setArea(map, startX, endX, startY, endY)
                }
                3 -> {
                    if (castlesLeft != 0) {
                        println("Нужно разместить все замки!")
                        continue
                    }
                    println("Введите имя файла карты:")
                    val fileName = readlnOrNull()?.trim().orEmpty().ifEmpty { "custom" }

                    val saveDir = File("C:/game/maps/$name")
                    if (!saveDir.exists()) saveDir.mkdirs()

                    val file = File(saveDir, "$fileName.map")
                    file.writeText(map.serialize())

                    println("Карта сохранена в ${file.absolutePath}")
                    return map

                }
                else -> println("Некорректный ввод")
            }
        }
    }


    companion object {
        fun deserialize(content: String): Map {
            val lines = content.trim().lines()
            val (width, height) = lines[0].trim().split(",").map{it.toInt()}
            val grid = Array(height) {
                y ->
                val line = lines[y + 1]
                val cellStrings = line.split(";")
                Array(width) {x ->
                    Cell.deserialize(cellStrings[x])
                }
            }
            return Map(width, height).also { it.grid = grid }
        }

    }
}