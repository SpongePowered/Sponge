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
package org.spongepowered.vanilla.generator.world.level.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.Tilt;
import org.spongepowered.vanilla.generator.BlockStateDataProviderGenerator;
import org.spongepowered.vanilla.generator.BlockStatePropertiesGenerator;
import org.spongepowered.vanilla.generator.BlockStatePropertyKeysGenerator;
import org.spongepowered.vanilla.generator.Context;
import org.spongepowered.vanilla.generator.EnumEntriesValidator;
import org.spongepowered.vanilla.generator.Generator;
import org.spongepowered.vanilla.generator.RegistryEntriesGenerator;
import org.spongepowered.vanilla.generator.RegistryScope;

import java.util.List;

public class BlockRegistries {

    public static List<Generator> enumRegistries(final Context context) {
        return List.<Generator>of(
            new EnumEntriesValidator<>(
                "data.type",
                "BambooLeavesTypes",
                BambooLeaves.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "InstrumentTypes",
                NoteBlockInstrument.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "DripstoneSegments",
                DripstoneThickness.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "Tilts",
                Tilt.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "SculkSensorStates",
                SculkSensorPhase.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "TrialSpawnerStates",
                TrialSpawnerState.class,
                "getSerializedName",
                "sponge"
            )
        );
    }

    public static List<Generator> registries(final Context context) {
        return List.<Generator>of(
            new BlockStateDataProviderGenerator(),
            new BlockStatePropertiesGenerator(),
            new BlockStatePropertyKeysGenerator(),
            new RegistryEntriesGenerator<>(
                "block",
                "BlockTypes",
                "BLOCK_TYPE",
                context.relativeClass("block", "BlockType"),
                Registries.BLOCK
            ),
            new RegistryEntriesGenerator<>(
                "block.entity",
                "BlockEntityTypes",
                "BLOCK_ENTITY_TYPE",
                context.relativeClass("block.entity", "BlockEntityType"),
                Registries.BLOCK_ENTITY_TYPE
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "BannerPatternShapes",
                "BANNER_PATTERN_SHAPE",
                context.relativeClass("data.type", "BannerPatternShape"),
                Registries.BANNER_PATTERN,
                $ -> true,
                RegistryScope.SERVER
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "CreakingHearts",
                CreakingHeartBlock.CreakingHeartState.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "world.explosion",
                "ExplosionBlockInteractions",
                Explosion.BlockInteraction.class,
                "name",
                "sponge"
            )
        );
    }
}
