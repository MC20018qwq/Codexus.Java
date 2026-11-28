dependencies {
    api(project(":development-sdk"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

version = "2.0.0"
tasks.jar { archiveBaseName.set("Codexus.Base12111.Plugin") }

tasks.processTestResources {
    from(rootProject.layout.projectDirectory.file("tools/plugin-oracle/vectors.json"))
}
tasks.test {
    workingDir(layout.buildDirectory.dir("tmp/test host").get().asFile)
    doFirst { workingDir.mkdirs() }
}
