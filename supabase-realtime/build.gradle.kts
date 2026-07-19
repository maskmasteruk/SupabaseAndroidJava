plugins {
    alias(libs.plugins.android.library)
    id("org.jetbrains.dokka") version "2.2.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
    signing
}

android {
    namespace = "io.github.maskmasteruk.supabase.realtime"
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
    coordinates("io.github.maskmasteruk", "supabase-android-realtime", "0.0.1")

    pom {
        name.set("Supabase Realtime SDK for Android")
        description.set("Java-based Android SDK for subscribing to Supabase Realtime events and database changes.")
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
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    implementation(project(":supabase-core"))
}