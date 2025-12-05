import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.CompilationArtifact
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

@TaskAction
fun createFatJar(
    @Input originalJar: CompilationArtifact,
    @Input runtimeClasspath: Classpath,
    @Output fatJar: Path,
) {
    makeFatJar(originalJar.artifact, runtimeClasspath.resolvedFiles, fatJar)
}

// GPT-generated code, not to be blindly trusted
fun makeFatJar(mainJar: Path, dependencyJars: List<Path>, outJar: Path) {
    Files.createDirectories(outJar.parent ?: outJar.toAbsolutePath().parent)

    val added = HashSet<String>()

    JarFile(mainJar.toFile()).use { main ->
        val manifest = main.manifest

        Files.newOutputStream(outJar).use { fos ->
            val jos = if (manifest != null) JarOutputStream(fos, manifest) else JarOutputStream(fos)
            jos.use {
                fun copyFrom(jarPath: Path, skipManifest: Boolean) {
                    JarFile(jarPath.toFile()).use { jf ->
                        val entries = jf.entries()
                        while (entries.hasMoreElements()) {
                            val e = entries.nextElement()
                            val name = e.name

                            if (e.isDirectory) continue
                            if (skipManifest && name.equals("META-INF/MANIFEST.MF", ignoreCase = true)) continue
                            if (isSignatureFile(name)) continue
                            if (!added.add(name)) continue

                            val outEntry = ZipEntry(name).apply { time = e.time }
                            it.putNextEntry(outEntry)
                            jf.getInputStream(e).use { input -> input.copyTo(it) }
                            it.closeEntry()
                        }
                    }
                }

                // main jar first (wins duplicates)
                copyFrom(mainJar, skipManifest = true)

                // then deps
                for (dep in dependencyJars) {
                    if (Files.isRegularFile(dep)) {
                        copyFrom(dep, skipManifest = true)
                    }
                }
            }
        }
    }
}

private fun isSignatureFile(name: String): Boolean {
    val u = name.uppercase()
    if (!u.startsWith("META-INF/")) return false
    return u.endsWith(".SF") || u.endsWith(".RSA") || u.endsWith(".DSA")
}
