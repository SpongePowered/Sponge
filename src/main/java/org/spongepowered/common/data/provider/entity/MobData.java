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

import net.minecraft.entity.MobEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.HandPreferences;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.accessor.entity.MobEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class MobData {

    private MobData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(MobEntity.class)
                    .create(Keys.DOMINANT_HAND)
                        .get(h -> (HandPreference) (Object) h.getPrimaryHand())
                        .set((h, v) -> h.setLeftHanded(v.equals(HandPreferences.LEFT.get())))
                    .create(Keys.LEASH_HOLDER)
                        .get(h -> ((Entity) h.getLeashHolder()))
                        .set((h, v) -> h.setLeashHolder(h, true))
                .asMutable(MobEntityAccessor.class)
                    .create(Keys.IS_AI_ENABLED)
                        .get(h -> !h.accessor$isAIDisabled())
                        .set((h, v) -> h.accessor$setNoAI(!v))
                    .create(Keys.IS_PERSISTENT)
                        .get(h -> ((MobEntity) h).isNoDespawnRequired())
                        .set(MobEntityAccessor::accessor$setPersistingRequired);
    }
    // @formatter:on
}
