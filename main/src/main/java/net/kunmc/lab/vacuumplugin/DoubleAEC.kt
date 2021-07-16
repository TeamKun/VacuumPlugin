package net.kunmc.lab.vacuumplugin

import org.bukkit.entity.LivingEntity

class DoubleAEC(livingEntity: LivingEntity) {
    /**
     * 下のほうのAEC
     */
    val first = spawnAEC(livingEntity)

    /**
     * 上の方のAEC
     */
    val second = spawnAEC(livingEntity)

    init {
        first.forceGetOn(livingEntity)
        second.forceGetOn(first)
    }

    fun addPassenger(e: LivingEntity) {
        second.addPassenger(e)
    }

    fun removePassenger(e: LivingEntity) {
        second.removePassenger(e)
    }

    fun containPassenger(e: LivingEntity) = second.containPassenger(e)
}
