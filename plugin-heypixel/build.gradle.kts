dependencies {
    api(project(":plugin-base1206"))
    implementation(project(":interceptors"))
    implementation(project(":game-launcher"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

version = "2.3.3"
tasks.jar { archiveBaseName.set("Codexus.Heypixel.Plugin") }

tasks.processTestResources {
    from(rootProject.layout.projectDirectory.file("tools/plugin-oracle/vectors.json"))
}
tasks.test {
    workingDir(layout.buildDirectory.dir("tmp/test host").get().asFile)
    doFirst { workingDir.mkdirs() }
}
