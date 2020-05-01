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
package org.spongepowered.common.text.placeholder;

import com.google.common.base.Preconditions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;
import org.spongepowered.api.text.placeholder.PlaceholderText;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;

import java.util.function.Function;

import javax.annotation.Nullable;

public class SpongePlaceholderParserBuilder implements PlaceholderParser.Builder {

    @Nullable private PluginContainer pluginContainer;
    @Nullable private String id;
    @Nullable private String name;
    @Nullable private Function<PlaceholderContext, Text> parser;

    @Override
    public PlaceholderParser.Builder plugin(Object plugin) {
        this.pluginContainer = SpongeImpl.getPluginContainer(plugin);
        return this;
    }

    @Override
    public PlaceholderParser.Builder id(String id) {
        Preconditions.checkArgument(!id.contains(":") && !id.contains(" "), "The ID must not contain a color or space.");
        this.id = id;
        return this;
    }

    @Override
    public PlaceholderParser.Builder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public PlaceholderParser.Builder parser(Function<PlaceholderContext, Text> parser) {
        this.parser = parser;
        return this;
    }

    @Override
    public PlaceholderParser build() throws IllegalStateException {
        Preconditions.checkState(this.pluginContainer != null, "Plugin must be set");
        Preconditions.checkState(this.id != null, "ID must be set");
        Preconditions.checkState(this.parser != null, "Parser must be set");
        final String name = this.name == null ? this.id : this.name;
        final String pluginId = this.pluginContainer.getId() + ":" + this.id;
        return new SpongePlaceholderParser(pluginId, name, this.parser);
    }

    @Override
    public PlaceholderParser.Builder from(PlaceholderParser value) {
        Preconditions.checkState(value instanceof SpongePlaceholderParser, "Must be a SpongePlaceholderParser");
        final SpongePlaceholderParser spongePlaceholderParser = (SpongePlaceholderParser) value;
        final String[] id = spongePlaceholderParser.getId().split(":", 2);
        this.pluginContainer = Sponge.getPluginManager().getPlugin(id[0])
                .orElseThrow(() -> new IllegalStateException("Cannot get plugin starting with " + id[0]));
        this.id = id[1];
        this.name = spongePlaceholderParser.getName();
        this.parser = spongePlaceholderParser.getParser();
        return this;
    }

    @Override
    public PlaceholderParser.Builder reset() {
        this.pluginContainer = null;
        this.id = null;
        this.name = null;
        this.parser = null;
        return this;
    }

}
