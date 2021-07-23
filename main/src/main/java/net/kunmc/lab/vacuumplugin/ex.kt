package net.kunmc.lab.vacuumplugin

import org.bukkit.Bukkit
import org.bukkit.entity.*

fun Entity.containPassenger(other: Entity) = passengers.contains(other)

fun spawnAEC(p: Entity): AreaEffectCloud {
    val e = p.world.spawnEntity(p.location, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
    e.radiusOnUse = 0.0f
    e.radius = 0.0f
    e.duration = 100000
    return e
}

/**
 * 各プレイヤーの間に挟むダミー(豚)をスポーン
 */
fun spawnDummy(p: Entity): LivingEntity {
    val e = p.world.spawnEntity(p.location, EntityType.PIG) as Pig
    e.isInvisible = true
//    e.setSaddle(true)
    e.setAI(false)
    e.isSilent = true
    e.isInvulnerable = true
    return e
}

fun Entity.isCarrying(other: Entity) = containPassenger(other)

fun Entity.isCarried() = Bukkit.getOnlinePlayers().any { it.isCarrying(this) }
fun Entity.isCarriedBy(carrier: Entity) = carrier.isCarrying(this)
fun Entity.getOffAll(manager: VacuumEntryManager) {
    log("Entity-${this.name} is Getting Off")
    manager.entries.forEach { vacuumEntry ->
        vacuumEntry.doubleAEC?.first?.removePassenger(this)

        vacuumEntry.entities.map {
            it.second
        }.forEach {
            it.removePassenger(this)
        }

    }
}

fun Entity.forceGetOn(to: Entity, manager: VacuumEntryManager) {
    getOffAll(manager)
    to.addPassenger(this)
}