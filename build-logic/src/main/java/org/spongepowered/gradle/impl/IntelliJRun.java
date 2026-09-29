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
package org.spongepowered.gradle.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * A native IntelliJ Application run configuration.
 *
 * <p>IntelliJ derives a run's classpath from its module's dependencies, which keeps each module's own
 * library versions instead of Gradle's conflict-resolved ones. So the configuration is anchored on a small
 * {@link #getModule() module} that contains the main class, and the run's real classpath is supplied
 * explicitly from Gradle, with project build outputs swapped for IntelliJ's own outputs.</p>
 */
public abstract class IntelliJRun implements Named {

    private final String name;

    @Inject
    public IntelliJRun(final String name) {
        this.name = name;
    }

    @Override
    @Input
    public String getName() {
        return this.name;
    }

    @Input
    public abstract Property<String> getMainClass();

    /** The IntelliJ module that contains the main class, e.g. {@code Sponge.bootstrap.forge}. */
    @Input
    public abstract Property<String> getModule();

    /** The Gradle-resolved runtime classpath the run should see. */
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    /** The runtime classpath IntelliJ derives for {@link #getModule()}; its jars missing from {@link #getClasspath()} are excluded. */
    @Classpath
    public abstract ConfigurableFileCollection getModuleClasspath();

    @Input
    public abstract ListProperty<String> getArgs();

    @Input
    public abstract ListProperty<String> getJvmArgs();

    @Input
    public abstract MapProperty<String, String> getEnvironment();

    @Input
    public abstract Property<String> getWorkingDirectory();

    /**
     * Adds the args, JVM args, system properties and environment Forge's userdev metadata declares for {@code side},
     * resolving the tokens SlimeLauncher would otherwise fill in at launch.
     *
     * @param slimeMetadata ForgeGradle's extracted Slime Launcher metadata directory (holding {@code runs.json})
     * @param assetsRoot the directory holding the Minecraft assets
     * @param mappings the MCP mappings name, e.g. {@code official_26.3}
     */
    @SuppressWarnings("unchecked")
    public void forgeUserdev(final Provider<Directory> slimeMetadata, final String side, final Provider<String> assetsRoot, final String mappings) {
        final Provider<Map<String, Object>> run = slimeMetadata.map(dir -> (Map<String, Object>) IntelliJRun.readJson(dir.file("runs.json")).get(side));
        final Provider<Map<String, String>> tokens = slimeMetadata.zip(assetsRoot, (dir, assets) -> Map.of(
            "{asset_index}", (String) ((Map<String, Object>) IntelliJRun.readJson(dir.file("metadata/minecraft/version.json")).get("assetIndex")).get("id"),
            "{assets_root}", assets,
            "{mcp_mappings}", mappings
        ));
        this.getArgs().addAll(run.zip(tokens, (r, t) -> ((List<String>) r.get("args")).stream().map(arg -> IntelliJRun.replaceTokens(arg, t)).toList()));
        this.getJvmArgs().addAll(run.map(r -> {
            final List<String> jvmArgs = new ArrayList<>((List<String>) r.get("jvmArgs"));
            ((Map<String, String>) r.getOrDefault("props", Map.of())).forEach((key, value) -> jvmArgs.add("-D" + key + "=" + value));
            return jvmArgs;
        }));
        this.getEnvironment().putAll(run.zip(tokens, (r, t) -> {
            final Map<String, String> env = new LinkedHashMap<>();
            ((Map<String, String>) r.get("env")).forEach((key, value) -> env.put(key, IntelliJRun.replaceTokens(value, t)));
            return env;
        }));
    }

    private static Map<String, Object> readJson(final RegularFile file) {
        try (final Reader reader = Files.newBufferedReader(file.getAsFile().toPath())) {
            return new Gson().fromJson(reader, new TypeToken<Map<String, Object>>() {}.getType());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String replaceTokens(String value, final Map<String, String> tokens) {
        for (final Map.Entry<String, String> token : tokens.entrySet()) {
            value = value.replace(token.getKey(), token.getValue());
        }
        return value;
    }
}
