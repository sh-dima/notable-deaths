package io.gitlab.shdima.mcdeaths

import de.exlll.configlib.Configuration
import org.bukkit.entity.EntityType

@Configuration
data class EntityConfiguration(var enabled: Boolean = false, var location: Boolean = false)

@Configuration
class Config {
    var mobs: Map<EntityType, EntityConfiguration> = mapOf(
        Pair(EntityType.VILLAGER, EntityConfiguration(enabled = true, location = false)),
        Pair(EntityType.WANDERING_TRADER, EntityConfiguration(enabled = true, location = false)),
    )
}
