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
package org.spongepowered.common.mixin.api.mcp.world.gen;

import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import org.spongepowered.api.world.biome.BiomeProvider;
import org.spongepowered.api.world.generation.ChunkGeneratorTemplate;
import org.spongepowered.api.world.generation.settings.structure.StructureGenerationSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.generation.SpongeChunkGeneratorTemplate;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin_API implements org.spongepowered.api.world.generation.ChunkGenerator {

    // @formatter:off
    @Shadow public abstract net.minecraft.world.biome.provider.BiomeProvider shadow$getBiomeSource();
    @Shadow public abstract DimensionStructuresSettings shadow$getSettings();
    // @formatter:on

    @Override
    public BiomeProvider biomeProvider() {
        return (BiomeProvider) this.shadow$getBiomeSource();
    }

    @Override
    public StructureGenerationSettings structureSettings() {
        return (StructureGenerationSettings) this.shadow$getSettings();
    }

    @Override
    public ChunkGeneratorTemplate asTemplate() {
        return new SpongeChunkGeneratorTemplate((ChunkGenerator) (Object) this);
    }
}
