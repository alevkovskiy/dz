package Characters

import kotlin.math.max
import kotlin.math.min

abstract class Army(var maxHp: Int, var damage: Int, val cost: Int) {
    var hp: Int = maxHp

    val isAlive: Boolean get() = hp > 0

    fun takeDamage(damage: Int): Boolean {
        hp = max(0, hp - damage)
        return !isAlive
    }

    fun heal(amount: Int) {
        hp = min(hp + amount, maxHp)
    }
}

class Spearman(hp: Int, damage: Int, cost: Int): Army(hp, damage, cost){
    constructor() : this(80, 25, 10)
}

class Assassin(hp: Int = 60, damage: Int = 35, cost: Int = 10): Army(hp, damage, cost){
}

class Knight(hp: Int, damage: Int, cost: Int): Army(hp, damage, cost){
    constructor() : this(120, 15, 10)
}

class OP(hp: Int, damage: Int, cost: Int): Army(hp, damage, cost){
    constructor() : this(9999, 9999, 10)
}