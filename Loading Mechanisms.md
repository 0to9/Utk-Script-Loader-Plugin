# Code Loading Mechanisms
## Text-Based Script Files
The primary method for loading in custom scripts is via text files. The scripts, which must meet the specifications as outlined in the language specifications, are recursively loaded based on the single script file [```scripts/main.txt```](src/main/resources/scripts/main.txt) found in the plugin data folder. Plugin and script users can modify this main inclusion file as well as other included files even during runtime, and changes can be subsequently loaded with the [```reload```](Plugin%20Features.md#usl-reload) sub-command.

## JAR-Based Implementations
Though easier to work with due to proper syntax highlighting and code completion tools, JAR-based implementations lose the ease of modification associated with the text file scripts.

JAR loading is currently not implemented, but is planned as a feature. *TODO JAR loading for scripts*

## Which Is Better?
*TODO add this section*
