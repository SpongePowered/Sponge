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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    public static void setSaveSuppressed(boolean suppressed) {
        ConfigHandle.saveSuppressed = suppressed;
        if (!suppressed && !ConfigHandle.saveQueue.isEmpty()) {
            for (final Iterator<ConfigHandle<?>> it = ConfigHandle.saveQueue.iterator(); it.hasNext();) {
                try {
                    it.next().doSave();
                } catch (IOException | ObjectMappingException ex) {
                    LOGGER.error("Unable to save a Sponge configuration!", ex);
                }
                it.remove();
            }
        }
    }

    protected final @Nullable ConfigurationLoader<? extends CommentedConfigurationNode> loader;
    protected final ObjectMapper<T>.BoundInstance mapper;
    protected @MonotonicNonNull CommentedConfigurationNode node;

    ConfigHandle(final T instance) {
        try {
            this.mapper = ObjectMapper.forObject(instance);
        } catch (final ObjectMappingException ex) {
            // object mapper classes are constant from compile onwards, this failure will always happen.
            throw new AssertionError(ex);
        }
        this.loader = null;
    }

    ConfigHandle(final T instance, final @Nullable ConfigurationLoader<? extends CommentedConfigurationNode> loader) {
        try {
            this.mapper = ObjectMapper.forObject(instance);
        } catch (ObjectMappingException ex) {
            // object mapper classes are constant from compile onwards, this failure will always happen.
            throw new AssertionError(ex);
        }
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
        return this.mapper.getInstance();
    }

    public CompletableFuture<T> updateAndSave(final UnaryOperator<T> updater) {
        final T updated = requireNonNull(updater, "updater").apply(this.mapper.getInstance());
        return asyncFailableFuture(() -> {
            // TODO: Force one save at a time
            this.save();
            return updated;
        }, ForkJoinPool.commonPool());
    }

    void load() throws IOException, ObjectMappingException {
        if (this.loader == null) { // we are virtual
            return;
        }

        this.node = this.loader.load();
        this.doVersionUpdate();
        this.mapper.populate(this.node);
        this.doSave();
    }

    protected final void doVersionUpdate() {
        final boolean wasEmpty = this.node.isEmpty();
        final CommentedConfigurationNode versionNode = this.node.getNode(VERSION_PATH);
        final int existingVersion = versionNode.getInt(-1);
        this.mapper.getInstance().getTransformation().apply(this.node);
        final int newVersion = versionNode.getInt(-1);
        if (!wasEmpty && newVersion > existingVersion) {
            LOGGER.info("Updated {} from version {} to {}", this.mapper.getInstance(), existingVersion, newVersion);
        }
        versionNode.setCommentIfAbsent(VERSION_COMMENT);
        this.node.getNode(VERSION_PATH).setValue(versionNode); // workaround for some weird issue, can remove @configurate 4.0
    }

    public void reload() throws IOException, ObjectMappingException {
        // TODO: Something nicer?
        this.load();
    }

    public final void save() {
        if (ConfigHandle.saveSuppressed) {
            ConfigHandle.saveQueue.add(this);
        } else {
            try {
                doSave();
            } catch (final IOException | ObjectMappingException ex) {
                LOGGER.error("Unable to save configuration to {}", this.loader, ex);
            }
        }
    }

    protected void doSave() throws ObjectMappingException, IOException {
        if (this.loader == null) {
            return;
        }

        if (this.node == null) {
            this.node = this.loader.createEmptyNode();
        }

        this.mapper.serialize(this.node);
        this.loader.save(this.node);
    }

    @Nullable
    private CommentedConfigurationNode getSetting(String key) {
        if (key.equalsIgnoreCase("config-enabled")) {
            return this.node.getNode(key);
        } else if (!key.contains(".") || key.indexOf('.') == key.length() - 1) {
            return null;
        } else {
            CommentedConfigurationNode node = this.node;
            final String[] split = key.split("\\.");
            return node.getNode((Object[]) split);
        }
    }

    public CompletableFuture<CommentedConfigurationNode> updateSetting(String key, Object value) {
        return asyncFailableFuture(() -> {
            CommentedConfigurationNode upd = this.getSetting(key);
            this.mapper.populate(this.node);
            this.save();
            return upd;
        }, ForkJoinPool.commonPool());
    }

    public <V> CompletableFuture<CommentedConfigurationNode> updateSetting(String key, V value, TypeToken<V> token) {
        return asyncFailableFuture(() -> {
            CommentedConfigurationNode upd = this.getSetting(key);
            upd.setValue(token, value);
            this.mapper.populate(this.node);
            this.save();
            return upd;
        }, ForkJoinPool.commonPool());
    }

    private static <T> CompletableFuture<T> asyncFailableFuture(final Callable<T> action, final Executor executor) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                future.complete(action.call());
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    public ConfigurationNode getNode() {
        return this.node;
    }
}
