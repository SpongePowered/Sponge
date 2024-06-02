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

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.common.accessor.world.entity.player.AbilitiesAccessor;
import org.spongepowered.common.accessor.world.entity.player.PlayerAccessor;
import org.spongepowered.common.accessor.world.food.FoodDataAccessor;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.ExperienceHolderUtil;

public final class PlayerData {

    private static final double EXHAUSTION_MAX = 40.0;
    private static final double SATURATION_MAX = 40.0;
    private static final int FOOD_LEVEL_MAX = 20;

    private PlayerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Player.class)
                    .create(Keys.CAN_FLY)
                        .get(h -> h.getAbilities().mayfly)
                        .set((h, v) -> {
                            h.getAbilities().mayfly = v;
                            h.onUpdateAbilities();
                        })
                    .create(Keys.DOMINANT_HAND)
                        .get(h -> (HandPreference) (Object) h.getMainArm())
                        .set((h, v) -> h.setMainArm((HumanoidArm) (Object) v))
                    .create(Keys.EXHAUSTION)
                        .get(h -> (double) h.getFoodData().getExhaustionLevel())
                        .set((h, v) -> ((FoodDataAccessor) h.getFoodData()).accessor$exhaustionLevel(v.floatValue()))
                    .create(Keys.EXPERIENCE)
                        .get(h -> h.totalExperience)
                        .set(ExperienceHolderUtil::setExperience)
                        .delete(h -> ExperienceHolderUtil.setExperience(h, 0))
                    .create(Keys.EXPERIENCE_FROM_START_OF_LEVEL)
                        .get(Player::getXpNeededForNextLevel)
                    .create(Keys.EXPERIENCE_LEVEL)
                        .get(h -> h.experienceLevel)
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.totalExperience = ExperienceHolderUtil.xpAtLevel(v);
                            h.experienceProgress = 0;
                            h.experienceLevel = v;
                            ((ServerPlayerBridge) h).bridge$refreshExp();
                            return true;
                        })
                    .create(Keys.EXPERIENCE_SINCE_LEVEL)
                        .get(h -> ((PlayerBridge) h).bridge$getExperienceSinceLevel())
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            ExperienceHolderUtil.setExperienceSinceLevel(h, v);
                            return true;
                        })
                        .delete(h -> ExperienceHolderUtil.setExperience(h, 0))
                    .create(Keys.FLYING_SPEED)
                        .get(h -> (double) h.getAbilities().getFlyingSpeed())
                        .set((h, v) -> {
                            ((AbilitiesAccessor) h.getAbilities()).accessor$flyingSpeed(v.floatValue());
                            h.onUpdateAbilities();
                        })
                    .create(Keys.FOOD_LEVEL)
                        .get(h -> h.getFoodData().getFoodLevel())
                        .set((h, v) -> ((FoodDataAccessor) h.getFoodData()).accessor$foodLevel(v))
                    .create(Keys.IS_FLYING)
                        .get(h -> h.getAbilities().flying)
                        .set((h, v) -> {
                            h.getAbilities().flying = v;
                            h.onUpdateAbilities();
                        })
                    .create(Keys.IS_SLEEPING)
                        .get(Player::isSleeping)
                    .create(Keys.MAX_EXHAUSTION)
                        .get(h -> PlayerData.EXHAUSTION_MAX)
                    .create(Keys.MAX_FOOD_LEVEL)
                        .get(h -> PlayerData.FOOD_LEVEL_MAX)
                    .create(Keys.MAX_SATURATION)
                        .get(h -> PlayerData.SATURATION_MAX)
                    .create(Keys.SATURATION)
                        .get(h -> (double) h.getFoodData().getSaturationLevel())
                        .set((h, v) -> ((FoodDataAccessor) h.getFoodData()).accessor$saturationLevel(v.floatValue()))
                    .create(Keys.SLEEP_TIMER)
                        .get(Player::getSleepTimer)
                        .set((p, i) -> ((PlayerAccessor) p).accessor$sleepCounter(i))
                    .create(Keys.WALKING_SPEED)
                        .get(h -> (double) h.getAbilities().getWalkingSpeed())
                        .set((h, v) -> {
                            ((AbilitiesAccessor) h.getAbilities()).accessor$walkingSpeed(v.floatValue());
                            h.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(v);
                            h.onUpdateAbilities();
                        })
                .asMutable(PlayerBridge.class)
                    .create(Keys.AFFECTS_SPAWNING)
                        .get(PlayerBridge::bridge$affectsSpawning)
                        .set(PlayerBridge::bridge$setAffectsSpawning);
    }
    // @formatter:on
}
