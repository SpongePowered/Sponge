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
package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import net.minecraft.entity.monster.EntityZombie;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVillagerZombieData;
import org.spongepowered.api.data.manipulator.mutable.entity.VillagerZombieData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVillagerZombieData;

import java.util.Optional;

public class VillagerZombieBuilder implements DataManipulatorBuilder<VillagerZombieData, ImmutableVillagerZombieData> {

    @Override
    public VillagerZombieData create() {
        return new SpongeVillagerZombieData();
    }

    @Override
    public Optional<VillagerZombieData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityZombie) {
            return Optional.of(new SpongeVillagerZombieData(((EntityZombie) dataHolder).isVillager()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<VillagerZombieData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.IS_VILLAGER_ZOMBIE.getQuery())) {
            return Optional.of(new SpongeVillagerZombieData(getData(container, Keys.IS_VILLAGER_ZOMBIE)));
        }
        return Optional.empty();
    }
}
