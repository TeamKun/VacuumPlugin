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
class VacuumEntry(val e: VacuumEntity) {
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
                doubleAEC = DoubleAEC(ee)
            }

            if (!doubleAEC!!.first.isCarriedBy(ee)) {
                // AECだけおいていかれない対策
                doubleAEC!!.first.forceGetOn(ee)
            }

            val nl = entities
                .mapNotNull {
                    val e = it.first.getEntity()
                    if (e == null) null
                    else Pair(e, it.second)
                }

            nl.forEachIndexed { index, livingEntity ->
                if (livingEntity is Player) {
                    log("Teleporting:" + ComponentUtils.toText(livingEntity.displayName()))
                }

                when (index) {
                    0 -> {
                        if (!livingEntity.second.isCarriedBy(doubleAEC!!.second)) {
                            // 豚、AECに乗る
                            error("Force Getting On")
                            livingEntity.second.forceGetOn(doubleAEC!!.second)
                        }

                        if (!livingEntity.first.isCarriedBy(livingEntity.second)) {
                            // 人、透明な豚に乗る
                            error("Force Getting On")
                            livingEntity.first.forceGetOn(livingEntity.second)
                        }
                    }

                    else -> {
                        val beforeE = nl[index - 1]
                        if (!livingEntity.second.isCarriedBy(beforeE.first)) {
                            // 豚、下の人に乗る
                            error("Force Getting On")
                            livingEntity.second.forceGetOn(beforeE.first)
                        }

                        if (!livingEntity.first.isCarriedBy(livingEntity.second)) {
                            // 人、透明な豚に乗る
                            error("Force Getting On")
                            livingEntity.first.forceGetOn(livingEntity.second)
                        }
                    }
                }
            }
            log("Size:${nl.count()}")
        }
    }

    fun carry(ee: LivingEntity) {
        entities.add(Pair(VacuumEntity(ee), spawnDummy(ee)))
    }

    fun unCarry(ee: LivingEntity) {
        entities.removeAll { it.first.getEntity()?.uniqueId == ee.uniqueId }
    }

    fun unCarryAll() {
        entities.forEach {
            it.first.getEntity()?.getOffAll()
            it.second.health = 0.0
        }
        entities.clear()
    }

    fun isSingle() = entities.isEmpty()
}