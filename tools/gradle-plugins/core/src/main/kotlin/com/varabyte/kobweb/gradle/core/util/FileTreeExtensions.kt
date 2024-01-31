package com.varabyte.kobweb.gradle.core.util

import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails

fun <T> FileTree.mapFiles(transform: (FileVisitDetails) -> T): List<T> {
    return buildList {
        this@mapFiles.visit {
            if (isDirectory) return@visit
            add(transform(this))
        }
    }
}
