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
package org.spongepowered.common.applaunch.plugin.discovery;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.common.applaunch.plugin.PluginServiceLoader;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.PluginLoader;
import org.spongepowered.plugin.PluginService;
import org.spongepowered.plugin.discovery.*;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.MetadataParser;

import java.lang.module.ModuleDescriptor;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PluginDiscovery extends PluginServiceLoader {
    private Map<PluginResource, Candidate> candidates;
    private Collection<PluginMetadataReader> readers;

    protected PluginDiscovery(final Environment environment) {
        super(environment);
    }

    public final void discoverPluginResources() {
        final Map<PluginResource, Candidate> candidates = new HashMap<>();
        final Set<Class<? extends PluginResourceLocator>> locatorsFound = new HashSet<>();

        int maxBatches = 10;
        final String maxBatchesProp = System.getProperty("sponge.discovery.maxBatches");
        if (maxBatchesProp != null) {
            try {
                maxBatches = Integer.parseInt(maxBatchesProp);
            } catch (final NumberFormatException ignored) {}
        }

        int batch = 1;

        while (true) {
            this.environment.logger().info("Running plugin locators batch #{} ...", batch);

            final List<SpongeJVMPluginResource> batchServices = new ArrayList<>();

            for (final PluginResourceLocator locator : this.<PluginResourceLocator>loadServices("resource locator", PluginResourceLocator.class, locatorsFound::add).values()) {
                final Collection<PluginResourceLocator.Result> results;
                try {
                    results = locator.locatePluginResources(this.environment);
                } catch (final Exception e) {
                    this.environment.logger().error("Service '{}' failed to locate plugin resources.", locator.name(), e);
                    continue;
                }
                this.environment.logger().info("Service '{}' located {} resources.", locator.name(), results.size());

                for (final PluginResourceLocator.Result result : results) {
                    Candidate candidate = candidates.get(result.resource());
                    if (candidate == null) {
                        candidate = new Candidate(result.resource(), result.unknownResourceStrategy());
                        candidates.put(result.resource(), candidate);
                        candidate.detectServices();
                        if (candidate.locatorFound || candidate.readerFound) {
                            batchServices.add((SpongeJVMPluginResource) result.resource());
                        }
                    } else {
                        candidate.unknownResourceStrategy = candidate.unknownResourceStrategy.merge(result.unknownResourceStrategy());
                    }
                    candidate.locators.add(locator);
                }
            }

            this.environment.logger().info("Found {} new discovery services.", batchServices.size());
            if (batchServices.isEmpty()) {
                break;
            }

            if (++batch > maxBatches) {
                this.environment.logger().warn("Max locator batches reached.");
                break;
            }

            try {
                this.appendDiscoveryServices(batchServices, batch);
            } catch (final Exception e) {
                this.environment.logger().error("Failed to build new service layer.", e);
                break;
            }
        }

        for (final Candidate candidate : candidates.values()) {
            this.environment.logger().debug("Found {} located by [{}] ({}).", candidate.resource, candidate.locatorNames(), candidate.serviceNames());
        }

        final Collection<PluginMetadataReader> readers = this.loadServices("metadata reader", PluginMetadataReader.class).values();

        this.candidates = candidates;
        this.readers = readers;
    }

    protected abstract void appendDiscoveryServices(final List<SpongeJVMPluginResource> resources, final int batch) throws Exception;

    public final Collection<Candidate> candidates() {
        return Collections.unmodifiableCollection(this.candidates.values());
    }

    public final Candidate candidate(final PluginResource resource) {
        Candidate candidate = this.candidates.get(resource);
        if (candidate == null) {
            candidate = new Candidate(resource, UnknownResourceStrategy.IGNORE);
            this.candidates.put(resource, candidate);
            this.environment.logger().debug("Found {} located by the platform.", resource);
        }
        return candidate;
    }

    public final Collection<PluginResource> resources(final ResourceLoading loading) {
        return this.candidates.values().stream().filter((c) -> c.loading() == loading).map(Candidate::resource).toList();
    }

    public final void logMetadataWarnings() {
        for (final String warning : MetadataParser.warnings()) {
            this.environment.logger().warn(warning);
        }
    }

    public final class Candidate {
        private final PluginResource resource;
        private final List<PluginResourceLocator> locators = new ArrayList<>();
        private @MonotonicNonNull UnknownResourceStrategy unknownResourceStrategy;
        private final SequencedMap<String, PluginMetadata> metadata = new LinkedHashMap<>();
        private boolean locatorFound, readerFound, loaderFound, modFound;

        public Candidate(final PluginResource resource, final UnknownResourceStrategy unknownResourceStrategy) {
            this.resource = resource;
            this.unknownResourceStrategy = unknownResourceStrategy;
        }

        public PluginResource resource() {
            return this.resource;
        }

        private void detectServices() {
            if (this.resource instanceof SpongeJVMPluginResource jvmResource) {
                final Set<String> services = jvmResource.module().provides().stream().map(ModuleDescriptor.Provides::service).collect(Collectors.toSet());
                this.locatorFound = services.contains(PluginResourceLocator.class.getName());
                this.readerFound = services.contains(PluginMetadataReader.class.getName());
                this.loaderFound = services.contains(PluginLoader.class.getName());
            }
        }

        private String serviceNames() {
            final StringJoiner joiner = new StringJoiner(", ");
            if (this.locatorFound) {
                joiner.add("locator");
            }
            if (this.readerFound) {
                joiner.add("reader");
            }
            if (this.loaderFound) {
                joiner.add("loader");
            }
            return joiner.toString();
        }

        private String locatorNames() {
            return this.locators.stream().map(PluginService::name).collect(Collectors.joining(", "));
        }

        public List<PluginResourceLocator> locators() {
            return Collections.unmodifiableList(this.locators);
        }

        public void readMetadata() {
            this.metadata.clear();
            for (final PluginMetadataReader reader : PluginDiscovery.this.readers) {
                final Collection<? extends PluginMetadata> plugins;
                try {
                    plugins = reader.readPluginMetadata(PluginDiscovery.this.environment, this.resource, this.locators());
                } catch (final Exception e) {
                    PluginDiscovery.this.environment.logger().error("Service '{}' failed to read plugin metadata", reader.name(), e);
                    continue;
                }
                for (final PluginMetadata plugin : plugins) {
                    this.metadata.put(plugin.id(), plugin);
                }
            }
        }

        public SequencedCollection<PluginMetadata> metadata() {
            return Collections.unmodifiableSequencedCollection(this.metadata.sequencedValues());
        }

        public boolean pluginFound() {
            return !this.metadata.isEmpty();
        }

        public ResourceLoading loading() {
            if (this.pluginFound() || this.loaderFound) {
                return ResourceLoading.GAME_LIBRARY;
            }
            if (this.locatorFound || this.readerFound) {
                return ResourceLoading.IGNORED;
            }
            return this.unknownResourceStrategy.loading();
        }

        public void setModFound() {
            this.modFound = true;
        }

        public void logResult() {
            if (this.pluginFound()) {
                PluginDiscovery.this.environment.logger().debug("Found {} metadata in {}.",  this.metadata.size(), this.resource);
                return;
            }
            if (this.locatorFound || this.readerFound || this.loaderFound || this.modFound) {
                return;
            }

            final String result = switch (this.unknownResourceStrategy.loading()) {
                case IGNORED -> "ignored";
                case LIBRARY -> "loaded as a standard library";
                case GAME_LIBRARY -> "loaded as a game library";
            };

            if (this.unknownResourceStrategy.warn()) {
                PluginDiscovery.this.environment.logger().warn("The unknown resource {} will be {}.", this.resource, result);
            } else {
                PluginDiscovery.this.environment.logger().debug("The unknown resource {} will be {}.", this.resource, result);
            }
        }
    }
}
