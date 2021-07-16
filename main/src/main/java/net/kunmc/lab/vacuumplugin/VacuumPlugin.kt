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
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team

class VacuumPlugin : JavaPlugin(){
    var VacuumEntryManager: VacuumEntryManager? = null

    override fun onEnable() {
        // Plugin startup logic
        val command = VacuumCommand(this)
        command.register("va")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        VacuumEntryManager?.reset()
    }
}