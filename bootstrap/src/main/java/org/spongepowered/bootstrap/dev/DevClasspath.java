/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.bootstrap.dev;

import org.spongepowered.bootstrap.Bootstrap;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DevClasspath {

    /**
     * IntelliJ only builds source sets that are explicitly on the classpath.
     * So we declare everything into the initial classpath then we sort things and put them back in the right classloaders.
     * Since different IDEs might want to put things in different locations, we sadly have to identify the resources by their filenames.
     */
    public static List<Path[]> resolve() {
        final String[] cp = System.getProperty("java.class.path").split(File.pathSeparator);
        final Path spongeRoot = Paths.get(System.getProperty("sponge.dev.root")).toAbsolutePath();
        final Set<String> bootNames = Set.of(System.getProperty("sponge.dev.boot").split(File.pathSeparator));
        final Set<String> gameShadedNames = Set.of(System.getProperty("sponge.dev.gameShaded").split(File.pathSeparator));

        // boot layer
        final Multimap<String, WeightedPath> bootUnions = new Multimap<>();
        final List<Path> bootLibs = new ArrayList<>();

        // game or plugin layer
        final Multimap<String, WeightedPath> unions = new Multimap<>();
        final List<Path> libs = new ArrayList<>();

        final AtomicBoolean hasAPISourceSet = new AtomicBoolean(false);

        for (final String str : cp) {
            final Path path = Paths.get(str);
            if (!Files.exists(path)) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Non-existent: " + path);
                }
                continue;
            }

            final SourceSet sourceSet = SourceSet.identify(path);
            if (sourceSet != null) {
                if (sourceSet.project().startsWith(spongeRoot)) {
                    if (Bootstrap.DEBUG) {
                        System.out.println("Sponge SourceSet (" + sourceSet + "): " + path);
                    }

                    final String projectName = spongeRoot.relativize(sourceSet.project()).toString();
                    switch (projectName) {
                        case "bootstrap":
                            // ignore
                            break;
                        case "modlauncher-transformers", "library-manager":
                            bootUnions.add(projectName, new WeightedPath(0, path));
                            break;
                        case "SpongeAPI":
                            switch (sourceSet.name()) {
                                case "ap":
                                    // ignore
                                    break;
                                case "main":
                                    hasAPISourceSet.set(true);
                                    // no break
                                default:
                                    unions.add("sponge", new WeightedPath(0, path));
                                    break;
                            }
                            break;
                        case "", "vanilla", "forge", "neoforge":
                            final WeightedPath weightedPath = new WeightedPath(projectName.isEmpty() ? 1 : 2, path);
                            switch (sourceSet.name()) {
                                case "applaunchConfig":
                                case "applaunch":
                                    bootUnions.add("applaunch", weightedPath);
                                    break;
                                case "lang":
                                    unions.add("lang", weightedPath);
                                    break;
                                default:
                                    unions.add("sponge", weightedPath);
                                    break;
                            }
                            break;
                        default:
                            unions.add(projectName, new WeightedPath(0, path));
                            break;
                    }
                } else {
                    if (Bootstrap.DEBUG) {
                        System.out.println("External SourceSet (" + sourceSet + "): " + path);
                    }

                    unions.add(sourceSet.project().toString(), new WeightedPath(0, path));
                }
                continue;
            }

            final String fileName = path.getFileName().toString();

            if (fileName.startsWith("junit-") || fileName.startsWith("byte-buddy-")) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Ignored: " + path);
                }
                continue;
            }

            if (bootNames.contains(fileName) || fileName.startsWith("org.jacoco.core-") || fileName.startsWith("mockito-") || fileName.startsWith("objenesis-")) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Boot: " + path);
                }
                bootLibs.add(path);
                continue;
            }

            if (gameShadedNames.contains(fileName)) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Sponge: " + path);
                }
                unions.add("sponge", new WeightedPath(0, path));
                continue;
            }

            if (Bootstrap.DEBUG) {
                System.out.println("Game: " + path);
            }
            libs.add(path);
        }

        final List<Path[]> classpath = new ArrayList<>();

        for (final Path lib : bootLibs) {
            classpath.add(new Path[] { lib });
        }

        for (final List<WeightedPath> sourceSets : bootUnions.values()) {
            classpath.add(sourceSets.stream().sorted().map(WeightedPath::path).toArray(Path[]::new));
        }

        if (hasAPISourceSet.get()) {
            unions.get("sponge").removeIf((w) -> {
                final Path path = w.path();
                if (Files.isRegularFile(path)) {
                    final String fileName = path.getFileName().toString();
                    if (fileName.startsWith("spongeapi") && fileName.endsWith(".jar")) {
                        if (Bootstrap.DEBUG) {
                            System.out.println("Removing spongeapi jar in favor of sourceset: " + path);
                        }
                        return true;
                    }
                }
                return false;
            });
        }

        final StringBuilder resourcesEnvBuilder = new StringBuilder();
        for (final Path resource : libs) {
            resourcesEnvBuilder.append(resource).append(File.pathSeparator);
        }
        for (final List<WeightedPath> project : unions.values()) {
            project.stream().sorted().forEachOrdered(w -> resourcesEnvBuilder.append(w.path()).append('&'));
            resourcesEnvBuilder.setCharAt(resourcesEnvBuilder.length() - 1, File.pathSeparatorChar);
        }
        resourcesEnvBuilder.setLength(resourcesEnvBuilder.length() - 1);
        final String resourcesEnv = resourcesEnvBuilder.toString();

        if (Bootstrap.DEBUG) {
            System.out.println("Resources env: " + resourcesEnv);
        }
        System.setProperty("sponge.resources", resourcesEnv);

        return classpath;
    }
}
