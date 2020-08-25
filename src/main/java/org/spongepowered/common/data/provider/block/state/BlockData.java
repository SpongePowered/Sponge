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
package org.spongepowered.common.data.provider.block.state;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.NoteBlockInstrument;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.MatterStates;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.common.accessor.block.BlockAccessor;
import org.spongepowered.common.accessor.block.FireBlockAccessor;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.Collections;

public final class BlockData {

    private BlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.BLAST_RESISTANCE)
                        .get(h -> (double) ((BlockAccessor) h.getBlock()).accessor$getBlockResistance())
                    .create(Keys.CONNECTED_DIRECTIONS)
                        .get(h -> {
                            if (h.get(ChestBlock.TYPE) == ChestType.SINGLE) {
                                return null;
                            }
                            return Collections.singleton(Constants.DirectionFunctions.getFor(ChestBlock.getDirectionToAttached(h)));
                        })
                    .create(Keys.HARDNESS)
                        .get(h -> (double) ((BlockAccessor) h.getBlock()).accessor$getBlockHardness())
                    .create(Keys.HELD_ITEM)
                        .get(h -> {
                            final Item item = h.getBlock().asItem();
                            if (item instanceof BlockItem) {
                                return (ItemType) item;
                            }
                            return null;
                        })
                        .supports(h -> h.getBlock().asItem() instanceof BlockItem)
                    .create(Keys.IS_GRAVITY_AFFECTED)
                        .get(h -> h.getBlock() instanceof FallingBlock)
                    .create(Keys.IS_PASSABLE)
                        .get(h -> !h.getMaterial().blocksMovement())
                    .create(Keys.IS_UNBREAKABLE)
                        .get(h -> ((BlockAccessor) h.getBlock()).accessor$getBlockHardness() < 0)
                    .create(Keys.IS_FLAMMABLE)
                        .get(h -> ((FireBlockAccessor) Blocks.FIRE).accessor$func_220274_q(h) > 0)
                    .create(Keys.IS_SOLID)
                        .get(h -> h.getMaterial().isSolid())
                    .create(Keys.IS_REPLACEABLE)
                        .get(h -> h.getMaterial().isReplaceable())
                    .create(Keys.IS_SURROGATE_BLOCK)
                        .get(h -> ((BlockBridge) h.getBlock()).bridge$isDummy())
                    .create(Keys.LIGHT_EMISSION)
                        .get(BlockState::getLightValue)
                    .create(Keys.MATTER_STATE)
                        .get(h -> {
                            if (h.getBlock() instanceof FlowingFluidBlock) {
                                return MatterStates.LIQUID.get();
                            } else if (h.getMaterial() == Material.AIR) {
                                return MatterStates.GAS.get();
                            } else {
                                return MatterStates.SOLID.get();
                            }
                        })
                    .create(Keys.REPRESENTED_INSTRUMENT)
                        .get(h -> (InstrumentType) (Object) NoteBlockInstrument.byState(h))
                    .create(Keys.WOOD_TYPE)
                        .get(h -> {
                            throw new MissingImplementationException("BlockStateData", "WOOD_TYPE::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("BlockStateData", "WOOD_TYPE::setter");
                        });
    }
    // @formatter:on
}
