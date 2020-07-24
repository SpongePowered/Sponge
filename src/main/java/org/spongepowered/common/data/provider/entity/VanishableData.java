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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class VanishableData {

    private VanishableData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(VanishableBridge.class)
                    .create(Keys.IS_INVISIBLE)
                        .get(VanishableBridge::bridge$isInvisible)
                        .set(VanishableBridge::bridge$setInvisible)
                    .create(Keys.VANISH)
                        .get(VanishableBridge::bridge$isVanished)
                        .setAnd((h, v) -> {
                            if (h instanceof Entity && ((Entity) h).world.isRemote) {
                                return false;
                            }
                            h.bridge$setVanished(v);
                            return true;
                        })
                    .create(Keys.VANISH_IGNORES_COLLISION)
                        .get(VanishableBridge::bridge$isUncollideable)
                        .setAnd((h, v) -> {
                            if (h instanceof Entity && ((Entity) h).world.isRemote) {
                                return false;
                            }
                            if (!h.bridge$isVanished()) {
                                return false;
                            }
                            h.bridge$setUncollideable(v);
                            return true;
                        })
                    .create(Keys.VANISH_PREVENTS_TARGETING)
                        .get(VanishableBridge::bridge$isUntargetable)
                        .setAnd((h, v) -> {
                            if (h instanceof Entity && ((Entity) h).world.isRemote) {
                                return false;
                            }
                            if (!h.bridge$isVanished()) {
                                return false;
                            }
                            h.bridge$setUntargetable(v);
                            return true;
                        });
    }
    // @formatter:on
}
