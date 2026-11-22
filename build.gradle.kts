plugins { base }

subprojects {
    apply(plugin = "java-library")
    group = "codexus"
    version = "0.1.0-port"

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        withSourcesJar()
    }
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

// The application targets Windows x64; keep the native OCR dependency platform-specific.
apply(from = "gradle/onnxruntime-windows.gradle.kts")
