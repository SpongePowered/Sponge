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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A holder for load/save state of a configuration.
 */
public class ConfigHandle<T extends Config> {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * Location where current version is stored
     */
    public static final NodePath VERSION_PATH = NodePath.path("version");
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
                    ConfigHandle.LOGGER.error("Unable to save a Sponge configuration!", ex);
                }
                it.remove();
            }
        }
    }

    protected final @Nullable ConfigurationLoader<? extends CommentedConfigurationNode> loader;
    protected final Class<T> instanceType;
    protected volatile T instance;
    protected @MonotonicNonNull CommentedConfigurationNode node;
    private final @Nullable Supplier<ConfigurationTransformation> transformer;

    protected ConfigHandle(final Class<T> type) {
        try {
            this.instance = BasicConfigurationNode.root(SpongeConfigs.OPTIONS).get(type);
        } catch (final SerializationException ex) {
            throw new AssertionError(ex);
        }
        this.instanceType = type;
        this.loader = null;
        this.transformer = null;
    }

    protected ConfigHandle(final Class<T> instanceType, final @Nullable Supplier<ConfigurationTransformation> versionUpdater, final @Nullable ConfigurationLoader<? extends CommentedConfigurationNode> loader) {
        this.instanceType = instanceType;
        this.loader = loader;
        this.transformer = versionUpdater;
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
        return ConfigHandle.asyncFailableFuture(() -> {
            // TODO: Force one save at a time
            this.save();
            return updated;
        }, ForkJoinPool.commonPool());
    }

    void load() throws ConfigurateException {
        if (this.loader == null) { // we are virtual
            return;
        }

        final CommentedConfigurationNode node = this.loader.load();
        this.doVersionUpdate(node);
        this.node = node;
        this.instance = node.get(this.instanceType);
        this.doSave();
    }

    protected final void doVersionUpdate(final CommentedConfigurationNode node) throws ConfigurateException {
        if (this.transformer != null) {
            final boolean wasEmpty = node.empty();
            final CommentedConfigurationNode versionNode = node.node(ConfigHandle.VERSION_PATH);
            final int existingVersion = versionNode.getInt(-1);
            this.transformer.get().apply(node);
            final int newVersion = versionNode.getInt(-1);
            if (!wasEmpty && newVersion > existingVersion) {
                ConfigHandle.LOGGER.info("Updated {} from version {} to {}", this.instance, existingVersion, newVersion);
            }
            versionNode.commentIfAbsent(ConfigHandle.VERSION_COMMENT);
            node.node(ConfigHandle.VERSION_PATH).set(versionNode);
        }
    }

    public CompletableFuture<?> reload() {
        // TODO: Something nicer?
        return ConfigHandle.asyncFailableFuture(() -> {
            this.load();
            return null;
        }, ForkJoinPool.commonPool());
    }

    public final void save() {
        if (ConfigHandle.saveSuppressed) {
            ConfigHandle.saveQueue.add(this);
        } else {
            try {
                this.doSave();
            } catch (final ConfigurateException ex) {
                ConfigHandle.LOGGER.error("Unable to save configuration to {}", this.loader, ex);
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

        final T instance = this.instance;
        final CommentedConfigurationNode node = this.node;
        if (instance != null && node != null) {
            node.set(this.instanceType, instance);
        }
        this.loader.save(node);
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
        return ConfigHandle.asyncFailableFuture(() -> {
            final CommentedConfigurationNode upd = this.getSetting(key);
            upd.set(value);
            this.instance = this.node.get(this.instanceType);
            this.save();
            return upd;
        }, ForkJoinPool.commonPool());
    }

    public <V> CompletableFuture<CommentedConfigurationNode> updateSetting(final String key, final V value, final TypeToken<V> token) {
        return ConfigHandle.asyncFailableFuture(() -> {
            final CommentedConfigurationNode upd = this.getSetting(key);
            upd.set(token, value);
            this.instance = this.node.get(this.instanceType);
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
