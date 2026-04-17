import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("io.gitlab.arturbosch.detekt").version("1.23.7")
    id("org.jlleitschuh.gradle.ktlint") version "12.0.2"
    id("com.google.gms.google-services") version "4.4.2" apply false
}

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    tasks.withType<Detekt> {
        jvmTarget = JavaVersion.VERSION_21.toString()

        reports {
            xml {
                required.set(true)
                outputLocation.set(file("reports/detekt.xml"))
            }
            html {
                required.set(true)
                outputLocation.set(file("reports/detekt.html"))
            }
        }
    }

    detekt {
        config.setFrom(files("$rootDir/config/detekt.yml"))
        baseline = file("build/reports/detekt/baseline.xml")
        reports {
            xml {
                enabled = true
                destination = file("build/reports/detekt/detekt.xml")
            }
            html {
                enabled = true
                destination = file("build/reports/detekt/detekt.html")
            }
        }
    }


}
