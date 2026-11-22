plugins { `java-library` }

dependencies {
    api(project(":cipher"))
    api(project(":development-sdk"))
    implementation(libs.commons.compress)
    implementation(libs.xz)
    implementation(libs.jna.platform)
    testImplementation(project(":interceptors"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

val prepareTestFixtures = tasks.register<Copy>("prepareTestFixtures") {
    from(rootProject.layout.projectDirectory.dir("tools")) {
        include("oracle/*.json", "fixtures/*.json")
    }
    into(layout.buildDirectory.dir("tmp/tools"))
}

tasks.test {
    dependsOn(prepareTestFixtures)
    workingDir(layout.buildDirectory.dir("tmp/test host").get().asFile)
    doFirst { workingDir.mkdirs() }
}
