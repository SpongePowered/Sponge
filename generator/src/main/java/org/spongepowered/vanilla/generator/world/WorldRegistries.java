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
package org.spongepowered.vanilla.generator.world;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.scores.DisplaySlot;
import org.spongepowered.vanilla.generator.Context;
import org.spongepowered.vanilla.generator.EnumEntriesValidator;
import org.spongepowered.vanilla.generator.Generator;
import org.spongepowered.vanilla.generator.RegistryEntriesGenerator;
import org.spongepowered.vanilla.generator.RegistryEntriesValidator;
import org.spongepowered.vanilla.generator.RegistryScope;

import java.util.List;

public class WorldRegistries {

    public static List<Generator> worldRegistries(final Context context) {
        return List.<Generator>of(
            new EnumEntriesValidator<>(
                "world.difficulty",
                "Difficulties",
                Difficulty.class,
                "getSerializedName",
                "sponge"
            ),
            new RegistryEntriesGenerator<>(
                "effect.particle",
                "ParticleTypes",
                "PARTICLE_TYPE",
                context.relativeClass("effect.particle", "ParticleType"),
                Registries.PARTICLE_TYPE
            ),
            new RegistryEntriesGenerator<>(
                "effect.potion",
                "PotionEffectTypes",
                "POTION_EFFECT_TYPE",
                context.relativeClass("effect.potion", "PotionEffectType"),
                Registries.MOB_EFFECT
            ),
            new RegistryEntriesGenerator<>(
                "effect.sound",
                "SoundTypes",
                "SOUND_TYPE",
                context.relativeClass("effect.sound", "SoundType"),
                Registries.SOUND_EVENT
            ),
            new RegistryEntriesGenerator<>(
                "statistic",
                "Statistics",
                "STATISTIC",
                context.relativeClass("statistic", "Statistic"),
                Registries.CUSTOM_STAT
            ),
            new RegistryEntriesValidator<>(
                "statistic",
                "StatisticCategories",
                Registries.STAT_TYPE
            ),
            new RegistryEntriesGenerator<>(
                "advancement.criteria.trigger",
                "Triggers",
                "TRIGGER",
                ParameterizedTypeName.get(context.relativeClass("advancement.criteria.trigger", "Trigger"), WildcardTypeName.subtypeOf(Object.class)),
                Registries.TRIGGER_TYPE,
                $ -> true,
                RegistryScope.GAME
            ),
            new RegistryEntriesGenerator<>(
                "event.cause.entity.damage",
                "DamageTypes",
                "DAMAGE_TYPE",
                context.relativeClass("event.cause.entity.damage", "DamageType"),
                Registries.DAMAGE_TYPE,
                a -> true, RegistryScope.SERVER
            ),
            new EnumEntriesValidator<>(
                "event.cause.entity.damage",
                "DamageEffects",
                DamageEffects.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "event.cause.entity.damage",
                "DamageScalings",
                DamageScaling.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "scoreboard.displayslot",
                "DisplaySlots",
                DisplaySlot.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "PushReactions",
                PushReaction.class,
                "name",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "advancement",
                "AdvancementTypes",
                AdvancementType.class,
                "getSerializedName",
                "sponge"
            )
        );
    }
}
