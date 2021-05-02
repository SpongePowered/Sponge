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
package org.spongepowered.common.config.inheritable;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class InheritableConfigHandle<T extends BaseConfig> extends ConfigHandle<T> {

    /**
     * The parent configuration - values are inherited from this
     */
    private final @Nullable InheritableConfigHandle<?> parent;

    /**
     * A node representation of {@link #node}, merged with the data of {@link #parent}.
     */
    private CommentedConfigurationNode mergedNode;

    public InheritableConfigHandle(final Class<T> instanceType, final @Nullable InheritableConfigHandle<?> parent) {
        super(instanceType);
        this.parent = parent;
    }

    public InheritableConfigHandle(
        final Class<T> instanceType,
        final @Nullable Supplier<ConfigurationTransformation> versionUpdater,
        final ConfigurationLoader<? extends CommentedConfigurationNode> loader,
        final @Nullable InheritableConfigHandle<?> parent
    ) {
        super(instanceType, versionUpdater, loader);
        this.parent = parent;
    }

    /**
     * Get or create a value, initializing in the parent configuration if not present.
     *
     * @param getter Function to get value
     * @param setter Function to set default value
     * @param populate whether default value should be written
     * @param <V> value type
     * @return value or default
     */
    public <V> V getOrCreateValue(final Function<T, V> getter, final Consumer<BaseConfig> setter, final boolean populate) {
        V ret = getter.apply(this.get());
        if (ret == null && populate) {
            setter.accept(this.get());
            if (this.parent != null) {
                setter.accept(this.parent.get());
            }
            ret = getter.apply(this.get());
        }
        return ret;
    }

    public void load() throws ConfigurateException {
        final CommentedConfigurationNode node;
        final CommentedConfigurationNode mergedNode;
        if (this.isAttached()) {
            // store "what's in the file" separately in memory
            node = this.loader.load();
            // and perform any necessary version updates
            this.doVersionUpdate(node);

            // make a copy of the file data
            mergedNode = node.copy();
        } else {
            node = null;
            mergedNode = CommentedConfigurationNode.root(SpongeConfigs.OPTIONS);
        }

        // merge with settings from parent
        if (this.parent != null && this.parent.mergedNode != null) {
            mergedNode.mergeFrom(this.parent.mergedNode);
        }

        // populate the config object
        this.instance = mergedNode.get(this.instanceType);
        this.node = node;
        this.mergedNode = mergedNode;
        this.doSave();
    }

    @Override
    public void doSave() throws ConfigurateException {
        if (!this.isAttached()) {
            return;
        }

        // save from the mapped object --> node
        this.node.set(this.instanceType, this.instance);

        // before saving this config, remove any values already declared with the same value on the parent
        if (this.parent != null) {
            this.removeDuplicates(this.node);
        }

        // save the data to disk
        this.loader.save(this.node);

        // In order for the removeDuplicates method to function properly, it is extremely
        // important to avoid running save on parent BEFORE children save. Doing so will
        // cause duplicate nodes to not be removed as parent would have cleaned up
        // all duplicates prior.
        // While this issue would only occur when there are multiple levels of inheritance,
        // which we don't currently have, there's no harm in supporting it anyways.
        // To handle the above issue, we save AFTER saving child config.
        if (this.parent != null) {
            this.parent.doSave();
        }
    }

    /**
     * Traverses the given {@code root} config node, removing any values which
     * are also present and set to the same value on this configs "parent".
     *
     * @param root The node to process
     */
    private void removeDuplicates(final CommentedConfigurationNode root) {
        if (!this.isAttached()) {
            return;
        }
        if (this.parent == null) {
            throw new IllegalStateException("parent is null");
        }

        DuplicateRemovalVisitor.visit(root, this.parent.mergedNode);
    }
}
