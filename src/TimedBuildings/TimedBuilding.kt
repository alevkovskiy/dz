package TimedBuildings

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread
import GameHandler.*

interface TimedBuilding {
    val name: String
    val maxVisitors: Int
    val currentVisitors: MutableList<Visit>
    val lock: Any

    var player: Player?
    var bot: Bot?

    fun runBuildingLoop()
    fun tryVisit(visitor: Visitor, service: Service): Boolean
    fun status(): String
    fun availableServices(): List<Service>
}

data class Visitor(val name: String, val isPlayer: Boolean = false)

data class Visit(
    val visitor: Visitor,
    val service: Service,
    val endTime: Long
)

data class Service(
    val name: String,
    val bonusType: BonusType,
    val bonusValue: Int,
    val durationMs: Long
)

enum class BonusType {
    DAMAGE_KNIGHT, DAMAGE_ASSASSIN, ARMOR_ALL, DAMAGE_ALL, NONE
}

class LancelotCamp: TimedBuilding {
    override val name = "Тренировочный лагерь Ланцелота"
    override val maxVisitors = 5
    override val currentVisitors = CopyOnWriteArrayList<Visit>()
    override val lock = Any()


    override var player: Player? = null
    override var bot: Bot? = null

    private val services = listOf(
        Service("Простая тренировка", BonusType.DAMAGE_KNIGHT, 1, 12 * 100),
        Service("Интенсивный курс", BonusType.DAMAGE_KNIGHT, 8, 3 * 24 * 100)
    )

    init { thread(start = true) { runBuildingLoop() } }

    override fun runBuildingLoop() {
        while (true) {
            Thread.sleep(100)
            val now = System.currentTimeMillis()
            currentVisitors.removeIf {
                if (it.endTime <= now) {
                    println("⚔️ ${it.visitor.name} завершил ${it.service.name} в $name")

                    if (it.visitor.isPlayer) {
                        player?.applyBonus(it.service)
                    } else {
                        bot?.applyBonus(it.service)
                    }

                    true
                } else false
            }
        }
    }

    override fun tryVisit(visitor: Visitor, service: Service): Boolean {
        synchronized(lock) {
            if (currentVisitors.size >= maxVisitors) return false
            currentVisitors.add(Visit(visitor, service, System.currentTimeMillis() + service.durationMs))
        }
        return true
    }


    override fun availableServices() = services

    override fun status(): String = buildString {
        appendLine("🏕️ $name — ${currentVisitors.size}/$maxVisitors")

        currentVisitors.forEach {
            val left = (it.endTime - System.currentTimeMillis()).coerceAtLeast(0)
            appendLine("🕒 ${it.visitor.name}: ${it.service.name} (${left}мс осталось)")
        }
    }
}

class CreedBrotherhood: TimedBuilding {
    override val name = "Братство Кредо"
    override val maxVisitors = 5
    override val currentVisitors = CopyOnWriteArrayList<Visit>()
    override val lock = Any()


    override var player: Player? = null
    override var bot: Bot? = null

    private val services = listOf(
        Service("Обычная отработка", BonusType.DAMAGE_ASSASSIN, 1, 12 * 100),
        Service("Секретные техники", BonusType.DAMAGE_ASSASSIN, 7, 3 * 24 * 100)
    )

    init { thread(start = true) { runBuildingLoop() } }

    override fun runBuildingLoop() {
        while (true) {
            Thread.sleep(100)
            val now = System.currentTimeMillis()
            currentVisitors.removeIf {
                if (it.endTime <= now) {
                    println("🗡️ ${it.visitor.name} завершил ${it.service.name} в $name")
                    true
                } else false
            }
        }
    }

    override fun tryVisit(visitor: Visitor, service: Service): Boolean {
        synchronized(lock) {
            if (currentVisitors.size >= maxVisitors) return false
            currentVisitors.add(Visit(visitor, service, System.currentTimeMillis() + service.durationMs))
        }
        return true
    }

    override fun availableServices() = services

    override fun status(): String = buildString {
        appendLine("🕵️‍♂️ $name — ${currentVisitors.size}/$maxVisitors")
        currentVisitors.forEach {
            val left = (it.endTime - System.currentTimeMillis()).coerceAtLeast(0)
            appendLine("🕒 ${it.visitor.name}: ${it.service.name} (${left}мс осталось)")
        }
    }
}

class DwarfForge: TimedBuilding {
    override val name = "Кузница Дварфа"
    override val maxVisitors = 4
    override val currentVisitors = CopyOnWriteArrayList<Visit>()
    override val lock = Any()


    override var player: Player? = null
    override var bot: Bot? = null

    private val services = listOf(
        Service("Обновить оружие", BonusType.DAMAGE_ALL, 3, 2 * 24 * 100),
        Service("Обновить броню", BonusType.ARMOR_ALL, 5, 4 * 24 * 100)
    )

    init { thread(start = true) { runBuildingLoop() } }

    override fun runBuildingLoop() {
        while (true) {
            Thread.sleep(100)
            val now = System.currentTimeMillis()
            currentVisitors.removeIf {
                if (it.endTime <= now) {
                    println("🛠️ ${it.visitor.name} завершил ${it.service.name} в $name")
                    true
                } else false
            }
        }
    }

    override fun tryVisit(visitor: Visitor, service: Service): Boolean {
        synchronized(lock) {
            if (currentVisitors.size >= maxVisitors) return false
            currentVisitors.add(Visit(visitor, service, System.currentTimeMillis() + service.durationMs))
        }
        return true
    }

    override fun availableServices() = services

    override fun status(): String = buildString {
        appendLine("🧱 $name — ${currentVisitors.size}/$maxVisitors")
        currentVisitors.forEach {
            val left = (it.endTime - System.currentTimeMillis()).coerceAtLeast(0)
            appendLine("🕒 ${it.visitor.name}: ${it.service.name} (${left}мс осталось)")
        }
    }
}
