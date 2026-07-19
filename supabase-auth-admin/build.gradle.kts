plugins {
    alias(libs.plugins.android.library)
    id("org.jetbrains.dokka") version "2.2.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
    signing
}

android {
    namespace = "io.github.maskmasteruk.supabase.auth.admin"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

mavenPublishing {
    coordinates("io.github.maskmasteruk", "supabase-android-auth-admin", "0.0.1")

    pom {
        name.set("Supabase Admin Auth SDK for Android")
        description.set("Java-based Android SDK for Supabase Admin Authentication using a Service Role or Secret Key.")
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
    implementation(project(":supabase-core"))
    implementation(project(":supabase-auth"))
}