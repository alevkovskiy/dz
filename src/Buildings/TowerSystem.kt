package Buildings

import GameHandler.*

class TowerSystem(val owner: Owner) {
    val towers = mutableListOf<SauronTower>()
    val towerCount: Int get() = towers.size

    companion object {
        const val MAX_TOWERS = 3
    }

    fun addTower(position: Position): Boolean {
        if (towerCount >= MAX_TOWERS) {
            println("Достигнут лимит башен ($MAX_TOWERS)")
            return false
        }
        towers.add(SauronTower(owner, position))
        return true
    }

    fun upgradeTower(): Boolean {
        println("Выберите башню для улучшения:")
        this.getTowerInfo().forEach{towerInfo -> println(towerInfo)}
        val towerIndex: Int = readln().toInt().minus(1)
        if (towerIndex !in towers.indices) return false
        towers[towerIndex].upgrade()
        return true
    }

    fun getTowerInfo(): List<String> {
        return towers.mapIndexed { index, tower ->
            "Башня #${index+1} [${tower.position.x},${tower.position.y}] " +
                    "Ур.${tower.level} радиус ${tower.radius})"
        }
    }


}