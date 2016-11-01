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

import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

public class SpongeKeyCategoryBuilder implements KeyCategory.Builder {

    private Text title;

    @Override
    public KeyCategory.Builder title(Text title) {
        this.title = checkNotNull(title, "title");
        return this;
    }

    @Override
    public KeyCategory.Builder from(KeyCategory value) {
        this.title = value.getTitle();
        return this;
    }

    @Override
    public KeyCategory.Builder reset() {
        this.title = null;
        return this;
    }

    @Override
    public KeyCategory build(Object plugin, String identifier) {
        final PluginContainer pluginContainer = getPlugin(plugin);
        final String name = getName(pluginContainer, identifier);
        checkState(this.title != null, "The title must be set");
        return new SpongeKeyCategory(pluginContainer, name, this.title, false);
    }
}
