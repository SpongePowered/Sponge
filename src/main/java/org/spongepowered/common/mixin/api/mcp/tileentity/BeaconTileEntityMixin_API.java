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
package org.spongepowered.common.mixin.api.mcp.tileentity;

import net.minecraft.tileentity.BeaconTileEntity;
import org.spongepowered.api.block.entity.carrier.Beacon;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

import java.util.Set;

@Mixin(BeaconTileEntity.class)
public abstract class BeaconTileEntityMixin_API extends TileEntityMixin_API implements Beacon {

    @Shadow private int levels;

    @Shadow public abstract int getField(int id);

    @Override
    public int getCompletedLevels() {
        return this.levels < 0 ? 0 : this.levels;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(Constants.TileEntity.Beacon.PRIMARY, this.getField(1));
        container.set(Constants.TileEntity.Beacon.SECONDARY, this.getField(2));
        return container;
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // NameableBlockEntity
        values.add(this.displayName().asImmutable());

        this.primaryEffect().map(Value::asImmutable).ifPresent(values::add);
        this.secondaryEffect().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
