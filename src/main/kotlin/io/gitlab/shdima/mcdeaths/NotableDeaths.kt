package io.gitlab.shdima.mcdeaths

import org.bstats.bukkit.Metrics
import de.exlll.configlib.YamlConfigurations
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

@Suppress("unused")
class NotableDeaths : JavaPlugin() {

    private lateinit var config: Config

    override fun onEnable() {
        val configFile = Path.of(dataFolder.path, "config.yml")
        config = try {
            YamlConfigurations.load(configFile, Config::class.java)
        } catch (e: Exception) {
            Config()
        }

//        YamlConfigurations.save(configFile, Config::class.java, config)

        try {
            Metrics(this, 27578)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
