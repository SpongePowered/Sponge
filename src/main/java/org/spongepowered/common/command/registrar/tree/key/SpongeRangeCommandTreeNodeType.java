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
package org.spongepowered.common.command.registrar.tree.key;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeType;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.command.registrar.tree.builder.RangeCommandTreeNode;

import java.util.function.BiFunction;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.LongArgumentSerializer;

public final class SpongeRangeCommandTreeNodeType<N extends Number> extends AbstractResourceKeyed implements CommandTreeNodeType<CommandTreeNode.@NonNull Range<@NonNull N>> {

    public static @Nullable SpongeRangeCommandTreeNodeType<?> createFrom(final ResourceKey key, final ArgumentSerializer<?> serializer) {
        if (serializer instanceof FloatArgumentSerializer) {
            return new SpongeRangeCommandTreeNodeType<>(key, FloatArgumentType::floatArg, Float.MIN_VALUE, Float.MAX_VALUE);
        }
        if (serializer instanceof DoubleArgumentSerializer) {
            return new SpongeRangeCommandTreeNodeType<>(key, DoubleArgumentType::doubleArg, Double.MIN_VALUE, Double.MAX_VALUE);
        }
        if (serializer instanceof IntegerArgumentSerializer) {
            return new SpongeRangeCommandTreeNodeType<>(key, IntegerArgumentType::integer, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        if (serializer instanceof LongArgumentSerializer) {
            return new SpongeRangeCommandTreeNodeType<>(key, LongArgumentType::longArg, Long.MIN_VALUE, Long.MAX_VALUE);
        }
        return null;
    }

    private final BiFunction<N, N, ArgumentType<?>> typeCreator;
    private final N min;
    private final N max;

    private SpongeRangeCommandTreeNodeType(final ResourceKey key, final BiFunction<N, N, ArgumentType<?>> typeCreator, final N min, final N max) {
        super(key);

        this.typeCreator = typeCreator;
        this.min = min;
        this.max = max;
    }

    @Override
    public CommandTreeNode.@NonNull Range<@NonNull N> createNode() {
        return new RangeCommandTreeNode<>(this, this.typeCreator, this.min, this.max);
    }

}
