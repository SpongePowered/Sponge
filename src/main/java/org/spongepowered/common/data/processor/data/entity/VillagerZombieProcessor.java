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

import com.google.common.collect.Iterables;
import net.minecraft.entity.monster.EntityZombie;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVillagerZombieData;
import org.spongepowered.api.data.manipulator.mutable.entity.VillagerZombieData;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.Professions;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVillagerZombieData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;

import java.util.Optional;

public class VillagerZombieProcessor
        extends AbstractEntitySingleDataProcessor<EntityZombie, Profession, Value<Profession>, VillagerZombieData, ImmutableVillagerZombieData> {

    public VillagerZombieProcessor() {
        super(EntityZombie.class, Keys.VILLAGER_ZOMBIE_PROFESSION);
    }

    @Override
    protected boolean set(EntityZombie entity, Profession value) {
        // TODO: Fix for ZombieType (add Mixin)
        //entity.setVillagerType(((SpongeProfession) value).type);
        return true;
    }

    @Override
    protected Optional<Profession> getVal(EntityZombie entity) {
        if (entity.isVillager()) {
            return Optional.of(Iterables.get(ProfessionRegistryModule.getInstance().getAll(), entity.getVillagerType()));
        }
        return Optional.empty();
    }

    @Override
    protected Value<Profession> constructValue(Profession actualValue) {
        return new SpongeValue<>(Keys.VILLAGER_ZOMBIE_PROFESSION, Professions.FARMER, actualValue);
    }

    @Override
    protected ImmutableValue<Profession> constructImmutableValue(Profession value) {
        return ImmutableSpongeValue.cachedOf(Keys.VILLAGER_ZOMBIE_PROFESSION, Professions.FARMER, value);
    }

    @Override
    protected VillagerZombieData createManipulator() {
        return new SpongeVillagerZombieData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        EntityZombie zombie = (EntityZombie) container;

        Optional<Profession> oldValue = this.getVal(zombie);
        if (!oldValue.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        //((EntityZombie) container).setToNotVillager(); // TODO: -> ZombieType
        return DataTransactionResult.successRemove(constructImmutableValue(oldValue.get()));
    }

}
