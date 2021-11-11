/*
 * This file is part of spongegradle-convention, licensed under the MIT License (MIT).
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
import com.google.gson.GsonBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class OutputDependenciesToJson extends DefaultTask {

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * A single dependency.
     */
    static final class DependencyDescriptor implements Comparable<DependencyDescriptor> {

        final String group;
        final String module;
        final String version;
        final String md5;

        DependencyDescriptor(final String group, final String module, final String version, final String md5) {
            this.group = group;
            this.module = module;
            this.version = version;
            this.md5 = md5;
        }

        @Override
        public int compareTo(final DependencyDescriptor that) {
            final int group = this.group.compareTo(that.group);
            if (group != 0) {
                return group;
            }

            final int module = this.module.compareTo(that.module);
            if (module != 0) {
                return module;
            }

            return this.version.compareTo(that.version);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || this.getClass() != other.getClass()) {
                return false;
            }
            final DependencyDescriptor that = (DependencyDescriptor) other;
            return Objects.equals(this.group, that.group)
                && Objects.equals(this.module, that.module)
                && Objects.equals(this.version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.group, this.module, this.version);
        }

        @Override
        public String toString() {
            return "DependencyDescriptor{" +
                "group='" + this.group + '\'' +
                ", module='" + this.module + '\'' +
                ", version='" + this.version + '\'' +
                ", md5='" + this.md5 + '\'' +
                '}';
        }
    }

    /**
     * A manifest containing a list of dependencies.
     *
     * <p>At runtime, transitive dependencies won't be traversed, so this needs to
     * include direct + transitive depends.</p>
     */
    static final class DependencyManifest {

        final Map<String, List<DependencyDescriptor>> dependencies;

        DependencyManifest(final Map<String, List<DependencyDescriptor>> dependencies) {
            this.dependencies = dependencies;
        }
    }

    /**
     * Configuration to gather dependency artifacts from.
     */
    @Nested
    public abstract MapProperty<String, ConfigurationHolder> getDependencies();

    public final void dependencies(final String key, final NamedDomainObjectProvider<Configuration> config) {
        this.getDependencies().put(key, config.map(ConfigurationHolder::new));
    }

    /**
     * Excludes configuration, to remove certain entries from dependencies and
     * transitive dependencies of {@link #getDependencies()}.
     */
    @Input
    @Optional
    public abstract Property<Configuration> getExcludedDependencies();

    public final void excludedDependencies(final NamedDomainObjectProvider<Configuration> config) {
        this.getExcludedDependencies().set(config);
    }

    /**
     * Classifiers to include in the dependency manifest. The empty string identifies no classifier.
     */
    @Input
    public abstract SetProperty<String> getAllowedClassifiers();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    public OutputDependenciesToJson() {
        this.getAllowedClassifiers().add("");
    }

    @TaskAction
    public void generateDependenciesJson() {
        final Set<ModuleComponentIdentifier> excludedDeps = new HashSet<>();
        if (this.getExcludedDependencies().isPresent()) {
            for (final ResolvedArtifactResult result : this.getExcludedDependencies().get().getIncoming().getArtifacts()) {
                if (result.getId().getComponentIdentifier() instanceof ModuleComponentIdentifier) {
                    excludedDeps.add((ModuleComponentIdentifier) result.getId().getComponentIdentifier());
                }
            }
        }

        final Map<String, ConfigurationHolder> inputConfigs = this.getDependencies().get();
        final Map<String, List<DependencyDescriptor>> dependenciesMap = new TreeMap<>();

        for (final Map.Entry<String, ConfigurationHolder> entry : inputConfigs.entrySet()) {
            dependenciesMap.put(entry.getKey(), this.configToDescriptor(entry.getValue().getConfiguration().getIncoming().getArtifacts(), excludedDeps));
        }
        final DependencyManifest manifest = new DependencyManifest(dependenciesMap);

        this.getLogger().info("Writing to {}", this.getOutputFile().get().getAsFile());
        try (
            final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(this.getOutputFile().get().getAsFile()), StandardCharsets.UTF_8))
        ) {
            OutputDependenciesToJson.GSON.toJson(manifest, writer);
        } catch (final IOException ex) {
            throw new GradleException("Failed to write dependencies manifest", ex);
        }
    }

    private List<DependencyDescriptor> configToDescriptor(final ArtifactCollection conf, final Set<ModuleComponentIdentifier> excludedDeps) {
        return conf.getArtifacts().stream()
            .filter(dep -> {
                final ComponentIdentifier ident = dep.getId().getComponentIdentifier();
                return ident instanceof ModuleComponentIdentifier && !excludedDeps.contains(ident);
            })
            .distinct()
            .map(dependency -> {
                final ModuleComponentIdentifier id = (ModuleComponentIdentifier) dependency.getId().getComponentIdentifier();

                // Get file input stream for reading the file content
                final String md5hash;
                try (final InputStream in = new FileInputStream(dependency.getFile())) {
                    final MessageDigest hasher = MessageDigest.getInstance("MD5");
                    final byte[] buf = new byte[4096];
                    int read;
                    while ((read = in.read(buf)) != -1) {
                        hasher.update(buf, 0, read);
                    }

                    md5hash = OutputDependenciesToJson.toHexString(hasher.digest());
                } catch (final IOException | NoSuchAlgorithmException ex) {
                    throw new GradleException("Failed to create hash for " + dependency, ex);
                }

                // create descriptor
                return new DependencyDescriptor(
                    id.getGroup(),
                    id.getModule(),
                    id.getVersion(),
                    md5hash
                );
            })
            .sorted(Comparator.naturalOrder()) // sort dependencies for stable output
            .collect(Collectors.toList());
    }

    public static String toHexString(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = OutputDependenciesToJson.hexArray[v >>> 4];
            hexChars[j * 2 + 1] = OutputDependenciesToJson.hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static class ConfigurationHolder {
        private final Configuration configuration;

        public ConfigurationHolder(final Configuration configuration) {
            this.configuration = configuration;
        }

        @InputFiles
        public Configuration getConfiguration() {
            return this.configuration;
        }
    }

}
