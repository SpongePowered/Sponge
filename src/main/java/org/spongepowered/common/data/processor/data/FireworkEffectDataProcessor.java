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
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkEffectData;
import org.spongepowered.api.data.manipulator.mutable.FireworkEffectData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeFireworkEffectData;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkEffectData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataProcessor;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.persistence.SpongeSerializationManager;

import java.util.List;
import java.util.Optional;

public class FireworkEffectDataProcessor extends AbstractSingleDataProcessor<List<FireworkEffect>, ListValue<FireworkEffect>, FireworkEffectData, ImmutableFireworkEffectData> {

    public FireworkEffectDataProcessor() {
        super(Keys.FIREWORK_EFFECTS);
    }

    @Override
    protected FireworkEffectData createManipulator() {
        return new SpongeFireworkEffectData();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        if(dataHolder instanceof ItemStack) {
            return ((ItemStack) dataHolder).getItem() == Items.firework_charge
                    || ((ItemStack) dataHolder).getItem() == Items.fireworks;
        }

        return dataHolder instanceof EntityFireworkRocket;
    }

    @Override
    public boolean supports(EntityType entityType) {
        return entityType.equals(EntityTypes.FIREWORK);
    }

    @Override
    public Optional<FireworkEffectData> from(DataHolder dataHolder) {
        Optional<List<FireworkEffect>> effects = FireworkUtils.getFireworkEffects(dataHolder);
        if(effects.isPresent()) {
            return Optional.of(new SpongeFireworkEffectData(effects.get()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<FireworkEffectData> fill(DataContainer container, FireworkEffectData fireworkEffectData) {
        DataUtil.checkDataExists(container, Keys.FIREWORK_EFFECTS.getQuery());
        List<FireworkEffect> effects = container.getSerializableList(Keys.FIREWORK_EFFECTS.getQuery(),
                                                                     FireworkEffect.class, SpongeSerializationManager.getInstance()).get();

        return Optional.of(fireworkEffectData.set(Keys.FIREWORK_EFFECTS, effects));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, FireworkEffectData manipulator, MergeFunction function) {
        Optional<FireworkEffectData> oldData = dataHolder.get(FireworkEffectData.class);
        FireworkEffectData newData = function.merge(oldData.orElse(null), manipulator);

        DataTransactionBuilder result = DataTransactionBuilder.builder();
        if(oldData.isPresent()) {
            result.replace(oldData.get().getValues());
        }

        List<FireworkEffect> effects = newData.effects().get();
        if(FireworkUtils.setFireworkEffects(dataHolder, effects)) {
            return result.success(newData.getValues()).result(DataTransactionResult.Type.SUCCESS).build();
        } else {
            return DataTransactionBuilder.failResult(newData.getValues());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<ImmutableFireworkEffectData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableFireworkEffectData immutable) {
        if(key.equals(Keys.FIREWORK_EFFECTS)) {
            return Optional.of(new ImmutableSpongeFireworkEffectData((List<FireworkEffect>) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if(FireworkUtils.removeFireworkEffects(dataHolder)) {
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }
}
