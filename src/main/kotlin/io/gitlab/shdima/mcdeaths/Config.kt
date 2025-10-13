package io.gitlab.shdima.mcdeaths

import de.exlll.configlib.Configuration
import org.bukkit.entity.EntityType

@Configuration
data class EntityConfiguration(var all: Boolean = false, var named: Boolean = false, var tamed: Boolean = false, var location: Boolean = false)

@Configuration
class Config {
    var default = EntityConfiguration(
        all = false,
        named = true,
        tamed = false,
        location = false,
    )

    var mobs: Map<EntityType, EntityConfiguration> = mapOf(
        Pair(EntityType.VILLAGER, EntityConfiguration(all = true, location = false)),
        Pair(EntityType.WANDERING_TRADER, EntityConfiguration(all = true, location = false)),
    )
}
