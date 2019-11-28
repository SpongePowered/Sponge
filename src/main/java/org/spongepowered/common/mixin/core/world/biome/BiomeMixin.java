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
package org.spongepowered.common.mixin.core.world.biome;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.biome.BiomeBridge;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.WorldGenConstants;

import javax.annotation.Nullable;

@Mixin(Biome.class)
public abstract class BiomeMixin implements BiomeBridge {

    @Shadow public BlockState topBlock;
    @Shadow public BlockState fillerBlock;
    @Shadow public BiomeDecorator decorator;

    @Nullable @MonotonicNonNull private String impl$id;
    @Nullable @MonotonicNonNull private String impl$modId;

    @Override
    public void bridge$buildPopulators(final World world, final SpongeBiomeGenerationSettings gensettings) {
        WorldGenConstants.buildPopulators(world, gensettings, this.decorator, this.topBlock, this.fillerBlock);

    }

    @Inject(method = "registerBiome", at = @At("HEAD"))
    private static void onRegisterBiome(final int id, final String name, final Biome biome, final CallbackInfo ci) {
        final String modId = SpongeImplHooks.getModIdFromClass(biome.getClass());
        final String biomeName = name.toLowerCase().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");

        ((BiomeBridge) biome).bridge$setModId(modId);
        ((BiomeBridge) biome).bridge$setId(modId + ":" + biomeName);
    }

    @Override
    public void bridge$setId(final String id) {
        checkState(this.impl$id == null, "Attempt made to set ID!");

        this.impl$id = id;
    }

    @Override
    public String bridge$getId() {
        return checkNotNull(this.impl$id, "BiomeType id is null");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public String bridge$getModId() {
        return this.impl$modId;
    }

    @Override
    public void bridge$setModId(final String modId) {
        checkState(this.impl$modId == null || "unknown".equals(this.impl$modId), "Attempt made to set Mod ID!");

        this.impl$modId = modId;
    }

}
