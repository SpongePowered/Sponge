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
package org.spongepowered.common.block;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.Registries;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.common.SpongeCommon;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class BlockStateSerializerDeserializer {

    public static String serialize(final BlockState state) {
        return BlockStateParser.serialize((net.minecraft.world.level.block.state.BlockState) state);
    }

    public static Optional<BlockState> deserialize(final String string) {
        final String state = Objects.requireNonNull(string, "Id cannot be null!").toLowerCase(Locale.ENGLISH);
        try {
            final BlockStateParser.BlockResult result = BlockStateParser.parseForBlock(SpongeCommon.vanillaRegistry(Registries.BLOCK), state, true);
            return Optional.of((BlockState) result.blockState());
        } catch (final CommandSyntaxException e) {
            return Optional.empty();
        }
    }
}
