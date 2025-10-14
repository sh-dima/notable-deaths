package io.gitlab.shdima.mcdeaths

import de.exlll.configlib.NameFormatters
import de.exlll.configlib.YamlConfigurationProperties
import de.exlll.configlib.YamlConfigurations
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.event.entity.TameableDeathMessageEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bstats.bukkit.Metrics
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

@Suppress("unused")
class NotableDeaths : JavaPlugin(), Listener {

    private lateinit var mm: MiniMessage
    private lateinit var config: Config

    override fun onEnable() {
        mm = MiniMessage.miniMessage()

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
        val entityConfig = config.mobs[type] ?: config.default

        val announceAll = entityConfig.all
        if (announceAll) return true

        val announceNamed = entityConfig.named
        if (announceNamed && entity.customName() != null) return true

        val announceTamed = entityConfig.tamed
        if (announceTamed && entity is Tameable && entity.owner != null) return true

        return false
    }

    private fun announceDeath(entity: LivingEntity) {
        val nmsEntity = (entity as CraftLivingEntity).handle
        var deathMessage = PaperAdventure.asAdventure(nmsEntity.combatTracker.deathMessage.copy())

        val type = entity.type
        val showLocation = config.mobs[type]?.location ?: config.default.location

        if (showLocation) {
            val location = entity.location

            val coordinates = Component.translatable(
                "chat.coordinates",
                Component.text(location.blockX),
                Component.text(location.blockY),
                Component.text(location.blockZ),
            )

            val clickToTeleport = Component.translatable("chat.coordinates.tooltip")
            val wrapped = Component.translatable("chat.square_brackets", coordinates)

            deathMessage = mm.deserialize("<green><click:suggest_command:'/tp @s ${location.blockX} ${location.blockY} ${location.blockZ}'><hover:show_text:'${mm.serialize(clickToTeleport)}'>${mm.serialize(wrapped)}</hover></click></green> ${mm.serialize(deathMessage)}")
        }

        server.onlinePlayers.forEach {
            it.sendMessage(deathMessage)
        }

        server.consoleSender.sendMessage(deathMessage)
    }

    @EventHandler
    private fun onTamedDeath(event: TameableDeathMessageEvent) {
        val entity = event.entity

        if (shouldAnnounceDeath(entity)) event.isCancelled = true
    }

    @EventHandler
    private fun onDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (shouldAnnounceDeath(entity)) announceDeath(entity)
    }
}

fun Component.stripStyles(): Component {
    // Create a new component with empty style, and recursively process children
    return this.style(Style.empty()).children(
        this.children().map { it.stripStyles() }
    )
}
