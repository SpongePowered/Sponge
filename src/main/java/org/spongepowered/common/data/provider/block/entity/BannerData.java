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

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.tileentity.BannerTileEntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class BannerData {

    private BannerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(BannerBlockEntity.class)
                    .create(Keys.BANNER_PATTERN_LAYERS)
                        .get(h -> ((BannerTileEntityBridge) h).bridge$getLayers())
                        .setAnd((h, v) -> {
                            final Level world = h.getLevel();
                            if (world != null && !world.isClientSide) { // This avoids a client crash because clientside.
                                ((BannerTileEntityBridge) h).bridge$setLayers(v);
                                return true;
                            }
                            return false;
                        })
                    .create(Keys.DYE_COLOR)
                        .get(h -> ((BannerTileEntityBridge) h).bridge$getBaseColor())
                        .setAnd((h, v) -> {
                            final Level world = h.getLevel();
                            if (world != null && !world.isClientSide) {
                                ((BannerTileEntityBridge) h).bridge$setBaseColor(v);
                                return true;
                            }
                            return false;
                        });
    }
    // @formatter:on
}
