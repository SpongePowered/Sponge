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
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.util.gen.ChunkPrimerBuffer;

import java.util.List;
import java.util.Random;

/**
 * Similar class to {@link ChunkProviderGenerate}, but instead gets its blocks
 * from a custom chunk generator.
 */
public final class CustomChunkProviderGenerate implements IChunkProvider {

    private static final Vector2i CHUNK_AREA = new Vector2i(16, 16);

    private final BiomeGenerator biomeGenerator;
    private final GeneratorPopulator baseGenerator;
    private final List<GeneratorPopulator> generatorPopulators;
    private final World world;
    private final ByteArrayMutableBiomeBuffer cachedBiomes;

    /**
     * Gets the chunk generator from the given generator populator and biome
     * generator.
     *
     * @param world The world to bind the chunk provider to.
     * @param biomeGenerator Biome generator used to generate chunks.
     * @param baseGenerator The base generator
     * @param generatorPopulators The generator populators
     * @return The chunk generator.
     * @throws IllegalArgumentException If the generator populator cannot be
     *         bound to the given world.
     */
    public static IChunkProvider of(World world, BiomeGenerator biomeGenerator, GeneratorPopulator baseGenerator,
        List<GeneratorPopulator> generatorPopulators) {
        if (baseGenerator instanceof SpongeGeneratorPopulator) {
            // Unwrap instead of wrap
            return ((SpongeGeneratorPopulator) baseGenerator).getHandle(world);
        }
        // Wrap a custom GeneratorPopulator implementation
        return new CustomChunkProviderGenerate(world, biomeGenerator, baseGenerator, generatorPopulators);
    }

    private CustomChunkProviderGenerate(World world, BiomeGenerator biomeGenerator, GeneratorPopulator baseGenerator,
        List<GeneratorPopulator> generatorPopulators) {
        this.world = checkNotNull(world, "world");
        this.baseGenerator = checkNotNull(baseGenerator, "baseGenerator");
        this.biomeGenerator = checkNotNull(biomeGenerator, "biomeGenerator");
        this.generatorPopulators = checkNotNull(generatorPopulators, "generatorPopulators");

        // Make initially empty biome cache
        this.cachedBiomes = new ByteArrayMutableBiomeBuffer(Vector2i.ZERO, CHUNK_AREA);
        this.cachedBiomes.detach();
    }

    public GeneratorPopulator getBaseGenerator() {
        return this.baseGenerator;
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
        Random random = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
        BlockFalling.fallInstantly = true;

        // Calling the events makes the Sponge-added populators fire
        // TODO Deamon says he'll figure this one out soon

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean func_177460_a(IChunkProvider chunkProvider, Chunk chunk, int chunkX, int chunkZ) {
        // This method normally places ocean monuments in existing chunks
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        BiomeGenBase biome = this.world.getBiomeGenForCoords(pos);
        @SuppressWarnings("unchecked")
        List<SpawnListEntry> creatures = biome.getSpawnableList(creatureType);
        return creatures;
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        // Maybe allow to register stronghold locations?
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {
        // No structure support
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {

        // Generate biomes
        this.cachedBiomes.reuse(new Vector2i(chunkX << 4, chunkZ << 4));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);

        // Generate base terrain
        ChunkPrimer chunkprimer = new ChunkPrimer();
        MutableBlockVolume blockBuffer = new ChunkPrimerBuffer(chunkprimer, chunkX, chunkZ);
        ImmutableBiomeArea biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();
        this.baseGenerator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);

        // Apply the generator populators to complete the blockBuffer
        for (GeneratorPopulator populator : this.generatorPopulators) {
            populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
        }

        // Assemble chunk
        Chunk chunk = new Chunk(this.world, chunkprimer, chunkX, chunkZ);
        byte[] biomeArray = chunk.getBiomeArray();
        System.arraycopy(this.cachedBiomes.detach(), 0, biomeArray, 0, biomeArray.length);
        chunk.generateSkylightMap();

        return chunk;
    }

    // Methods below are simply mirrors of the methods in ChunkProviderGenerate

    @Override
    public Chunk provideChunk(BlockPos blockPosIn) {
        return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public String makeString() {
        return "RandomLevelSource";
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public boolean saveChunks(boolean bool, IProgressUpdate progressUpdate) {
        return true;
    }

    @Override
    public void saveExtraData() {
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

}
