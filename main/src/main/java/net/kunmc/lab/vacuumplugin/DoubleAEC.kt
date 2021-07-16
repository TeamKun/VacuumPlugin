package net.kunmc.lab.vacuumplugin

import org.bukkit.entity.LivingEntity

class DoubleAEC(val livingEntity: LivingEntity) {
    /**
     * 下のほうのAEC
     */
    var first = spawnAEC(livingEntity)

    /**
     * 上の方のAEC
     */

    init {
        first.forceGetOn(livingEntity)
    }

    fun addPassenger(e: LivingEntity) {
        first.addPassenger(e)
    }

    fun removePassenger(e: LivingEntity) {
        first.removePassenger(e)
    }

    fun containPassenger(e: LivingEntity) = first.containPassenger(e)
}
