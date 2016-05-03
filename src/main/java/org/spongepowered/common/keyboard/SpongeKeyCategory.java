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
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SpongeKeyCategory implements KeyCategory {

    private final List<KeyBinding> bindings = new ArrayList<>();
    private final String id;
    private final String name;
    private final Text title;
    private final boolean def;

    private int internalId = -1;

    private SpongeKeyCategory(String id, Text title, String name, boolean def) {
        this.title = title;
        this.name = name;
        this.def = def;
        this.id = id;
    }

    public SpongeKeyCategory(String pluginId, String name, Text title, boolean def) {
        this(pluginId + ':' + name, title, name, def);
    }

    public SpongeKeyCategory(String pluginId, String name, Text title) {
        this(pluginId, name, title, false);
    }

    public SpongeKeyCategory(String id, Text title) {
        this(id, title, getName(id), false);
    }

    static String getName(String id) {
        int index = id.indexOf(':');
        return index == -1 ? id : id.substring(index + 1);
    }

    public void addBinding(KeyBinding keyBinding) {
        this.bindings.add(checkNotNull(keyBinding, "keyBinding"));
    }

    @Override
    public List<KeyBinding> getBindings() {
        return ImmutableList.copyOf(this.bindings);
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public int getInternalId() {
        return this.internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public boolean isDefault() {
        return this.def;
    }
}
