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
package org.spongepowered.common.data.provider.block.entity;

import net.minecraft.tileentity.BannerTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.bridge.tileentity.BannerTileEntityBridge;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class BannerTileEntityBaseColorProvider extends GenericMutableDataProvider<BannerTileEntity, DyeColor> {

    public BannerTileEntityBaseColorProvider() {
        super(Keys.BANNER_BASE_COLOR.get());
    }

    @Override
    protected Optional<DyeColor> getFrom(BannerTileEntity dataHolder) {
        return Optional.of(((BannerTileEntityBridge) dataHolder).bridge$getBaseColor());
    }

    @Override
    protected boolean set(BannerTileEntity dataHolder, DyeColor value) {
        if (!dataHolder.getWorld().isRemote) {
            ((BannerTileEntityBridge) dataHolder).bridge$setBaseColor(value);
            return true;
        }
        return false;
    }
}
