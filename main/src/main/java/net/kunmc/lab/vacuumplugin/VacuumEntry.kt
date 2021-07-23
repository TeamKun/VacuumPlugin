package net.kunmc.lab.vacuumplugin

import com.github.bun133.flylib2.utils.ComponentUtils
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import kotlin.math.min

// 一番下のPlayerが代表してregister
class VacuumEntry(val e: VacuumEntity, val manager: VacuumEntryManager) {
    companion object {
        // 初めの人からどれぐらい離すか
        const val firstOffset = 2.0

        // それ以降どれぐらい離すか
        const val Offset = 2.0
    }

    /**
     * LivingEntity -> 豚
     */
    val entities = mutableListOf<Pair<VacuumEntity, LivingEntity>>()
    var doubleAEC: DoubleAEC? = null

    // 上にTP
    fun teleport() {
        entities.removeAll {
            val b = it.first.isDead()
            if (b) {
                log("${it.first.getEntity()?.name} is Removed")
            }

            return@removeAll b
        }
        val ee = e.getEntity()
        if (ee != null) {

            if (doubleAEC == null) {
                // DoubleAEC Init
                doubleAEC = DoubleAEC(ee, manager)
            }

            if (!doubleAEC!!.first.isCarriedBy(ee)) {
                // AECだけおいていかれない対策
                doubleAEC!!.first.forceGetOn(ee, manager)
            }

            val nl = entities
                .mapNotNull {
                    val e = it.first.getEntity()
                    if (e == null) null
                    else {
                        val second = it.second

                        Pair(e, second)
                    }
                }

            nl.forEachIndexed { index, livingEntity ->
                if (livingEntity is Player) {
                    log("Teleporting:" + ComponentUtils.toText(livingEntity.displayName()))
                }

                when (index) {
                    0 -> {

                        if (!livingEntity.second.isCarriedBy(doubleAEC!!.first)) {
                            // 豚、AECに乗る
                            error("Force Getting On")
                            livingEntity.second.forceGetOn(doubleAEC!!.first, manager)
                        }

                        if (!livingEntity.first.isCarriedBy(livingEntity.second)) {
                            // 人、透明な豚に乗る
                            error("Force Getting On")
                            livingEntity.first.forceGetOn(livingEntity.second, manager)
                        }
                    }

                    else -> {
                        val beforeE = nl[index - 1]
                        if (!livingEntity.second.isCarriedBy(beforeE.first)) {
                            // 豚、下の人に乗る
                            error("Force Getting On")
                            livingEntity.second.forceGetOn(beforeE.first, manager)
                        }

                        if (!livingEntity.first.isCarriedBy(livingEntity.second)) {
                            // 人、透明な豚に乗る
                            error("Force Getting On")
                            livingEntity.first.forceGetOn(livingEntity.second, manager)
                        }
                    }
                }
            }
        }
    }

    fun carry(ee: LivingEntity) {
        entities.add(Pair(VacuumEntity(ee), spawnDummy(ee)))
    }

    fun unCarry(ee: LivingEntity) {
        entities.removeAll { it.first.getEntity()?.uniqueId == ee.uniqueId }
    }

    fun unCarryAll() {
        val es = mutableListOf<Pair<VacuumEntity, LivingEntity>>()
        es.addAll(entities)
        entities.clear()
        es.forEachIndexed { index, pair ->
            val e = pair.first.getEntity()
            if (e != null) {
                log("Index:${index},Name:${pair.first.getEntity()?.name} is unCarryAll")
                if(!pair.second.removePassenger(e)) log("Error:in unCarryAll")
            }
            pair.second.health = 0.0
        }
    }

    fun isSingle() = entities.isEmpty()
}