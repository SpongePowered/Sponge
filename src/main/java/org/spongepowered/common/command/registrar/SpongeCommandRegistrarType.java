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
package org.spongepowered.common.command.registrar;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.CommandRegistrarType;

import java.util.function.Function;

public final class SpongeCommandRegistrarType<V> implements CommandRegistrarType<V> {
    private final TypeToken<V> handledType;
    private final Function<CommandManager.Mutable, CommandRegistrar<V>> supplier;

    public SpongeCommandRegistrarType(
            final TypeToken<V> handledType,
            final Function<CommandManager.Mutable, CommandRegistrar<V>> supplier
    ) {
        this.handledType = handledType;
        this.supplier = supplier;
    }

    public SpongeCommandRegistrarType(
            final Class<V> handledType,
            final Function<CommandManager.Mutable, CommandRegistrar<V>> supplier
    ) {
        this(TypeToken.get(handledType), supplier);
    }

    @Override
    public @NotNull TypeToken<V> handledType() {
        return this.handledType;
    }

    @Override
    public @NotNull CommandRegistrar<V> create(final CommandManager.@NotNull Mutable manager) {
        return this.supplier.apply(manager);
    }
}
