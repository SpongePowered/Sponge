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
package org.spongepowered.common.data.processor.dual.entity;

import net.minecraft.entity.EntityLiving;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableLeashData;
import org.spongepowered.api.data.manipulator.mutable.entity.LeashData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeLeashData;
import org.spongepowered.common.data.processor.dual.common.AbstractSingleTargetDualProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class LeashDualProcessor extends AbstractSingleTargetDualProcessor<EntityLiving, Entity, Value<Entity>, LeashData, ImmutableLeashData> {

    public LeashDualProcessor() {
        super(EntityLiving.class, Keys.LEASH_HOLDER);
    }

    @Override
    protected Value<Entity> constructValue(Entity actualValue) {
        return SpongeValueFactory.getInstance().createValue(Keys.LEASH_HOLDER, actualValue);
    }

    @Override
    protected boolean set(EntityLiving entity, Entity value) {
        entity.setLeashedToEntity((net.minecraft.entity.Entity) value, true);
        return true;
    }

    @Override
    protected Optional<Entity> getVal(EntityLiving entity) {
        return Optional.ofNullable((Entity) entity.getLeashedToEntity());
    }

    @Override
    protected ImmutableValue<Entity> constructImmutableValue(Entity value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected LeashData createManipulator() {
        return new SpongeLeashData(null);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if(supports(container)) {
            EntityLiving living = (EntityLiving) container;
            living.setLeashedToEntity(null, true);
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }
}
