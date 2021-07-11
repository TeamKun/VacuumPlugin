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

fun spawnDummy(p: Entity): LivingEntity {
    val e = p.world.spawnEntity(p.location,EntityType.PIG) as Pig
    e.isInvisible = true
    e.setSaddle(true)
    e.setAI(false)
    return e
}

fun Entity.isCarrying(other: Entity) = containPassenger(other)

fun Entity.isCarried() = Bukkit.getOnlinePlayers().any { it.isCarrying(this) }
fun Entity.isCarriedBy(carrier: Entity) = carrier.isCarrying(this)
fun Entity.getOffAll() =
    Bukkit.getOnlinePlayers().forEach { it.removePassenger(this) }

fun Entity.forceGetOn(to: Entity) {
    getOffAll()
    to.addPassenger(this)
}