package jp.simplespace.simplehardcore

import jp.simplespace.simplehardcore.listener.HardcoreListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.codehaus.plexus.util.FileUtils
import java.io.File
import java.io.IOException

class SimpleHardcore : JavaPlugin() {
    companion object {
        var plugin: SimpleHardcore? = null
        var reset = false
        var config: FileConfiguration? = null
        var sb: Scoreboard? = null
        var scheduler: TimeScheduler? = null
        var objective: Objective? = null
        var hpobj: Objective? = null
        var taskId = 0
    }
    override fun onEnable() {
        // Plugin startup logic
        plugin = this
        server.pluginManager.registerEvents(HardcoreListener(), this)
        reset = false
        setAutoSave(true)
        sb = server.scoreboardManager.mainScoreboard
        if (sb!!.getObjective("DeathCount") === null) {
            objective = sb!!.registerNewObjective("DeathCount", Criteria.DUMMY, Component.text("死亡回数"))
            objective!!.displaySlot = DisplaySlot.SIDEBAR
        } else objective = sb!!.getObjective("DeathCount")
        if (sb!!.getObjective("HP") === null) {
            hpobj = sb!!.registerNewObjective("HP", Criteria.HEALTH, MiniMessage.miniMessage().deserialize("<red>HP"))
            hpobj!!.displaySlot = DisplaySlot.BELOW_NAME
            hpobj!!.displaySlot = DisplaySlot.PLAYER_LIST
        } else hpobj = sb!!.getObjective("HP")
        saveDefaultConfig()
        Companion.config = config
        scheduler = TimeScheduler()
        taskId = plugin!!.getServer().scheduler.scheduleSyncRepeatingTask(plugin!!, scheduler!!, 0L, 20L)
        if (server.getWorld("world") == null) {
            logger.warning("このプラグインを正常に機能させるためにワールドの名前は「world」にしてください。")
        }
    }

    override fun onDisable() {
        plugin!!.saveConfig()
        objective!!.unregister()
        hpobj!!.unregister()
        logger.info(
            Companion.config!!.getInt("worldcount", 1).toString() + "代目ワールドの経過時間: " + scheduler?.let {
                it.timer(
                    it.count)
            }
        )
        if (Bukkit.getWorld("world") != null) {
            val container = Bukkit.getWorld("world")!!.persistentDataContainer
            scheduler?.count?.let { container.set(NamespacedKey(plugin!!, "count"), PersistentDataType.INTEGER, it) }
        }
        server.scheduler.cancelTask(taskId)
    }

    fun setAutoSave(boo: Boolean) {
        Bukkit.getWorld("world")!!.isAutoSave = boo
        Bukkit.getWorld("world_nether")!!.isAutoSave = boo
        Bukkit.getWorld("world_the_end")!!.isAutoSave = boo
    }

    fun renameWorld(name: String) {
        // Delete world
        Bukkit.unloadWorld(Bukkit.getWorld(name)!!, false)
        try {
            val oldFile = File(name + "_old")
            if (oldFile.exists()) {
                FileUtils.deleteDirectory(oldFile)
            }
            FileUtils.rename(File(name), File(name + "_old"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
