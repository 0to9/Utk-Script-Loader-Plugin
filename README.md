# Utk Script Loader Plugin
## About This Project
The Utk Script Loader Plugin is a custom script loading program that allows for quick and easy modifications to any minecraft server instance running [SpigotMC](https://www.spigotmc.org/wiki/about-spigot/), [PaperMC/PaperSpigot](https://paper.readthedocs.io/en/latest/), or any other modified server jar that provides or supports the Bukkit Plugin Development API.

*TODO more comprehensive support for extended APIs (such as the [Paper-API](https://papermc.io/javadocs))*

This plugin provides a feature-rich, custom scripting language that can be used to interface with the supported APIs in a simple and easily scalable manner. The scriptig interface is guaranteed to remain roughly consistent between plugin versions and to be compatible across a variety of Bukkit-extending APIs.

## The Scripting Language
The scripting language is a hybrid between C++ and Java, with additional custom elements thrown in for ease of development. For a comprehensive look at the specifics of the language and the custom-designed elements offered in by the language, check out the [```language specification```](language%20specification) folder.

## Code Loading Mechanisms
There are two main ways of loading custom scripts through this plugin: text-based script files and JARs with classes implementing various script API classes.

When a script is loaded in, its hook methods are loaded and stored into various handler methods internally. These hook methods are run whenever these event handlers are triggered. If a hook method throws an exception or causes an exception in the handler method, that hook's failure counter increments by 1. Once a hook method reaches 10 failures, the hook is immediately deactivated and cannot be re-triggered until the script implementation is refreshed, either by the [```/usl reload```](#usl-reload) sub-command or by a plugin or server restart.

### Text-Based Script Files
The primary method for loading in custom scripts is via text files. The scripts, which must meet the specifications as outlined in the language specifications, are recursively loaded based on the single script file [```scripts/main.txt```](src/main/resources/scripts/main.txt) found in the plugin data folder. Plugin and script users can modify this main inclusion file as well as other included files even during runtime, and changes can be subsequently loaded with the [```/usl reload```](#usl-reload) sub-command.

### JAR-Based Implementations
Though easier to work with due to proper syntax highlighting and code completion tools, JAR-based implementations lose the ease of modification associated with the text file scripts.

JAR loading is currently not implemented, but is planned as a feature. *TODO JAR loading for scripts*

## Plugin Features
In addition to script loading, this plugin also provides some core functionality, including a few default sub-commands for this plugin's command and a collection of starter scripts (which were all ported over from this project's precursor: the Utk Plugin Suite).

### The ```/usl``` Command
The Bukkit/Spigot API doesn't permit plugins to define commands on-the-fly. All commands must be explicitly defined within the plugin's JAR in the [```plugin.yml```](src/main/resources/plugin.yml) file prior to being loaded in. As a result, the plugin cannot support custom command generation for scripts. To overcome this problem, this plugin defines a single command ```/usl``` (and 3 aliases ```/utk```, ```/vpp```, and ```/v++``` for legacy support), for which scripts can define sub-commands.

For this application, a sub-command is defined to be the first argument passed into the main command, and all remaining arguments from the original command call are forwarded to the sub-command hander, as implemented by the script.

In addition to the sub-commands provided by scripts, the plugin defines 5 default sub-commands, which are loaded in regardless of the included scripts. These are [```/usl help```](#usl-help), [```/usl list```](#usl-list), [```/usl version```](#usl-version), [```/usl changelog```](#usl-changelog), [```/usl config```](#usl-config), and [```/usl reload```](#usl-reload).

#### ```/usl help```
The ```/usl help``` sub-command provides a help menu for [the ```/usl``` command](#the-usl-command) and all of its sub-commands. Additionally, sub-command ids can be passed as a singular argument to print a script-defined help menu for that specific sub-command.

#### ```/usl list```
The ```/usl list``` sub-command lists all of the sub-commands that are available at the time of the sub-command call.

#### ```/usl version```
The ```/usl version``` sub-command prints the version of the plugin that is currently running.

#### ```/usl changelog```
The ```/usl changelog``` sub-command prints the developer defined changelog for the current plugin version. Additionally, previous version ids can be passed as a singular argument to print the changelog for the specified version.

#### ```/usl config```
The ```/usl config``` sub-command is not available to all players. This command can be used by the server console or op-ed players to modify settings in this plugin's ```config.yml``` file.

#### ```/usl reload```
The ```/usl reload``` sub-command is also not available to all players. Yet again, only the server console and op-ed players can use this command. Upon a call to this command, the plugin reloads all script implementations in use.

### Default Scripts
The plugin also provides many default scripts, corresponding to modules from the Utk Plugin Suite. These script implementations can be found in the [```scripts```](src/main/resources/scripts) resource folder. The default script modules are the following: [Easy Crafting](#easy-crafting), [Entity Protection Services](#entity-protection-services), [Instamine](#instamine), and [Scoreboard](#scoreboard).

So far, not all of the default modules have been ported over. *TODO complete module port*

#### Easy Crafting
The Easy Crafting module provides powerful crafting-related upgrades. The main script file for this module is [```scripts/easy_crafting.txt```](src/main/resources/scripts/easy_crafting.txt).

#### Entity Protection Services
The Entity Protection Services module provides entity protection services, as the name so cleverly implies. It prevents players from unintentionally attacking mobs. The main script file for this module is [```scripts/entity_protection_services.txt```](src/main/resources/scripts/entity_protection_services.txt).

#### Instamine
The Instamine module adds instamine support for various blocks that can't be instamined but logically should be. The main script file for this module is [```scripts/instamine.txt```](src/main/resources/scripts/instamine.txt).

#### Scoreboard
The Scoreboard module adds a powerful scoreboard sub-command that allows players to customize the scoreboards they can see. Additionally, it provides a custom scoreboard objective that tracks the distance players have traveled. The main script file for this module is [```scripts/scoreboard.txt```](src/main/resources/scripts/scoreboard.txt).

### Plugin Config ([```config.yml```](src/main/resources/config.yml))
This plugin's config file only contains 2 values, both of which can be modified at runtime by the [```/usl config```](#usl-config) sub-command: ["Print Debug Output"](#print-debug-output) and ["Install Default Scripts"](#install-default-scripts).

#### Print Debug Output
If this config value is ```true```, then the plugin will print a verbose error message whenever an error is logged internally. Otherwise, a brief one-line description will be provided instead. In either case, the verbose error message will be saved to a log file within the plugin data folder.

#### Install Default Scripts
If this config value is ```true```, then the plugin will install the default scripts into the [```scripts```](src/main/resources/scripts) folder on its next load. After that, it will automatically set this config value to ```false```. If this config value is ```false```, the plugin does not install the default scrpts while loading.
