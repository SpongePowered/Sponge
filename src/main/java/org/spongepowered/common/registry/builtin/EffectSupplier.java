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
package org.spongepowered.common.registry.builtin;

import net.minecraft.potion.Effects;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class EffectSupplier {

    private EffectSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(PotionEffectType.class, "speed", () -> (PotionEffectType) Effects.SPEED)
            .registerSupplier(PotionEffectType.class, "slowness", () -> (PotionEffectType) Effects.SLOWNESS)
            .registerSupplier(PotionEffectType.class, "haste", () -> (PotionEffectType) Effects.HASTE)
            .registerSupplier(PotionEffectType.class, "mining_fatigue", () -> (PotionEffectType) Effects.MINING_FATIGUE)
            .registerSupplier(PotionEffectType.class, "strength", () -> (PotionEffectType) Effects.STRENGTH)
            .registerSupplier(PotionEffectType.class, "instant_health", () -> (PotionEffectType) Effects.INSTANT_HEALTH)
            .registerSupplier(PotionEffectType.class, "instant_damage", () -> (PotionEffectType) Effects.INSTANT_DAMAGE)
            .registerSupplier(PotionEffectType.class, "jump_boost", () -> (PotionEffectType) Effects.JUMP_BOOST)
            .registerSupplier(PotionEffectType.class, "nausea", () -> (PotionEffectType) Effects.NAUSEA)
            .registerSupplier(PotionEffectType.class, "regeneration", () -> (PotionEffectType) Effects.REGENERATION)
            .registerSupplier(PotionEffectType.class, "resistance", () -> (PotionEffectType) Effects.RESISTANCE)
            .registerSupplier(PotionEffectType.class, "fire_resistance", () -> (PotionEffectType) Effects.FIRE_RESISTANCE)
            .registerSupplier(PotionEffectType.class, "water_breathing", () -> (PotionEffectType) Effects.WATER_BREATHING)
            .registerSupplier(PotionEffectType.class, "invisibility", () -> (PotionEffectType) Effects.INVISIBILITY)
            .registerSupplier(PotionEffectType.class, "blindness", () -> (PotionEffectType) Effects.BLINDNESS)
            .registerSupplier(PotionEffectType.class, "night_vision", () -> (PotionEffectType) Effects.NIGHT_VISION)
            .registerSupplier(PotionEffectType.class, "hunger", () -> (PotionEffectType) Effects.HUNGER)
            .registerSupplier(PotionEffectType.class, "weakness", () -> (PotionEffectType) Effects.WEAKNESS)
            .registerSupplier(PotionEffectType.class, "poison", () -> (PotionEffectType) Effects.POISON)
            .registerSupplier(PotionEffectType.class, "wither", () -> (PotionEffectType) Effects.WITHER)
            .registerSupplier(PotionEffectType.class, "health_boost", () -> (PotionEffectType) Effects.HEALTH_BOOST)
            .registerSupplier(PotionEffectType.class, "absorption", () -> (PotionEffectType) Effects.ABSORPTION)
            .registerSupplier(PotionEffectType.class, "saturation", () -> (PotionEffectType) Effects.SATURATION)
            .registerSupplier(PotionEffectType.class, "glowing", () -> (PotionEffectType) Effects.GLOWING)
            .registerSupplier(PotionEffectType.class, "levitation", () -> (PotionEffectType) Effects.LEVITATION)
            .registerSupplier(PotionEffectType.class, "luck", () -> (PotionEffectType) Effects.LUCK)
            .registerSupplier(PotionEffectType.class, "unluck", () -> (PotionEffectType) Effects.UNLUCK)
            .registerSupplier(PotionEffectType.class, "slow_falling", () -> (PotionEffectType) Effects.SLOW_FALLING)
            .registerSupplier(PotionEffectType.class, "conduit_power", () -> (PotionEffectType) Effects.CONDUIT_POWER)
            .registerSupplier(PotionEffectType.class, "dolphins_grace", () -> (PotionEffectType) Effects.DOLPHINS_GRACE)
            .registerSupplier(PotionEffectType.class, "bad_omen", () -> (PotionEffectType) Effects.BAD_OMEN)
        ;
    }
}
