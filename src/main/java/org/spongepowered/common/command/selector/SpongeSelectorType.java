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
package org.spongepowered.common.command.selector;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.EntitySelectorParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.common.accessor.command.arguments.EntitySelectorParserAccessor;

public final class SpongeSelectorType implements SelectorType {

    private final String selectorToken;
    private final ResourceKey resourceKey;
    private final Selector selector;

    public SpongeSelectorType(
            final String selectorToken,
            final ResourceKey resourceKey) {
        this.selectorToken = selectorToken;
        this.resourceKey = resourceKey;
        try {
            this.selector = (Selector) new EntitySelectorParser(new StringReader(this.selectorToken)).parse();
        } catch (final CommandSyntaxException exception) {
            // This should never happen, if it does, it's a bug in our code.
            throw new RuntimeException(exception);
        }
    }

    @Override
    @NonNull
    public final String selectorToken() {
        return this.selectorToken;
    }

    @Override
    @NonNull
    public final Selector toSelector() {
        return this.selector;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public final Selector.@NonNull Builder toBuilder() {
        final EntitySelectorParser parser = new EntitySelectorParser(new StringReader(this.selectorToken));
        ((EntitySelectorParserAccessor) parser).accessor$parseSelector();
        return (Selector.Builder) parser;
    }

    @Override
    @NonNull
    public final ResourceKey getKey() {
        return this.resourceKey;
    }

}
