package com.varabyte.kobweb.gradle.library

import com.varabyte.kobweb.gradle.core.KobwebCorePlugin
import com.varabyte.kobweb.gradle.core.kmp.JsTarget
import com.varabyte.kobweb.gradle.core.kmp.JvmTarget
import com.varabyte.kobweb.gradle.core.kmp.buildTargets
import com.varabyte.kobweb.gradle.core.kmp.jsTarget
import com.varabyte.kobweb.gradle.core.kmp.jvmTarget
import com.varabyte.kobweb.gradle.core.ksp.applyKspPlugin
import com.varabyte.kobweb.gradle.core.ksp.setupKspJs
import com.varabyte.kobweb.gradle.core.ksp.setupKspJvm
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

@Suppress("unused") // KobwebApplicationPlugin is found by Gradle via reflection
class KobwebLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(KobwebCorePlugin::class.java)
        project.applyKspPlugin()

        project.buildTargets.withType<KotlinJsIrTarget>().configureEach {
            val jsTarget = JsTarget(this)
            project.setupKspJs(jsTarget, includeAppData = false)
        }

        project.buildTargets.withType<KotlinJvmTarget>().configureEach {
            val jvmTarget = JvmTarget(this)
            project.setupKspJvm(jvmTarget)
        }
    }
}

@Deprecated(
    "Add the task outputs to the source set directly instead. Note that you may have to adjust the task to output a directory instead of a file.",
    ReplaceWith("kotlin.sourceSets.getByName(\"jsMain\").kotlin.srcDir(task)"),
)
fun Project.notifyKobwebAboutFrontendCodeGeneratingTask(task: Task) {
    tasks.matching { it.name == jsTarget.kspKotlin }.configureEach { dependsOn(task) }
}

@Deprecated(
    "Add the task outputs to the source set directly instead. Note that you may have to adjust the task to output a directory instead of a file.",
    ReplaceWith("kotlin.sourceSets.getByName(\"jvmMain\").kotlin.srcDir(task)"),
)
fun Project.notifyKobwebAboutBackendCodeGeneratingTask(task: Task) {
    tasks.matching { it.name == jvmTarget?.kspKotlin }.configureEach { dependsOn(task) }
}
