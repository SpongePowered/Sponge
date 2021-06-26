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
package org.spongepowered.common.mixin.api.minecraft.world.level.dimension;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.biome.BiomeSampler;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.registry.provider.DimensionEffectProvider;
import org.spongepowered.common.util.SpongeMinecraftDayTime;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.dimension.DimensionType;

@Mixin(DimensionType.class)
@Implements(@Interface(iface = WorldType.class, prefix = "worldType$"))
public abstract class DimensionTypeMixin_API implements WorldType {

    // @formatter:off
    @Shadow @Final private ResourceLocation effectsLocation;
    @Shadow @Final private float ambientLight;
    @Shadow @Final private OptionalLong fixedTime;

    @Shadow public abstract BiomeZoomer shadow$getBiomeZoomer();
    @Shadow public abstract boolean shadow$ultraWarm();
    @Shadow public abstract boolean shadow$natural();
    @Shadow public abstract double shadow$coordinateScale();
    @Shadow public abstract boolean shadow$hasSkyLight();
    @Shadow public abstract boolean shadow$hasCeiling();
    @Shadow public abstract boolean shadow$piglinSafe();
    @Shadow public abstract boolean shadow$bedWorks();
    @Shadow public abstract boolean shadow$respawnAnchorWorks();
    @Shadow public abstract boolean shadow$hasRaids();
    @Shadow public abstract int shadow$logicalHeight();
    @Shadow public abstract boolean shadow$createDragonFight();
    // @formatter:on

    @Nullable private Context api$context;

    @Override
    public Context context() {
        if (this.api$context == null) {
            final ResourceLocation key = SpongeCommon.server().registryAccess().dimensionTypes().getKey((DimensionType) (Object) this);
            this.api$context = new Context(Context.DIMENSION_KEY, key.getPath());
        }

        return this.api$context;
    }

    @Override
    public WorldTypeEffect effect() {
        @Nullable final WorldTypeEffect effect = DimensionEffectProvider.INSTANCE.get((ResourceKey) (Object) this.effectsLocation);
        if (effect == null) {
            throw new IllegalStateException(String.format("The effect '%s' has not been registered!", this.effectsLocation));
        }
        return effect;
    }

    @Override
    public BiomeSampler biomeSampler() {
        return (BiomeSampler) this.shadow$getBiomeZoomer();
    }

    @Override
    public boolean scorching() {
        return this.shadow$ultraWarm();
    }

    @Intrinsic
    public boolean worldType$natural() {
        return this.shadow$natural();
    }

    @Override
    public double coordinateMultiplier() {
        return this.shadow$coordinateScale();
    }

    @Override
    public boolean hasSkylight() {
        return this.shadow$hasSkyLight();
    }

    @Intrinsic
    public boolean worldType$hasCeiling() {
        return this.shadow$hasCeiling();
    }

    @Override
    public float ambientLighting() {
        return this.ambientLight;
    }

    @Override
    public Optional<MinecraftDayTime> fixedTime() {
        final OptionalLong fixedTime = this.fixedTime;
        if (!fixedTime.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new SpongeMinecraftDayTime(fixedTime.getAsLong()));
    }

    @Intrinsic
    public boolean worldType$piglinSafe() {
        return this.shadow$piglinSafe();
    }

    @Override
    public boolean bedsUsable() {
        return this.shadow$bedWorks();
    }

    @Override
    public boolean respawnAnchorsUsable() {
        return this.shadow$respawnAnchorWorks();
    }

    @Override
    public WorldTypeTemplate asTemplate() {
        return new SpongeWorldTypeTemplate((ResourceKey) (Object) SpongeCommon.server().registryAccess().dimensionTypes().getKey((DimensionType) (Object) this), (DimensionType) (Object) this);
    }

    @Intrinsic
    public boolean worldType$hasRaids() {
        return this.shadow$hasRaids();
    }

    @Intrinsic
    public int worldType$logicalHeight() {
        return this.shadow$logicalHeight();
    }

    @Intrinsic
    public boolean worldType$createDragonFight() {
        return this.shadow$createDragonFight();
    }
}
