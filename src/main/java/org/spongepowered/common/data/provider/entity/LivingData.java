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

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.bridge.entity.LivingEntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.PotionEffectUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.registry.builtin.sponge.DamageTypeStreamGenerator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;

public final class LivingData {

    private LivingData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(LivingEntity.class)
                    .create(Keys.ABSORPTION)
                        .get(h -> (double) h.getAbsorptionAmount())
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.setAbsorptionAmount(v.floatValue());
                            return true;
                        })
                    .create(Keys.ACTIVE_ITEM)
                        .get(h -> ItemStackUtil.snapshotOf(h.getActiveItemStack()))
                        .setAnd((h, v) -> {
                            if (v.isEmpty()) {
                                h.stopActiveHand();
                                return true;
                            }
                            return false;
                        })
                        .delete(LivingEntity::stopActiveHand)
                    .create(Keys.BODY_ROTATIONS)
                        .get(h -> {
                            final double headYaw = h.getRotationYawHead();
                            final double pitch = h.rotationPitch;
                            final double yaw = h.rotationYaw;

                            return ImmutableMap.of(
                                    BodyParts.HEAD.get(), new Vector3d(pitch, headYaw, 0),
                                    BodyParts.CHEST.get(), new Vector3d(pitch, yaw, 0));
                        })
                        .set((h, v) -> {
                            final Vector3d headRotation = v.get(BodyParts.HEAD.get());
                            final Vector3d bodyRotation = v.get(BodyParts.CHEST.get());

                            if (bodyRotation != null) {
                                h.rotationYaw = (float) bodyRotation.getY();
                                h.rotationPitch = (float) bodyRotation.getX();
                            }
                            if (headRotation != null) {
                                h.rotationYawHead = (float) headRotation.getY();
                                h.rotationPitch = (float) headRotation.getX();
                            }
                        })
                    .create(Keys.CHEST_ROTATION)
                        .get(h -> new Vector3d(h.rotationPitch, h.rotationYaw, 0))
                        .set((h, v) -> {
                            final float headYaw = (float) v.getY();
                            final float pitch = (float) v.getX();
                            h.setRotationYawHead(headYaw);
                            h.rotationPitch = pitch;
                        })
                    .create(Keys.HEAD_ROTATION)
                        .get(h -> new Vector3d(h.rotationPitch, h.getRotationYawHead(), 0))
                        .set((h, v) -> {
                            final float yaw = (float) v.getY();
                            final float pitch = (float) v.getX();
                            h.rotationYaw = yaw;
                            h.rotationPitch = pitch;
                        })
                    .create(Keys.HEALTH)
                        .get(h -> (double) h.getHealth())
                        .setAnd((h, v) -> {
                            final double maxHealth = h.getMaxHealth();
                            // Check bounds
                            if (v < 0 || v > maxHealth) {
                                return false;
                            }

                            h.setHealth(v.floatValue());
                            if (v == 0) {
                                h.attackEntityFrom(DamageTypeStreamGenerator.IGNORED_DAMAGE_SOURCE, 1000F);
                            }
                            return true;
                        })
                    .create(Keys.IS_ELYTRA_FLYING)
                        .get(LivingEntity::isElytraFlying)
                        .set((h, v) -> ((EntityAccessor) h).accessor$setFlag(Constants.Entity.ELYTRA_FLYING_FLAG, v))
                    .create(Keys.LAST_ATTACKER)
                        .get(h -> (Entity) h.getRevengeTarget())
                        .setAnd((h, v) -> {
                            if (v instanceof LivingEntity) {
                                h.setRevengeTarget((LivingEntity) v);
                                return true;
                            }
                            return false;
                        })
                        .delete(h -> h.setRevengeTarget(null))
                    .create(Keys.MAX_AIR)
                        .get(LivingEntity::getMaxAir)
                        .set((h, v) -> ((LivingEntityBridge) h).bridge$setMaxAir(v))
                    .create(Keys.MAX_HEALTH)
                        .get(h -> (double) h.getMaxHealth())
                        .set((h, v) -> h.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(v))
                    .create(Keys.POTION_EFFECTS)
                        .get(h -> {
                            final Collection<EffectInstance> effects = h.getActivePotionEffects();
                            return effects.isEmpty() ? null : PotionEffectUtil.copyAsPotionEffects(effects);
                        })
                        .set((h, v) -> {
                            h.clearActivePotions();
                            for (final PotionEffect effect : v) {
                                h.addPotionEffect(PotionEffectUtil.copyAsEffectInstance(effect));
                            }
                        })
                        .delete(LivingEntity::clearActivePotions)
                    .create(Keys.REMAINING_AIR)
                        .get(h -> Math.max(0, h.getAir()))
                        .setAnd((h, v) -> {
                            if (v < 0 || v > h.getMaxAir()) {
                                return false;
                            }
                            if (v == 0 && h.getAir() < 0) {
                                return false;
                            }
                            h.setAir(v);
                            return true;
                        })
                    .create(Keys.SCALE)
                        .get(h -> (double) h.getRenderScale())
                    .create(Keys.STUCK_ARROWS)
                        .get(LivingEntity::getArrowCountInEntity)
                        .setAnd((h, v) -> {
                            if (v < 0 || v > Integer.MAX_VALUE) {
                                return false;
                            }
                            h.setArrowCountInEntity(v);
                            return true;
                        })
                    .create(Keys.WALKING_SPEED)
                        .get(h -> h.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue())
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(v);
                            return true;
                        })
                .asMutable(LivingEntityAccessor.class)
                    .create(Keys.LAST_DAMAGE_RECEIVED)
                        .get(h -> (double) h.accessor$getLastDamage())
                        .set((h, v) -> h.accessor$setLastDamage(v.floatValue()));
    }
    // @formatter:on
}
