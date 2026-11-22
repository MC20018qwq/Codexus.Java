import org.gradle.api.artifacts.transform.CacheableTransform
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/** Retains the original Java API and Win64 DLL bytes, without debug symbols or other platforms. */
@CacheableTransform
abstract class WindowsOnnxRuntime : TransformAction<TransformParameters.None> {
    @get:InputArtifact
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.get().asFile
        if (!input.name.startsWith("onnxruntime-")) {
            outputs.file(input)
            return
        }
        val nativePrefix = "ai/onnxruntime/native/"
        val requiredNatives = setOf(
            nativePrefix + "win-x64/onnxruntime.dll",
            nativePrefix + "win-x64/onnxruntime4j_jni.dll"
        )
        val requiredEntries = requiredNatives + setOf(
            "ai/onnxruntime/OrtEnvironment.class",
            "ai/onnxruntime/OrtSession.class",
            "META-INF/MANIFEST.MF",
            "ThirdPartyNotices.txt"
        )
        ZipFile(input).use { source ->
            for (name in requiredEntries) {
                require(source.getEntry(name)?.let { !it.isDirectory && it.size > 0 } == true) {
                    "ONNX Runtime is missing required entry $name in ${input.name}; review the Win64 packaging rule"
                }
            }
            val windowsDlls = source.entries().asSequence()
                .filter { !it.isDirectory && it.name.startsWith(nativePrefix + "win-x64/") && it.name.endsWith(".dll", true) }
                .map { it.name }.toSet()
            require(windowsDlls == requiredNatives) {
                "ONNX Runtime changed its Win64 native dependencies; review the packaging rule before upgrading"
            }
            val output = outputs.file(input.nameWithoutExtension + "-win-x64.jar")
            ZipOutputStream(output.outputStream().buffered()).use { target ->
                source.entries().asSequence()
                    .filter { !it.isDirectory }
                    .filter { !it.name.endsWith(".pdb", true) && !it.name.contains(".dSYM/", true) }
                    .filter { !it.name.startsWith(nativePrefix) || it.name in requiredNatives }
                    .sortedWith(compareBy<ZipEntry> { if (it.name == "META-INF/MANIFEST.MF") 0 else 1 }.thenBy { it.name })
                    .forEach { entry ->
                        // Fixed timestamps and ordering make repeated builds reproducible.
                        val packed = ZipEntry(entry.name).apply { time = 0L }
                        target.putNextEntry(packed)
                        source.getInputStream(entry).use { it.copyTo(target) }
                        target.closeEntry()
                    }
            }
        }
    }
}

val onnxPlatform = Attribute.of("codexus.onnxruntime.platform", String::class.java)

subprojects {
    dependencies {
        // Tag artifacts, not component variants: variant selection precedes artifact transforms.
        artifactTypes.maybeCreate(ArtifactTypeDefinition.JAR_TYPE).attributes
            .attribute(onnxPlatform, "universal")
        registerTransform(WindowsOnnxRuntime::class) {
            from.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                .attribute(onnxPlatform, "universal")
            to.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                .attribute(onnxPlatform, "windows-x64")
        }
    }
    // Apply equally to compile/runtime/test classpaths, including the Gateway distribution.
    configurations.configureEach {
        if (isCanBeResolved) attributes.attribute(onnxPlatform, "windows-x64")
    }
}
