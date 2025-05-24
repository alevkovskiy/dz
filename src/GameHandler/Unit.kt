package GameHandler

import Characters.Army
import Characters.Assassin
import Buildings.SauronTower
import Buildings.TowerSystem
import Characters.Knight
import Characters.OP
import Characters.Spearman
import TimedBuildings.*
import java.sql.Time
import kotlin.math.min
import kotlin.random.Random

open class Unit(var gold: Int, var energy: Int) {
    open var map: Map = Map(0,0)
    open var position: Position = Position(x=0, y=0)
    val assassins = mutableListOf<Assassin>()
    val spearmen = mutableListOf<Spearman>()
    val knights = mutableListOf<Knight>()
    val ops = mutableListOf<OP>()

    open var armyhp: Int = 0
    open var armydmg: Int = 0
    var turnsCount: Int = 0

    open fun applyEffect(tower: SauronTower) { // Метод взаимодействия башни с юнитами
        when (tower.mode) {
            Mode.HEAL -> {
                val healAmount = tower.effectPower
                // Лечим все типы юнитов
                assassins.forEach { it.heal(healAmount) }
                spearmen.forEach { it.heal(healAmount) }
                knights.forEach { it.heal(healAmount) }
                ops.forEach { it.heal(healAmount) }

                updateArmyStats()
                printHealEffect(tower, healAmount)
            }
            Mode.ATTACK -> {
                val damage = tower.effectPower
                var remainingDamage = damage

                val allUnits = mutableListOf<Army>().apply {
                    addAll(assassins)
                    addAll(spearmen)
                    addAll(knights)
                    addAll(ops)
                }.sortedBy { it.hp }  // Сначала с меньшим хп

                for (unit in allUnits) {
                    if (remainingDamage <= 0) break

                    val damageDealt = min(unit.hp, remainingDamage)
                    unit.takeDamage(damageDealt)
                    remainingDamage -= damageDealt

                    if (unit.hp <= 0) {
                        when (unit) {
                            is Assassin -> assassins.remove(unit)
                            is Spearman -> spearmen.remove(unit)
                            is Knight -> knights.remove(unit)
                            is OP -> ops.remove(unit)
                        }
                    }
                }

                updateArmyStats()
                printAttackEffect(tower, damage)
            }
        }
    }

    private fun printHealEffect(tower: SauronTower, amount: Int) {
        println("${tower.owner} башня в (${tower.position.x},${tower.position.y}) " +
                "исцелила ${this::class.simpleName} (+$amount HP)")
        println("Текущее состояние армии:")
        println(getArmy())
    }

    private fun printAttackEffect(tower: SauronTower, damage: Int) {
        println("${tower.owner} башня в (${tower.position.x},${tower.position.y}) " +
                "нанесла ${this::class.simpleName} $damage урона")
        println("Текущее состояние армии:")
        println(getArmy())
    }

    open fun getArmy(): String {
        return "Ассасины: ${assassins.size} (${assassins.sumOf { it.hp }}/${assassins.size * 60})\n" +
                "Копейщики: ${spearmen.size} (${spearmen.sumOf { it.hp }}/${spearmen.size * 80})\n" +
                "Рыцари: ${knights.size} (${knights.sumOf { it.hp }}/${knights.size * 120})" +
                if (ops.isNotEmpty()) "\nИмбы: ${ops.size}" else ""
    }

    fun updateArmyStats() {
        armyhp = assassins.sumOf { it.hp } +
                spearmen.sumOf { it.hp } +
                knights.sumOf { it.hp } +
                ops.sumOf { it.hp }

        armydmg = assassins.sumOf { it.damage } +
                spearmen.sumOf { it.damage } +
                knights.sumOf { it.damage } +
                ops.sumOf { it.damage }
    }
    fun getPos(): Position = position

    fun moveLegal(map: Map, x: Int, y: Int): Boolean =
        (x in 0 until  map.width) && (y in 0 until map.height) && (map.grid[x][y].terrain != Terrain.MOUNTAIN)

    fun applyBonus(service: Service) {
        when (service.bonusType) {
            BonusType.DAMAGE_KNIGHT -> knights.forEach { it.damage += service.bonusValue }
            BonusType.DAMAGE_ASSASSIN -> assassins.forEach { it.damage += service.bonusValue }
            BonusType.DAMAGE_ALL -> {
                knights.forEach { it.damage += service.bonusValue }
                spearmen.forEach { it.damage += service.bonusValue }
                assassins.forEach { it.damage += service.bonusValue }
            }
            BonusType.ARMOR_ALL -> {
                knights.forEach {
                    it.maxHp += service.bonusValue
                    it.hp += service.bonusValue
                }
                spearmen.forEach {
                    it.maxHp += service.bonusValue
                    it.hp += service.bonusValue
                }
                assassins.forEach {
                    it.maxHp += service.bonusValue
                    it.hp += service.bonusValue
                }
            }
            else -> {}
        }
        updateArmyStats()
        println("✅ Применён бонус: ${service.name} (+${service.bonusValue} ${service.bonusType})")
    }


    // Применяет урон к армии
    fun applyDamage(damage: Int) {
        var remainingDamage = damage
        val allUnits = mutableListOf<Army>().apply {
            addAll(assassins)
            addAll(spearmen)
            addAll(knights)
            addAll(ops)
        }.sortedBy { it.hp }

        for (unit in allUnits) {
            if (remainingDamage <= 0) break

            val damageToDeal = when {
                remainingDamage >= 120 && unit is Knight -> min(remainingDamage, unit.hp)
                remainingDamage >= 80 && unit is Spearman -> min(remainingDamage, unit.hp)
                remainingDamage >= 60 && unit is Assassin -> min(remainingDamage, unit.hp)
                else -> min(remainingDamage, unit.hp)
            }

            println("${unit::class.simpleName} получает $damageToDeal урона!")
            if (unit.takeDamage(damageToDeal)) {
                when (unit) {
                    is Assassin -> assassins.remove(unit)
                    is Spearman -> spearmen.remove(unit)
                    is Knight -> knights.remove(unit)
                    is OP -> ops.remove(unit)
                }
                println("${unit::class.simpleName} убит!")
            }
            remainingDamage -= damageToDeal
        }

        updateArmyStats()
    }

    // Лечит армию
    fun healArmy() {
        assassins.forEach { it.heal(5) }
        spearmen.forEach { it.heal(5) }
        knights.forEach { it.heal(5) }
        ops.forEach { it.heal(5) }
        updateArmyStats()
    }

    private fun fightEnd(player: Player, bot: Bot): Boolean =
        player.armyhp <= 0 || bot.armyhp <= 0

    private fun whoWon(player: Player, bot: Bot): String =
        if (player.armyhp <= 0) "Бот победил" else "Игрок победил"

    fun checkFight(player: Player, bot: Bot, i: Int) {
        if (player.getPos() == bot.getPos()) {
            fight(player, bot, i % 2 == 0)
        }
    }

    private fun fight(player: Player, bot: Bot, playerStarts: Boolean) {
        println("\n=== НАЧАЛО БИТВЫ ===")
        println("Армия игрока:\n${player.getArmy()}")
        println("Армия бота:\n${bot.getArmy()}")

        var turn = playerStarts
        var round = 1
        while (!fightEnd(player, bot)) {
            println("\n--- Раунд $round ---")
            if (!turn) {
                player.attack(bot)
            } else {
                bot.attack(player)
            }
            turn = !turn
            round++
        }
        println("\n=== КОНЕЦ БИТВЫ ===")
        println(whoWon(player, bot))
        println("Чтобы продолжить нажмите ENTER")
        val ans: String? = readlnOrNull()
    }

    protected fun attack(target: Unit) {
        val damage = this.armydmg
        println("${this::class.simpleName} атакует на $damage урона!")
        target.applyDamage(damage)
    }
}

class Player(gold: Int, energy: Int, Specs: List<TimedBuilding>) : Unit(gold, energy) {
    private var test_en = energy
    val buildings = Specs
    var towerSystem = TowerSystem(Owner.PLAYER)
    override var map: Map = Map(0,0)
    fun hasArmy(): Boolean{
        return (this.knights.isNotEmpty() || this.assassins.isNotEmpty() || this.ops.isNotEmpty() || this.spearmen.isNotEmpty())
    }

    fun move(direction: Char?, map: Map) {
        val x = position.x
        val y = position.y

        if (direction !in setOf('w', 'a', 's', 'd')) {
            println("Неизвестная команда")
            return
        }

        map.grid[x][y].occupant = Occupant.NONE

        val newX = when (direction) {
            'a' -> x - 1
            'd' -> x + 1
            else -> x
        }
        val newY = when (direction) {
            'w' -> y - 1
            's' -> y + 1
            else -> y
        }

        if (moveLegal(map, newX, newY)) {
            position = Position(newX, newY)
            nextMove(map)
        } else {
            println("Туда нельзя переместиться!")
        }
    }

    private fun nextMove(map: Map) {
        val x = position.x
        val y = position.y

        // Доход золота
        gold += 1

        // Управление энергией
        energy -= map.grid[x][y].getFine()
        if (energy < 0) {
            println("Недостаточно энергии! Пропуск хода, штраф 10 золота")
            gold -= 10
            energy = 12
            return
        }

        if (energy < 20) energy += 2
        energy = min(energy, 20)

        // Лечение армии
        healArmy()

        // Вербовка в таверне
        if (map.grid[x][y].terrain == Terrain.CASTLE_PLAYER) {
            visitTavern()
        }

        val building = buildings.find {
            when (map.grid[x][y].terrain) {
                Terrain.CREED -> it is CreedBrotherhood
                Terrain.LANCELOT_CAMP -> it is LancelotCamp
                Terrain.DWARF_FORGE -> it is DwarfForge
                else -> false
            }
        }

        if (building != null) {
            handleVisit(building, building.name)
        }

        updateArmyStats()
    }

    private fun handleVisit(building: TimedBuilding, label: String) {
        println("Вы вошли в $label")
        println(building.status())

        println("Хотите воспользоваться услугами? (1 — да, 0 — нет)")
        if (readlnOrNull()?.trim() != "1") return

        val services = building.availableServices()
        println("Услуги:")
        services.forEachIndexed { index, service ->
            println("${index + 1}. ${service.name} (+${service.bonusValue} ${service.bonusType}, ${service.durationMs}мс)")
        }

        val ans: Int = readln().toInt().minus(1)
        if (ans !in services.indices) {
            println("Неверный выбор")
            return
        }

        val visitor = Visitor("Игрок", isPlayer = true)
        if (building.tryVisit(visitor, services[ans])) {
            println("✅ Услуга принята: ${services[ans].name}")
        } else {
            println("❌ Нет свободных мест. Попробуйте позже.")
        }
    }

    fun checkTowerModeToggle(turnCount: Int){
        if (turnCount % 3 == 0 && towerSystem.towers.isNotEmpty()) {
            println("\n=== СМЕНА РЕЖИМА БАШЕН ===")
            println("Текущие башни:")
            towerSystem.getTowerInfo().forEach { println(it) }

            println("Хотите изменить режим башни?\n 1.Да\n 2.Нет")
            if (readln().equals("1", ignoreCase = true)) {
                println("Выберите башню (1-${towerSystem.towers.size}):")
                val index = readln().toIntOrNull()?.minus(1)

                index?.takeIf { it in towerSystem.towers.indices }?.let {
                    towerSystem.towers[it].toggleMode()
                } ?: println("Неверный выбор башни")
            }
        }
    }

    private fun buyTower(buyer: Player, map: Map){
        if (buyer.gold in 0..SauronTower.BASE_COST - 1){println("Недостаточно золота")}
        else{
            println(">>> map.width = ${map.width}, map.height = ${map.height}")

            println("Выберете позицию новой башни(x == y)")
            val pos = readln().toInt().minus(1)
            if (!buyer.moveLegal(map, pos, pos)) {
                println("Координаты выходят за границы карты!")
            }
            else if (map.grid[pos][pos].terrain == Terrain.SAURON_TOWER || map.grid[pos][pos].terrain == Terrain.MOUNTAIN){
                println("Тут нельзя строить башню")
            }
            else {
                towerSystem.addTower(Position(pos, pos))
                buyer.gold -= SauronTower.BASE_COST
                map.grid[pos][pos].terrain = Terrain.SAURON_TOWER
                println("Построена башня на координатах x = $pos, y = $pos\nОсталось золота: $gold")
            }
        }
    }

    private fun visitTavern() {
        println("\n=== ТАВЕРНА ===")
        println("Ваше золото: ${this.gold}")
        println("Текущая армия:\n${getArmy()}")
        if (towerSystem.towers.isNotEmpty()) {
            println("Ваши башни Саурона:")
            towerSystem.getTowerInfo().forEach{towerInfo -> println(towerInfo)}
        }

        while (true) {
            println("\n1. Нанять воинов (10 золота каждый)")
            println("2. Купить башню Саурона (20 золота)")
            if (towerSystem.towers.isNotEmpty()) {
                println("3. Улучшить башню Саурона (10 золота за уровень)")
            }
            println("4. Выйти из таверны")

            when (readLine()) {
                "1" -> {
                    println("\nНанять воинов:")
                    println("1. Ассасин (60 HP, 35 урона)")
                    println("2. Копейщик (80 HP, 25 урона)")
                    println("3. Рыцарь (120 HP, 15 урона)")
                    println("4. Имба (9999 HP, 9999 урона)")
                    println("5. Назад")

                    when (readLine()) {
                        "1" -> recruit(Assassin())
                        "2" -> recruit(Spearman())
                        "3" -> recruit(Knight())
                        "4" -> recruit(OP())
                        else -> continue
                    }
                }
                "2" -> this.buyTower(this, map)
                "3" -> if (towerSystem.towers.isNotEmpty()) towerSystem.upgradeTower()
                "4" -> break
                else -> println("Неверный выбор")
            }
        }
    }


    private fun recruit(warrior: Army) {
        if (gold >= warrior.cost) {
            gold -= warrior.cost
            when (warrior) {
                is Assassin -> assassins.add(warrior)
                is Spearman -> spearmen.add(warrior)
                is Knight -> knights.add(warrior)
                is OP -> ops.add(warrior)
            }
            updateArmyStats()
            println("Нанят ${warrior::class.simpleName}. Осталось золота: $gold")
            println("Текущая армия:\n${getArmy()}")
        } else {
            println("Недостаточно золота для найма ${warrior::class.simpleName}")
        }
    }

    fun getStatus() {
        println("\n=== СТАТУС ===")
        println("Позиция: (${position.x}, ${position.y})")
        println("Золото: $gold")
        println("Энергия: $energy")
        println("Армия:\n${getArmy()}")
    }
}

class Bot(gold: Int, energy: Int) : Unit(gold, energy) {
    var testingStrategyOverride: String? = null
    var towerSystem = TowerSystem(Owner.AI)
    override var map: Map = Map(0,0)
    private var hasRecruitedInitialUnits = false

    fun hasArmy(): Boolean{
        return (this.knights.isNotEmpty() || this.assassins.isNotEmpty() || this.spearmen.isNotEmpty())
    }

    fun getFirstMoves(): List<Position>{//- переписать
        val aim = Position(map.getCastleAI()[0], map.getCastleAI()[1])
        println(aim)
        val pos: Position = this.getPos()
        println(pos)
        return when{
            aim.x == map.width - 1 && aim.y in map.height - 1 downTo 1 -> listOf(Position(pos.x, pos.y + 1), Position(pos.x + 1, pos.y + 1))
            aim.y == map.height - 1 && aim.x in map.width - 1 downTo 1 -> listOf(Position(pos.x - 1, pos.y), Position(pos.x - 1, pos.y + 1))
            else -> listOf(Position(pos.x + 1, pos.y), Position(pos.x, pos.y - 1))
        }
    }

    fun move(map: Map, turn: Int) {
        turnsCount++
        println(turnsCount)
        nextMove(map)
        val x = position.x
        val y = position.y
        var goingBack = false

        map.grid[x][y].occupant = Occupant.NONE

        position = when (turnsCount) {
            1 -> getFirstMoves()[0]
            2 -> getFirstMoves()[1]
            else -> {

                if (map.grid[x][y].terrain == Terrain.CASTLE_PLAYER) {
                    goingBack = true
                }

                if (map.grid[x][y].terrain == Terrain.CASTLE_AI) {
                    goingBack = false
                }

                if (goingBack) {
                    if (turnsCount % 2 == 0) {
                        Position(x, y + 1).takeIf { moveLegal(map, x, y + 1) } ?: position
                    } else {
                        Position(x - 1, y).takeIf { moveLegal(map, x - 1, y) } ?: position
                    }
                } else {
                    when ((1..4).random()) {
                        1 -> Position(x + 1, y).takeIf { moveLegal(map, x + 1, y) } ?: position
                        2 -> Position(x - 1, y).takeIf { moveLegal(map, x - 1, y) } ?: position
                        3 -> Position(x, y + 1).takeIf { moveLegal(map, x, y + 1) } ?: position
                        else -> Position(x, y - 1).takeIf { moveLegal(map, x, y - 1) } ?: position
                    }
                }
            }
        }


        if (turnsCount <= 2 && map.grid[position.x][position.y].terrain == Terrain.CASTLE_AI && !hasRecruitedInitialUnits) {
            recruitInitialUnits()
            hasRecruitedInitialUnits = true
        }
    }



    fun checkTowerModeToggle(turnCount: Int) {
        if (turnCount % 3 == 0 && towerSystem.towers.isNotEmpty() && Random.nextBoolean()) {
            val randomTower = towerSystem.towers.random()
            randomTower.toggleMode()
            println("Бот изменил режим башни в (${randomTower.position.x},${randomTower.position.y}) на ${randomTower.mode}")
        }
    }

    private fun recruitInitialUnits() {
        // Случайный выбор стратегии
        val strategy = testingStrategyOverride ?: if (Random.nextBoolean()) "full_army" else "army_and_tower"

        val availableUnits = listOf(
            { Assassin() },
            { Spearman() },
            { Knight() }
        )

        when (strategy) {
            "full_army" -> {
                // Покупаем 5 случайных юнитов
                while (gold >= (availableUnits.minOf { it().cost })) {
                    val randomUnit = availableUnits[Random.nextInt(availableUnits.size)]()
                    when (randomUnit) {
                        is Assassin -> assassins.add(randomUnit)
                        is Spearman -> spearmen.add(randomUnit)
                        is Knight -> knights.add(randomUnit)
                    }
                    gold -= randomUnit.cost
                }
                println("Бот набрал полную армию")
            }
            "army_and_tower" -> {
                var tries: Int = 0
                var pos: Int
                if (gold >= 20) {
                    gold -= 20
                    do {
                        pos = Random.nextInt(map.width)
                        tries++
                    }
                    while (map.grid[pos][pos].terrain == Terrain.SAURON_TOWER || map.grid[pos][pos].terrain == Terrain.MOUNTAIN && tries < map.height)
                    towerSystem.addTower(Position(pos, pos))
                    println("Бот построил башню Саурона")
                }
                // Покупаем 3 юнита и башню
                while (gold >= availableUnits.minOf { it().cost }) {
                    val randomUnit = availableUnits[Random.nextInt(availableUnits.size)]()
                    when (randomUnit) {
                        is Assassin -> assassins.add(randomUnit)
                        is Spearman -> spearmen.add(randomUnit)
                        is Knight -> knights.add(randomUnit)
                    }
                    gold -= randomUnit.cost
                }
                println("Бот набрал армию и башню")
            }
        }

        updateArmyStats()
        println("Армия бота: ${getArmy()}")
        println("Золото бота после найма: $gold")
    }

    private fun nextMove(map: Map) {
        gold += 1
        energy -= map.grid[position.x][position.y].getFine()
        if (energy < 20) energy += 2
        energy = min(energy, 20)

        // Лечим армию
        assassins.forEach { it.heal(5) }
        spearmen.forEach { it.heal(5) }
        knights.forEach { it.heal(5) }

        updateArmyStats()
    }

    fun publicAPI() = recruitInitialUnits()
}