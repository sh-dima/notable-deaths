Notable Deaths
==============

This is a Minecraft Paper plugin that allows server owners to customize which entities' deaths should be announced.

The plugin lets you configure which entities should have their death message broadcast across the whole server.
This can also depend on whether the entity is a pet or has a custom name.

The location of the death may also be displayed in the messages.

The plugin uses Minecraft's message system meaning all languages are supported.

Configuration
-------------

By default, the plugin will show the death messages of villagers, wandering traders, as well as any named entity. This can be edited in `plugins/NotableDeaths/config.yml`. Here is the default configuration:

```yaml
# The default configuration for mobs
# Configuration under 'mobs' overrides this
default:
  # Whether death messages should be announced for all entities of this type
  # Other conditions such as 'named' or 'tamed' will be ignored if this is true
  all: false
  # Whether death messages should be announced for named entities of this type
  named: true
  # Whether death messages should be announced for tamed entities of this type
  tamed: false
  # Whether the location should be broadcast along with the death message for entities of this type
  location: false
# Overrides for specific entities
mobs:
  VILLAGER:
    all: true
    named: false
    tamed: false
    location: false
  WANDERING_TRADER:
    all: true
    named: false
    tamed: false
    location: false
  WITHER:
    all: true
    named: false
    tamed: false
    location: false
  ENDER_DRAGON:
    all: true
    named: false
    tamed: false
    location: false
  ELDER_GUARDIAN:
    all: true
    named: false
    tamed: false
    location: false
  WARDEN:
    all: true
    named: false
    tamed: false
    location: false
  ILLUSIONER:
    all: true
    named: false
    tamed: false
    location: false
  GIANT:
    all: true
    named: false
    tamed: false
    location: false
# Discord configuration: will send messages to the channel specified by channel ID
# if channel ID and bot token are specified
channel-id: ''
bot-token: ''
```

The section under `default` applies to all entities, unless there exists an entry for that entity under `mobs`, in which case that entry takes priority.

Links
-----

* [GitLab][gitlab]
* [GitHub][github]
* [Modrinth][modrinth]
* [Hangar][hangar]
* [SpigotMC][spigot]
* [bStats][bstats]
* [E-mail][email]

License
-------

© 2025 [Дима Ш.][author]

[Notable Deaths](./) is licensed under the [AGPL 3.0](./LICENSE.txt) only.

Disclaimer
----------

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.

[author]: https://shdima.gitlab.io/ (The author of this project)
[email]: mailto:dima.o.sh@proton.me (E-mail the author of this project)

[gitlab]: https://gitlab.com/-/p/75274998 (The source code of this project on GitLab)
[github]: https://github.com/sh-dima/notable-deaths (The source code of this project on GitHub)
[modrinth]: https://modrinth.com/project/ZA82D8Xe (This project on Modrinth)
[spigot]: https://www.spigotmc.org/resources/notable-deaths-paper-only.129507/ (This project on SpigotMC)
[hangar]: https://hangar.papermc.io/EsotericEnderman/NotableDeaths (This project on Hangar)
[bstats]: https://bstats.org/plugin/bukkit/Notable%20Deaths/27578 (This project on bStats)
