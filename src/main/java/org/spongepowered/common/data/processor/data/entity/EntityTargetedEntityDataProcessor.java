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

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetedEntityData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetedEntityData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTargetedEntityData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.IEntityTargetingEntity;
import org.spongepowered.common.mixin.core.entity.projectile.AccessorShulkerBulletEntity;

import java.util.Optional;

public final class EntityTargetedEntityDataProcessor extends AbstractSingleDataSingleTargetProcessor<AccessorShulkerBulletEntity, EntitySnapshot,
        Value<EntitySnapshot>, TargetedEntityData, ImmutableTargetedEntityData> {

    public EntityTargetedEntityDataProcessor() {
        super(Keys.TARGETED_ENTITY, AccessorShulkerBulletEntity.class);
    }

    @Override
    protected boolean set(AccessorShulkerBulletEntity dataHolder, org.spongepowered.api.entity.EntitySnapshot value) {
        if (!value.getUniqueId().isPresent()) {
            return false;
        }
        final Entity newTarget = ((WorldServer) ((EntityShulkerBullet) dataHolder).world).getEntityFromUuid(value.getUniqueId().get());
        if (newTarget == null) {
            return false;
        }
        dataHolder.accessor$setTarget(newTarget);
        dataHolder.accessor$setTargetId(newTarget.getUniqueID());
        return true;
    }

    @Override
    protected Optional<org.spongepowered.api.entity.EntitySnapshot> getVal(AccessorShulkerBulletEntity dataHolder) {
        Entity entity = ((IEntityTargetingEntity) dataHolder).getTargetedEntity();
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(((org.spongepowered.api.entity.Entity) entity).createSnapshot());
    }

    @Override
    protected ImmutableValue<org.spongepowered.api.entity.EntitySnapshot> constructImmutableValue(org.spongepowered.api.entity.EntitySnapshot value) {
        return new ImmutableSpongeValue<>(this.key, value);
    }

    @Override
    public boolean supports(AccessorShulkerBulletEntity dataHolder) {
        return dataHolder instanceof IEntityTargetingEntity;
    }

    @Override
    protected Value<org.spongepowered.api.entity.EntitySnapshot> constructValue(org.spongepowered.api.entity.EntitySnapshot actualValue) {
        return new SpongeValue<>(this.key, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof AccessorShulkerBulletEntity) {
            final Entity target = ((AccessorShulkerBulletEntity) container).accessor$getTarget();
            if (target == null) {
                return DataTransactionResult.successNoData();
            }
            final DataTransactionResult
                result =
                DataTransactionResult.builder()
                    .replace(new ImmutableSpongeValue<>(Keys.TARGETED_ENTITY, ((org.spongepowered.api.entity.Entity) target).createSnapshot()))
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();

            ((AccessorShulkerBulletEntity) container).accessor$setTarget(null);
            ((AccessorShulkerBulletEntity) container).accessor$setTargetId(null);
            return result;
        }

        return DataTransactionResult.successNoData();
    }

    @Override
    protected TargetedEntityData createManipulator() {
        return new SpongeTargetedEntityData(null);
    }
}
