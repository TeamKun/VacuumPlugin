package net.kunmc.lab.vacuumplugin

import com.github.bun133.flylib2.utils.ComponentUtils
import org.bukkit.Bukkit
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

class ScoreBoardManager(val vacuum: VacuumPlugin, val manager: VacuumEntryManager) {
    var scoreboard: Scoreboard? = null
    var objective: Objective? = null

    fun onUpdate() {
        if (scoreboard == null) {
            scoreboard = vacuum.server.scoreboardManager.mainScoreboard
            objective = scoreboard!!.getObjective("score")
            if (objective == null) {
                objective = scoreboard!!.registerNewObjective("score", "dummy", ComponentUtils.fromText("score"))
            }
            objective!!.displaySlot = DisplaySlot.SIDEBAR
        }

        Bukkit.getOnlinePlayers()
            .map { player ->
                val l = manager.getOfBottom(player)
                if(l == null){
                    Pair(player,0)
                }else {
                    Pair(player,l.entities.size)
                }
            }
            .forEach {
                objective!!.getScore(it.first.name).score = it.second
            }
    }
}