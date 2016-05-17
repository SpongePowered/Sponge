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
package org.spongepowered.common.world.gen;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector2i;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.IntCache;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.util.gen.ObjectArrayMutableBiomeBuffer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Implementation of {@link BiomeProvider} based on a {@link BiomeGenerator}.
 *
 * <p>This class does the opposite of {@link SpongeBiomeGenerator}, that class
 * wraps a world chunk manager so that it is usable as a {@link BiomeGenerator}
 * .</p>
 */
public final class CustomBiomeProvider extends BiomeProvider {

    private static final Vector2i CACHED_AREA_SIZE = new Vector2i(40, 40);

    private final ByteArrayMutableBiomeBuffer areaForGeneration = new ByteArrayMutableBiomeBuffer(Vector2i.ZERO, CACHED_AREA_SIZE);
    private final BiomeGenerator biomeGenerator;

    /**
     * Gets a world chunk manager based on the given biome generator.
     *
     * @param biomeGenerator
     *            The biome generator.
     * @return The world chunk manager.
     */
    public static BiomeProvider of(BiomeGenerator biomeGenerator) {
        if (biomeGenerator instanceof BiomeProvider) {
            return ((BiomeProvider) biomeGenerator);
        }
        // Biome generator set to some custom implementation
        return new CustomBiomeProvider(biomeGenerator);
    }

    private CustomBiomeProvider(BiomeGenerator biomeGenerator) {
        this.biomeGenerator = checkNotNull(biomeGenerator, "biomeGenerator");
    }

    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    /**
     * Return a list of biomes for the specified blocks. Args: listToReuse, x,
     * y, width, length, cacheFlag (if false, don't check biomeCache to avoid
     * infinite loop in BiomeCacheBlock)
     *
     * @param cacheFlag
     *            If false, don't check biomeCache to avoid infinite loop in
     *            BiomeCacheBlock
     */
    @Override
    public Biome[] getBiomeGenAt(Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.loadBlockGeneratorData(listToReuse, x, z, width, length);
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomeArrayZoomedOut, int xStart, int zStart, int xSize, int zSize) {
        // "Biomes for generation" are a 4x zoomed out (on both the x and z
        // axis) version of the normal biomes
        // The easiest way to obtain these biomes is to obtain the normal
        // scale biomes and then downscale them

        if (biomeArrayZoomedOut == null || biomeArrayZoomedOut.length < xSize * zSize) {
            biomeArrayZoomedOut = new Biome[xSize * zSize];
        }

        // Transform to normal scale
        int xStartBlock = (xStart + 2) * 4;
        int zStartBlock = (zStart + 2) * 4;
        int xSizeBlock = xSize * 4;
        int zSizeBlock = zSize * 4;

        // Get biomes
        ByteArrayMutableBiomeBuffer buffer = getBiomeBuffer(xStartBlock, zStartBlock, xSizeBlock, zSizeBlock);
        this.biomeGenerator.generateBiomes(buffer);

        // Downscale
        byte[] biomesForBlocks = buffer.detach();
        for (int i = 0; i < biomeArrayZoomedOut.length; i++) {
            biomeArrayZoomedOut[i] = Biome.getBiome(biomesForBlocks[i * 4] & 0xff, Biomes.OCEAN);
        }

        return biomeArrayZoomedOut;
    }

    private ByteArrayMutableBiomeBuffer getBiomeBuffer(int xStart, int zStart, int xSize, int zSize) {
        if (xSize == CACHED_AREA_SIZE.getX() && zSize == CACHED_AREA_SIZE.getY() && this.areaForGeneration.isDetached()) {
            this.areaForGeneration.reuse(new Vector2i(xStart, zStart));
            return this.areaForGeneration;
        } else {
            return new ByteArrayMutableBiomeBuffer(new Vector2i(xStart, zStart), new Vector2i(xSize, zSize));
        }
    }

    @Override
    public boolean areBiomesViable(int xCenter, int zCenter, int range, @SuppressWarnings("rawtypes") List searchingForBiomes) {
        IntCache.resetIntCache();
        int xStartSegment = xCenter - range >> 2;
        int zStartSegment = zCenter - range >> 2;
        int xMaxSegment = xCenter + range >> 2;
        int zMaxSegment = zCenter + range >> 2;
        int xSizeSegments = xMaxSegment - xStartSegment + 1;
        int zSizeSegments = zMaxSegment - zStartSegment + 1;

        ByteArrayMutableBiomeBuffer buffer = getBiomeBuffer(xStartSegment << 2, zStartSegment << 2, xSizeSegments << 2, zSizeSegments << 2);
        this.biomeGenerator.generateBiomes(buffer);
        byte[] biomes = buffer.detach();

        for (int i = 0; i < xSizeSegments * zSizeSegments; ++i) {
            Biome biome = Biome.getBiome(biomes[i << 2] & 0xff);

            if (!searchingForBiomes.contains(biome)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockPos findBiomePosition(int xCenter, int zCenter, int range, List<Biome> biomes, Random random) {
        IntCache.resetIntCache();
        int xStartSegment = xCenter - range >> 2;
        int zStartSegment = zCenter - range >> 2;
        int xMaxSegment = xCenter + range >> 2;
        int zMaxSegment = zCenter + range >> 2;
        int xSizeSegments = xMaxSegment - xStartSegment + 1;
        int zSizeSegments = zMaxSegment - zStartSegment + 1;

        ByteArrayMutableBiomeBuffer buffer = getBiomeBuffer(xStartSegment << 2, zStartSegment << 2, xSizeSegments << 2, zSizeSegments << 2);
        this.biomeGenerator.generateBiomes(buffer);
        byte[] aint = buffer.detach();

        BlockPos blockpos = null;
        int k1 = 0;

        for (int l1 = 0; l1 < xSizeSegments * zSizeSegments; ++l1)
        {
            int i2 = xStartSegment + l1 % xSizeSegments << 2;
            int j2 = zStartSegment + l1 / xSizeSegments << 2;
            Biome biomegenbase = Biome.getBiome(aint[l1]);

            if (biomes.contains(biomegenbase) && (blockpos == null || random.nextInt(k1 + 1) == 0))
            {
                blockpos = new BlockPos(i2, 0, j2);
                ++k1;
            }
        }

        return blockpos;
    }

    @Override
    public Biome[] loadBlockGeneratorData(Biome[] biomeArray, int startX, int startZ, int sizeX, int sizeZ) {
        if (biomeArray == null || biomeArray.length < sizeX * sizeZ) {
            biomeArray = new Biome[sizeX * sizeZ];
        } else {
            // Biome generators don't have to set every position. If we set
            // all positions to ocean first, every position not set will be
            // ocean, and not some random biome from the last time this array
            // was used
            Arrays.fill(biomeArray, Biomes.OCEAN);
        }

        MutableBiomeArea biomeArea = new ObjectArrayMutableBiomeBuffer(biomeArray, new Vector2i(startX, startZ), new Vector2i(sizeX, sizeZ));
        this.biomeGenerator.generateBiomes(biomeArea);

        return biomeArray;
    }

}
