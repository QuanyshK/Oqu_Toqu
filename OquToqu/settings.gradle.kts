pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/QuanyshK/scihubparser")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_PASSWORD")
            }
        }
    }
}

rootProject.name = "Oqu Toqu"
include(":app")
include(":domain")
include(":data")
