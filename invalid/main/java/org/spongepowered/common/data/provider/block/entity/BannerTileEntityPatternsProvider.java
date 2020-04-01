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
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.common.bridge.tileentity.BannerTileEntityBridge;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.List;
import java.util.Optional;

public class BannerTileEntityPatternsProvider extends GenericMutableDataProvider<BannerTileEntity, List<PatternLayer>> {

    public BannerTileEntityPatternsProvider() {
        super(Keys.BANNER_PATTERNS);
    }

    @Override
    protected Optional<List<PatternLayer>> getFrom(BannerTileEntity dataHolder) {
        return Optional.of(((BannerTileEntityBridge) dataHolder).bridge$getLayers());
    }

    @Override
    protected boolean set(BannerTileEntity dataHolder, List<PatternLayer> value) {
        @Nullable final World world = dataHolder.getWorld();
        if (world != null && !world.isRemote) { // This avoids a client crash because clientside.
            ((BannerTileEntityBridge) dataHolder).bridge$setLayers(value);
            return true;
        }
        return false;
    }
}
