package net.kunmc.lab.vacuumplugin

import com.github.bun133.flylib2.commands.Commander
import com.github.bun133.flylib2.commands.CommanderBuilder
import com.github.bun133.flylib2.commands.TabChain
import com.github.bun133.flylib2.commands.TabObject
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

fun VacuumCommand(plugin: VacuumPlugin) = Commander(
    plugin,
    "Main Command of VacuumPlugin",
    "/va start|end",
    CommanderBuilder<VacuumPlugin>()
        // startコマンド
        .addFilter(CommanderBuilder.Filters.OP())
        .addTabChain(TabChain(TabObject("start")))
        .setInvoker { _, _, _ ->
            if (plugin.VacuumEntryManager != null) {
                plugin.VacuumEntryManager!!.reset()
            } else {
                plugin.VacuumEntryManager = VacuumEntryManager(plugin)
            }
            plugin.VacuumEntryManager!!.isGoingOn = true // 一応
            @Suppress("DEPRECATION")
            Bukkit.broadcastMessage("吸収開始!")
            return@setInvoker true
        },
    CommanderBuilder<VacuumPlugin>()
        .addFilter(CommanderBuilder.Filters.OP())
        .addTabChain(TabChain(TabObject("end")))
        .setInvoker { _, _, _ ->
            plugin.VacuumEntryManager?.isGoingOn = false
            @Suppress("DEPRECATION")
            Bukkit.broadcastMessage("吸収終了!")
            return@setInvoker true
        },
    CommanderBuilder<VacuumPlugin>()
        .addFilter(CommanderBuilder.Filters.OP())
        .addTabChain(TabChain(TabObject("debug"), TabObject("true", "false")))
        .setInvoker { _, _, strings ->
            isLogOutPut = strings[1].toBooleanStrict()
            return@setInvoker true
        },
    CommanderBuilder<VacuumPlugin>()
        .addFilter(CommanderBuilder.Filters.OP())
        .addTabChain(TabChain(TabObject("test"), TabObject("List")))
        .setInvoker { _, sender, strings ->
            when (strings[1]) {
                "List" -> {
                    val manager = plugin.VacuumEntryManager
                    if (manager == null) {
                        sender.sendMessage("Manager is Null")
                        return@setInvoker true
                    }
                    sender.sendMessage("Listing All Entry")
                    manager.entries.forEachIndexed { index, vacuumEntry ->
                        sender.sendMessage("VacuumEntry[$index]:")
                        sender.sendVacuumEntry(vacuumEntry.e)
                        vacuumEntry.entities.forEachIndexed { i, pair ->
                            sender.sendMessage("VacuumEntities[$i]:")
                            sender.sendVacuumEntry(pair.first)
                        }
                    }
                }
            }

            return@setInvoker true
        }
)

fun CommandSender.sendVacuumEntry(e: VacuumEntity) {
    val ee = e.getEntity()
    if (ee == null) {
        sendMessage("E.name:Null")
    } else {
        sendMessage("E.name:${ee.name}")
    }
    sendMessage("E.isDead:${e.isDead()}")
}