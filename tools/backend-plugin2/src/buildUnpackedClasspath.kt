/*
 * Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.Configurable
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.io.path.writeText

@OptIn(ExperimentalPathApi::class)
@TaskAction
fun buildUnpackedClasspath(
    @Output outputDir: Path,
    @Input baseClasspath: Classpath,
    @Input extraClasspaths: Map<String, Classpath> = emptyMap(),
    subdirectoryName: String?,
    jarListFileName: String?,
) {
    val targetDir = subdirectoryName?.let { outputDir / it } ?: outputDir
    outputDir.deleteRecursively()
//    cleanDirectory(targetDir)
    val classpaths = buildMap {
        put("lib", baseClasspath)
        putAll(extraClasspaths)
    }
    classpaths.forEach { (name, paths) ->
        val dir = (targetDir / name).createParentDirectories()
        // some jars have the exact same filename even though they don't come from the same artifact
//        val alreadySeenFilenames = mutableSetOf<String>()
        for (path in paths.resolvedFiles) {
//            println("x: $path")
//            val alreadyExists = !alreadySeenFilenames.add(path.name)
//            val filename = if (alreadyExists) {
//                "${path.nameWithoutExtension}-${path.pathString.sha256String().take(8)}.${path.extension}"
//            } else {
//                path.name
//            }
            val filename = path.name
            path.copyTo(dir.resolve(filename).createParentDirectories())
        }
    }
    jarListFileName?.let { fileName ->
        targetDir.resolve(fileName).writeText(baseClasspath.resolvedFiles.joinToString("\n") { it.name })
    }
}

@Configurable
interface DistributionSettings {
    val extraClasspaths: Map<String, Classpath> get() = emptyMap()
    val embedClasspathAsResources: EmbedClasspathAsResources
}

@Configurable
interface EmbedClasspathAsResources {
    val classpath: Classpath
    val resourceDirName: String
}
