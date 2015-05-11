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
package org.spongepowered.common.data.manipulators.entities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.DamagingData;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;

import java.util.Map;

public class SpongeDamagingData extends SpongeAbstractData<DamagingData> implements DamagingData {

    private double damage;
    private Map<EntityType, Double> damageMap = Maps.newHashMap();

    public SpongeDamagingData() {
        super(DamagingData.class);
    }

    @Override
    public double getDamage() {
        return this.damage;
    }

    @Override
    public DamagingData setDamage(double damage) {
        checkArgument(damage >= 0);
        this.damage = damage;
        return null;
    }

    @Override
    public double getDamageForEntity(EntityType entityType) {
        return this.damageMap.containsKey(checkNotNull(entityType)) ? this.damageMap.get(entityType) : this.damage;
    }

    @Override
    public DamagingData setDamageForEntity(EntityType entityType, double damage) {
        this.damageMap.put(checkNotNull(entityType), damage);
        return this;
    }

    @Override
    public int compareTo(DamagingData o) {
        return (int) Math.floor(o.getDamage() - this.damage);
    }

    @Override
    public DamagingData copy() {
        final DamagingData  damagingData = new SpongeDamagingData();
        damagingData.setDamage(this.damage);
        for (Map.Entry<EntityType, Double> entry : this.damageMap.entrySet()) {
            damagingData.setDamageForEntity(entry.getKey(), entry.getValue());
        }
        return damagingData;
    }

    @Override
    public DataContainer toContainer() {
        final DataView damages = new MemoryDataContainer()
                .set(of("Damage"), this.damage)
                .createView(of("EntityTypeDamages"));
        for (Map.Entry<EntityType, Double> entry : this.damageMap.entrySet()) {
            damages.set(of(entry.getKey().getId()), entry.getValue());
        }
        return damages.getContainer();
    }
}
