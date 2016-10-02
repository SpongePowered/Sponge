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
package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableZombieData;
import org.spongepowered.api.data.manipulator.mutable.entity.ZombieData;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.ZombieType;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeZombieData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class ImmutableSpongeZombieData extends AbstractImmutableData<ImmutableZombieData, ZombieData> implements ImmutableZombieData {

    private final ZombieType type;
    private final Optional<Profession> profession;
    private final ImmutableValue<ZombieType> typeValue;
    private final ImmutableOptionalValue<Profession> professionValue;

    public ImmutableSpongeZombieData(ZombieType type, Optional<Profession> profession) {
        super();
        this.type = type;
        this.profession = profession;

        this.typeValue = ImmutableSpongeValue.cachedOf(Keys.ZOMBIE_TYPE, DataConstants.Catalog.DEFAULT_ZOMBIE_TYPE, type);
        this.professionValue = new ImmutableSpongeOptionalValue<>(Keys.VILLAGER_ZOMBIE_PROFESSION, profession);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public ImmutableValue<ZombieType> type() {
        return this.typeValue;
    }

    @Override
    public ImmutableOptionalValue<Profession> profession() {
        return this.professionValue;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.ZOMBIE_TYPE, ImmutableSpongeZombieData.this::getType);
        registerFieldGetter(Keys.VILLAGER_ZOMBIE_PROFESSION, ImmutableSpongeZombieData.this::getProfession);

        registerKeyValue(Keys.ZOMBIE_TYPE, ImmutableSpongeZombieData.this::type);
        registerKeyValue(Keys.VILLAGER_ZOMBIE_PROFESSION, ImmutableSpongeZombieData.this::profession);
    }

    private ZombieType getType() {
        return this.type;
    }

    private Optional<Profession> getProfession() {
        return this.profession;
    }

    @Override
    public ZombieData asMutable() {
        return new SpongeZombieData(this.type, this.profession);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer data = super.toContainer();
        data.set(Keys.ZOMBIE_TYPE.getQuery(), getType().getId());
        if (getProfession().isPresent()) {
            data.set(Keys.VILLAGER_ZOMBIE_PROFESSION.getQuery(), getProfession().get().getId());
        }

        return data;
    }
}
