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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.common.keyboard.SpongeKeyContextBuilder.getName;
import static org.spongepowered.common.keyboard.SpongeKeyContextBuilder.getPlugin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.event.keyboard.InteractKeyEvent;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.keyboard.KeyCategories;
import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.keyboard.KeyContext;
import org.spongepowered.api.keyboard.KeyContexts;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class SpongeKeyBindingBuilder implements KeyBinding.Builder {

    @Nullable private KeyCategory keyCategory;
    @Nullable private KeyContext keyContext;
    private final Multimap<Class<?>, Consumer<?>> eventConsumers = HashMultimap.create();
    private Text displayName;

    public SpongeKeyBindingBuilder() {
        reset();
    }

    @Override
    public KeyBinding.Builder category(KeyCategory category) {
        this.keyCategory = checkNotNull(category, "category");
        return this;
    }

    @Override
    public KeyBinding.Builder context(KeyContext keyContext) {
        this.keyContext = checkNotNull(keyContext, "keyContext");
        return this;
    }

    @Override
    public KeyBinding.Builder displayName(Text displayName) {
        this.displayName = checkNotNull(displayName, "displayName");
        return this;
    }

    @Override
    public <E extends InteractKeyEvent> KeyBinding.Builder listener(Class<E> type, Consumer<E> listener) {
        checkNotNull(type, "type");
        checkNotNull(listener, "listener");
        this.eventConsumers.put(type, listener);
        return this;
    }

    @Override
    public KeyBinding.Builder from(KeyBinding value) {
        this.keyCategory = value.getCategory();
        this.displayName = value.getDisplayName();
        return this;
    }

    @Override
    public KeyBinding.Builder reset() {
        this.eventConsumers.clear();
        this.keyCategory = null;
        this.displayName = null;
        return this;
    }

    @Override
    public KeyBinding build(Object plugin, String identifier) {
        final PluginContainer pluginContainer = getPlugin(plugin);
        final String name = getName(pluginContainer, identifier);
        checkState(this.displayName != null, "The display name must be set");
        final SpongeKeyCategory keyCategory = (SpongeKeyCategory) (this.keyCategory == null ? KeyCategories.MISC : this.keyCategory);
        final SpongeKeyContext keyContext = (SpongeKeyContext) (this.keyContext == null ? KeyContexts.UNIVERSAL : this.keyContext);
        return new SpongeKeyBinding(pluginContainer, name, keyContext, keyCategory, this.displayName,
                ImmutableMultimap.copyOf(this.eventConsumers), false);
    }
}
