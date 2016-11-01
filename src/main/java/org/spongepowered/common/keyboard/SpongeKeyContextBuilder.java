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
package org.spongepowered.common.keyboard;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.keyboard.KeyContext;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.function.Predicate;

public class SpongeKeyContextBuilder implements KeyContext.Builder {

    private Predicate<Player> activePredicate;
    private Predicate<KeyContext> conflictPredicate;

    public SpongeKeyContextBuilder() {
        reset();
    }

    @Override
    public KeyContext.Builder from(KeyContext value) {
        checkNotNull(value, "value");
        this.activePredicate = ((SpongeKeyContext) value).activePredicate;
        this.conflictPredicate = ((SpongeKeyContext) value).conflictPredicate;
        return this;
    }

    @Override
    public KeyContext.Builder reset() {
        this.activePredicate = player -> true;
        this.conflictPredicate = context -> true;
        return this;
    }

    @Override
    public KeyContext.Builder active(Predicate<Player> activePredicate) {
        this.activePredicate = checkNotNull(activePredicate, "activePredicate");
        return this;
    }

    @Override
    public KeyContext.Builder conflicts(Predicate<KeyContext> conflictPredicate) {
        this.conflictPredicate = checkNotNull(conflictPredicate, "conflictPredicate");
        return this;
    }

    @Override
    public KeyContext build(Object plugin, String identifier) {
        final PluginContainer pluginContainer = getPlugin(plugin);
        final String name = getName(pluginContainer, identifier);
        return new SpongeKeyContext(pluginContainer, name, this.activePredicate, this.conflictPredicate);
    }

    static String getName(PluginContainer pluginContainer, String identifier) {
        final int index = identifier.indexOf(':');
        if (index == -1) {
            checkArgument(!identifier.isEmpty(), "The identifier may not be empty");
            return identifier;
        }
        final String pluginId = identifier.substring(index);
        checkArgument(pluginContainer.getId().equalsIgnoreCase(identifier.substring(index + 1)),
                "The identifier contains a different plugin id then the specified plugin container, got %s in %s, but expected %s",
                pluginId, identifier, pluginContainer.getId());
        identifier = identifier.substring(index + 1);
        checkArgument(!identifier.isEmpty(), "The identifier may not be empty");
        return identifier;
    }

    static PluginContainer getPlugin(Object plugin) {
        final Optional<PluginContainer> container = Sponge.getPluginManager().fromInstance(plugin);
        checkArgument(container.isPresent(), "Unknown plugin: %s", plugin);
        return container.get();
    }
}
