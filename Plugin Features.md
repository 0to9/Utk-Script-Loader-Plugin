# Plugin Features
In addition to script loading, this plugin also provides some core functionality, including a few default sub-commands for this plugin's command and a collection of starter scripts (which were all ported over from this project's precursor: the Utk Plugin Suite).

## The ```/usl``` Command
The Bukkit/Spigot API doesn't permit plugins to define commands on-the-fly. All commands must be explicitly defined within the plugin's JAR in the [```plugin.yml```](src/main/resources/plugin.yml) file prior to being loaded in. As a result, the plugin cannot support custom command generation for scripts. To overcome this problem, this plugin defines a single command ```/usl``` (and 3 aliases ```/utk```, ```/vpp```, and ```/v++``` for legacy support), for which scripts can define sub-commands.

For this application, a sub-command is defined to be the first argument passed into the main command, and all remaining arguments from the original command call are forwarded to the sub-command hander, as implemented by the script.

In addition to the sub-commands provided by scripts, the plugin defines 5 default sub-commands, which are loaded in regardless of the included scripts. These are the [```help```](#usl-help), [```list```](#usl-list), [```version```](#usl-version), [```changelog```](#usl-changelog), [```config```](#usl-config), and [```reload```](#usl-reload) sub-commands.

### ```/usl help```
The ```help``` sub-command provides a help menu for the [```/usl```](#the-usl-command) command and all of its sub-commands. Additionally, sub-command ids can be passed as a singular argument to print a script-defined help menu for that specific sub-command.

### ```/usl list```
The ```list``` sub-command lists all of the sub-commands that are available at the time of the sub-command call.

### ```/usl version```
The ```version``` sub-command prints the version of the plugin that is currently running.

### ```/usl changelog```
The ```changelog``` sub-command prints the developer defined changelog for the current plugin version. Additionally, previous version ids can be passed as a singular argument to print the changelog for the specified version.

### ```/usl config```
The ```config``` sub-command is not available to all players. This command can be used by the server console or op-ed players to modify settings in this plugin's ```config.yml``` file.

### ```/usl reload```
The ```reload``` sub-command is also not available to all players. Yet again, only the server console and op-ed players can use this command. Upon a call to this command, the plugin reloads all script implementations in use.

## Default Scripts
The plugin also provides many default scripts, corresponding to modules from the Utk Plugin Suite. These script implementations can be found in the [```scripts```](src/main/resources/scripts) resource folder. The default script modules are the following: [Easy Crafting](#easy-crafting), [Entity Protection Services](#entity-protection-services), [Instamine](#instamine), and [Scoreboard](#scoreboard).

So far, not all of the default modules have been ported over. *TODO complete module port*

### Easy Crafting
The Easy Crafting module provides powerful crafting-related upgrades. The main script file for this module is [```scripts/easy_crafting.txt```](src/main/resources/scripts/easy_crafting.txt).

### Entity Protection Services
The Entity Protection Services module provides entity protection services, as the name so cleverly implies. It prevents players from unintentionally attacking mobs. The main script file for this module is [```scripts/entity_protection_services.txt```](src/main/resources/scripts/entity_protection_services.txt).

### Instamine
The Instamine module adds instamine support for various blocks that can't be instamined but logically should be. The main script file for this module is [```scripts/instamine.txt```](src/main/resources/scripts/instamine.txt).

### Scoreboard
The Scoreboard module adds a powerful scoreboard sub-command that allows players to customize the scoreboards they can see. Additionally, it provides a custom scoreboard objective that tracks the distance players have traveled. The main script file for this module is [```scripts/scoreboard.txt```](src/main/resources/scripts/scoreboard.txt).

## Plugin Config ([```config.yml```](src/main/resources/config.yml))
This plugin's config file only contains 2 values, both of which can be modified at runtime by the [```config```](#usl-config) sub-command: ["Print Debug Output"](#print-debug-output) and ["Install Default Scripts"](#install-default-scripts).

### Print Debug Output
If this config value is ```true```, then the plugin will print a verbose error message whenever an error is logged internally. Otherwise, a brief one-line description will be provided instead. In either case, the verbose error message will be saved to a log file within the plugin data folder.

### Install Default Scripts
If this config value is ```true```, then the plugin will install the default scripts into the [```scripts```](src/main/resources/scripts) folder on its next load. After that, it will automatically set this config value to ```false```. If this config value is ```false```, the plugin does not install the default scrpts while loading.
