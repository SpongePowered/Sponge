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
package org.spongepowered.common.mixin.core.tileentity;

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.api.block.tileentity.carrier.Beacon;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@NonnullByDefault
@Mixin(TileEntityBeacon.class)
public abstract class MixinTileEntityBeacon extends MixinTileEntityLockable implements Beacon {

    @Shadow private int primaryEffect;
    @Shadow private int secondaryEffect;
    @Shadow private int levels;

    @Override
    public int getCompletedLevels() {
        return this.levels < 0 ? 0 : this.levels;
    }

    /**
     * @author gabizou - March 4th, 2016
     *
     * Bypass the vanilla check that sprouted between 1.8 and 1.8.8 such that it
     * prevented any non-vanilla beacon defined potions from being applied
     * to a beacon. This method is used for both setfield and when reading from nbt.
     */
    @Overwrite
    private int func_183001_h(int p_183001_1_) {
        if (p_183001_1_ >= 0 && p_183001_1_ < Potion.potionTypes.length && Potion.potionTypes[p_183001_1_] != null) {
            Potion potion = Potion.potionTypes[p_183001_1_];
            return potion == null ? 0 : p_183001_1_;
        } else {
            return 0;
        }
    }


    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(of("effect1"), getField(1));
        container.set(of("effect2"), getField(2));
        return container;
    }

    @Override
    public void sendDataToContainer(DataView dataView) {
        dataView.set(of("effect1"), getField(1));
        dataView.set(of("effect2"), getField(2));
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getBeaconData());
    }
}
