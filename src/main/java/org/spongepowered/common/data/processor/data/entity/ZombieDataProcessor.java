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

import com.google.common.collect.Maps;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeZombieData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.entity.EntityUtil;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class ZombieDataProcessor
        extends AbstractEntityDataProcessor<EntityZombie, org.spongepowered.api.data.manipulator.mutable.entity.ZombieData,
    org.spongepowered.api.data.manipulator.immutable.entity.ImmutableZombieData> {

    public ZombieDataProcessor() {
        super(EntityZombie.class);
    }

    @Override
    protected boolean doesDataExist(EntityZombie dataHolder) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(EntityZombie dataHolder, Map<Key<?>, Object> keyValues) {
        throw new UnsupportedOperationException("ZombieData is deprecated - zombie types are now separate entities!");
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityZombie dataHolder) {
        Map<Key<?>, Object> values = Maps.newHashMap();

        org.spongepowered.api.data.type.ZombieType type = org.spongepowered.api.data.type.ZombieTypes.NORMAL;
        if (dataHolder instanceof EntityHusk) {
            type = org.spongepowered.api.data.type.ZombieTypes.HUSK;
        } else if (dataHolder instanceof EntityZombieVillager) {
            type = org.spongepowered.api.data.type.ZombieTypes.VILLAGER;
        }

        values.put(Keys.ZOMBIE_TYPE, type);
        if (type != org.spongepowered.api.data.type.ZombieTypes.VILLAGER) {
            values.put(Keys.VILLAGER_ZOMBIE_PROFESSION, Optional.empty());
            return values;
        }

        values.put(Keys.VILLAGER_ZOMBIE_PROFESSION, EntityUtil.validateProfession(((EntityZombieVillager) dataHolder).getProfession()));
        return values;
    }

    @Override
    protected org.spongepowered.api.data.manipulator.mutable.entity.ZombieData createManipulator() {
        return new SpongeZombieData();
    }

    @Override
    public Optional<org.spongepowered.api.data.manipulator.mutable.entity.ZombieData> fill(DataContainer container, org.spongepowered.api.data.manipulator.mutable.entity.ZombieData zombieData) {
        if (container.contains(Keys.ZOMBIE_TYPE)) {
            zombieData.set(Keys.ZOMBIE_TYPE,
                    container.getCatalogType(Keys.ZOMBIE_TYPE.getQuery(), org.spongepowered.api.data.type.ZombieType.class).get());
        }
        if (container.contains(Keys.VILLAGER_ZOMBIE_PROFESSION)) {
            zombieData.set(Keys.VILLAGER_ZOMBIE_PROFESSION,
                    container.getCatalogType(Keys.VILLAGER_ZOMBIE_PROFESSION.getQuery(), Profession.class));
        }
        return Optional.of(zombieData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
