package net.kunmc.lab.vacuumplugin

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import kotlin.math.min

class VacuumEntryManager(val plugin: VacuumPlugin) : Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.server.scheduler.runTaskTimer(plugin, Runnable { update() }, 1, 1)
    }

    val entries = mutableListOf<VacuumEntry>()
    var isGoingOn = false
    var ScoreBoardManager = ScoreBoardManager(plugin, this)


    @EventHandler
    fun onDamage(e: EntityDamageByEntityEvent) {
        if (e.entity is LivingEntity) {
            val entry = get(e.entity as LivingEntity)
            if (entry != null) {
                if (entry.entities.any { it.first.getEntity()?.uniqueId == e.damager.uniqueId }) {
                    // 乗っけてる人からのダメージ
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        if (entries.any { vacuumEntry -> vacuumEntry.entities.any { it.second == e.entity } }) {
            // 豚ちゃん保護
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        if (!isGoingOn) return
        val entry = get(e.entity) // 死んだほう
        if (e.entity.killer != null) {
            val killer = getOrRegister(e.entity.killer!!) // Killerは登録
            if (entry == null) {
                // 担いでいなく、なおかつregisterされていない
                // →　ただただ担ぐ
                log("ただただ担ぐ")
                // キラー回復
                val ee = killer.e.getEntity()
                if (ee != null) {
                    ee.health =
                        min(
                            ee.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value, ee.health + 2.0
                        )
                }

                killer.carry(e.entity)
                e.isCancelled = true
            } else {
                // registerされてる
                if (isCarried(e.entity)) {
                    // 担がれているかわいそうな人
                    log("担がれているかわいそうな人")
                    e.isCancelled = true
                } else {
                    // 担がれていない自由人
                    if (entry.isSingle()) {
                        // 誰も担いでいない
                        // ①
                        log("誰も担いでいない")
                        killer.carry(e.entity)
                        // キラー回復
                        val ee = killer.e.getEntity()
                        if (ee != null) {
                            ee.health =
                                min(
                                    ee.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value, ee.health + 2.0
                                )
                        }
                        entries.remove(entry)
                        e.isCancelled = true
                    } else {
                        // 誰か担いでいる
                        // ②
                        log("誰か担いでいる")
                        entry.entities
                            .mapNotNull { it.first.getEntity() }
                            .forEach {
                                // 担がれてる人全員回復
                                it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
//                                it.getOffAll(this)
                          }
                        entry.unCarryAll()
                        // 1tick後に体力を50%に
                        val l = entry.e.getEntity()
                        if (l != null) limited.add(l)
                        e.isCancelled = true
                    }
                }
            }
        } else {
            // 自滅
            // でもなにもしなくていいかも
            log("自滅")
        }

    }

    @EventHandler
    fun onVehicleExit(e: VehicleExitEvent) {
        if (isGoingOn) {
            if (isCarried(e.exited)) {
                log("onVehicleExit")
                e.vehicle.addPassenger(e.exited)
                e.isCancelled = true
            } else {
//                error("isCarried:False")
            }
        }
    }


    // 復活時に体力50%にしなきゃいけない人
    val limited = mutableListOf<LivingEntity>()

    fun get(e: LivingEntity): VacuumEntry? {
        val l =
            entries.filter { entry ->
                val ee = entry.e.getEntity()
                if (ee != null) {
                    entry.entities.any { it.first.getEntity()?.uniqueId == e.uniqueId } || ee == e
                } else {
                    entry.entities.any { it.first.getEntity()?.uniqueId == e.uniqueId }
                }
            }
        return if (l.isNotEmpty()) l[0]
        else null
    }

    fun getOfBottom(e: LivingEntity): VacuumEntry? {
        val l =
            entries.filter { entry ->
                val ee = entry.e.getEntity()
                if (ee != null) {
                    ee == e
                } else {
                    false
                }
            }
        return if (l.isNotEmpty()) l[0]
        else null
    }

    fun getOrRegister(e: LivingEntity): VacuumEntry {
        val g = get(e)
        if (g != null) return g
        log("Register!:$e")
        val gg = VacuumEntry(VacuumEntity(e),this)
        entries.add(gg)
        return gg
    }

    fun isCarried(e: LivingEntity): Boolean =
        entries.any { entry -> entry.entities.any { it.first.getEntity()?.uniqueId == e.uniqueId } }

    // 全員の体力調整
    fun updateAbility() {
        entries.map { Pair(it.e, it.entities.count()) }.forEach { pair ->
            val ee = pair.first.getEntity()
            if (ee != null) {
                val attr = ee.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!
                attr.modifiers.filter { it.name == "vacuum_health" }.forEach { attr.removeModifier(it) }
                attr.addModifier(generateHealthMod(pair.second))
            }
        }
    }

    companion object {
        const val health = 1.0

        // どれぐらい体力増やそうかなというあれ
        // 増やす分を返すよ!!!!!
        fun getHealth(count: Int): Double {
            return count * health
        }

        fun generateHealthMod(count: Int): AttributeModifier {
            return AttributeModifier("vacuum_health", getHealth(count), AttributeModifier.Operation.ADD_NUMBER)
        }
    }


    fun update() {
        if (!isGoingOn) return
        entries.forEach { it.teleport() }
        updateAbility()

        limited.forEach {
            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value / 2.0
        }

        limited.clear()

        ScoreBoardManager.onUpdate()
    }

    fun reset() {
        entries.forEach { it.unCarryAll() }
        entries.clear()
    }
}