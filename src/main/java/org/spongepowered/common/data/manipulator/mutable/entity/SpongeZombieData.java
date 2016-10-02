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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableZombieData;
import org.spongepowered.api.data.manipulator.mutable.entity.ZombieData;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.ZombieType;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeZombieData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

@ImplementationRequiredForTest
public class SpongeZombieData extends AbstractData<ZombieData, ImmutableZombieData> implements ZombieData {

    private ZombieType type;
    private Optional<Profession> profession;

    public SpongeZombieData() {
        this(DataConstants.Catalog.DEFAULT_ZOMBIE_TYPE, Optional.empty());
    }

    public SpongeZombieData(ZombieType type, Optional<Profession> profession) {
        super(ZombieData.class);
        this.type = type;
        this.profession = profession;
    }

    private ZombieType getType() {
        return this.type;
    }

    private void setType(ZombieType type) {
        this.type = type;
    }

    private Optional<Profession> getProfession() {
        return this.profession;
    }

    private void setProfession(Optional<Profession> profession) {
        this.profession = profession;
    }

    @Override
    public Value<ZombieType> type() {
        return new SpongeValue<>(Keys.ZOMBIE_TYPE, DataConstants.Catalog.DEFAULT_ZOMBIE_TYPE, this.type);
    }

    @Override
    public OptionalValue<Profession> profession() {
        return new SpongeOptionalValue<>(Keys.VILLAGER_ZOMBIE_PROFESSION, getProfession());
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.ZOMBIE_TYPE, SpongeZombieData.this::getType);
        registerFieldGetter(Keys.VILLAGER_ZOMBIE_PROFESSION, SpongeZombieData.this::getProfession);

        registerFieldSetter(Keys.ZOMBIE_TYPE, SpongeZombieData.this::setType);
        registerFieldSetter(Keys.VILLAGER_ZOMBIE_PROFESSION, SpongeZombieData.this::setProfession);

        registerKeyValue(Keys.ZOMBIE_TYPE, SpongeZombieData.this::type);
        registerKeyValue(Keys.VILLAGER_ZOMBIE_PROFESSION, SpongeZombieData.this::profession);
    }

    @Override
    public ZombieData copy() {
        return new SpongeZombieData(this.type, this.profession);
    }

    @Override
    public ImmutableZombieData asImmutable() {
        return new ImmutableSpongeZombieData(this.type, this.profession);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer data = super.toContainer();
        data.set(Keys.ZOMBIE_TYPE, getType());
        if (getProfession().isPresent()) {
            data.set(Keys.VILLAGER_ZOMBIE_PROFESSION.getQuery(), getProfession().get());
        }

        return data;
    }
}
