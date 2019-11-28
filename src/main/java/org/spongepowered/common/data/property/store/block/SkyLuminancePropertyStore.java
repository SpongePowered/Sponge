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

import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.property.store.common.AbstractSpongePropertyStore;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class SkyLuminancePropertyStore extends AbstractSpongePropertyStore<SkyLuminanceProperty> {

    @Override
    public Optional<SkyLuminanceProperty> getFor(PropertyHolder propertyHolder) {
        if (propertyHolder instanceof Location && ((Location<?>) propertyHolder).getExtent() instanceof Chunk) {
            final Chunk chunk = (Chunk) ((Location<?>) propertyHolder).getExtent();
            final float light = chunk.getLightFor(LightType.SKY, VecHelper.toBlockPos((Location<?>) propertyHolder));
            return Optional.of(new SkyLuminanceProperty(light));
        }
        return super.getFor(propertyHolder);
    }

    @Override
    public Optional<SkyLuminanceProperty> getFor(Location<World> location) {
        final net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        final float light = world.getLightFor(LightType.SKY, VecHelper.toBlockPos(location));
        return Optional.of(new SkyLuminanceProperty(light));
    }

    @Override
    public Optional<SkyLuminanceProperty> getFor(Location<World> location, Direction direction) {
        // TODO gabziou fix this
        return Optional.empty();
    }
}
