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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.WorldCarverWrapper;
import net.minecraft.world.gen.feature.CompositeFeature;
import net.minecraft.world.gen.feature.CompositeFlowerFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.surfacebuilders.CompositeSurfaceBuilder;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.world.biome.IMixinBiome;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(Biome.class)
public abstract class MixinBiome implements BiomeType, IMixinBiome {

    /*
    All of these fields are built by the biome configurations.
     */
    @Shadow @Nullable protected String translationKey;
    /** The base height of this biome. Default 0.1. */
    @Shadow @Final protected float depth;
    /** The variation from the base height of the biome. Default 0.3. */
    @Shadow @Final protected float scale;
    /** The temperature of this biome. */
    @Shadow @Final protected float temperature;
    /** The rainfall in this biome. */
    @Shadow @Final protected float downfall;
    /** Color tint applied to water depending on biome */
    @Shadow @Final protected int waterColor;
    @Shadow @Final protected int waterFogColor;
    /** The unique identifier of the biome for which this is a mutation of. */
    @Shadow @Final @Nullable protected String parent;
    @Shadow @Final protected CompositeSurfaceBuilder<?> surfaceBuilder;
    @Shadow @Final protected Biome.Category category;
    @Shadow @Final protected Biome.RainType precipitation;
    @Shadow @Final protected Map<GenerationStage.Carving, List<WorldCarverWrapper<?>>> carvers;
    @Shadow @Final protected Map<GenerationStage.Decoration, List<CompositeFeature<?, ?>>> features;
    @Shadow @Final protected List<CompositeFlowerFeature<?>> flowers;
    @Shadow @Final protected Map<Structure<?>, IFeatureConfig> structures;
    @Shadow @Final private Map<EnumCreatureType, List<Biome.SpawnListEntry>> spawns;

    private CatalogKey id;
    private String modId;
    private String biomeName;

    @Inject(method = "register", at = @At("HEAD"))
    private static void onRegisterBiome(int id, String name, Biome biome, CallbackInfo ci) {
        final String modId = SpongeImplHooks.getModIdFromClass(biome.getClass());
        final String biomeName = name.toLowerCase().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");

        ((IMixinBiome) biome).setModId(modId);
        ((IMixinBiome) biome).setId(CatalogKey.of(modId, biomeName));
    }

    @Override
    public String getName() {
        return this.biomeName;
    }

    @Override
    public CatalogKey getKey() {
        return this.id;
    }

    @Override
    public void setId(CatalogKey id) {
        checkState(this.id == null, "Attempt made to set ID!");

        this.id = id;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public void setModId(String modId) {
        checkState(this.modId == null, "Attempt made to set Mod ID!");

        this.modId = modId;
    }

    @Override
    public double getTemperature() {
        return this.temperature;
    }

    @Override
    public double getHumidity() {
        return this.downfall;
    }
}
