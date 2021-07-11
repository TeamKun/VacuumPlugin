package net.kunmc.lab.vacuumplugin

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

var isLogOutPut = false

fun log(s: String) {
    if (isLogOutPut) {
        Bukkit.broadcastMessage(s)
        println(s)
    }
}

fun log(s: String, p: Player) {
    if (isLogOutPut) p.sendMessage(s)
}

fun error(e: String) {
    Bukkit.broadcastMessage("" + ChatColor.RED + e)
    println(e)
}