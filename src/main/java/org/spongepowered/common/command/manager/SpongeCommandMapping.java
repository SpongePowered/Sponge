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
package org.spongepowered.common.command.manager;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.plugin.PluginContainer;

import java.util.Set;

public final class SpongeCommandMapping implements CommandMapping {

    private final String alias;
    private final Set<String> allAliases;
    private final PluginContainer container;
    private final CommandRegistrar<?> registrar;

    public SpongeCommandMapping(final String alias,
                                final Set<String> allAliases,
                                final PluginContainer container,
                                final CommandRegistrar<?> registrar) {
        this.alias = alias;
        this.allAliases = ImmutableSet.copyOf(allAliases);
        this.container = container;
        this.registrar = registrar;
    }

    @Override
    public @NonNull String primaryAlias() {
        return this.alias;
    }

    @Override
    public @NonNull Set<String> allAliases() {
        return this.allAliases;
    }

    @Override
    public @NonNull PluginContainer plugin() {
        return this.container;
    }

    @Override
    public @NonNull CommandRegistrar<?> registrar() {
        return this.registrar;
    }

    @Override
    public String toString() {
        return "SpongeCommandMapping{" +
                "alias='" + alias + '\'' +
                ", allAliases=" + allAliases +
                ", container=" + container +
                ", registrar=" + registrar +
                '}';
    }

}
