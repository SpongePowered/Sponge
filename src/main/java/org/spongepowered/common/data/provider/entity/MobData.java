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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.HandPreferences;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.accessor.world.entity.MobAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class MobData {

    private MobData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Mob.class)
                    .create(Keys.DOMINANT_HAND)
                        .get(h -> (HandPreference) (Object) h.getMainArm())
                        .set((h, v) -> h.setLeftHanded(v.equals(HandPreferences.LEFT.get())))
                    .create(Keys.TARGET_ENTITY)
                        .get(h -> (Living) h.getTarget())
                        .setAnd((h, v) -> {
                            if (!(v instanceof Living)) {
                                return false;
                            }
                            h.setTarget((LivingEntity) v);
                            return true;
                        })
                        .delete(h -> h.setTarget(null))
                .asMutable(MobAccessor.class)
                    .create(Keys.IS_AI_ENABLED)
                        .get(h -> !h.invoker$isNoAi())
                        .set((h, v) -> h.invoker$setNoAi(!v))
                    .create(Keys.IS_PERSISTENT)
                        .get(h -> ((Mob) h).isPersistenceRequired())
                        .set(MobAccessor::accessor$persistenceRequired);
    }
    // @formatter:on
}
