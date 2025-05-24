package GameHandler

class Cell(var terrain: Terrain, var occupant: Occupant) {
//    var occupant позже (тип того что/кто стоит на клетке)
    fun getSymbol() : String{
        return when{
            occupant == Occupant.HERO_AI -> "🗿"
            occupant == Occupant.HERO_PLAYER -> "👮🏿‍♂️"
            terrain == Terrain.TOWER_RADIUS_HEAL -> "🟩"
            terrain == Terrain.TOWER_RADIUS_ATTACK -> "🔴"
            terrain == Terrain.SAURON_TOWER -> "👁️"
            terrain == Terrain.MOUNTAIN -> "🏔️"
            terrain == Terrain.CASTLE_AI -> "🏰"
            terrain == Terrain.CASTLE_PLAYER -> "🏰"
            terrain == Terrain.NORMAL -> "⬜️"
            terrain == Terrain.ROAD -> "⬛️"
            terrain == Terrain.CREED -> "🫂"
            terrain == Terrain.LANCELOT_CAMP -> "🤺"
            terrain == Terrain.DWARF_FORGE -> "⛓️"
            else -> "👨🏿‍🦲"
        }
    }

    fun getFine(): Int{
        return when (terrain){
            Terrain.NORMAL -> 4
            Terrain.ROAD -> 1
            Terrain.MOUNTAIN -> 999
            else -> 0
        }
    }

    fun serialize(): String {
        return "${terrain.name},${occupant.name}"
    }

    companion object {
        fun deserialize(str: String): Cell {
            val parts = str.split(",")
            val terrain = Terrain.valueOf(parts[0])
            val occupant = Occupant.valueOf(parts[1])
            return Cell(terrain, occupant)
        }
    }

}

enum class Terrain {
    NORMAL, ROAD, CASTLE_AI, CASTLE_PLAYER, MOUNTAIN, SAURON_TOWER, TOWER_RADIUS_HEAL, TOWER_RADIUS_ATTACK, CREED, LANCELOT_CAMP, DWARF_FORGE
}

enum class Occupant{
    HERO_AI, HERO_PLAYER, NONE
}

data class Position(var x: Int, var y: Int){
}

enum class Owner{
    PLAYER, AI
}

enum class Mode{
    HEAL, ATTACK
}