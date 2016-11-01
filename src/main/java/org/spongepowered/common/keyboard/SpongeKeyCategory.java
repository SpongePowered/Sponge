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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SpongeKeyCategory extends AbstractCatalogType implements KeyCategory {

    private final List<KeyBinding> bindings = new ArrayList<>();
    @Nullable private List<KeyBinding> immutableBindings = null;
    private final Text title;
    private final boolean def;

    public SpongeKeyCategory(PluginContainer plugin, String name, Text title, boolean def) {
        super(plugin, name);
        this.title = title;
        this.def = def;
    }

    public SpongeKeyCategory(String id, Text title, boolean def) {
        super(id);
        this.title = title;
        this.def = def;
    }

    public void addBinding(KeyBinding keyBinding) {
        this.bindings.add(checkNotNull(keyBinding, "keyBinding"));
        this.immutableBindings = null;
    }

    @Override
    public List<KeyBinding> getBindings() {
        if (this.immutableBindings == null) {
            this.immutableBindings = ImmutableList.copyOf(this.bindings);
        }
        return this.immutableBindings;
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    public boolean isDefault() {
        return this.def;
    }
}
