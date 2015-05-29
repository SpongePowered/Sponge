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
package org.spongepowered.common.data.component.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.DamagingComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

import java.util.Map;

public class SpongeDamagingComponent extends SpongeAbstractComponent<DamagingComponent> implements DamagingComponent {

    public static final DataQuery ENTITY_TYPE_DAMAGES = of("EntityTypeDamages");
    private double damage;
    private Map<EntityType, Double> damageMap = Maps.newHashMap();

    public SpongeDamagingComponent() {
        super(DamagingComponent.class);
    }

    @Override
    public double getDamage() {
        return this.damage;
    }

    @Override
    public DamagingComponent setDamage(double damage) {
        checkArgument(damage >= 0);
        this.damage = damage;
        return this;
    }

    @Override
    public double getDamageForEntity(EntityType entityType) {
        return this.damageMap.containsKey(checkNotNull(entityType)) ? this.damageMap.get(entityType) : this.damage;
    }

    @Override
    public DamagingComponent setDamageForEntity(EntityType entityType, double damage) {
        this.damageMap.put(checkNotNull(entityType), damage);
        return this;
    }

    @Override
    public DamagingComponent copy() {
        final DamagingComponent damagingComponent = new SpongeDamagingComponent();
        damagingComponent.setDamage(this.damage);
        for (Map.Entry<EntityType, Double> entry : this.damageMap.entrySet()) {
            damagingComponent.setDamageForEntity(entry.getKey(), entry.getValue());
        }
        return damagingComponent;
    }

    @Override
    public DamagingComponent reset() {
        this.damageMap = Maps.newHashMap();
        return setDamage(0);
    }

    @Override
    public int compareTo(DamagingComponent o) {
        return (int) Math.floor(o.getDamage() - this.damage);
    }

    @Override
    public DataContainer toContainer() {
        final DataView damages = new MemoryDataContainer()
                .set(Tokens.DAMAGE.getQuery(), this.damage)
                .createView(ENTITY_TYPE_DAMAGES);
        for (Map.Entry<EntityType, Double> entry : this.damageMap.entrySet()) {
            damages.set(of(entry.getKey().getId()), entry.getValue());
        }
        return damages.getContainer();
    }
}
