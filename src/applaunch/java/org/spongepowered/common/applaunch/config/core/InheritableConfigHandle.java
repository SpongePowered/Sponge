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

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.applaunch.config.inheritable.BaseConfig;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class InheritableConfigHandle<T extends BaseConfig> extends ConfigHandle<T> {

    /**
     * The parent configuration - values are inherited from this
     */
    private final @Nullable InheritableConfigHandle<?> parent;

    /**
     * A node representation of {@link #node}, merged with the data of {@link #parent}.
     */
    private CommentedConfigurationNode mergedNode;

    public InheritableConfigHandle(final T instance, final @Nullable InheritableConfigHandle<?> parent) {
        super(instance);
        this.parent = parent;
    }

    public InheritableConfigHandle(final T instance,
            final ConfigurationLoader<? extends CommentedConfigurationNode> loader,
            final @Nullable InheritableConfigHandle<?> parent) {
        super(instance, loader);
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
    public <V> V getOrCreateValue(final Function<T, V> getter, final Consumer<BaseConfig> setter, boolean populate) {
        V ret = getter.apply(this.get());
        if (ret == null && populate) {
            setter.accept(this.get());
            if(this.parent != null) {
                setter.accept(this.parent.get());
            }
            ret = getter.apply(this.get());
        }
        return ret;
    }

    public void load() throws IOException, ObjectMappingException {
        if (this.isAttached()) {
            // store "what's in the file" separately in memory
            this.node = this.loader.load();
            // and perform any necessary version updates
            this.doVersionUpdate();

            // make a copy of the file data
            this.mergedNode = this.node.copy();
        } else {
            this.mergedNode = CommentedConfigurationNode.root(SpongeConfigs.OPTIONS);
        }

        // merge with settings from parent
        if (this.parent != null) {
            this.mergedNode.mergeValuesFrom(this.parent.mergedNode);
        }

        // populate the config object
        this.mapper.populate(this.mergedNode);
        this.doSave();
    }

    public void doSave() {
        if (!this.isAttached()) {
            return;
        }

        try {
            // save from the mapped object --> node
            this.mapper.serialize(this.node);

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
        } catch (IOException | ObjectMappingException e) {
            SpongeConfigs.LOGGER.error("Failed to save configuration", e);
        }
    }

    /**
     * Traverses the given {@code root} config node, removing any values which
     * are also present and set to the same value on this configs "parent".
     *
     * @param root The node to process
     */
    private void removeDuplicates(CommentedConfigurationNode root) {
        if (!this.isAttached()) {
            return;
        }
        if (this.parent == null) {
            throw new IllegalStateException("parent is null");
        }

        DuplicateRemovalVisitor.visit(root, this.parent.mergedNode);
    }
}
