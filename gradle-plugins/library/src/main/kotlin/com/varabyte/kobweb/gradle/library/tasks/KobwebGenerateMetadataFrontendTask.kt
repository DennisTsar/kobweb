@file:Suppress("LeakingThis") // Following official Gradle guidance

package com.varabyte.kobweb.gradle.library.tasks

//@CacheableTask
//abstract class KobwebGenerateMetadataFrontendTask @Inject constructor(kobwebBlock: KobwebBlock) :
//    KobwebGenerateMetadataTask<FrontendData>(
//        kobwebBlock,
//        "Generate Kobweb metadata about this project's frontend structure that can be consumed later by a Kobweb app."
//    ) {
//
//    override fun getSourceFiles() = getSourceFilesJs()
//    override fun getGeneratedMetadataFile() = kobwebBlock.getGenJsResRoot(project).resolve(KOBWEB_METADATA_FRONTEND)
//
//    override fun createProcessor() = FrontendDataProcessor(
//        LoggingReporter(project.logger),
//        resolvePackageShortcut(project.group.toString(), kobwebBlock.pagesPackage.get())
//    )
//
//    override fun encodeToString(value: FrontendData) = Json.encodeToString(value)
//}
