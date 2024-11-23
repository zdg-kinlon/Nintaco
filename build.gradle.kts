plugins {
    id("java")
}

group = "nintaco"
version = "2024.10.27"

repositories {
    maven { setUrl("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}

dependencies {
    implementation("commons-logging:commons-logging:1.3.4")
    implementation("com.github.junrar:junrar:7.5.5")
    implementation("org.apache.commons:commons-compress:1.27.1") {
        exclude(module = "commons-io")
        exclude(module = "commons-codec")
        exclude(module = "commons-lang3")
    }
    implementation("ch.randelshofer:org.monte.media:17.1")
    implementation("org.tukaani:xz:1.10")
    implementation("net.java.jinput:jinput:2.0.7")
    // https://repo1.maven.org/maven2/net/java/jinput/jinput/2.0.10/jinput-2.0.10-natives-all.jar
    // compileOnly("net.java.jinput:jinput:2.0.10:natives-all") Not Supporting x86
    compileOnly("net.java.jinput:jinput-platform:2.0.7:natives-windows")
    compileOnly("net.java.jinput:jinput-platform:2.0.7:natives-linux")
    compileOnly("net.java.jinput:jinput-platform:2.0.7:natives-osx")
}

//gradle.projectsEvaluated {
//    tasks.withType<JavaCompile> {
//        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
//    }
//}

tasks.test {
    useJUnitPlatform()
}

tasks.named("build") {
    dependsOn("createJre", "generateStartScript")
}

tasks.named("compileJava") {
    dependsOn("extractJarJinputPlatform")
}

tasks.named("clean") {
    delete(layout.projectDirectory.dir("lib/native"))
}

tasks.register<Copy>("extractJarJinputPlatform") {
    configurations.compileClasspath.get().resolvedConfiguration.resolvedArtifacts.filter {
        it.moduleVersion.id.name == "jinput-platform"
                && it.moduleVersion.id.group == "net.java.jinput"
                && it.type == "jar"
    }.map {
        from(zipTree(it.file)).into(layout.projectDirectory.dir("lib/native"))
    }
    doLast {
        file(layout.projectDirectory.file("lib/native/libjinput-osx.jnilib")).copyTo(
            file(layout.projectDirectory.file("lib/native/libjinput-osx.dylib")), true
        )
    }
}

tasks.register<Copy>("copyNativeFiles") {
    dependsOn("extractJarJinputPlatform")
    from(layout.projectDirectory.dir("lib/native"))
    into(layout.buildDirectory.dir("libs/lib/native"))
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath.get().asFileTree)
    into(layout.buildDirectory.dir("libs/lib/"))
}

tasks.jar {
    dependsOn("copyNativeFiles")
    dependsOn("copyDependencies")

    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Main-Class" to "${project.group}.Main"
        )

        attributes["Class-Path"] = configurations.compileClasspath.get().resolvedConfiguration.resolvedArtifacts
            .map { it.file.relativeTo(project.layout.projectDirectory.asFile) }
            .map { it.path.replace("\\", "/") }
            .joinToString(" ") { "lib/${it.substringAfterLast("/")}" }
    }
}

tasks.register<Exec>("createJre") {
    val javaHome = System.getProperty("java.home")
    val outputJreDir = file(layout.buildDirectory.dir("libs/jre/"))

    commandLine(
        "${javaHome}/bin/jlink",
        "--module-path", "${javaHome}/jmods",
        "--add-modules", "java.base,java.desktop,java.logging",
        "--output", outputJreDir.absolutePath
    )
}

tasks.register("generateStartScript") {
    doLast {
        val scriptName = when {
            System.getProperty("os.name").contains("Windows") -> "start-app.bat"
            System.getProperty("os.name").contains("Mac") -> "start-app.command"
            else -> "start-app.sh"
        }

        val jarName = "${project.name}-${project.version}.jar"
        val scriptContent = when (scriptName) {
            "start-app.bat" -> """
                @echo off
                ".\jre\bin\java.exe" -jar "$jarName"
            """.trimIndent()

            else -> """
                #!/bin/bash
                "./jre/bin/java" -jar "./$jarName"
            """.trimIndent()
        }

        val scriptFile = file(layout.buildDirectory.dir("libs/$scriptName"))

        scriptFile.writeText(scriptContent)

        if (scriptName.endsWith(".sh") || scriptName.endsWith(".command")) {
            scriptFile.setExecutable(true)
        }
    }
}