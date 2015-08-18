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
package org.spongepowered.common.data.processor.data.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFoodData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.util.DataUtil;

public class FoodDataProcessor implements DataProcessor<FoodData, ImmutableFoodData> {

    @Override
    public Optional<FoodData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.FOOD_LEVEL.getQuery()) && container.contains(Keys.SATURATION.getQuery())
                && container.contains(Keys.EXHAUSTION.getQuery())) {
            final int foodLevel = DataUtil.getData(container, Keys.FOOD_LEVEL, Integer.class);
            final float foodSaturationLevel = DataUtil.getData(container, Keys.SATURATION, Double.class).floatValue();
            final float foodExhaustionLevel = DataUtil.getData(container, Keys.EXHAUSTION, Double.class).floatValue();
            return Optional.<FoodData>of(new SpongeFoodData(foodLevel, foodSaturationLevel, foodExhaustionLevel));
        }
        return Optional.absent();
    }

    @Override
    public FoodData create() {
        return new SpongeFoodData();
    }

    @Override
    public ImmutableFoodData createImmutable() {
        return new ImmutableSpongeFoodData(20, 5.0F, 0);
    }

    @Override
    public Optional<FoodData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            EntityPlayer player = (EntityPlayer) dataHolder;
            return Optional.<FoodData>of(new SpongeFoodData(player.getFoodStats().getFoodLevel(), player.getFoodStats().foodSaturationLevel, player
                    .getFoodStats().foodExhaustionLevel));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityPlayer;
    }

    @Override
    public Optional<FoodData> from(DataHolder dataHolder) {
        return createFrom(dataHolder);
    }

    @Override
    public Optional<FoodData> fill(DataHolder dataHolder, FoodData manipulator) {
        if (supports(dataHolder)) {
            manipulator.set(Keys.FOOD_LEVEL, ((EntityPlayer) dataHolder).getFoodStats().getFoodLevel());
            manipulator.set(Keys.SATURATION, (double) ((EntityPlayer) dataHolder).getFoodStats().getSaturationLevel());
            manipulator.set(Keys.SATURATION, (double) ((EntityPlayer) dataHolder).getFoodStats().foodExhaustionLevel);
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<FoodData> fill(DataHolder dataHolder, FoodData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final FoodData merged = overlap.merge(checkNotNull(manipulator).copy(), from(dataHolder).get());
            manipulator.set(Keys.FOOD_LEVEL, merged.foodLevel().get())
                    .set(Keys.SATURATION, merged.saturation().get())
                    .set(Keys.EXHAUSTION, merged.exhaustion().get());
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<FoodData> fill(DataContainer container, FoodData foodData) {
        foodData.set(Keys.FOOD_LEVEL, getData(container, Keys.FOOD_LEVEL));
        foodData.set(Keys.SATURATION, getData(container, Keys.SATURATION));
        foodData.set(Keys.EXHAUSTION, getData(container, Keys.EXHAUSTION));
        return Optional.of(foodData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, FoodData manipulator) {
        return set(dataHolder, manipulator, null);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, FoodData manipulator, MergeFunction function) {
        if (!supports(dataHolder)) {
            return DataTransactionBuilder.failResult(manipulator.getValues());
        }

        try {
            Optional<FoodData> oldData = from(dataHolder);
            if (function != null && oldData.isPresent()) {
                manipulator = function.merge(oldData.get(), manipulator);
            }

            ((EntityPlayer) dataHolder).getFoodStats().setFoodLevel(manipulator.foodLevel().get());
            ((EntityPlayer) dataHolder).getFoodStats().setFoodSaturationLevel(manipulator.saturation().get().floatValue());
            ((EntityPlayer) dataHolder).getFoodStats().foodExhaustionLevel = manipulator.exhaustion().get().floatValue();
            if (oldData.isPresent()) {
                return DataTransactionBuilder.successReplaceResult(manipulator.getValues(), oldData.get().getValues());
            } else {
                return DataTransactionBuilder.builder().success(manipulator.getValues()).build();
            }
        } catch (Exception e) {
            return DataTransactionBuilder.builder().reject(manipulator.getValues()).result(Type.ERROR).build();
        }
    }

    @Override
    public Optional<ImmutableFoodData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableFoodData immutable) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

}
