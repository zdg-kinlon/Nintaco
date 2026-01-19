# Nintaco FC(NES) Emulator

这个仓库是在源代码的基础上，适配了Gradle的开发环境，支持 IntelliJ IDEA 和 Visual Studio Code 开发工具。

未经修改的源代码仓库 [TechStark/Nintaco](https://github.com/TechStark/Nintaco) 由于项目结构的原因，只能使用 NetBeans
进行开发，因此这个仓库出现了。

代码的版权归 Nintaco 所有，欢迎使用官方开发的版本。下载地址：[Download Nintaco](https://nintaco.com/index.html)

这个仓库使用最新发布的 JDK 开发和最新的公共库，部分过时的方法会进行替换，对源码进行了部分修改，可能会出现 BUG。

# 安装

1. 下载这个仓库的源代码
2. 下载 Java v25 https://adoptium.net/temurin/releases/ 配置`JAVA_HOME`系统变量的路径
3. 下载 Gradle v9.3 以上的版本 https://gradle.org/releases/ 配置`GRADLE_HOME`系统变量的路径
4. 使用 IntelliJ IDEA 或者 Visual Studio Code 打开 Nintaco 文件夹
5. 项目即可自动加载

# 编译

1. 编译的脚本在 [build.gradle.kts](build.gradle.kts)
2. 编译项目命令：`gradle build` 执行后会在 [libs](build/libs) 文件夹中得到生成的程序，并且自动生成JRE、native
   files、依赖的公共jar
3. 运行 [`start-app.bat`](build/libs/start-app.bat)

# 关于 JInput 在 Gradle 开发环境下，配置 native 文件的方法

在 Gradle 的脚本中，通过将依赖的Jar文件解压缩后，释放到项目开发的环境中，然后通过类加载器指向文件的位置，最后将路径配置到
`net.java.games.input.librarypath`环境中，这样在开发时即可正常运行，编译后也需要根据项目路径的配置放到正确的位置上才能运行

这个仓库是示例，示范了如何将`dll`、`so`这些本地文件进行加载并运行，可以学习使用，或许有更好的方法，不需要从jar压缩文件中释放

具体过程参考：[InputUtil.java](src/main/java/nintaco/input/InputUtil.java) 的 `public static void init()`方法
