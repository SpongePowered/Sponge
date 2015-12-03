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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.common.ImmutableSpongeEntityValue;
import org.spongepowered.common.data.value.mutable.common.SpongeEntityValue;

import java.util.Optional;

public class TargetLivingValueProcessor extends AbstractSpongeValueProcessor<EntityLiving, Living, Value<Living>> {

    public TargetLivingValueProcessor() {
        super(EntityLiving.class, Keys.TARGET);
    }

    @Override
    protected Value<Living> constructValue(Living defaultValue) {
        return new SpongeEntityValue<>(Keys.TARGET, defaultValue);
    }

    @Override
    protected boolean set(EntityLiving container, Living value) {
        if (!supports(container) || !container.canAttackClass(value.getClass())) {
            return false;
        }

        container.setAttackTarget((EntityLivingBase) value);
        return true;
    }

    @Override
    protected Optional<Living> getVal(EntityLiving container) {
        return Optional.ofNullable((Living) container.getAttackTarget());
    }

    @Override
    protected ImmutableValue<Living> constructImmutableValue(Living value) {
        return new ImmutableSpongeEntityValue<>(Keys.TARGET, value);
    }

    @Override
    protected boolean supports(EntityLiving container) {
        return !(container instanceof EntityWither);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!supports(container)) {
            return DataTransactionResult.failNoData();
        }

        final Optional<Living> current = container.get(Keys.TARGET);
        DataTransactionResult result;
        if (current.isPresent()) {
            result = DataTransactionResult.builder().replace(constructImmutableValue(current.get())).build();
            ((EntityLiving) container).setAttackTarget(null);
        } else {
            result = DataTransactionResult.successNoData();
        }
        return result;
    }
}
