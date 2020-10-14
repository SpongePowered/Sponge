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
package org.spongepowered.common.applaunch.config.core;

import static java.util.Objects.requireNonNull;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.UnaryOperator;


/**
 * A holder for load/save state of a configuration.
 */
public class ConfigHandle<T extends Config> {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * Location where current version is stored
     */
    public static final Object[] VERSION_PATH = new Object[] {"version"};
    private static final String VERSION_COMMENT = "Active configuration version\n"
            + "This has no relation to the current Sponge version, and will be updated automatically\n"
            + "Manual changes may cause unpredictable results.";

    // Save suppression
    // Temporary fix for IO spam -- just don't save during server load.
    private static boolean saveSuppressed = true;
    private static final Set<ConfigHandle<?>> saveQueue = ConcurrentHashMap.newKeySet();

    /**
     * Enable or disable save suppression.
     *
     * <p>Save on initial load will always be performed, no matter what this setting is.</p>
     *
     * @implNote Default save suppression is {@code true}.
     * @param suppressed whether saves should be suppressed
     */
    public static void setSaveSuppressed(final boolean suppressed) {
        ConfigHandle.saveSuppressed = suppressed;
        if (!suppressed && !ConfigHandle.saveQueue.isEmpty()) {
            for (final Iterator<ConfigHandle<?>> it = ConfigHandle.saveQueue.iterator(); it.hasNext();) {
                try {
                    it.next().doSave();
                } catch (final ConfigurateException ex) {
                    LOGGER.error("Unable to save a Sponge configuration!", ex);
                }
                it.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectMapper.Mutable<T> mutableMapper(final T instance) {
        try {
            final ObjectMapper<?> mapper = SpongeConfigs.OBJECT_MAPPERS.get(instance.getClass());
            if (mapper instanceof ObjectMapper.Mutable<?>) {
                return (ObjectMapper.Mutable<T>) mapper;
            }
        } catch (final SerializationException ex) {
            throw new AssertionError(ex);
        }
        // object mapper classes are constant from compile onwards, this failure will always happen.
        throw new AssertionError("Object mapper for " + instance + " was not mutable");
    }

    protected final @Nullable ConfigurationLoader<? extends CommentedConfigurationNode> loader;
    protected final ObjectMapper.Mutable<T> mapper;
    protected final T instance;
    protected @MonotonicNonNull CommentedConfigurationNode node;

    ConfigHandle(final T instance) {
        this.mapper = mutableMapper(instance);
        this.instance = instance;
        this.loader = null;
    }

    ConfigHandle(final T instance, final @Nullable ConfigurationLoader<? extends CommentedConfigurationNode> loader) {
        this.mapper = mutableMapper(instance);
        this.instance = instance;
        this.loader = loader;
    }

    /**
     * If this configuration refers to a transient world or dimension type, it
     * will be marked as {@code detached}, meaning that it will not
     * be persisted.
     *
     * @return whether the node is attached, false if detached
     */
    public boolean isAttached() {
        return this.loader != null;
    }

    public T get() {
        return this.instance;
    }

    public CompletableFuture<T> updateAndSave(final UnaryOperator<T> updater) {
        final T updated = requireNonNull(updater, "updater").apply(this.instance);
        return asyncFailableFuture(() -> {
            // TODO: Force one save at a time
            this.save();
            return updated;
        }, ForkJoinPool.commonPool());
    }

    void load() throws ConfigurateException {
        if (this.loader == null) { // we are virtual
            return;
        }

        this.node = this.loader.load();
        this.doVersionUpdate();
        this.mapper.load(this.instance, this.node);
        this.doSave();
    }

    protected final void doVersionUpdate() throws ConfigurateException {
        final boolean wasEmpty = this.node.empty();
        final CommentedConfigurationNode versionNode = this.node.node(VERSION_PATH);
        final int existingVersion = versionNode.getInt(-1);
        this.instance.<CommentedConfigurationNode>getTransformation().apply(this.node);
        final int newVersion = versionNode.getInt(-1);
        if (!wasEmpty && newVersion > existingVersion) {
            LOGGER.info("Updated {} from version {} to {}", this.instance, existingVersion, newVersion);
        }
        versionNode.commentIfAbsent(VERSION_COMMENT);
        this.node.node(VERSION_PATH).set(versionNode);
    }

    public void reload() throws ConfigurateException {
        // TODO: Something nicer?
        this.load();
    }

    public final void save() {
        if (ConfigHandle.saveSuppressed) {
            ConfigHandle.saveQueue.add(this);
        } else {
            try {
                doSave();
            } catch (final ConfigurateException ex) {
                LOGGER.error("Unable to save configuration to {}", this.loader, ex);
            }
        }
    }

    protected void doSave() throws ConfigurateException {
        if (this.loader == null) {
            return;
        }

        if (this.node == null) {
            this.node = this.loader.createNode();
        }

        this.mapper.save(this.instance, this.node);
        this.loader.save(this.node);
    }

    private @Nullable CommentedConfigurationNode getSetting(final String key) {
        if (key.equalsIgnoreCase("config-enabled")) {
            return this.node.node(key);
        } else if (!key.contains(".") || key.indexOf('.') == key.length() - 1) {
            return null;
        } else {
            final CommentedConfigurationNode node = this.node;
            final String[] split = key.split("\\.");
            return node.node((Object[]) split);
        }
    }

    public CompletableFuture<CommentedConfigurationNode> updateSetting(final String key, final Object value) {
        return asyncFailableFuture(() -> {
            final CommentedConfigurationNode upd = this.getSetting(key);
            upd.set(value);
            this.mapper.load(this.instance, this.node);
            this.save();
            return upd;
        }, ForkJoinPool.commonPool());
    }

    public <V> CompletableFuture<CommentedConfigurationNode> updateSetting(final String key, final V value, final TypeToken<V> token) {
        return asyncFailableFuture(() -> {
            final CommentedConfigurationNode upd = this.getSetting(key);
            upd.set(token, value);
            this.mapper.load(this.instance, this.node);
            this.save();
            return upd;
        }, ForkJoinPool.commonPool());
    }

    private static <T> CompletableFuture<T> asyncFailableFuture(final Callable<T> action, final Executor executor) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                future.complete(action.call());
            } catch (final Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    public ConfigurationNode getNode() {
        return this.node;
    }
}
