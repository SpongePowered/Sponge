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
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.common.accessor.block.AbstractBlockAccessor;
import org.spongepowered.common.accessor.block.AbstractBlock_PropertiesAccessor;
import org.spongepowered.common.accessor.block.AbstractFireBlockAccessor;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.block.DyeColorBlockBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

import java.util.Collections;

public final class BlockData {

    private BlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.BLAST_RESISTANCE)
                        .get(h -> (double) ((AbstractBlock_PropertiesAccessor) h.getBlock()).accessor$explosionResistance())
                    .create(Keys.CONNECTED_DIRECTIONS)
                        .get(h -> {
                            if (h.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                                return null;
                            }
                            return Collections.singleton(Constants.DirectionFunctions.getFor(ChestBlock.getConnectedDirection(h)));
                        })
                        .supports(h -> h.hasProperty(ChestBlock.TYPE))
                    .create(Keys.DYE_COLOR)
                        .get(h -> ((DyeColorBlockBridge) h.getBlock()).bridge$getDyeColor().orElse(null))
                        .supports(h -> h.getBlock() instanceof DyeColorBlockBridge)
                    .create(Keys.DESTROY_SPEED)
                        .get(state -> (double) ((AbstractBlock_PropertiesAccessor) ((AbstractBlockAccessor) state.getBlock()).accessor$properties()).accessor$destroyTime())
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
                        .get(h -> !h.getMaterial().blocksMotion())
                    .create(Keys.IS_UNBREAKABLE)
                        .get(h -> ((AbstractBlock_PropertiesAccessor) h.getBlock()).accessor$destroyTime() < 0)
                    .create(Keys.IS_FLAMMABLE)
                        .get(((AbstractFireBlockAccessor) Blocks.FIRE)::invoker$canBurn)
                    .create(Keys.IS_SOLID)
                        .get(h -> h.getMaterial().isSolid())
                    .create(Keys.IS_REPLACEABLE)
                        .get(h -> h.getMaterial().isReplaceable())
                    .create(Keys.IS_SURROGATE_BLOCK)
                        .get(h -> ((BlockBridge) h.getBlock()).bridge$isDummy())
                    .create(Keys.LIGHT_EMISSION)
                        .get(BlockState::getLightEmission)
                    .create(Keys.MATTER_TYPE)
                        .get(h -> {
                            if (h.getBlock() instanceof FlowingFluidBlock) {
                                return MatterTypes.LIQUID.get();
                            } else if (h.getMaterial() == Material.AIR) {
                                return MatterTypes.GAS.get();
                            } else {
                                return MatterTypes.SOLID.get();
                            }
                        })
                    .create(Keys.REPRESENTED_INSTRUMENT)
                        .get(h -> (InstrumentType) (Object) NoteBlockInstrument.byState(h));
    }
    // @formatter:on
}
