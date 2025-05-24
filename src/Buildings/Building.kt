package Buildings

import GameHandler.*
import GameHandler.Map
import kotlin.math.*

class SauronTower(
    val owner: Owner,
    var position: Position,
    var level: Int = 1,
    var mode: Mode = Mode.HEAL
) {
    companion object {
        const val BASE_COST = 20
        const val UPGRADE_COST = 10
        const val MAX_LEVEL = 3
        const val BASE_POWER = 10
    }

    val effectPower: Int get() = BASE_POWER + (level - 1) * 5
    val radius: Int get() = level  // Радиус равен уровню башни

    fun upgrade(){
        if (level < MAX_LEVEL){
            println("Башня улучшена")
            level++
        }

        println("Невозможно улучшить, достигнут максимальный уровень")

    }

    fun toggleMode() {
        mode = when (mode) {
            Mode.HEAL -> Mode.ATTACK
            Mode.ATTACK -> Mode.HEAL
        }
        println("Башня теперь в режиме $mode")
    }

    fun getRadiusAndApply(map: Map, player: Player, AI: Bot) {
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val x = this.position.x + dx
                val y = this.position.y + dy
                // Все на расстоянии от 0 до dx/dy от башни

                if (x in 0 until map.width &&
                    y in 0 until map.height &&
                    abs(dx) + abs(dy) <= radius) {
                    // не выходим за карту

                    when (map.grid[x][y].terrain) {
                        Terrain.NORMAL, Terrain.ROAD ->
                            if (this.mode == Mode.HEAL) map.grid[x][y].terrain = Terrain.TOWER_RADIUS_HEAL
                            else if (this.mode == Mode.ATTACK) map.grid[x][y].terrain = Terrain.TOWER_RADIUS_ATTACK
                        else -> {} // Не закрашиваем постройки и прочее
                    }
                }

                when (map.grid[x][y].occupant) {
                    Occupant.HERO_PLAYER -> if (shouldAffectPlayer()) {
                        player.applyEffect(this)
                    }
                    Occupant.HERO_AI -> if (shouldAffectAI()) {
                        AI.applyEffect(this)
                    }
                    else -> continue
                }
            }
        }
    }

    private fun shouldAffectPlayer(): Boolean {
        return when (mode) {
            Mode.HEAL -> owner == Owner.PLAYER
            Mode.ATTACK -> owner == Owner.AI
        }
    }

    private fun shouldAffectAI(): Boolean {
        return when (mode) {
            Mode.HEAL -> owner == Owner.AI
            Mode.ATTACK -> owner == Owner.PLAYER
        }
    }
}