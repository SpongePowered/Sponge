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
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.data.fixer.SpongeDataCodec;

public final class SpongeDimensionTypes {

    // TODO handle empty sponge tag
    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
        .create(r -> r
            .group(
                Codec.BOOL.optionalFieldOf("create_dragon_fight", Boolean.FALSE).forGetter(v -> v.createDragonFight)
            )
            .apply(r, r.stable(SpongeDataSection::new))
        );


    public static Codec<DimensionType> DIRECT_CODEC;

    public static Codec<DimensionType> injectCodec(Codec<DimensionType> vanillaCodec) {
        DIRECT_CODEC = new MapCodec.MapCodecCodec<DimensionType>(new SpongeDataCodec<>(vanillaCodec, SpongeDimensionTypes.SPONGE_CODEC,
                (type, data) -> ((DimensionTypeBridge) (Object) type).bridge$decorateData(data),
                type -> ((DimensionTypeBridge) (Object) type).bridge$createData()));
        return DIRECT_CODEC;
    }

    public record SpongeDataSection(boolean createDragonFight) {}
}
