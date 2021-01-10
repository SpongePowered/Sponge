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
package org.spongepowered.common.accessor.world.biome.provider;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;
import org.spongepowered.common.UntransformedInvokerError;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(NetherBiomeProvider.class)
public interface NetherBiomeProviderAccessor {

    @Accessor("DEFAULT_NOISE_PARAMETERS") static NetherBiomeProvider.Noise accessor$DEFAULT_NOISE_PARAMETERS() {
        throw new UntransformedAccessorError();
    }

    @Invoker("<init>") static NetherBiomeProvider invoker$new(final long seed, final List<Pair<Biome.Attributes, Supplier<Biome>>> attributedBiomes,
            final NetherBiomeProvider.Noise temperatureConfig, final NetherBiomeProvider.Noise humidityConfig, final NetherBiomeProvider.Noise altitudeConfig,
            final NetherBiomeProvider.Noise weirdnessConfig, final Optional<Pair<Registry<Biome>, NetherBiomeProvider.Preset>> empty) {
        throw new UntransformedInvokerError();
    }
}
