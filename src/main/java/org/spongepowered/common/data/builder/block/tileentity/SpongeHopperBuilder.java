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
package org.spongepowered.common.data.builder.block.tileentity;

import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.mixin.core.tileentity.TileEntityHopperAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.tileentity.HopperTileEntity;

public class SpongeHopperBuilder extends SpongeLockableBuilder<Hopper> {

    public SpongeHopperBuilder() {
        super(Hopper.class, 1);
    }

    @Override
    protected Optional<Hopper> buildContent(final DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(hopper -> {
            if (container.contains(Constants.TileEntity.CUSTOM_NAME)) {
                ((HopperTileEntity) hopper).setCustomName(container.getString(Constants.TileEntity.CUSTOM_NAME).get());
            }
            if (!container.contains(Keys.COOLDOWN.getQuery())) {
                ((HopperTileEntity) hopper).remove();
                return Optional.empty();
            }
            ((TileEntityHopperAccessor) hopper).accessor$setTransferCooldown(container.getInt(Keys.COOLDOWN.getQuery()).get());
            ((HopperTileEntity) hopper).validate();
            return Optional.of(hopper);
        });
    }
}
