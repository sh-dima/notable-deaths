package io.gitlab.shdima.mcdeaths

import de.exlll.configlib.NameFormatters
import de.exlll.configlib.YamlConfigurationProperties
import de.exlll.configlib.YamlConfigurations
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.event.entity.TameableDeathMessageEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bstats.bukkit.Metrics
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.nio.file.Path
import java.util.UUID

@Suppress("unused")
class NotableDeaths : JavaPlugin(), Listener {

    private lateinit var mm: MiniMessage
    private lateinit var config: Config

    private var jda: JDA? = null

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

            if (!config.channelId.isEmpty() && !config.botToken.isEmpty()) {
                jda = JDABuilder.createDefault(config.botToken).build().awaitReady()
                logger.info("Successfully loaded JDA")
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        jda?.shutdownNow()
    }

    private fun getDeathWitnesses(entity: LivingEntity): List<UUID> {
        val everyone = server.onlinePlayers.map { it.uniqueId }

        val type = entity.type
        val entityConfig = config.mobs[type] ?: config.default

        val announceAll = entityConfig.all
        if (announceAll) return everyone

        val announceNamed = entityConfig.named
        if (announceNamed && entity.customName() != null) return everyone

        val announceTamed = entityConfig.tamed
        if (announceTamed && entity is Tameable && entity.owner != null) {
            return if (config.broadcastTamedDeaths) everyone else listOf(entity.owner!!.uniqueId)
        }

        return listOf()
    }

    private fun announceDeath(entity: LivingEntity, toAnnounceTo: List<UUID>) {
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

        toAnnounceTo.forEach {
            val player = server.getPlayer(it)
            player?.sendMessage(deathMessage)
        }

        server.consoleSender.sendMessage(deathMessage)

        if (jda == null) {
            if (!config.channelId.isEmpty() || !config.botToken.isEmpty()) {
                logger.warning("Invalid JDA configuration! Unable to send death message")
            }

            return
        }

        val textChannel = jda!!.getChannelById<TextChannel>(TextChannel::class.java, config.channelId)

        if (textChannel == null) {
            logger.warning("Couldn't find text channel with ID ${config.channelId}")
            return
        }

        val embed = EmbedBuilder()
            .setAuthor(PlainTextComponentSerializer.plainText().serialize(deathMessage))
            .setColor(Color.getHSBColor(1.0F, 1.0F, 0.001F)) // Force color to be exactly black, same as DiscordSRV
            .build()

        val action = textChannel.sendMessageEmbeds(embed)
        action.submit()
    }

    @EventHandler
    private fun onTamedDeath(event: TameableDeathMessageEvent) {
        val entity = event.entity

        val witnesses = getDeathWitnesses(entity)
        if (witnesses.isNotEmpty()) event.isCancelled = true
    }

    @EventHandler
    private fun onDeath(event: EntityDeathEvent) {
        val entity = event.entity

        val witnesses = getDeathWitnesses(entity)
        announceDeath(entity, witnesses)
    }
}
