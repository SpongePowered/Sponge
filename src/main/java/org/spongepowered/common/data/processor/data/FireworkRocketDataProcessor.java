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

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkRocketData;
import org.spongepowered.api.data.manipulator.mutable.FireworkRocketData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeFireworkRocketData;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkRocketData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataProcessor;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntityFireworkRocket;

import java.util.Optional;

public class FireworkRocketDataProcessor extends AbstractSingleDataProcessor<Integer, MutableBoundedValue<Integer>, FireworkRocketData, ImmutableFireworkRocketData> {

    public FireworkRocketDataProcessor() {
        super(Keys.FIREWORK_FLIGHT_MODIFIER);
    }

    @Override
    protected FireworkRocketData createManipulator() {
        return new SpongeFireworkRocketData();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        if(dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem().equals(Items.fireworks)) return true;
        return dataHolder instanceof EntityFireworkRocket;
    }

    @Override
    public boolean supports(EntityType entityType) {
        return entityType.equals(EntityTypes.FIREWORK);
    }

    @Override
    public Optional<FireworkRocketData> from(DataHolder dataHolder) {
        ItemStack item = null;
        if(dataHolder instanceof ItemStack) {
            item = (ItemStack) dataHolder;
        }
        if(dataHolder instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) dataHolder);
        }

        if(item != null) {
            NBTTagCompound fireworks = item.getSubCompound("Fireworks", true);
            if(fireworks.hasKey("Flight")) {
                return Optional.of(new SpongeFireworkRocketData(fireworks.getByte("Flight")));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<FireworkRocketData> fill(DataContainer container, FireworkRocketData fireworkRocketData) {
        int modifier = DataUtil.getData(container, Keys.FIREWORK_FLIGHT_MODIFIER);
        return Optional.of(fireworkRocketData.set(Keys.FIREWORK_FLIGHT_MODIFIER, modifier));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, FireworkRocketData manipulator, MergeFunction function) {
        Optional<FireworkRocketData> oldData = dataHolder.get(FireworkRocketData.class);
        FireworkRocketData newData = function.merge(oldData.orElse(null), manipulator);

        int modifier = newData.get(Keys.FIREWORK_FLIGHT_MODIFIER).get();

        DataTransactionBuilder result = DataTransactionBuilder.builder();
        if(oldData.isPresent()) {
            result.replace(oldData.get().getValues());
        }

        ItemStack item = null;
        if(dataHolder instanceof ItemStack) {
            item = (ItemStack) dataHolder;
        }
        if(dataHolder instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) dataHolder);
        }
        if(item != null) {
            NBTTagCompound fireworks = ((ItemStack) dataHolder).getSubCompound("Fireworks", true);
            fireworks.setByte("Flight", (byte) modifier);
            if(dataHolder instanceof EntityFireworkRocket) {
                ((IMixinEntityFireworkRocket) dataHolder).setModifier((byte) modifier);
            }
            return result.success(newData.getValues()).result(DataTransactionResult.Type.SUCCESS).build();
        }


        return DataTransactionBuilder.failResult(newData.getValues());
    }

    @Override
    public Optional<ImmutableFireworkRocketData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableFireworkRocketData immutable) {
        if(key.equals(Keys.FIREWORK_FLIGHT_MODIFIER)) {
            return Optional.of(new ImmutableSpongeFireworkRocketData((int) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if(dataHolder instanceof ItemStack) {
            NBTTagCompound fireworks = ((ItemStack) dataHolder).getSubCompound("Fireworks", false);
            if(fireworks != null) {
                fireworks.removeTag("Flight");
            }
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }

}
