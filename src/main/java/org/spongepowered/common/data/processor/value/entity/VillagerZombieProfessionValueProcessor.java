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

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.ZombieType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.ZombieTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.entity.EntityUtil;

import java.util.Optional;

public class VillagerZombieProfessionValueProcessor extends AbstractSpongeValueProcessor<EntityZombie, Optional<Profession>,
        OptionalValue<Profession>> {

    public VillagerZombieProfessionValueProcessor() {
        super(EntityZombie.class, Keys.VILLAGER_ZOMBIE_PROFESSION);
    }

    @Override
    protected OptionalValue<Profession> constructValue(Optional<Profession> actualValue) {
        return new SpongeOptionalValue<>(Keys.VILLAGER_ZOMBIE_PROFESSION, actualValue);
    }

    @Override
    protected boolean set(EntityZombie container, Optional<Profession> value) {
        if (value.isPresent()) {
            container.setZombieType(EntityUtil.toNative(ZombieTypes.VILLAGER, value.get()));
        } else {
            // Should we change to normal, or return false?
            container.setZombieType(ZombieType.NORMAL);
        }
        return true;
    }

    @Override
    protected Optional<Optional<Profession>> getVal(EntityZombie container) {
        return Optional.of(EntityUtil.profFromNative(container.getZombieType()));
    }

    @Override
    protected ImmutableValue<Optional<Profession>> constructImmutableValue(Optional<Profession> value) {
        return new ImmutableSpongeOptionalValue<>(Keys.VILLAGER_ZOMBIE_PROFESSION, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        //TODO: Remove or not to remove? That is the question
//        if(container instanceof EntityZombie) {
//            EntityZombie zombie = (EntityZombie) container;
//            Profession prof = ZombieUtils.profFromNative(zombie.getZombieType());
//            zombie.setZombieType(ZombieType.NORMAL);
//            return DataTransactionResult.successRemove(constructImmutableValue(Optional.of(prof)));
//        }
        return DataTransactionResult.failNoData();
    }
}
