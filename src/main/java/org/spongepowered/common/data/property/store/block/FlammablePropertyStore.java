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
package org.spongepowered.common.data.property.store.block;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.data.property.block.FlammableProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.property.store.common.AbstractSpongePropertyStore;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class FlammablePropertyStore extends AbstractSpongePropertyStore<FlammableProperty> {

    private static final FlammableProperty TRUE = new FlammableProperty(true);
    private static final FlammableProperty FALSE = new FlammableProperty(false);

    @Override
    public Optional<FlammableProperty> getFor(Location<World> location) {
        final net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        final BlockPos pos = VecHelper.toBlockPos(location);
        final Block block = world.getBlockState(pos).getBlock();
        for (net.minecraft.util.Direction facing : net.minecraft.util.Direction.values()) {
            if (SpongeImplHooks.isBlockFlammable(block, world, pos, facing)) {
                return Optional.of(TRUE);
            }
        }
        return Optional.of(FALSE);
    }

    @Override
    public Optional<FlammableProperty> getFor(Location<World> location, Direction direction) {
        final net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        final net.minecraft.util.Direction facing = toEnumFacing(direction);
        final BlockPos pos = VecHelper.toBlockPos(location);
        final boolean flammable = SpongeImplHooks.isBlockFlammable(world.getBlockState(pos).getBlock(), world, pos, facing);
        return Optional.of(flammable ? TRUE : FALSE);
    }

}
