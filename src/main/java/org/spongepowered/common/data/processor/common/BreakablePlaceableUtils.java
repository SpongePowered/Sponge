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
package org.spongepowered.common.data.processor.common;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.Set;

public final class BreakablePlaceableUtils {

    public static boolean set(final ItemStack stack, final String nbtKey, final Set<? extends BlockType> value) {
        CompoundNBT stackTag = stack.func_77978_p();

        if (value.isEmpty()) {
            if (stackTag != null) {
                stackTag.func_82580_o(nbtKey);
                if (stackTag.func_82582_d()) {
                    stack.func_77982_d(null);
                }
            }
        } else {
            final ListNBT breakableIds = new ListNBT();
            for (final BlockType breakable : value) {
                String id = breakable.getId();
                if (id.startsWith("minecraft:")) {
                    id = id.substring("minecraft:".length());
                }
                breakableIds.func_74742_a(new StringNBT(id));
            }
            if (stackTag == null) {
                stackTag = new CompoundNBT();
                stack.func_77982_d(stackTag);
            }
            stackTag.func_74782_a(nbtKey, breakableIds);
        }

        return true;
    }

    public static Optional<Set<BlockType>> get(final ItemStack stack, final String nbtKey) {
        final CompoundNBT tag = stack.func_77978_p();
        if (tag == null) {
            return Optional.empty();
        }
        final ListNBT blockIds = tag.func_150295_c(nbtKey, Constants.NBT.TAG_STRING);
        if (blockIds.func_82582_d()) {
            return Optional.empty();
        }
        final Set<BlockType> blockTypes = Sets.newHashSetWithExpectedSize(blockIds.func_74745_c());
        for (int i = 0; i < blockIds.func_74745_c(); i++) {
            BlockTypeRegistryModule.getInstance().getById(blockIds.func_150307_f(i)).ifPresent(blockTypes::add);
        }
        return Optional.of(blockTypes);
    }

}
