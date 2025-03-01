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
package org.spongepowered.common.world.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.dimension.LevelStem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.data.fixer.SpongeDataCodec;
import org.spongepowered.common.serialization.EnumCodec;
import org.spongepowered.common.serialization.MathCodecs;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public record SpongeWorldTemplate(ResourceKey key, LevelStem levelStem) {

    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            SpongeAdventure.STRING_CODEC.optionalFieldOf("display_name").forGetter(v -> Optional.ofNullable(v.displayName)),
                            ResourceLocation.CODEC.optionalFieldOf("game_mode").forGetter(v -> Optional.ofNullable(v.gameMode).map(t -> ResourceLocation.fromNamespaceAndPath("sponge", t.getName()))),
                            ResourceLocation.CODEC.optionalFieldOf("difficulty").forGetter(v -> Optional.ofNullable(v.difficulty).map(t -> ResourceLocation.fromNamespaceAndPath("sponge", t.getKey()))),
                            EnumCodec.create(SerializationBehavior.class).optionalFieldOf("serialization_behavior")
                                    .forGetter(v -> Optional.ofNullable(v.serializationBehavior)),
                            Codec.INT.optionalFieldOf("view_distance").forGetter(v -> Optional.ofNullable(v.viewDistance)),
                            MathCodecs.VECTOR_3i.optionalFieldOf("spawn_position").forGetter(v -> Optional.ofNullable(v.spawnPosition)),
                            Codec.BOOL.optionalFieldOf("load_on_startup").forGetter(v -> Optional.ofNullable(v.loadOnStartup)),
                            Codec.BOOL.optionalFieldOf("performs_spawn_logic").forGetter(v -> Optional.ofNullable(v.performsSpawnLogic)),
                            Codec.BOOL.optionalFieldOf("hardcore").forGetter(v -> Optional.ofNullable(v.hardcore)),
                            Codec.BOOL.optionalFieldOf("commands").forGetter(v -> Optional.ofNullable(v.commands)),
                            Codec.BOOL.optionalFieldOf("pvp").forGetter(v -> Optional.ofNullable(v.pvp)),
                            Codec.LONG.optionalFieldOf("seed").forGetter(v -> Optional.ofNullable(v.seed))
                    )
                    // *Chuckles* I continue to be in danger...
                    .apply(r, (f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12) ->
                            new SpongeDataSection(f1.orElse(null),
                                    f2.map(l -> GameType.byName(l.getPath())).orElse(null),
                                    f3.map(l -> Difficulty.byName(l.getPath())).orElse(null),
                                    f4.orElse(null), f5.orElse(null), f6.orElse(null),
                                    f7.orElse(null), f8.orElse(null), f9.orElse(null),
                                    f10.orElse(null), f11.orElse(null), f12.orElse(null))
                    )
            );

    public static final Codec<LevelStem> DIRECT_CODEC = new MapCodec.MapCodecCodec<LevelStem>(new SpongeDataCodec<>(LevelStem.CODEC,
            SpongeWorldTemplate.SPONGE_CODEC, (type, data) -> ((LevelStemBridge) (Object) type).bridge$decorateData(data),
            type -> null));

    public record SpongeDataSection(@Nullable Component displayName,
                                    @Nullable GameType gameMode,
                                    @Nullable Difficulty difficulty,
                                    @Nullable SerializationBehavior serializationBehavior,
                                    @Nullable Integer viewDistance,
                                    @Nullable Vector3i spawnPosition,
                                    @Nullable Boolean loadOnStartup,
                                    @Nullable Boolean performsSpawnLogic,
                                    @Nullable Boolean hardcore,
                                    @Nullable Boolean commands,
                                    @Nullable Boolean pvp,
                                    @Nullable Long seed) {
    }
}
