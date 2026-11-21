plugins { `java-library` }

dependencies {
    api(project(":csharpsup"))
    api(libs.netty.codec)
    api(libs.netty.proxy)
}
