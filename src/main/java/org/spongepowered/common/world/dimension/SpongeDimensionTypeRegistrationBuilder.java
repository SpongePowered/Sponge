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
package org.spongepowered.common.world.dimension;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.DefaultBiomeMagnifier;
import net.minecraft.world.biome.IBiomeMagnifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.world.dimension.DimensionEffect;
import org.spongepowered.api.world.dimension.DimensionEffects;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypeRegistration;
import org.spongepowered.common.accessor.world.DimensionTypeAccessor;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

import java.util.Objects;
import java.util.OptionalLong;

public final class SpongeDimensionTypeRegistrationBuilder extends AbstractResourceKeyedBuilder<DimensionTypeRegistration,
        DimensionTypeRegistration.Builder> implements DimensionTypeRegistration.Builder {

    private DimensionEffect effect = DimensionEffects.OVERWORLD;
    private MinecraftDayTime fixedTime;
    private IBiomeMagnifier biomeMagnifier = DefaultBiomeMagnifier.INSTANCE;
    private ITag.INamedTag<Block> infiniburn = BlockTags.INFINIBURN_OVERWORLD;

    private boolean scorching, natural, skylight, ceiling, piglinZombify, bedsUsable, respawnAnchorUsable, omenTriggerRaids;
    private float ambientLighting;
    private int maxTeleportTransferHeight;
    private double coordinateMultiplier;

    @Override
    public DimensionType.Builder setEffect(final DimensionEffect effect) {
        Objects.requireNonNull(effect, "effect");
        this.effect = effect;
        return this;
    }

    @Override
    public DimensionType.Builder setScorching(final boolean scorching) {
        this.scorching = scorching;
        return this;
    }

    @Override
    public DimensionType.Builder setNatural(final boolean natural) {
        this.natural = natural;
        return this;
    }

    @Override
    public DimensionType.Builder setCoordinateMultiplier(final double coordinateMultiplier) {
        this.coordinateMultiplier = coordinateMultiplier;
        return this;
    }

    @Override
    public DimensionType.Builder setHasSkylight(final boolean skylight) {
        this.skylight = skylight;
        return this;
    }

    @Override
    public DimensionType.Builder setHasCeiling(final boolean ceiling) {
        this.ceiling = ceiling;
        return this;
    }

    @Override
    public DimensionType.Builder setAmbientLighting(final float ambientLighting) {
        this.ambientLighting = ambientLighting;
        return this;
    }

    @Override
    public DimensionType.Builder setFixedTime(@Nullable final MinecraftDayTime fixedTime) {
        this.fixedTime = fixedTime;
        return this;
    }

    @Override
    public DimensionType.Builder setPiglinZombify(final boolean piglinZombify) {
        this.piglinZombify = piglinZombify;
        return this;
    }

    @Override
    public DimensionType.Builder setBedsUsable(final boolean bedsUsable) {
        this.bedsUsable = bedsUsable;
        return this;
    }

    @Override
    public DimensionType.Builder setRespawnAnchorUsable(final boolean respawnAnchorUsable) {
        this.respawnAnchorUsable = respawnAnchorUsable;
        return this;
    }

    @Override
    public DimensionType.Builder setOmenTriggerRaids(final boolean omenTriggerRaids) {
        this.omenTriggerRaids = omenTriggerRaids;
        return this;
    }

    @Override
    public DimensionType.Builder setMaxTeleportTransferHeight(final int maxTeleportTransferHeight) {
        this.maxTeleportTransferHeight = maxTeleportTransferHeight;
        return this;
    }

    @Override
    public DimensionType.Builder from(final DimensionType value) {
        Objects.requireNonNull(value, "value");

        this.effect = value.effect();
        this.fixedTime = value.fixedTime().orElse(null);
        this.biomeMagnifier = ((net.minecraft.world.DimensionType) value).getBiomeZoomer();
        this.infiniburn = (ITag.INamedTag<Block>) ((net.minecraft.world.DimensionType) value).infiniburn();
        this.scorching = value.scorching();
        this.natural = value.natural();
        this.skylight = value.hasSkylight();
        this.ceiling = value.hasCeiling();
        this.piglinZombify = value.piglinZombify();
        this.bedsUsable = value.bedUsable();
        this.respawnAnchorUsable = value.respawnAnchorUsable();
        this.omenTriggerRaids = value.omenTriggersRaids();
        this.ambientLighting = value.ambientLighting();
        this.maxTeleportTransferHeight = value.maxTeleportTransferHeight();
        this.coordinateMultiplier = value.coordinateMultiplier();
        return this;
    }

    @Override
    public @NonNull DimensionTypeRegistration build0() {
        final OptionalLong fixedTime = this.fixedTime == null ? OptionalLong.empty() : OptionalLong.of(this.fixedTime.asTicks().getTicks());

        final net.minecraft.world.DimensionType dimensionType = DimensionTypeAccessor.invoker$construct(fixedTime, this.skylight, this.ceiling,
                this.scorching, this.natural, this.coordinateMultiplier, false, !this.piglinZombify, this.bedsUsable,
                this.respawnAnchorUsable, this.omenTriggerRaids, this.maxTeleportTransferHeight, this.biomeMagnifier, this.infiniburn.getName(),
                (ResourceLocation) (Object) this.effect.getKey(), this.ambientLighting
        );

        return (DimensionType) dimensionType;
    }
}
