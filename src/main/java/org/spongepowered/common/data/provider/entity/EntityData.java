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

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.EntityMaxAirBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.SpongeEntityArchetype;
import org.spongepowered.common.entity.SpongeEntitySnapshot;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;

import java.util.stream.Collectors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class EntityData {

    private EntityData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Entity.class)
                    .create(Keys.AGE)
                        .get(h -> h.tickCount)
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.tickCount = v;
                            return true;
                        })
                    .create(Keys.BASE_SIZE)
                        .get(h -> (double) h.getBbWidth())
                    .create(Keys.BASE_VEHICLE)
                        .get(h -> (org.spongepowered.api.entity.Entity) h.getRootVehicle())
                    .create(Keys.CUSTOM_NAME)
                        .get(h -> h.hasCustomName() ? SpongeAdventure.asAdventure(h.getCustomName()) : null)
                        .set((h, v) -> h.setCustomName(SpongeAdventure.asVanilla(v)))
                        .delete(h -> {
                            h.setCustomName(null);
                            h.setCustomNameVisible(false);
                        })
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> SpongeAdventure.asAdventure(h.getDisplayName()))
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
                        .get(h -> new SpongeTicks(((EntityAccessor) h).invoker$getFireImmuneTicks()))
                        .setAnd((h, v) -> {
                            final int ticks = (int) v.ticks();
                            if (ticks < 1 || ticks > Short.MAX_VALUE) {
                                return false;
                            }
                            ((EntityBridge) h).bridge$setFireImmuneTicks(ticks);
                            return ((EntityAccessor) h).invoker$getFireImmuneTicks() == ticks;
                        })
                    .create(Keys.FIRE_TICKS)
                        .get(h -> ((EntityAccessor) h).accessor$remainingFireTicks() > 0 ? Ticks.of(((EntityAccessor) h).accessor$remainingFireTicks()) : null)
                        .set((h, v) -> {
                            final int ticks = (int) v.ticks();
                            ((EntityAccessor) h).accessor$remainingFireTicks(Math.max(ticks, Constants.Entity.MINIMUM_FIRE_TICKS));
                        })
                        .deleteAndGet(h -> {
                            final EntityAccessor accessor = (EntityAccessor) h;
                            final int ticks = accessor.accessor$remainingFireTicks();
                            if (ticks < Constants.Entity.MINIMUM_FIRE_TICKS) {
                                return DataTransactionResult.failNoData();
                            }
                            final DataTransactionResult.Builder dtrBuilder = DataTransactionResult.builder();
                            dtrBuilder.replace(Value.immutableOf(Keys.FIRE_TICKS, new SpongeTicks(ticks)));
                            dtrBuilder.replace(Value.immutableOf(Keys.FIRE_DAMAGE_DELAY,
                                    new SpongeTicks(((EntityAccessor) h).invoker$getFireImmuneTicks())));
                            h.clearFire();
                            return dtrBuilder.result(DataTransactionResult.Type.SUCCESS).build();
                        })
                    .create(Keys.HEIGHT)
                        .get(h -> (double) h.getBbHeight())
                    .create(Keys.INVULNERABILITY_TICKS)
                        .get(h -> new SpongeTicks(h.invulnerableTime))
                        .setAnd((h, v) -> {
                            final int ticks = (int) v.ticks();
                            if (ticks < 0) {
                                return false;
                            }
                            h.invulnerableTime = ticks;
                            if (h instanceof LivingEntity) {
                                ((LivingEntity) h).hurtTime = ticks;
                            }
                            return true;
                        })
                    .create(Keys.IS_CUSTOM_NAME_VISIBLE)
                        .get(Entity::isCustomNameVisible)
                        .set(Entity::setCustomNameVisible)
                    .create(Keys.IS_FLYING)
                        .get(h -> h.hasImpulse)
                        .set((h, v) -> h.hasImpulse = v)
                        .supports(h -> !(h instanceof Player))
                    .create(Keys.IS_GLOWING)
                        .get(Entity::isGlowing)
                        .set(Entity::setGlowing)
                    .create(Keys.IS_GRAVITY_AFFECTED)
                        .get(h -> !h.isNoGravity())
                        .set((h, v) -> h.setNoGravity(!v))
                    .create(Keys.IS_SNEAKING)
                        .get(Entity::isShiftKeyDown)
                        .set(Entity::setShiftKeyDown)
                    .create(Keys.IS_SPRINTING)
                        .get(Entity::isSprinting)
                        .set(Entity::setSprinting)
                    .create(Keys.IS_SILENT)
                        .get(Entity::isSilent)
                        .set(Entity::setSilent)
                    .create(Keys.IS_WET)
                        .get(Entity::isInWaterOrRain)
                    .create(Keys.ON_GROUND)
                        .get(Entity::isOnGround)
                    .create(Keys.PASSENGERS)
                        .get(h -> h.getPassengers().stream().map(org.spongepowered.api.entity.Entity.class::cast).collect(Collectors.toList()))
                        .set((h, v) -> {
                            ((EntityAccessor) h).accessor$passengers().clear();
                            v.forEach(v1 -> ((EntityAccessor) h).accessor$passengers().add((Entity) v1));
                        })
                    .create(Keys.REMAINING_AIR)
                        .get(h -> Math.max(0, h.getAirSupply()))
                        .setAnd((h, v) -> {
                            if (v < 0 || v > h.getMaxAirSupply()) {
                                return false;
                            }
                            if (v == 0 && h.getAirSupply() < 0) {
                                return false;
                            }
                            h.setAirSupply(v);
                            return true;
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
                        .get(h -> ((EntityAccessor) h).invoker$getEncodeId() == null)
                        .set((h, v) -> ((EntityBridge) h).bridge$setTransient(v))
                    .create(Keys.VEHICLE)
                        .get(h -> (org.spongepowered.api.entity.Entity) h.getVehicle())
                        .set((h, v) -> h.startRiding((Entity) v, true))
                    .create(Keys.VELOCITY)
                        .get(h -> VecHelper.toVector3d(h.getDeltaMovement()))
                        .set((h, v) -> h.setDeltaMovement(VecHelper.toVanillaVector3d(v)))
                    .create(Keys.SWIFTNESS)
                        .get(m -> m.getDeltaMovement().length())
                        .set((m, v) -> m.setDeltaMovement(m.getDeltaMovement().normalize().scale(v)))
                        .supports(m -> m.getDeltaMovement().lengthSqr() > 0)
                .asMutable(EntityMaxAirBridge.class)
                    .create(Keys.MAX_AIR)
                        .get(EntityMaxAirBridge::bridge$getMaxAir)
                        .set(EntityMaxAirBridge::bridge$setMaxAir)
                ;

        registrator.spongeDataStore(ResourceKey.sponge("max_air"), EntityMaxAirBridge.class, Keys.MAX_AIR);
        registrator.newDataStore(SpongeEntitySnapshot.class, SpongeEntityArchetype.class)
                .dataStore(Keys.CUSTOM_NAME,
                    (dv, v) -> dv.set(Constants.Entity.CUSTOM_NAME, GsonComponentSerializer.gson().serialize(v)),
                    dv -> dv.getString(Constants.Entity.CUSTOM_NAME).map(GsonComponentSerializer.gson()::deserialize));

        // @formatter:on
    }

}
