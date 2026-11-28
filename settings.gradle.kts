rootProject.name = "codexus-java"
include("csharpsup", "development-sdk", "cipher", "interceptors", "game-launcher", "gateway", "plugin-base12111", "plugin-heypixel")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { mavenCentral() }
}
