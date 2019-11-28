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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkEffectData;
import org.spongepowered.api.data.manipulator.mutable.FireworkEffectData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkEffectData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.item.FireworkRocketEntity;

public class FireworkEffectDataProcessor extends
        AbstractEntitySingleDataProcessor<FireworkRocketEntity, List<FireworkEffect>, ListValue<FireworkEffect>, FireworkEffectData, ImmutableFireworkEffectData> {

    public FireworkEffectDataProcessor() {
        super(FireworkRocketEntity.class, Keys.FIREWORK_EFFECTS);
    }

    @Override
    protected FireworkEffectData createManipulator() {
        return new SpongeFireworkEffectData();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return entityType.equals(EntityTypes.FIREWORK);
    }

    @Override
    protected Optional<List<FireworkEffect>> getVal(FireworkRocketEntity firework) {
        return FireworkUtils.getFireworkEffects(firework);
    }

    @Override
    public Optional<FireworkEffectData> fill(DataContainer container, FireworkEffectData fireworkEffectData) {
        DataUtil.checkDataExists(container, Keys.FIREWORK_EFFECTS.getQuery());
        List<FireworkEffect> effects = container.getSerializableList(Keys.FIREWORK_EFFECTS.getQuery(),
                FireworkEffect.class).get();

        return Optional.of(fireworkEffectData.set(Keys.FIREWORK_EFFECTS, effects));
    }

    @Override
    protected boolean set(FireworkRocketEntity firework, List<FireworkEffect> effects) {
        return FireworkUtils.setFireworkEffects(firework, effects);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (FireworkUtils.removeFireworkEffects(container)) {
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected ListValue<FireworkEffect> constructValue(List<FireworkEffect> value) {
        return new SpongeListValue<>(Keys.FIREWORK_EFFECTS, value);
    }

    @Override
    protected ImmutableValue<List<FireworkEffect>> constructImmutableValue(List<FireworkEffect> value) {
        return new ImmutableSpongeListValue<>(Keys.FIREWORK_EFFECTS, ImmutableList.copyOf(value));
    }

}
