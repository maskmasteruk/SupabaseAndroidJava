plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("com.vanniktech.maven.publish") version "0.37.0"
    `java-gradle-plugin`
    signing
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

mavenPublishing {
    coordinates("io.github.maskmasteruk", "supabase-android-plugin", "0.0.1")

    pom {
        name.set("Supabase Gradle Plugin")
        description.set(
            "Gradle plugin that reads Supabase credentials from supabase-config.json and generates BuildConfig fields such as BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY, and BuildConfig.SUPABASE_SECRET_KEY."
        )
        url.set("https://github.com/maskmasteruk/SupabaseAndroidJava")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("maskmasteruk")
                name.set("Udhayakrishna K G")
                email.set("k.g.u2006@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/maskmasteruk/SupabaseAndroidJava.git")
            developerConnection.set("scm:git:ssh://github.com/maskmasteruk/SupabaseAndroidJava.git")
            url.set("https://github.com/maskmasteruk/SupabaseAndroidJava/tree/main")
        }
    }

    signAllPublications()
    publishToMavenCentral()
}


dependencies {
    implementation("org.json:json:20260522")
    compileOnly("com.android.tools.build:gradle:9.2.1")
}

gradlePlugin {
    plugins {
        create("supabasePlugin") {
            id = "io.github.maskmasteruk.supabase"
            implementationClass = "io.github.maskmasteruk.supabase.plugin.SupabasePlugin"
        }
    }
}