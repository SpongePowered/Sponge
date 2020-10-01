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
package org.spongepowered.common.registry.builtin.vanilla;

import com.google.common.collect.ImmutableMap;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.effect.particle.NumericalParticleType;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class ParticleTypeSupplier {

    private ParticleTypeSupplier() {
    }

    // @formatter:off

    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(ParticleType.class, "ambient_entity_effect", () -> (ParticleType) ParticleTypes.AMBIENT_ENTITY_EFFECT)
            .registerSupplier(ParticleType.class, "angry_villager", () -> (ParticleType) ParticleTypes.ANGRY_VILLAGER)
            .registerSupplier(ParticleType.class, "barrier", () -> (ParticleType) ParticleTypes.BARRIER)
            .registerSupplier(ParticleType.class, "block", () -> (ParticleType) ParticleTypes.BLOCK)
            .registerSupplier(ParticleType.class, "bubble", () -> (ParticleType) ParticleTypes.BUBBLE)
            .registerSupplier(ParticleType.class, "cloud", () -> (ParticleType) ParticleTypes.CLOUD)
            .registerSupplier(ParticleType.class, "crit", () -> (ParticleType) ParticleTypes.CRIT)
            .registerSupplier(ParticleType.class, "damage_indicator", () -> (ParticleType) ParticleTypes.DAMAGE_INDICATOR)
            .registerSupplier(ParticleType.class, "dragon_breath", () -> (ParticleType) ParticleTypes.DRAGON_BREATH)
            .registerSupplier(ParticleType.class, "dripping_lava", () -> (ParticleType) ParticleTypes.DRIPPING_LAVA)
            .registerSupplier(ParticleType.class, "falling_lava", () -> (ParticleType) ParticleTypes.FALLING_LAVA)
            .registerSupplier(ParticleType.class, "landing_lava", () -> (ParticleType) ParticleTypes.LANDING_LAVA)
            .registerSupplier(ParticleType.class, "dripping_water", () -> (ParticleType) ParticleTypes.DRIPPING_WATER)
            .registerSupplier(ParticleType.class, "falling_water", () -> (ParticleType) ParticleTypes.FALLING_WATER)
            .registerSupplier(ParticleType.class, "dust", () -> (ParticleType) ParticleTypes.DUST)
            .registerSupplier(ParticleType.class, "effect", () -> (ParticleType) ParticleTypes.EFFECT)
            .registerSupplier(ParticleType.class, "elder_guardian", () -> (ParticleType) ParticleTypes.ELDER_GUARDIAN)
            .registerSupplier(ParticleType.class, "enchanted_hit", () -> (ParticleType) ParticleTypes.ENCHANTED_HIT)
            .registerSupplier(ParticleType.class, "enchant", () -> (ParticleType) ParticleTypes.ENCHANT)
            .registerSupplier(ParticleType.class, "end_rod", () -> (ParticleType) ParticleTypes.END_ROD)
            .registerSupplier(ParticleType.class, "entity_effect", () -> (ParticleType) ParticleTypes.ENTITY_EFFECT)
            .registerSupplier(ParticleType.class, "explosion_emitter", () -> (ParticleType) ParticleTypes.EXPLOSION_EMITTER)
            .registerSupplier(ParticleType.class, "explosion", () -> (ParticleType) ParticleTypes.EXPLOSION)
            .registerSupplier(ParticleType.class, "falling_dust", () -> (ParticleType) ParticleTypes.FALLING_DUST)
            .registerSupplier(ParticleType.class, "firework", () -> (ParticleType) ParticleTypes.FIREWORK)
            .registerSupplier(ParticleType.class, "fishing", () -> (ParticleType) ParticleTypes.FISHING)
            .registerSupplier(ParticleType.class, "flame", () -> (ParticleType) ParticleTypes.FLAME)
            .registerSupplier(ParticleType.class, "flash", () -> (ParticleType) ParticleTypes.FLASH)
            .registerSupplier(ParticleType.class, "happy_villager", () -> (ParticleType) ParticleTypes.HAPPY_VILLAGER)
            .registerSupplier(ParticleType.class, "composter", () -> (ParticleType) ParticleTypes.COMPOSTER)
            .registerSupplier(ParticleType.class, "heart", () -> (ParticleType) ParticleTypes.HEART)
            .registerSupplier(ParticleType.class, "instant_effect", () -> (ParticleType) ParticleTypes.INSTANT_EFFECT)
            .registerSupplier(ParticleType.class, "item", () -> (ParticleType) ParticleTypes.ITEM)
            .registerSupplier(ParticleType.class, "item_slime", () -> (ParticleType) ParticleTypes.ITEM_SLIME)
            .registerSupplier(ParticleType.class, "item_snowball", () -> (ParticleType) ParticleTypes.ITEM_SNOWBALL)
            .registerSupplier(ParticleType.class, "large_smoke", () -> (ParticleType) ParticleTypes.LARGE_SMOKE)
            .registerSupplier(ParticleType.class, "lava", () -> (ParticleType) ParticleTypes.LAVA)
            .registerSupplier(ParticleType.class, "mycelium", () -> (ParticleType) ParticleTypes.MYCELIUM)
            .registerSupplier(ParticleType.class, "note", () -> (ParticleType) ParticleTypes.NOTE)
            .registerSupplier(ParticleType.class, "poof", () -> (ParticleType) ParticleTypes.POOF)
            .registerSupplier(ParticleType.class, "portal", () -> (ParticleType) ParticleTypes.PORTAL)
            .registerSupplier(ParticleType.class, "rain", () -> (ParticleType) ParticleTypes.RAIN)
            .registerSupplier(ParticleType.class, "smoke", () -> (ParticleType) ParticleTypes.SMOKE)
            .registerSupplier(ParticleType.class, "sneeze", () -> (ParticleType) ParticleTypes.SNEEZE)
            .registerSupplier(ParticleType.class, "spit", () -> (ParticleType) ParticleTypes.SPIT)
            .registerSupplier(ParticleType.class, "squid_ink", () -> (ParticleType) ParticleTypes.SQUID_INK)
            .registerSupplier(ParticleType.class, "sweep_attack", () -> (ParticleType) ParticleTypes.SWEEP_ATTACK)
            .registerSupplier(ParticleType.class, "totem_of_undying", () -> (ParticleType) ParticleTypes.TOTEM_OF_UNDYING)
            .registerSupplier(ParticleType.class, "underwater", () -> (ParticleType) ParticleTypes.UNDERWATER)
            .registerSupplier(ParticleType.class, "splash", () -> (ParticleType) ParticleTypes.SPLASH)
            .registerSupplier(ParticleType.class, "witch", () -> (ParticleType) ParticleTypes.WITCH)
            .registerSupplier(ParticleType.class, "bubble_pop", () -> (ParticleType) ParticleTypes.BUBBLE_POP)
            .registerSupplier(ParticleType.class, "current_down", () -> (ParticleType) ParticleTypes.CURRENT_DOWN)
            .registerSupplier(ParticleType.class, "bubble_column_up", () -> (ParticleType) ParticleTypes.BUBBLE_COLUMN_UP)
            .registerSupplier(ParticleType.class, "nautilus", () -> (ParticleType) ParticleTypes.NAUTILUS)
            .registerSupplier(ParticleType.class, "dolphin", () -> (ParticleType) ParticleTypes.DOLPHIN)
            .registerSupplier(ParticleType.class, "campfire_cosy_smoke", () -> (ParticleType) ParticleTypes.CAMPFIRE_COSY_SMOKE)
            .registerSupplier(ParticleType.class, "campfire_signal_smoke", () -> (ParticleType) ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)
            .registerSupplier(ParticleType.class, "dripping_honey", () -> (ParticleType) ParticleTypes.DRIPPING_HONEY)
            .registerSupplier(ParticleType.class, "falling_honey", () -> (ParticleType) ParticleTypes.FALLING_HONEY)
            .registerSupplier(ParticleType.class, "landing_honey", () -> (ParticleType) ParticleTypes.LANDING_HONEY)
            .registerSupplier(ParticleType.class, "falling_nectar", () -> (ParticleType) ParticleTypes.FALLING_NECTAR)
        ;

        registry
            .registerCatalogAndSupplier(ParticleType.class, "fire_smoke",
                () -> new NumericalParticleType(2000, ResourceKey.sponge("fire_smoke"), ImmutableMap.of(ParticleOptions.DIRECTION.get(), Direction.UP),
                        effect -> {
                            final Direction direction = effect.getOptionOrDefault(ParticleOptions.DIRECTION).get();
                            return SpongeParticleHelper.getDirectionId(direction);
                        }))
            .registerCatalogAndSupplier(ParticleType.class, "break_block",
                () -> new NumericalParticleType(2001, ResourceKey.sponge("break_block"), ImmutableMap.of(ParticleOptions.BLOCK_STATE.get(), BlockTypes.AIR.get().getDefaultState()),
                        effect -> SpongeParticleHelper.getBlockStateId(effect, effect.getType().getDefaultOption(ParticleOptions.BLOCK_STATE))))
            .registerCatalogAndSupplier(ParticleType.class, "splash_potion",
                () -> new NumericalParticleType(2002, ResourceKey.sponge("splash_potion"), ImmutableMap.of(ParticleOptions.POTION_EFFECT_TYPE.get(), PotionEffectTypes.ABSORPTION.get()),
                        effect -> {
                            final Effect effectType = (Effect) effect.getOptionOrDefault(ParticleOptions.POTION_EFFECT_TYPE).get();
                            for (final Potion potionType : Registry.POTION) {
                                for (final EffectInstance potionEffect : potionType.getEffects()) {
                                    if (potionEffect.getPotion() == effectType) {
                                        return Registry.POTION.getId(potionType);
                                    }
                                }
                            }
                            return 0;
                        }))
            .registerCatalogAndSupplier(ParticleType.class, "break_eye_of_ender",
                () -> new NumericalParticleType(2003, ResourceKey.sponge("break_eye_of_ender")))
            .registerCatalogAndSupplier(ParticleType.class, "mobspawner_flames",
                () -> new NumericalParticleType(2004, ResourceKey.sponge("mobspawner_flames")))
            .registerCatalogAndSupplier(ParticleType.class, "fertilizer",
                () -> new NumericalParticleType(2005, ResourceKey.sponge("fertilizer"), ImmutableMap.of(ParticleOptions.QUANTITY.get(), 1),
                        effect -> effect.getOptionOrDefault(ParticleOptions.QUANTITY).get()))
            .registerCatalogAndSupplier(ParticleType.class, "dragon_breath_attack",
                () -> new NumericalParticleType(2006, ResourceKey.sponge("dragon_breath_attack")))
        ;
    }
}
