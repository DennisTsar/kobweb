import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmTarget = JvmTarget.JVM_11
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = jvmTarget.target
    targetCompatibility = jvmTarget.target
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(jvmTarget)
}
