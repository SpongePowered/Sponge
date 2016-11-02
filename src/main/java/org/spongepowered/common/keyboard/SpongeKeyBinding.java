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

import com.google.common.collect.Multimap;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class SpongeKeyBinding extends AbstractCatalogType implements KeyBinding {

    @Nullable private final Multimap<Class<?>, Consumer<?>> eventConsumers;
    @Nullable private final SpongeKeyContext keyContext;
    private final int keyContextId;
    private final SpongeKeyCategory keyCategory;
    private final Text displayName;
    private final boolean def;

    public SpongeKeyBinding(PluginContainer plugin, String name, SpongeKeyContext keyContext,
            SpongeKeyCategory keyCategory, Text displayName, @Nullable Multimap<Class<?>, Consumer<?>> eventConsumers, boolean def) {
        super(plugin, name);
        this.eventConsumers = eventConsumers;
        this.keyContext = keyContext;
        this.keyContextId = -1;
        this.keyCategory = keyCategory;
        this.displayName = displayName;
        this.def = def;
    }

    public SpongeKeyBinding(String id, int keyContextId, SpongeKeyCategory keyCategory, Text displayName, boolean def) {
        super(id);
        this.keyContext = null;
        this.eventConsumers = null;
        this.keyContextId = keyContextId;
        this.keyCategory = keyCategory;
        this.displayName = displayName;
        this.def = def;
    }

    @Override
    public SpongeKeyContext getContext() {
        checkArgument(this.keyContext != null, "The key context isn't available on the client.");
        return this.keyContext;
    }

    @Override
    public SpongeKeyCategory getCategory() {
        return this.keyCategory;
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    public boolean isDefault() {
        return this.def;
    }

    @Nullable
    public Multimap<Class<?>, Consumer<?>> getEventConsumers() {
        return this.eventConsumers;
    }

    public int getKeyContextId() {
        return this.keyContextId == -1 ? getContext().getInternalId() : this.keyContextId;
    }
}
