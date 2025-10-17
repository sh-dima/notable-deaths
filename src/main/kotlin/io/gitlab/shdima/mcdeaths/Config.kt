package io.gitlab.shdima.mcdeaths

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration
import org.bukkit.entity.EntityType

@Configuration
data class EntityConfiguration(
    @Comment("Whether death messages should be announced for all entities of this type\nOther conditions such as 'named' or 'tamed' will be ignored if this is true")
    var all: Boolean = false,
    @Comment("Whether death messages should be announced for named entities of this type")
    var named: Boolean = false,
    @Comment("Whether death messages should be announced for tamed entities of this type")
    var tamed: Boolean = false,
    @Comment("Whether the location should be broadcast along with the death message for entities of this type")
    var location: Boolean = false,
)

@Configuration
class Config {
    @Comment("The default configuration for mobs\nConfiguration under 'mobs' overrides this")
    var default = EntityConfiguration(named = true)

    @Comment("Overrides for specific entities")
    var mobs: Map<EntityType, EntityConfiguration> = mapOf(
        Pair(EntityType.VILLAGER, EntityConfiguration(all = true)),
        Pair(EntityType.WANDERING_TRADER, EntityConfiguration(all = true)),
        Pair(EntityType.WITHER, EntityConfiguration(all = true)),
        Pair(EntityType.ENDER_DRAGON, EntityConfiguration(all = true)),
    )
}
