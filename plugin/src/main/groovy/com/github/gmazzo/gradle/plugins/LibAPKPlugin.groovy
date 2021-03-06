package com.github.gmazzo.gradle.plugins

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.github.gmazzo.gradle.plugins.tasks.DexFiles
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class LibAPKPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.with {
            def android = null

            plugins.all { plugin ->
                if (plugin instanceof LibraryPlugin) {
                    android = plugin.extension as LibraryExtension

                    android.libraryVariants.all { variant ->
                        def name = variant.name
                        def workDir = file("$buildDir/intermediates/libapk/$name")

                        def extractClasses = tasks.create("extractClassesJarFromBundle${name.capitalize()}", Copy) {
                            from zipTree(variant.packageLibrary.archivePath).matching {
                                include 'classes.jar'
                            }
                            destinationDir workDir

                            dependsOn variant.packageLibrary
                        }

                        def classesJar = file("$workDir/classes.jar")
                        def dexTask = tasks.create("transformLibraryDex${name.capitalize()}", DexFiles).with {
                            inputFiles = files(classesJar)
                            outputFile = file("$workDir/classes.dex")

                            dependsOn extractClasses
                        }

                        tasks.create("bundleLibApk${name.capitalize()}", Zip).with { self ->
                            description "Assembles a bundle containing the compiled library (dex classes) in $name."
                            from dexTask.outputs
                            from zipTree(classesJar).matching { exclude '**/*.class' }
                            destinationDir file("$buildDir/outputs/libapk")
                            archiveName "${variant.baseName}.lib.apk"

                            dependsOn dexTask
                            tasks["assemble${name.capitalize()}"].dependsOn self
                        }
                    }
                }
            }

            afterEvaluate {
                if (!android) {
                    throw new IllegalStateException("The 'com.android.library' plugin is required.")
                }
            }
        }
    }

}
