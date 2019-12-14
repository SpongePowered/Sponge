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

import org.spongepowered.api.block.entity.PlayerHead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.mutable.SpongeSkullData;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import net.minecraft.tileentity.SkullTileEntity;

@Mixin(SkullTileEntity.class)
public abstract class SkullTileEntityMixin_API extends TileEntityMixin_API implements PlayerHead {

    @Override
    public SkullData getSkullData() {
        return new SpongeSkullData(SkullUtils.getSkullType(((SkullTileEntity) (Object) this).getSkullType()));
    }

    @Override
    public Mutable<SkullType> skullType() {
        return new SpongeValue<>(Keys.SKULL_TYPE, Constants.TileEntity.Skull.DEFAULT_TYPE,
            SkullUtils.getSkullType(((SkullTileEntity) (Object) this).getSkullType()));
    }

    @Override
    public void supplyVanillaManipulators(List<org.spongepowered.api.data.DataManipulator.Mutable<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(this.getSkullData());
        Optional<RepresentedPlayerData> profileData = this.get(RepresentedPlayerData.class);
        if (profileData.isPresent()) {
            manipulators.add(profileData.get());
        }
    }


}
