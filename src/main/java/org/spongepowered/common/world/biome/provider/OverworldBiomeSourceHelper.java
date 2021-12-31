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
package org.spongepowered.common.world.biome.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import org.spongepowered.common.bridge.world.level.biome.OverworldBiomeSourceBridge;
import org.spongepowered.common.data.fixer.SpongeDataCodec;

public final class OverworldBiomeSourceHelper {

    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            Biome.LIST_CODEC.optionalFieldOf("biomes").forGetter(v -> Optional.of(v.biomes))
                    )
                    .apply(r, f1 -> new SpongeDataSection(f1.orElse(new ArrayList<>())))
            );

    public static final Codec<OverworldBiomeSource> DIRECT_CODEC = new MapCodec.MapCodecCodec<>(new SpongeDataCodec<>(OverworldBiomeSource.CODEC,
        OverworldBiomeSourceHelper.SPONGE_CODEC, (type, data) -> ((OverworldBiomeSourceBridge) type).bridge$decorateData(data),
        type -> ((OverworldBiomeSourceBridge) type).bridge$createData()));

    public static final class SpongeDataSection {
        public final List<Supplier<Biome>> biomes;

        public SpongeDataSection(final List<Supplier<Biome>> biomes) {
            this.biomes = biomes;
        }
    }

}
