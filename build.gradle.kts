import nl.javadude.gradle.plugins.license.LicenseExtension

buildscript {
    repositories {
        mavenCentral()
        gradleScriptKotlin()
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin"))
        classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.10")
        classpath("gradle.plugin.nl.javadude.gradle.plugins:license-gradle-plugin:0.13.1")
    }
}

apply {
    plugin("kotlin")
    plugin("info.solidsoft.pitest")
    plugin("jacoco")
    plugin("com.github.hierynomus.license")
}

group = "com.probablycoding"
version = "0.1-SNAPSHOT"

extensions.getByType<LicenseExtension>(LicenseExtension::class.java).apply {
    header = file("$rootDir/HEADER.txt")
    ignoreFailures = false
    strictCheck = true

    mapping("kt", "SLASHSTAR_STYLE")
}

repositories {
    mavenCentral()
    gradleScriptKotlin()
}

dependencies {
    compile(kotlinModule("stdlib"))

    testCompile("junit:junit:4.12")
    testCompile(kotlinModule("test-junit"))
}
