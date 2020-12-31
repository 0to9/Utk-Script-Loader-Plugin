# Utk Script Loader Plugin
## About This Project
The Utk Script Loader Plugin is a custom script loading program that allows for quick and easy modifications to any minecraft server instance running [SpigotMC](https://www.spigotmc.org/wiki/about-spigot/), [PaperMC/PaperSpigot](https://paper.readthedocs.io/en/latest/), or any other modified server jar that provides or supports the Bukkit Plugin Development API.

*TODO more comprehensive support for extended APIs (such as the [Paper-API](https://papermc.io/javadocs))*

This plugin provides a feature-rich, custom scripting language that can be used to interface with the supported APIs in a simple and easily scalable manner. The scriptig interface is guaranteed to remain roughly consistent between plugin versions and to be compatible across a variety of Bukkit-extending APIs.

## The Scripting Language
The scripting language is a hybrid between [C++](http://www.cplusplus.com/) and [Java](https://www.oracle.com/java/), with additional custom elements thrown in for ease of development. For a comprehensive look at the specifics of the language and the custom-designed elements offered in by the language, check out the [```language specification```](language%20specification) folder.

## Code Loading Mechanisms
When a script is loaded in, its hook methods are stored internally. These hook methods are run whenever events are triggered. If a hook method throws an exception or causes an exception in the handler method, that hook's failure counter increments by 1. Once a hook method reaches 10 failures, the hook is immediately deactivated and cannot be re-triggered until the script implementation is refreshed, either by the [```reload```](#usl-reload) sub-command or by a plugin or server restart.

There are two main ways of loading custom scripts through this plugin: text-based script files and JARs with classes implementing various script API classes. For details on both load mechanisms and the pros and cons of each method, check out [```Loading Mechanisms.md```](Loading%20Mechanisms.md).

## Plugin Features
In addition to script loading, this plugin also provides some core functionality, including a few default sub-commands for this plugin's command and a collection of starter scripts (which were all ported over from this project's precursor: the Utk Plugin Suite).

For a close look at all of the specific features provided by this plugin, check out [```Plugin Features.md```](Plugin%20Features.md).

## Why Script?
*Why should any developer use this scripting plugin over simply creating their own? After all, the script is essentially most of the code required for a stand-alone plugin, right?*

While creating a script for this script loading plugin may seem like most of the work for creating a plugin, the plugin itself would also require working extensively with the bukkit event listeners. Additionally, the plugin would lose the support of a lot of custom events and event handlers written specifically for this scripting plugin.

On the other hand, going with the script-based solution also benefits the end user. With an text-based script, users can read the implementations for themselves and customize the code with little more than a text editor. A few of the default scripts included with the plugin showcase this principle very well.

And, last but certainly not least, writing scripts for this script loading plugin abstracts the issue of working with multiple different APIs to this plugin. Because of the guarantee of functionality for APIs derived from the Bukkit API, a script written for one API can function as well as possible under other APIs, often with API-optimized event-handling.
