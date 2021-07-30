package net.kunmc.lab.vacuumplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun a(p: Player, material: Material, inventory: Inventory) {
    for (i in 1..p.inventory.size) {
        val stack = p.inventory.getItem(i)
        if (stack != null) {
            p.inventory.setItem(i, ItemStack(material, stack.amount))
        }
    }

    for (i in 1..inventory.size) {
        val stack = inventory.getItem(i)
        if (stack != null) {
            inventory.setItem(i, ItemStack(material, stack.amount))
        }
    }
}

class Yps {
    val queue = mutableListOf<Pair<Unit, Int>>()

    fun tick() {
        queue.forEach {
            it.second
        }
    }

    /**
     * @author bun133
     */
    fun translate(p: Player, material: Material): Component {
        val e = Component.translatable(material.translationKey)
        val translator = GlobalTranslator.renderer()
        return translator.render(e, p.locale())
    }

    fun a() {
        Bukkit.getOnlinePlayers().forEach {
            val comp = translate(it, Material.DIRT)
            it.sendMessage(
                Component.text("Before").append(comp).append(Component.text("After"))
                    .style(Style.style(NamedTextColor.GOLD))
            )
        }
    }
}

fun Inventory.replaceAll(material: Material) {
    this.forEach {
        it.type = material
    }
}