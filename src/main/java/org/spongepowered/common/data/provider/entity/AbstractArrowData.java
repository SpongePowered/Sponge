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

import net.minecraft.entity.projectile.AbstractArrowEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PickupRule;
import org.spongepowered.common.accessor.entity.projectile.AbstractArrowEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class AbstractArrowData {

    private AbstractArrowData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AbstractArrowEntity.class)
                    .create(Keys.ATTACK_DAMAGE)
                        .get(AbstractArrowEntity::getBaseDamage)
                        .set(AbstractArrowEntity::setBaseDamage)
                    .create(Keys.IS_CRITICAL_HIT)
                        .get(AbstractArrowEntity::isCritArrow)
                        .set(AbstractArrowEntity::setCritArrow)
                    .create(Keys.KNOCKBACK_STRENGTH)
                        .get(h -> (double) ((AbstractArrowEntityAccessor) h).accessor$knockback())
                        .set((h, v) -> h.setKnockback((int) Math.round(v)))
                    .create(Keys.PICKUP_RULE)
                        .get(h -> (PickupRule) (Object) h.pickup)
                        .set((h, v) -> h.pickup = ((AbstractArrowEntity.PickupStatus) (Object) v));
    }
    // @formatter:on
}
