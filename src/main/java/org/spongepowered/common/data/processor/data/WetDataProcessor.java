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
package org.spongepowered.common.data.processor.data;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.manipulator.mutable.WetData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeWetData;
import org.spongepowered.common.data.manipulator.mutable.SpongeWetData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;

import com.google.common.base.Optional;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;

public class WetDataProcessor extends AbstractSpongeDataProcessor<WetData, ImmutableWetData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityWolf || ((dataHolder instanceof ItemStack) && ((ItemStack) dataHolder).getItem().equals(ItemTypes.SPONGE));
    }

    @Override
    public Optional<WetData> from(DataHolder dataHolder) {
        if (this.supports(dataHolder)) {
            if (dataHolder instanceof EntityWolf) {
                EntityWolf wolf = (EntityWolf) dataHolder;
                SpongeWetData data = new SpongeWetData(wolf.isWet());
            	
                return Optional.<WetData>of(data);
            } else if ((dataHolder instanceof ItemStack)) {
                ItemStack stack = (ItemStack) dataHolder;
                
                if (stack.getItem().equals(ItemTypes.SPONGE)) {
                    SpongeWetData data = new SpongeWetData(stack.getItemDamage() == 1);
                    return Optional.<WetData>of(data);
                }
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<WetData> createFrom(DataHolder dataHolder) {
        if (this.supports(dataHolder)) {
            return this.from(dataHolder);
        }
        return Optional.absent();
    }

    @Override
    public Optional<WetData> fill(DataHolder dataHolder, WetData manipulator, MergeFunction overlap) {
        if (this.supports(dataHolder)) {
            WetData merged = overlap.merge(checkNotNull(manipulator.copy()), this.from(dataHolder).get());
            return Optional.of(manipulator.set(Keys.IS_WET, merged.wet().get()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<WetData> fill(DataContainer container, WetData wetData) {
        Boolean isWet = DataUtil.getData(container, Keys.IS_WET);

        return Optional.of(wetData.set(Keys.IS_WET, isWet));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, WetData manipulator, MergeFunction function) {
        if (this.supports(dataHolder)) {
            return dataHolder.offer(Keys.IS_WET, manipulator.wet().get());
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableWetData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableWetData immutable) {
        if (key == Keys.IS_WET) {
            return Optional.<ImmutableWetData>of(ImmutableDataCachingUtil.getManipulator(ImmutableSpongeWetData.class, (Boolean) value));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
    	if (this.supports(dataHolder)) {
    		if (dataHolder instanceof ItemStack) {
    			ItemStack stack = (ItemStack) dataHolder;
    			
    			if (stack.getItem().equals(ItemTypes.SPONGE)) {
    				stack.setItemDamage(0);
    				return DataTransactionBuilder.successNoData();
    			}
    		}
    	}
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return EntityWolf.class.isAssignableFrom(entityType.getEntityClass());
    }

}
