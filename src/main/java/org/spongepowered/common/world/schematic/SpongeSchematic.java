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
package org.spongepowered.common.world.schematic;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class SpongeSchematic extends SpongeArchetypeVolume implements Schematic {

    @Nullable
    private final MutableBiomeVolume biomes;
    private final Palette<BiomeType> biomePalette;
    @Nullable private final Palette<BlockState> overriddenBlockPalette;
    private DataView metadata;

    SpongeSchematic(SpongeSchematicBuilder builder) {
        super(builder.backingVolume, builder.tiles, builder.entities);
        this.metadata = builder.metadata;
        this.biomes = builder.biomeVolume;
        this.biomePalette = builder.biomePalette;
        if (this.getPalette().getType() != builder.blockPalette.getType()) {
            this.overriddenBlockPalette = builder.blockPalette;
        } else {
            this.overriddenBlockPalette = null;
        }

    }

    @Override
    public void apply(Location<World> location, BlockChangeFlag changeFlag) {
        super.apply(location, changeFlag);
        if (this.biomes != null) {
            this.biomes.getBiomeWorker().iterate((v, x, y, z) -> {
                location.getExtent().setBiome(x + location.getBlockX(), y + location.getBlockY(), z + location.getBlockZ(), v.getBiome(x, y, z));
            });
        }
    }

    @Override
    public org.spongepowered.api.world.schematic.BlockPalette getPalette() {
        return (org.spongepowered.api.world.schematic.BlockPalette) (this.overriddenBlockPalette == null
                                                                     ? super.getPalette()
                                                                     : this.overriddenBlockPalette);
    }

    @Override
    public DataView getMetadata() {
        return this.metadata;
    }

    @Override
    public MutableBlockVolumeWorker<Schematic> getBlockWorker() {
        return new SpongeMutableBlockVolumeWorker<>(this);
    }

    @Override
    public Palette<BlockState> getBlockPalette() {
        return getPalette();
    }

    @Override
    public Palette<BiomeType> getBiomePalette() {
        return this.biomePalette;
    }

    @Override
    public Optional<MutableBiomeVolume> getBiomes() {
        return Optional.ofNullable(this.biomes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SpongeSchematic that = (SpongeSchematic) o;
        return Objects.equals(this.biomes, that.biomes) &&
               this.biomePalette.equals(that.biomePalette) &&
               Objects.equals(this.overriddenBlockPalette, that.overriddenBlockPalette) &&
               this.metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.biomes, this.biomePalette, this.overriddenBlockPalette, this.metadata);
    }
}
