package io.gitlab.shdima.mcdeaths

import de.exlll.configlib.NameFormatters
import de.exlll.configlib.YamlConfigurationProperties
import de.exlll.configlib.YamlConfigurations
import org.bstats.bukkit.Metrics
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

@Suppress("unused")
class NotableDeaths : JavaPlugin(), Listener {

    private lateinit var config: Config

    override fun onEnable() {
        val configFile = Path.of(dataFolder.path, "config.yml")
        val configProperties = YamlConfigurationProperties.newBuilder()
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .build()

        config = try {
            YamlConfigurations.load(configFile, Config::class.java, configProperties)
        } catch (e: Exception) {
            Config()
        }

        YamlConfigurations.save(configFile, Config::class.java, config, configProperties)

        try {
            Metrics(this, 27578)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        server.pluginManager.registerEvents(this, this)
    }

    private fun shouldAnnounceDeath(entity: LivingEntity): Boolean {
        val type = entity.type
        val entityConfig = config.mobs[type] ?: return false

        return entityConfig.all
    }

    @EventHandler
    private fun onDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (!shouldAnnounceDeath(entity)) return

        val nmsEntity = (entity as CraftLivingEntity).handle

        val deathMessage = nmsEntity.combatTracker.deathMessage.copy()

        server.onlinePlayers.forEach {
            val serverPlayer = (it as CraftPlayer).handle

            serverPlayer.displayClientMessage(
                deathMessage,
                false, // Whether the message is an action bar message
            )
        }
    }
}
