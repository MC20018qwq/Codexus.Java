plugins { application }

dependencies {
    implementation(project(":cipher"))
    implementation(project(":development-sdk"))
    implementation(project(":interceptors"))
    implementation(project(":game-launcher"))
    implementation(libs.netty.http)}

application {
    mainClass.set("codexus.gateway.Gateway")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
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

// The fixed data layout is relative to the distribution, including double-clicking bin/gateway.bat.
tasks.startScripts {
    doLast {
        val marker = "for %%i in (\"%APP_HOME%\") do set APP_HOME=%%~fi"
        windowsScript.writeText(windowsScript.readText().replace(marker, marker + "\r\ncd /d \"%APP_HOME%\"\r\nif errorlevel 1 exit /b 1"))
        unixScript.writeText(unixScript.readText().replace("CLASSPATH=", "cd \"${'$'}APP_HOME\" || exit 1\n\nCLASSPATH="))
    }
}
