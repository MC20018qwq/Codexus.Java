plugins { `java-library` }

dependencies {
    api(project(":cipher"))
    api(project(":development-sdk"))
    implementation(libs.commons.compress)
    implementation(libs.xz)
    implementation(libs.jna.platform)}
