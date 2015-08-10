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
package org.spongepowered.common.mixin.core.data;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataManipulatorBuilder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.entity.player.SpongeUser;

import java.util.Collection;

@Mixin(value = {TileEntity.class, Entity.class, ItemStack.class, PotionEffect.class, SpongeUser.class}, priority = 999)
public abstract class MixinDataHolder implements DataHolder {

    @Override
    public <T extends DataManipulator<T>> Optional<T> getData(Class<T> dataClass) {
        Optional<SpongeDataProcessor<T>> builderOptional = SpongeManipulatorRegistry.getInstance().getUtil(dataClass);
        if (builderOptional.isPresent()) {
            return builderOptional.get().getFrom(this);
        }
        return Optional.absent();
    }

    @Override
    public <T extends DataManipulator<T>> Optional<T> getOrCreate(Class<T> manipulatorClass) {
        Optional<DataManipulatorBuilder<T>> builderOptional = SpongeManipulatorRegistry.getInstance().getBuilder(manipulatorClass);
        if (builderOptional.isPresent()) {
            return builderOptional.get().createFrom(this);
        }
        return Optional.absent();
    }

    @Override
    public <T extends DataManipulator<T>> boolean remove(Class<T> manipulatorClass) {
        Optional<SpongeDataProcessor<T>> utilOptional = SpongeManipulatorRegistry.getInstance().getUtil(manipulatorClass);
        if (utilOptional.isPresent()) {
            return utilOptional.get().remove(this);
        }
        return false;
    }

    @Override
    public <T extends DataManipulator<T>> boolean isCompatible(Class<T> manipulatorClass) {
        return false;
    }

    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(T manipulatorData) {
        return offer(manipulatorData, DataPriority.DATA_MANIPULATOR);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(T manipulatorData, DataPriority priority) {
        Optional<SpongeDataProcessor<T>> setterOptional = SpongeManipulatorRegistry.getInstance().getUtil((Class<T>) (Class) manipulatorData
                .getClass());
        if (setterOptional.isPresent()) {
            return setterOptional.get().setData(this, manipulatorData, priority);
        }
        return DataTransactionBuilder.fail(manipulatorData);
    }

    @Override
    public Collection<DataManipulator<?>> getManipulators() {
        return ImmutableList.of();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return Optional.absent();
    }

    @Override
    public Collection<Property<?, ?>> getProperties() {
        return ImmutableList.of();
    }

}
