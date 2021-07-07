package net.kunmc.lab.vacuumplugin

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerTeleportEvent

// 一番下のPlayerが代表してregister
class VacuumEntry(val e: LivingEntity) {
    companion object {
        // 初めの人からどれぐらい離すか
        const val firstOffset = 2.0

        // それ以降どれぐらい離すか
        const val Offset = 2.0
    }

    val entities = mutableListOf<LivingEntity>()

    // 上にTP
    fun teleport() {
        entities.removeAll { it.isDead || !it.isValid }
        entities.forEachIndexed { index, livingEntity ->
            livingEntity.location.set(e.location.x, e.location.y + firstOffset + Offset * index, e.location.z)
            val loc = e.location.add(.0, firstOffset + Offset * index, .0)
            loc.yaw = livingEntity.location.yaw
            loc.pitch = livingEntity.location.pitch
            livingEntity.teleport(
                loc,
                PlayerTeleportEvent.TeleportCause.PLUGIN
            )
        }
    }

    fun carry(ee: LivingEntity) {
        entities.add(ee)
    }

    fun unCarry(ee: LivingEntity) {
        entities.remove(ee)
    }

    fun unCarryAll() {
        entities.clear()
    }

    fun isSingle() = entities.isEmpty()
}

class VacuumEntryManager(val plugin: VacuumPlugin) : Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.server.scheduler.runTaskTimer(plugin, Runnable { update() }, 1, 1)
    }

    val entries = mutableListOf<VacuumEntry>()
    var isGoingOn = false

    @EventHandler
    fun onDamage(e:EntityDamageByEntityEvent){
        if(e.entity is LivingEntity){
            val entry = get(e.entity as LivingEntity)
            if(entry != null){
                if(entry.entities.contains(e.damager)){
                    // 乗っけてる人からのダメージ
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onDeath(e: EntityDeathEvent) {
        if (!isGoingOn) return
        val entry = get(e.entity) // 死んだほう
        if (e.entity.killer != null) {
            val killer = getOrRegister(e.entity.killer!!) // Killerは登録
            if (entry == null) {
                // 担いでいなく、なおかつregisterされていない
                // →　ただただ担ぐ
                println("ただただ担ぐ")
                killer.carry(e.entity)
                e.isCancelled = true
            } else {
                // registerされてる
                if (isCarried(e.entity)) {
                    // 担がれているかわいそうな人
                    println("担がれているかわいそうな人")
                    e.isCancelled = true
                } else {
                    // 担がれていない自由人
                    if (entry.isSingle()) {
                        // 誰も担いでいない
                        // ①
                        println("誰も担いでいない")
                        killer.carry(e.entity)
                        entries.remove(entry)
                        e.isCancelled = true
                    } else {
                        // 誰か担いでいる
                        println("誰か担いでいる")
                        entry.unCarryAll()
                        e.isCancelled = true
                    }
                }
            }
        } else {
            // 自滅
            // でもなにもしなくていいかも
            println("自滅")
        }

        println("Size:${entries.count()}")
    }

    fun get(e: LivingEntity): VacuumEntry? {
        val l = entries.filter { it.entities.contains(e) || it.e == e }
        return if (l.isNotEmpty()) l[0]
        else null
    }

    fun getOrRegister(e: LivingEntity): VacuumEntry {
        val g = get(e)
        if (g != null) return g
        println("Register!:$e")
        val gg = VacuumEntry(e)
        entries.add(gg)
        return gg
    }

    fun isCarried(e: LivingEntity): Boolean = entries.any { it.entities.contains(e) }

    // 全員の体力調整
    fun updateAbility() {
        entries.map { Pair(it.e, it.entities.count()) }.forEach { pair ->
            val attr = pair.first.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!
            attr.modifiers.filter { it.name == "vacuum_health" }.forEach { attr.removeModifier(it) }
            attr.addModifier(generateHealthMod(pair.second))
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
    }

    fun reset() {
        entries.clear()
    }
}