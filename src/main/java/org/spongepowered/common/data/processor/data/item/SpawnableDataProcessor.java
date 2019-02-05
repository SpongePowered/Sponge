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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSpawnableData;
import org.spongepowered.api.data.manipulator.mutable.SpawnableData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeSpawnableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

import java.util.Optional;

public class SpawnableDataProcessor extends AbstractItemSingleDataProcessor<EntityType, SpawnableData, ImmutableSpawnableData> {

    public SpawnableDataProcessor() {
        super(input -> input.getItem().equals(Items.SPAWN_EGG), Keys.SPAWNABLE_ENTITY_TYPE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean set(ItemStack itemStack, EntityType value) {
        final ResourceLocation name = EntityList.getKey((Class<? extends Entity>) value.getEntityClass());
        if (EntityList.ENTITY_EGGS.containsKey(name)) {
            final NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(itemStack);
            final NBTTagCompound subCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.SPAWNABLE_ENTITY_TAG);
            subCompound.setString(NbtDataUtil.ENTITY_TYPE_ID, name.toString());
            return true;
        }
        return false;
    }

    @Override
    public Optional<EntityType> getVal(ItemStack itemStack) {
        final ResourceLocation name = ItemMonsterPlacer.getNamedIdFrom(itemStack);
        if (name != null) {
            final Class<? extends Entity> entity = SpongeImplHooks.getEntityClass(name);
            return Optional.ofNullable(EntityTypeRegistryModule.getInstance().getForClass(entity));
        }
        return Optional.empty();
    }

    @Override
    protected Value.Mutable<EntityType> constructMutableValue(EntityType actualValue) {
        return new SpongeMutableValue<>(Keys.SPAWNABLE_ENTITY_TYPE, actualValue);
    }

    @Override
    public Value.Immutable<EntityType> constructImmutableValue(EntityType value) {
        return SpongeImmutableValue.cachedOf(Keys.SPAWNABLE_ENTITY_TYPE, value);
    }

    @Override
    public SpawnableData createManipulator() {
        return new SpongeSpawnableData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!supports(container)) {
            return DataTransactionResult.failNoData();
        }
        ItemStack itemStack = (ItemStack) container;
        Optional<EntityType> old = getVal(itemStack);
        if (!old.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        try {
            NbtDataUtil.getItemCompound(itemStack).get().removeTag(NbtDataUtil.SPAWNABLE_ENTITY_TAG);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        } catch (Exception e) {
            return DataTransactionResult.builder().result(DataTransactionResult.Type.ERROR).build();
        }
    }
}
