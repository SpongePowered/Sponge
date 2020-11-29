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

import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.FoxType;
import org.spongepowered.common.accessor.entity.passive.FoxEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.Optional;

public final class FoxData {

    private FoxData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(FoxEntity.class)
                    .create(Keys.FIRST_TRUSTED)
                        .get(h -> h.getDataManager().get(FoxEntityAccessor.accessor$getTrustedUuidMain()).orElse(null))
                        .set((h, v) -> h.getDataManager().set(FoxEntityAccessor.accessor$getTrustedUuidMain(), Optional.ofNullable(v)))
                    .create(Keys.FOX_TYPE)
                        .get(h -> (FoxType) (Object) h.getVariantType())
                        .set((h, v) -> ((FoxEntityAccessor) h).accessor$setVariantType((FoxEntity.Type) (Object) v))
                    .create(Keys.IS_CROUCHING)
                        .get(FoxEntity::isCrouching)
                        .set(FoxEntity::setCrouching)
                    .create(Keys.IS_DEFENDING)
                        .get(h -> ((FoxEntityAccessor) h).accessor$isFoxAggroed())
                        .set((h, v) -> ((FoxEntityAccessor) h).accessor$setFoxAggroed(v))
                    .create(Keys.IS_FACEPLANTED)
                        .get(FoxEntity::isStuck)
                        .set((h, v) -> ((FoxEntityAccessor) h).accessor$setStuck(v))
                    .create(Keys.IS_INTERESTED)
                        .get(FoxEntity::func_213467_eg)
                        .set(FoxEntity::func_213502_u)
                    .create(Keys.IS_POUNCING)
                        .get(FoxEntity::func_213480_dY)
                        .set(FoxEntity::func_213461_s)
                    .create(Keys.IS_SLEEPING)
                        .get(FoxEntity::isSleeping)
                        .set((h, v) -> ((FoxEntityAccessor) h).accessor$setSleeping(v))
                    .create(Keys.SECOND_TRUSTED)
                        .get(h -> h.getDataManager().get(FoxEntityAccessor.accessor$getTrustedUuidSecondary()).orElse(null))
                        .set((h, v) -> h.getDataManager().set(FoxEntityAccessor.accessor$getTrustedUuidSecondary(), Optional.ofNullable(v)));
    }
    // @formatter:on
}
