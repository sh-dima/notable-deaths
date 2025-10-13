package io.gitlab.shdima.mcdeaths

import de.exlll.configlib.NameFormatters
import de.exlll.configlib.YamlConfigurationProperties
import de.exlll.configlib.YamlConfigurations
import io.papermc.paper.event.entity.TameableDeathMessageEvent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import org.bstats.bukkit.Metrics
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import java.util.function.UnaryOperator

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
        var deathMessage = nmsEntity.combatTracker.deathMessage.copy()

        val type = entity.type
        val showLocation = config.mobs[type]?.location ?: config.default.location

        if (showLocation) {
            val location = entity.location

            val coordinates = Component.translatable("chat.coordinates", location.blockX.toString(), location.blockY.toString(), location.blockZ.toString())
            val wrapped = Component.translatable("chat.square_brackets", coordinates)

            wrapped.withStyle(
                UnaryOperator<Style> { style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(ClickEvent.SuggestCommand("/tp @s " + location.blockX + " " + location.blockY + " " + location.blockZ))
                        .withHoverEvent(HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip")))
                }
            )

            deathMessage.append(" ").append(wrapped)
        }

        server.onlinePlayers.forEach {
            val serverPlayer = (it as CraftPlayer).handle

            serverPlayer.displayClientMessage(
                deathMessage,
                false, // Whether the message is an action bar message
            )
        }
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
