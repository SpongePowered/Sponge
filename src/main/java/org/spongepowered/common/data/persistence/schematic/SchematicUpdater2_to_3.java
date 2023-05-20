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
package org.spongepowered.common.data.persistence.schematic;

import net.minecraft.core.registries.Registries;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.schematic.MutableBimapPalette;
import org.spongepowered.common.world.schematic.SchematicTranslator;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.biome.ByteArrayMutableBiomeBuffer;
import org.spongepowered.math.vector.Vector3i;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO - Migrate this to DataFixer DSL in 1.14.
public final class SchematicUpdater2_to_3 implements DataContentUpdater {

    @Override
    public int inputVersion() {
        return 2;
    }

    @Override
    public int outputVersion() {
        return 3;
    }

    @Override
    public DataView update(final DataView content) {
        // Move BlockData, Palette, BlockEntities -> Blocks.Data, Blocks.Palette, and Blocks.BlockEntities
        content.getView(Constants.Sponge.Schematic.Versions.V2_BLOCK_PALETTE).ifPresent(dataView -> {
            content.remove(Constants.Sponge.Schematic.Versions.V2_BLOCK_PALETTE);
            final byte[] blockData = (byte[]) content.get(Constants.Sponge.Schematic.Versions.V2_BLOCK_DATA)
                .orElseThrow(() -> new InvalidDataException("Missing BlockData for Schematic"));
            content.remove(Constants.Sponge.Schematic.Versions.V2_BLOCK_DATA);
            final List<DataView> blockEntities = content.getViewList(Constants.Sponge.Schematic.BLOCKENTITY_CONTAINER).orElse(
                Collections.emptyList());
            for (final DataView blockEntity : blockEntities) {
                for (final Map.Entry<DataQuery, Object> entry : blockEntity.values(false).entrySet()) {
                    final DataQuery key = entry.getKey();
                    if (key.equals(Constants.Sponge.Schematic.BLOCKENTITY_POS) || key.equals(Constants.Sponge.Schematic.BLOCKENTITY_ID)) {
                        continue;
                    }

                    blockEntity.remove(key);
                    blockEntity.set(Constants.Sponge.Schematic.BLOCKENTITY_DATA.then(key), entry.getValue());
                }
            }
            content.remove(Constants.Sponge.Schematic.BLOCKENTITY_CONTAINER);
            final DataView blockContainer = content.createView(Constants.Sponge.Schematic.BLOCK_CONTAINER);
            blockContainer.set(Constants.Sponge.Schematic.BLOCK_DATA, blockData);
            blockContainer.set(Constants.Sponge.Schematic.BLOCK_PALETTE, dataView);
            blockContainer.set(Constants.Sponge.Schematic.BLOCKENTITY_CONTAINER, blockEntities);
        });

        // Move BiomeData, BiomePalette -> Biomes.Data and Biomes.Palette
        content.get(Constants.Sponge.Schematic.Versions.V2_BIOME_DATA).ifPresent(biomeData -> {
            content.remove(Constants.Sponge.Schematic.Versions.V2_BIOME_DATA);
            // But first, convert from a 2D array to a 3D array, which basically means almost fully deserializing
            // the entirety of the biome into a new buffer.

            final int[] offset = (int[]) content.get(Constants.Sponge.Schematic.OFFSET).orElse(new int[3]);
            if (offset.length != 3) {
                throw new InvalidDataException("Schematic offset was not of length 3");
            }
            final int xOffset = offset[0];
            final int yOffset = offset[1];
            final int zOffset = offset[2];
            final DataView palette = content.getView(Constants.Sponge.Schematic.Versions.V2_BIOME_PALETTE)
                .orElseThrow(() -> new InvalidDataException("Missing Biome Palette for schematic"));
            final int width = content.getShort(Constants.Sponge.Schematic.WIDTH)
                .orElseThrow(() -> new InvalidDataException("Missing value for: " + Constants.Sponge.Schematic.WIDTH));
            final int height = content.getShort(Constants.Sponge.Schematic.HEIGHT)
                .orElseThrow(() -> new InvalidDataException("Missing value for: " + Constants.Sponge.Schematic.HEIGHT));
            final int length = content.getShort(Constants.Sponge.Schematic.LENGTH)
                .orElseThrow(() -> new InvalidDataException("Missing value for: " + Constants.Sponge.Schematic.LENGTH));

            final Set<DataQuery> biomeKeys = palette.keys(false);
            final Registry<Biome> biomeRegistry = VolumeStreamUtils.nativeToSpongeRegistry(SpongeCommon.vanillaRegistry(Registries.BIOME));
            final MutableBimapPalette<Biome, Biome> biomePalette = new MutableBimapPalette<>(
                PaletteTypes.BIOME_PALETTE.get(),
                biomeRegistry,
                RegistryTypes.BIOME,
                biomeKeys.size()
            );
            final ByteArrayMutableBiomeBuffer biomeBuffer = new ByteArrayMutableBiomeBuffer(
                biomePalette,
                new Vector3i(-xOffset, -yOffset, -zOffset),
                new Vector3i(width, height, length)
            );
            final DataView biomeView = content.createView(Constants.Sponge.Schematic.BIOME_CONTAINER);
            biomeView.set(Constants.Sponge.Schematic.BLOCK_PALETTE, palette);
            final byte[] biomes = (byte[]) biomeData;
            int biomeIndex = 0;
            int biomeJ= 0;
            int bVal = 0;
            int varIntLength = 0;
            final int yMin = biomeBuffer.min().y();
            final int yMax = biomeBuffer.max().y();

            while (biomeJ < biomes.length) {
                bVal = 0;
                varIntLength = 0;

                while (true) {
                    bVal |= (biomes[biomeJ] & 127) << (varIntLength++ * 7);
                    if (varIntLength > 5) {
                        throw new RuntimeException("VarInt too big (probably corrupted data)");
                    }
                    if (((biomes[biomeJ] & 128) != 128)) {
                        biomeJ++;
                        break;
                    }
                    biomeJ++;
                }
                final int z = (biomeIndex % (width * length)) / width;
                final int x = (biomeIndex % (width * length)) % width;
                final Biome type = biomePalette.get(bVal, Sponge.server()).get();
                // Stupid to do this, but eh, for all y positions, they have the same biome, this'll work.

                for (int y = yMin; y <= yMax; y++) {
                    biomeBuffer.setBiome(x - xOffset, y, z - zOffset, type);
                }

                biomeIndex++;
            }
            try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length)) {
                final int xMin = biomeBuffer.min().x();
                final int zMin = biomeBuffer.min().z();
                for (int y = 0; y < height; y++) {
                    final int y0 = yMin + y;
                    for (int z = 0; z < length; z++) {
                        final int z0 = zMin + z;
                        for (int x = 0; x < width; x++) {
                            final int x0 = xMin + x;
                            final Biome biome = biomeBuffer.biome(x0, y0, z0);
                            SchematicTranslator.writeIdToBuffer(buffer, biomePalette.orAssign(biome));
                        }

                    }
                }
                content.set(Constants.Sponge.Schematic.BIOME_DATA, buffer.toByteArray());
            } catch (final IOException e) {
                // Should never reach here.
            }
            content.remove(Constants.Sponge.Schematic.Versions.V2_BIOME_PALETTE);
        });
        content.set(Constants.Sponge.Schematic.VERSION, 3);
        return content;
    }

}
