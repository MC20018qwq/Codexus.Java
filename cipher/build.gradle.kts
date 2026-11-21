plugins { `java-library` }

dependencies {
    api(project(":development-sdk"))
    implementation(libs.onnxruntime)
}
