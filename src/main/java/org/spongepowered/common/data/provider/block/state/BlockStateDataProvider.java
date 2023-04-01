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

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.api.data.BlockStateKeys;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class BlockStateDataProvider {

    private BlockStateDataProvider() {
    }

    public static void register(final DataProviderRegistrator registrator) {
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.ATTACHED, BlockStateProperties.ATTACHED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.BOTTOM, BlockStateProperties.BOTTOM);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.CONDITIONAL, BlockStateProperties.CONDITIONAL);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.DISARMED, BlockStateProperties.DISARMED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.DRAG, BlockStateProperties.DRAG);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.ENABLED, BlockStateProperties.ENABLED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.EXTENDED, BlockStateProperties.EXTENDED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.EYE, BlockStateProperties.EYE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.FALLING, BlockStateProperties.FALLING);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HANGING, BlockStateProperties.HANGING);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HAS_BOTTLE_0, BlockStateProperties.HAS_BOTTLE_0);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HAS_BOTTLE_1, BlockStateProperties.HAS_BOTTLE_1);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HAS_BOTTLE_2, BlockStateProperties.HAS_BOTTLE_2);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HAS_RECORD, BlockStateProperties.HAS_RECORD);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HAS_BOOK, BlockStateProperties.HAS_BOOK);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.INVERTED, BlockStateProperties.INVERTED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.IN_WALL, BlockStateProperties.IN_WALL);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LIT, BlockStateProperties.LIT);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LOCKED, BlockStateProperties.LOCKED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.OCCUPIED, BlockStateProperties.OCCUPIED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.OPEN, BlockStateProperties.OPEN);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.PERSISTENT, BlockStateProperties.PERSISTENT);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.POWERED, BlockStateProperties.POWERED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.SHORT, BlockStateProperties.SHORT);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.SIGNAL_FIRE, BlockStateProperties.SIGNAL_FIRE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.SNOWY, BlockStateProperties.SNOWY);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.TRIGGERED, BlockStateProperties.TRIGGERED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.UNSTABLE, BlockStateProperties.UNSTABLE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.WATERLOGGED, BlockStateProperties.WATERLOGGED);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.VINE_END, BlockStateProperties.VINE_END);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HORIZONTAL_AXIS, BlockStateProperties.HORIZONTAL_AXIS);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AXIS, BlockStateProperties.AXIS);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.UP, BlockStateProperties.UP);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.DOWN, BlockStateProperties.DOWN);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.NORTH, BlockStateProperties.NORTH);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.EAST, BlockStateProperties.EAST);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.SOUTH, BlockStateProperties.SOUTH);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.WEST, BlockStateProperties.WEST);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.FACING, BlockStateProperties.FACING);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.FACING_HOPPER, BlockStateProperties.FACING_HOPPER);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HORIZONTAL_FACING, BlockStateProperties.HORIZONTAL_FACING);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.ORIENTATION, BlockStateProperties.ORIENTATION);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.ATTACH_FACE, BlockStateProperties.ATTACH_FACE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.BELL_ATTACHMENT, BlockStateProperties.BELL_ATTACHMENT);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.EAST_WALL, BlockStateProperties.EAST_WALL);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.NORTH_WALL, BlockStateProperties.NORTH_WALL);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.SOUTH_WALL, BlockStateProperties.SOUTH_WALL);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.WEST_WALL, BlockStateProperties.WEST_WALL);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.EAST_REDSTONE, BlockStateProperties.EAST_REDSTONE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.NORTH_REDSTONE, BlockStateProperties.NORTH_REDSTONE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.SOUTH_REDSTONE, BlockStateProperties.SOUTH_REDSTONE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.WEST_REDSTONE, BlockStateProperties.WEST_REDSTONE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.DOUBLE_BLOCK_HALF, BlockStateProperties.DOUBLE_BLOCK_HALF);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HALF, BlockStateProperties.HALF);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.RAIL_SHAPE, BlockStateProperties.RAIL_SHAPE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.RAIL_SHAPE_STRAIGHT, BlockStateProperties.RAIL_SHAPE_STRAIGHT);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AGE_1, BlockStateProperties.AGE_1);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AGE_2, BlockStateProperties.AGE_2);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AGE_3, BlockStateProperties.AGE_3);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AGE_5, BlockStateProperties.AGE_5);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AGE_7, BlockStateProperties.AGE_7);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AGE_15, BlockStateProperties.AGE_15);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.AGE_25, BlockStateProperties.AGE_25);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.BITES, BlockStateProperties.BITES);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.DELAY, BlockStateProperties.DELAY);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.DISTANCE, BlockStateProperties.DISTANCE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.EGGS, BlockStateProperties.EGGS);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.HATCH, BlockStateProperties.HATCH);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LAYERS, BlockStateProperties.LAYERS);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LEVEL_CAULDRON, BlockStateProperties.LEVEL_CAULDRON);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LEVEL_COMPOSTER, BlockStateProperties.LEVEL_COMPOSTER);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LEVEL_FLOWING, BlockStateProperties.LEVEL_FLOWING);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LEVEL_HONEY, BlockStateProperties.LEVEL_HONEY);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.LEVEL, BlockStateProperties.LEVEL);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.MOISTURE, BlockStateProperties.MOISTURE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.NOTE, BlockStateProperties.NOTE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.PICKLES, BlockStateProperties.PICKLES);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.POWER, BlockStateProperties.POWER);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.STAGE, BlockStateProperties.STAGE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.STABILITY_DISTANCE, BlockStateProperties.STABILITY_DISTANCE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.RESPAWN_ANCHOR_CHARGES, BlockStateProperties.RESPAWN_ANCHOR_CHARGES);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.ROTATION_16, BlockStateProperties.ROTATION_16);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.BED_PART, BlockStateProperties.BED_PART);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.CHEST_TYPE, BlockStateProperties.CHEST_TYPE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.MODE_COMPARATOR, BlockStateProperties.MODE_COMPARATOR);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.DOOR_HINGE, BlockStateProperties.DOOR_HINGE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.NOTEBLOCK_INSTRUMENT, BlockStateProperties.NOTEBLOCK_INSTRUMENT);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.PISTON_TYPE, BlockStateProperties.PISTON_TYPE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.SLAB_TYPE, BlockStateProperties.SLAB_TYPE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.STAIRS_SHAPE, BlockStateProperties.STAIRS_SHAPE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.STRUCTUREBLOCK_MODE, BlockStateProperties.STRUCTUREBLOCK_MODE);
        BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.BAMBOO_LEAVES, BlockStateProperties.BAMBOO_LEAVES);
    }

    // @formatter:off
    private static <T, V extends Comparable<V>> void registerProperty(final DataProviderRegistrator registrator, final Key<Value<T>> key, final Property<V> property) {

        registrator.asImmutable(BlockState.class)
                .create(key)
                    .supports(bs -> bs.getOptionalValue(property).isPresent())
                    .get(bs -> (T) bs.getOptionalValue(property).orElse(null))
                    .set((bs, v) -> bs.setValue(property, (V) v));
    }
    // @formatter:on

}
