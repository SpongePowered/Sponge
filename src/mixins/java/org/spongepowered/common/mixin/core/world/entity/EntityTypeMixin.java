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
package org.spongepowered.common.mixin.core.world.entity;

import co.aikar.timings.Timing;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.entity.EntityTypeBridge;
import co.aikar.timings.sponge.SpongeTimings;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements EntityTypeBridge {

    private boolean impl$isActivationRangeInitialized = false;
    private boolean impl$hasCheckedDamageEntity = false;
    private boolean impl$overridesDamageEntity = false;
    private Timing impl$timings;

    @Override
    public boolean bridge$isActivationRangeInitialized() {
        return this.impl$isActivationRangeInitialized;
    }

    @Override
    public void bridge$setActivationRangeInitialized(final boolean activationRangeInitialized) {
        this.impl$isActivationRangeInitialized = activationRangeInitialized;
    }

    @Override
    public boolean bridge$checkedDamageEntity() {
        return this.impl$hasCheckedDamageEntity;
    }

    @Override
    public void bridge$setCheckedDamageEntity(final boolean checkedDamageEntity) {
        this.impl$hasCheckedDamageEntity = checkedDamageEntity;
    }

    @Override
    public boolean bridge$overridesDamageEntity() {
        return this.impl$overridesDamageEntity;
    }

    @Override
    public void bridge$setOverridesDamageEntity(final boolean damagesEntity) {
        this.impl$overridesDamageEntity = damagesEntity;
    }

    @Override
    public Timing bridge$getTimings() {
        if (this.impl$timings == null) {
            this.impl$timings = SpongeTimings.getEntityTiming((EntityType) (Object) this);
        }
        return this.impl$timings;
    }


}
