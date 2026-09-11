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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen;

import com.mojang.serialization.JavaOps;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.world.generation.config.noise.Noise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.util.DataPackUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(NormalNoise.class)
public abstract class NormalNoiseMixin_API implements Noise {

    // ponytail: NormalNoise.Parameters is a private record, so read it back through the codec instead of an access widener
    @SuppressWarnings("unchecked")
    private Map<String, Object> api$parameters() {
        return (Map<String, Object>) NormalNoise.DIRECT_CODEC.encodeStart(JavaOps.INSTANCE, (NormalNoise) (Object) this).getOrThrow();
    }

    @Override
    public int octave() {
        return ((Number) this.api$parameters().get("base_octave")).intValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Double> amplitudes() {
        final Map<String, Object> parameters = this.api$parameters();
        final List<Double> modifiers = (List<Double>) parameters.getOrDefault("amplitude_modifiers", List.of());
        if (!modifiers.isEmpty()) {
            return modifiers;
        }
        return Collections.nCopies(((Number) parameters.getOrDefault("octave_count", 1)).intValue(), 1.0);
    }

    @Override
    public Optional<DataContainer> toDataPack(final RegistryHolder registryHolder) {
        return DataPackUtil.toDataContainer(registryHolder, NormalNoise.DIRECT_CODEC, (NormalNoise) (Object) this);
    }
}
