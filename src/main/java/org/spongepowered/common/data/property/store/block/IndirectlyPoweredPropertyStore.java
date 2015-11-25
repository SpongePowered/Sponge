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

import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.util.EnumFacing;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.block.IndirectlyPoweredProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.property.store.common.AbstractSpongePropertyStore;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class IndirectlyPoweredPropertyStore extends AbstractSpongePropertyStore<IndirectlyPoweredProperty> {

    private static final IndirectlyPoweredProperty TRUE = new IndirectlyPoweredProperty(true);
    private static final IndirectlyPoweredProperty FALSE = new IndirectlyPoweredProperty(false);

    @SuppressWarnings("unchecked")
    @Override
    public Optional<IndirectlyPoweredProperty> getFor(PropertyHolder propertyHolder) {
        if (propertyHolder instanceof Location) {
            if (((Location<?>) propertyHolder).getExtent() instanceof net.minecraft.world.World) {
                return getFor((Location<World>) propertyHolder);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<IndirectlyPoweredProperty> getFor(Location<World> location) {
        final net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        final boolean powered = world.isBlockIndirectlyGettingPowered(VecHelper.toBlockPos(location)) > 0;
        return Optional.of(powered ? TRUE : FALSE);
    }

    @Override
    public Optional<IndirectlyPoweredProperty> getFor(Location<World> location, Direction direction) {
        checkArgument(direction.isCardinal() || direction.isUpright(), "Direction must be a valid block face");
        final net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        final EnumFacing facing = DirectionFacingProvider.getInstance().get(direction).get();
        final boolean powered = world.getRedstonePower(VecHelper.toBlockPos(location).offset(facing), facing) > 0;
        return Optional.of(powered ? TRUE : FALSE);
    }
}
