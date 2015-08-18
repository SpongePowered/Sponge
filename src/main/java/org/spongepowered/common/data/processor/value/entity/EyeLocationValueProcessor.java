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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.IMixinEntity;

@SuppressWarnings("ConstantConditions")
public class EyeLocationValueProcessor implements ValueProcessor<Vector3d, Value<Vector3d>> {

    @Override
    public Key<? extends BaseValue<Vector3d>> getKey() {
        return Keys.EYE_LOCATION;
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof Entity;
    }

    @Override
    public Optional<Vector3d> getValueFromContainer(ValueContainer<?> container) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            return Optional.of(new Vector3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ));
        }
        return Optional.absent();
    }

    @Override
    public Optional<Value<Vector3d>> getApiValueFromContainer(ValueContainer<?> container) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            return Optional.<Value<Vector3d>>of(new SpongeValue<Vector3d>(Keys.EYE_LOCATION, new Vector3d(entity.posX, entity.posY, entity.posZ),
                new Vector3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ)));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<Vector3d, Vector3d> function) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            final Vector3d oldValue = new Vector3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            final Vector3d newValue = function.apply(oldValue);
            ((IMixinEntity) entity).setEyeHeight(newValue.getY() - oldValue.getY());
            return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<Vector3d>(Keys.EYE_LOCATION, newValue),
                new ImmutableSpongeValue<Vector3d>(Keys.EYE_LOCATION, oldValue));
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        final Object object = value.get();
        if (object instanceof Vector3d) {
            return offerToStore(container, (Vector3d) object);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Vector3d value) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            final Vector3d oldValue = new Vector3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            ((IMixinEntity) entity).setEyeHeight(value.getY() - oldValue.getY());
            return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<Vector3d>(Keys.EYE_LOCATION, value),
                new ImmutableSpongeValue<Vector3d>(Keys.EYE_LOCATION, oldValue));
        }
        return DataTransactionBuilder.failResult(new ImmutableSpongeValue<Vector3d>(Keys.EYE_LOCATION, value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

}
