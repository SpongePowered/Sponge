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

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.DirectionalUtil;

public final class WallData {

    private WallData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.CONNECTED_DIRECTIONS)
                        .get(h -> DirectionalUtil.getHorizontalUpFrom(h, WallBlock.EAST_WALL, WallBlock.WEST_WALL, WallBlock.NORTH_WALL, WallBlock.SOUTH_WALL, WallBlock.UP))
                        .set((h, v) -> DirectionalUtil.setHorizontalUpFor(h, v, WallBlock.EAST_WALL, WallBlock.WEST_WALL, WallBlock.NORTH_WALL, WallBlock.SOUTH_WALL, WallBlock.UP))
                        .supports(h -> h.getBlock() instanceof WallBlock)
                    .create(Keys.IS_CONNECTED_EAST)
                        .get(h -> h.getValue(WallBlock.EAST_WALL) != WallSide.NONE)
                        .set((h, v) -> WallData.setWallSideProperty(h, WallBlock.EAST_WALL, v))
                        .supports(h -> h.getBlock() instanceof WallBlock)
                    .create(Keys.IS_CONNECTED_NORTH)
                        .get(h -> h.getValue(WallBlock.NORTH_WALL) != WallSide.NONE)
                        .set((h, v) -> WallData.setWallSideProperty(h, WallBlock.NORTH_WALL, v))
                        .supports(h -> h.getBlock() instanceof WallBlock)
                    .create(Keys.IS_CONNECTED_SOUTH)
                        .get(h -> h.getValue(WallBlock.SOUTH_WALL) != WallSide.NONE)
                        .set((h, v) -> WallData.setWallSideProperty(h, WallBlock.SOUTH_WALL, v))
                        .supports(h -> h.getBlock() instanceof WallBlock)
                    .create(Keys.IS_CONNECTED_UP)
                        .get(h -> h.getValue(WallBlock.UP))
                        .set((h, v) -> h.setValue(WallBlock.UP, v))
                        .supports(h -> h.getBlock() instanceof WallBlock)
                    .create(Keys.IS_CONNECTED_WEST)
                        .get(h -> h.getValue(WallBlock.WEST_WALL) != WallSide.NONE)
                        .set((h, v) -> WallData.setWallSideProperty(h, WallBlock.WEST_WALL, v))
                        .supports(h -> h.getBlock() instanceof WallBlock)
                   .create(Keys.WALL_CONNECTION_STATES)
                        .get(h -> DirectionalUtil.getHorizontalFrom(h, WallBlock.EAST_WALL, WallBlock.WEST_WALL, WallBlock.NORTH_WALL, WallBlock.SOUTH_WALL))
                        .set((h, v) -> DirectionalUtil.setHorizontalFor(h, v, WallBlock.EAST_WALL, WallBlock.WEST_WALL, WallBlock.NORTH_WALL, WallBlock.SOUTH_WALL))
                        .supports(h -> h.getBlock() instanceof WallBlock)
        ;
    }
    // @formatter:on

    private static BlockState setWallSideProperty(final BlockState state, final EnumProperty<WallSide> property, final Boolean connected) {
        final WallSide wallSide = state.getValue(property);
        if (connected && wallSide != WallSide.NONE) {
            return state;
        }
        return state.setValue(property, connected ? WallSide.TALL : WallSide.NONE);}
}
