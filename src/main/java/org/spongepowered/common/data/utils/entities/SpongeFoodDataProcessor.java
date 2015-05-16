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
package org.spongepowered.common.data.utils.entities;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.FoodData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulators.entities.SpongeFoodData;

public class SpongeFoodDataProcessor implements SpongeDataProcessor<FoodData> {

    @Override
    public Optional<FoodData> fillData(DataHolder holder, FoodData manipulator, DataPriority priority) {
        if (!(holder instanceof EntityPlayer)) {
            return Optional.absent();
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                final FoodStats foodStats = ((EntityPlayer) holder).getFoodStats();
                manipulator.setExhaustion(foodStats.foodExhaustionLevel);
                manipulator.setFoodLevel(foodStats.getFoodLevel());
                manipulator.setSaturation(foodStats.getSaturationLevel());
                return Optional.of(manipulator);
            default:
                return Optional.absent();
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, FoodData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityPlayer)) {
            return fail(manipulator);
        } else {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                    return builder().reject(manipulator).result(DataTransactionResult.Type.SUCCESS).build();
                case DATA_MANIPULATOR:
                    final FoodStats foodStats = ((EntityPlayer) dataHolder).getFoodStats();
                    final FoodData oldData = createFrom(dataHolder).get();
                    foodStats.setFoodLevel(((int) Math.floor(manipulator.getFoodLevel())));
                    foodStats.foodExhaustionLevel = ((float) manipulator.getExhaustion());
                    foodStats.setFoodSaturationLevel(((float) manipulator.getSaturation()));
                    return builder().replace(oldData).result(DataTransactionResult.Type.SUCCESS).build();
                default:
                    return fail(manipulator);
            }
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityPlayer)) {
            return false;
        } else {
            final FoodStats foodStats = ((EntityPlayer) dataHolder).getFoodStats();
            foodStats.foodExhaustionLevel = 0;
            foodStats.setFoodLevel(20);
            foodStats.setFoodSaturationLevel(20);
            return true;
        }
    }

    @Override
    public Optional<FoodData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public FoodData create() {
        return new SpongeFoodData();
    }

    @Override
    public Optional<FoodData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityPlayer)) {
            return Optional.absent();
        }
        final FoodStats foodStats = ((EntityPlayer) dataHolder).getFoodStats();
        final FoodData foodData = create();
        foodData.setExhaustion(foodStats.foodExhaustionLevel);
        foodData.setFoodLevel(foodStats.getFoodLevel());
        foodData.setSaturation(foodStats.getSaturationLevel());
        return Optional.of(foodData);
    }

    @Override
    public Optional<FoodData> getFrom(DataHolder holder) {
        return Optional.absent();
    }
}
