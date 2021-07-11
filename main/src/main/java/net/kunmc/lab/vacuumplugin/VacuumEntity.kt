package net.kunmc.lab.vacuumplugin

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*

class VacuumEntity(private val e: LivingEntity) {
    var flag = false
    var uuid: UUID? = null

    init {
        if (e is Player) {
            // 気をつけたい
            flag = true
            uuid = e.uniqueId
        }
    }

    fun isDead(): Boolean {
        val e = getEntity()
        if (e == null) {
            return false
        } else {
            if (e is Player) {
                return e.isDead && e.isOnline && e.isValid
            }
            return e.isDead
        }
    }

    fun getEntity(): LivingEntity? {
        if (flag) {
            return Bukkit.getOnlinePlayers().filter { it.uniqueId == uuid }.getOrNull(0)
        } else {
            return e
        }
    }
}