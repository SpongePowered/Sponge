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

import com.google.common.base.Objects;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Locale;

import javax.annotation.Nullable;

public abstract class AbstractCatalogType implements CatalogType {

    @Nullable private final PluginContainer plugin;
    private final String name;
    private final String id;

    private int internalId = -1;

    protected AbstractCatalogType(PluginContainer plugin, String name) {
        this.plugin = checkNotNull(plugin, "plugin");
        this.name = checkNotNull(name, "name");
        this.id = plugin.getId() + ':' + name.toLowerCase(Locale.ENGLISH);
    }

    protected AbstractCatalogType(String id) {
        this.id = checkNotNull(id, "id");
        final int index = id.indexOf(':');
        checkArgument(index != -1, "Someone is tinkering with something funky");
        this.name = id.substring(index + 1);
        this.plugin = null;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    public PluginContainer getPlugin() {
        return this.plugin;
    }

    public int getInternalId() {
        return this.internalId;
    }

    public <T extends AbstractCatalogType> T setInternalId(int internalId) {
        this.internalId = internalId;
        return (T) this;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .omitNullValues()
                .add("id", this.id)
                .add("name", this.name)
                .add("internalId", this.internalId == -1 ? null : this.internalId)
                .toString();
    }
}
