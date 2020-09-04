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

import net.minecraft.tileentity.SkullTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.tileentity.SkullTileEntityAccessor;
import org.spongepowered.common.bridge.tileentity.SkullTileEntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.profile.SpongeGameProfile;

public final class SkullData {

    private SkullData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(SkullTileEntity.class)
                    .create(Keys.GAME_PROFILE)
                        .get(h -> SpongeGameProfile.of(((SkullTileEntityAccessor) h).accessor$getPlayerProfile()))
                        .set((h, v) -> ((SkullTileEntityBridge) h).bridge$setUnresolvedPlayerProfile(SpongeGameProfile.toMcProfile(v)))
                        .delete(h -> ((SkullTileEntityBridge) h).bridge$setUnresolvedPlayerProfile(null));
    }
    // @formatter:on
}
