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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.SpongeEntityArchetype;
import org.spongepowered.common.entity.SpongeEntitySnapshot;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.stream.Collectors;

public final class EntityData {

    private EntityData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Entity.class)
                    .create(Keys.AGE)
                        .get(h -> h.ticksExisted)
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.ticksExisted = v;
                            return true;
                        })
                    .create(Keys.BASE_SIZE)
                        .get(h -> (double) h.getWidth())
                    .create(Keys.BASE_VEHICLE)
                        .get(h -> (org.spongepowered.api.entity.Entity) h.getLowestRidingEntity())
                    .create(Keys.EYE_HEIGHT)
                        .get(h -> (double) h.getEyeHeight())
                    .create(Keys.EYE_POSITION)
                        .get(h -> VecHelper.toVector3d(h.getEyePosition(1f)))
                    .create(Keys.FALL_DISTANCE)
                        .get(h -> (double) h.fallDistance)
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.fallDistance = v.floatValue();
                            return true;
                        })
                    .create(Keys.FIRE_DAMAGE_DELAY)
                        .get(h -> ((EntityAccessor) h).accessor$getFireImmuneTicks())
                        .setAnd((h, v) -> {
                            if (v < 1 || v > Short.MAX_VALUE) {
                                return false;
                            }
                            ((EntityBridge) h).bridge$setFireImmuneTicks(v);
                            return ((EntityAccessor) h).accessor$getFireImmuneTicks() == v;
                        })
                    .create(Keys.FIRE_TICKS)
                        .get(h -> ((EntityAccessor) h).accessor$getFire() > 0 ? ((EntityAccessor) h).accessor$getFire() : null)
                        .set((h, v) -> ((EntityAccessor) h).accessor$setFire(Math.max(v, Constants.Entity.MINIMUM_FIRE_TICKS)))
                        .deleteAndGet(h -> {
                            final EntityAccessor accessor = (EntityAccessor) h;
                            final int ticks = accessor.accessor$getFire();
                            if (ticks < Constants.Entity.MINIMUM_FIRE_TICKS) {
                                return DataTransactionResult.failNoData();
                            }
                            final DataTransactionResult.Builder dtrBuilder = DataTransactionResult.builder();
                            dtrBuilder.replace(Value.immutableOf(Keys.FIRE_TICKS, ticks));
                            dtrBuilder.replace(Value.immutableOf(Keys.FIRE_DAMAGE_DELAY, ((EntityAccessor) h).accessor$getFireImmuneTicks()));
                            h.extinguish();
                            return dtrBuilder.result(DataTransactionResult.Type.SUCCESS).build();
                        })
                    .create(Keys.HEIGHT)
                        .get(h -> (double) h.getHeight())
                    .create(Keys.INVULNERABILITY_TICKS)
                        .get(h -> h.hurtResistantTime)
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.hurtResistantTime = v;
                            if (h instanceof LivingEntity) {
                                ((LivingEntity) h).hurtTime = v;
                            }
                            return true;
                        })
                    .create(Keys.IS_CUSTOM_NAME_VISIBLE)
                        .get(Entity::isCustomNameVisible)
                        .set(Entity::setCustomNameVisible)
                    .create(Keys.IS_FLYING)
                        .get(h -> h.isAirBorne)
                        .set((h, v) -> h.isAirBorne = v)
                        .supports(h -> !(h instanceof PlayerEntity))
                    .create(Keys.IS_GLOWING)
                        .get(Entity::isGlowing)
                        .set(Entity::setGlowing)
                    .create(Keys.IS_GRAVITY_AFFECTED)
                        .get(h -> !h.hasNoGravity())
                        .set((h, v) -> h.setNoGravity(!v))
                    .create(Keys.IS_SNEAKING)
                        .get(Entity::isSneaking)
                        .set(Entity::setSneaking)
                    .create(Keys.IS_SPRINTING)
                        .get(Entity::isSprinting)
                        .set(Entity::setSprinting)
                    .create(Keys.IS_SILENT)
                        .get(Entity::isSilent)
                        .set(Entity::setSilent)
                    .create(Keys.IS_WET)
                        .get(Entity::isWet)
                    .create(Keys.ON_GROUND)
                        .get(h -> h.onGround)
                    .create(Keys.PASSENGERS)
                        .get(h -> h.getPassengers().stream().map(org.spongepowered.api.entity.Entity.class::cast).collect(Collectors.toList()))
                        .set((h, v) -> {
                            h.getPassengers().clear();
                            v.forEach(v1 -> h.getPassengers().add((Entity) v1));
                        })
                    .create(Keys.SCALE)
                        .get(h -> 1d)
                    .create(Keys.SCOREBOARD_TAGS)
                        .get(Entity::getTags)
                        .set((h, v) -> {
                            h.getTags().clear();
                            h.getTags().addAll(v);
                        })
                    .create(Keys.TRANSIENT)
                        .get(h -> ((EntityAccessor) h).accessor$getEntityString() == null)
                        .set((h, v) -> ((EntityBridge) h).bridge$setTransient(v))
                    .create(Keys.VEHICLE)
                        .get(h -> (org.spongepowered.api.entity.Entity) h.getRidingEntity())
                        .set((h, v) -> h.startRiding((Entity) v, true))
                    .create(Keys.VELOCITY)
                        .get(h -> VecHelper.toVector3d(h.getMotion()))
                        .set((h, v) -> h.setMotion(VecHelper.toVec3d(v)))
                    .create(Keys.SWIFTNESS)
                        .get(m -> m.getMotion().length())
                        .set((m, v) -> m.setMotion(m.getMotion().normalize().scale(v)))
                        .supports(m -> m.getMotion().lengthSquared() > 0)
                .asMutable(EntityBridge.class)
                    .create(Keys.DISPLAY_NAME)
                        .get(EntityBridge::bridge$getDisplayNameText)
                        .set(EntityBridge::bridge$setDisplayName)
                        .delete(h -> h.bridge$setDisplayName(null));

        registrator.newDataStore(SpongeEntitySnapshot.class, SpongeEntityArchetype.class)
                .dataStore(Keys.DISPLAY_NAME,
                    (dv, v) -> dv.set(Constants.Entity.CUSTOM_NAME, SpongeAdventure.json(v)),
                    dv -> dv.getString(Constants.Entity.CUSTOM_NAME).map(SpongeAdventure::json));

        // @formatter:on
    }

}
