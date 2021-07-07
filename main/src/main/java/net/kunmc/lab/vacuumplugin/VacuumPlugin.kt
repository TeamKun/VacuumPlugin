package net.kunmc.lab.vacuumplugin

import com.github.bun133.flylib2.commands.Commander
import com.github.bun133.flylib2.commands.CommanderBuilder
import com.github.bun133.flylib2.commands.TabChain
import com.github.bun133.flylib2.commands.TabObject
import com.github.bun133.flylib2.utils.ComponentUtils
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team

class VacuumPlugin : JavaPlugin() {
    var VacuumEntryManager: VacuumEntryManager? = null

    override fun onEnable() {

        // Plugin startup logic
        val command = Commander(
            this,
            "Main Command of VacuumPlugin",
            "/va start|end",
            CommanderBuilder<VacuumPlugin>()
                // startコマンド
                .addFilter(CommanderBuilder.Filters.OP<VacuumPlugin>())
                .addTabChain(TabChain(TabObject("start")))
                .setInvoker { vacuumPlugin, commandSender, strings ->
                    if (VacuumEntryManager != null) {
                        VacuumEntryManager!!.reset()
                    } else {
                        VacuumEntryManager = VacuumEntryManager(this)
                    }
                    VacuumEntryManager!!.isGoingOn = true // 一応
                    Bukkit.broadcastMessage("吸収開始!")
                    return@setInvoker true
                },
            CommanderBuilder<VacuumPlugin>()
                .addFilter(CommanderBuilder.Filters.OP<VacuumPlugin>())
                .addTabChain(TabChain(TabObject("end")))
                .setInvoker { vacuumPlugin, commandSender, strings ->
                    VacuumEntryManager?.isGoingOn = false
                    Bukkit.broadcastMessage("吸収終了!")
                    return@setInvoker true
                }
        )

        command.register("va")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        VacuumEntryManager?.reset()
    }
}