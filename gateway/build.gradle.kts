plugins { application }

dependencies {
    implementation(project(":cipher"))
    implementation(project(":development-sdk"))
    implementation(project(":interceptors"))
    implementation(project(":game-launcher"))
    implementation(libs.netty.http)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

application {
    mainClass.set("codexus.gateway.Gateway")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    workingDir(layout.buildDirectory.dir("tmp/test host").get().asFile)
    doFirst { workingDir.mkdirs() }
}

// Development launches and distributions use the same fixed resources/static layout.
val prepareGatewayWebsite = tasks.register<Copy>("prepareGatewayWebsite") {
    from("src/main/dist/resources/static")
    into(layout.buildDirectory.dir("run/resources/static"))
}
tasks.named<JavaExec>("run") {
    dependsOn(prepareGatewayWebsite)
    workingDir(layout.buildDirectory.dir("run").get().asFile)
}

tasks.processTestResources {
    from(rootProject.layout.projectDirectory.file("tools/gateway-oracle/vectors.json"))
    from("src/main/dist/resources/static") { into("website") }
}

// The fixed data layout is relative to the distribution, including double-clicking bin/gateway.bat.
tasks.startScripts {
    doLast {
        val marker = "for %%i in (\"%APP_HOME%\") do set APP_HOME=%%~fi"
        windowsScript.writeText(windowsScript.readText().replace(marker, marker + "\r\ncd /d \"%APP_HOME%\"\r\nif errorlevel 1 exit /b 1"))
        unixScript.writeText(unixScript.readText().replace("CLASSPATH=", "cd \"${'$'}APP_HOME\" || exit 1\n\nCLASSPATH="))
    }
}
// Exercise the same JAR/classpath and bundled website users receive.
tasks.test { dependsOn(tasks.installDist) }

// Bundled Java plugins stay in plugins/ and are loaded by the SDK dependency loader.
val bundledJavaPlugins = listOf(":plugin-base1206", ":plugin-heypixel")
distributions.main {
    contents {
        bundledJavaPlugins.forEach { from(project(it).tasks.named("jar")) { into("plugins") } }
    }
}
val prepareGatewayPlugins = tasks.register<Copy>("prepareGatewayPlugins") {
    bundledJavaPlugins.forEach { from(project(it).tasks.named("jar")) }
    into(layout.buildDirectory.dir("run/plugins"))
}
tasks.named<JavaExec>("run") { dependsOn(prepareGatewayPlugins) }
