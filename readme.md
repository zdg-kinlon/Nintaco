# Nintaco FC (NES) Emulator

This repository adapts the development environment to Gradle based on the source code, supporting IntelliJ IDEA and Visual Studio Code as development tools.

The unmodified source code repository, [TechStark/Nintaco](https://github.com/TechStark/Nintaco), can only be developed using NetBeans due to its project structure, hence the creation of this repository.

The copyright of the code belongs to Nintaco. You are welcome to use the officially developed version. Download address: [Download Nintaco](https://nintaco.com/index.html)

This repository uses the latest released JDK for development and the latest public libraries. Some outdated methods will be replaced, and some modifications have been made to the source code, which may introduce bugs.

# Installation

1. Download the source code of this repository.
2. Download Java v25 from https://adoptium.net/temurin/releases/ and configure the `JAVA_HOME` system variable path.
3. Download Gradle version 9.3 or above from https://gradle.org/releases/ and configure the `GRADLE_HOME` system variable path.
4. Open the Nintaco folder using IntelliJ IDEA or Visual Studio Code.
5. The project will load automatically.

# Compilation

1. The build script is located in [build.gradle.kts](build.gradle.kts).
2. To compile the project, run the command: `gradle build`. After execution, the compiled program will be found in the [libs](build/libs) folder, along with automatically generated JRE, native files, and dependent public JARs.
3. run the [`start-app.bat`](build/libs/start-app.bat)

# Configuring Native Files for JInput in a Gradle Development Environment

In the Gradle script, the dependent JAR files are uncompressed and released into the project development environment. The class loader is then pointed to the file location, and the path is configured in the `net.java.games.input.librarypath` environment variable. This allows the application to run correctly during development. After compilation, the files need to be placed in the correct location according to the project path configuration to run.

This repository serves as an example, demonstrating how to load and run `dll`, `so`, and other native files. You can learn from it and perhaps find better methods that do not require extracting files from JAR archives.

For specific details, refer to the `public static void init()` method in [InputUtil.java](src/main/java/nintaco/input/InputUtil.java).