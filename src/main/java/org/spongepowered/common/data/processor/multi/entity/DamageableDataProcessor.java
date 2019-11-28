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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDamageableData;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeDamageableData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public class DamageableDataProcessor extends AbstractEntityDataProcessor<LivingEntity, DamageableData, ImmutableDamageableData> {

    public DamageableDataProcessor() {
        super(LivingEntity.class);
    }

    @Override
    protected boolean doesDataExist(LivingEntity dataHolder) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(LivingEntity dataHolder, Map<Key<?>, Object> keyValues) {
        final Optional<EntitySnapshot> lastAttacker = (Optional<EntitySnapshot>) keyValues.get(Keys.LAST_ATTACKER);
        if (lastAttacker == null || !lastAttacker.isPresent()) {
            dataHolder.setRevengeTarget(null);
            return true;
        }
        if (lastAttacker.get().getUniqueId().isPresent()) {
            final Optional<Entity> optionalEntity = lastAttacker.get().restore();
            if (optionalEntity.isPresent()) {
                final Entity entity = optionalEntity.get();
                if (entity.isLoaded() && entity instanceof LivingEntity) {
                    dataHolder.setRevengeTarget((LivingEntity) entity);
                    ((EntityLivingBaseAccessor) dataHolder).accessor$setLastDamage(((Optional<Double>)keyValues.get(Keys.LAST_DAMAGE)).orElse(0D).floatValue());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(LivingEntity dataHolder) {
        EntitySnapshot snapshot = dataHolder.getAttackingEntity() != null ? ((Entity) dataHolder.getAttackingEntity()).createSnapshot() : null;
        return ImmutableMap.of(Keys.LAST_ATTACKER, Optional.ofNullable(snapshot),
                Keys.LAST_DAMAGE, Optional.ofNullable(dataHolder.getAttackingEntity() == null ? null : ((EntityLivingBaseAccessor) dataHolder).accessor$getLastDamage()));
    }

    @Override
    protected DamageableData createManipulator() {
        return new SpongeDamageableData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<DamageableData> fill(DataContainer container, DamageableData damageableData) {
        if (container.contains(Keys.LAST_DAMAGE)) {
            container.get(Keys.LAST_DAMAGE.getQuery()).ifPresent(o -> damageableData.set(Keys.LAST_DAMAGE, (Optional<Double>) o));
        } else {
            damageableData.set(Keys.LAST_DAMAGE, Optional.empty());
        }

        if (container.contains(Keys.LAST_ATTACKER)) {
            container.get(Keys.LAST_ATTACKER.getQuery()).ifPresent(o -> damageableData.set(Keys.LAST_ATTACKER, (Optional<EntitySnapshot>) o));
        } else {
            container.set(Keys.LAST_ATTACKER, Optional.empty());
        }

        return Optional.of(damageableData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
