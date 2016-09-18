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

import co.aikar.timings.SpongeTimingsFactory;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.world.chunk.PopulateChunkEvent;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IChunkProviderOverworld;
import org.spongepowered.common.interfaces.world.gen.IFlaggedPopulator;
import org.spongepowered.common.interfaces.world.gen.IGenerationPopulator;
import org.spongepowered.common.util.gen.ChunkPrimerBuffer;
import org.spongepowered.common.util.gen.VirtualMutableBiomeBuffer;
import org.spongepowered.common.world.extent.SoftBufferExtentViewDownsize;
import org.spongepowered.common.world.gen.populators.SnowPopulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Similar class to {@link ChunkProviderOverworld}, but instead gets its blocks
 * from a custom chunk generator.
 */
public class SpongeChunkGenerator implements WorldGenerator, IChunkGenerator {

    private static final Vector2i CHUNK_AREA = new Vector2i(16, 16);

    protected BiomeGenerator biomeGenerator;
    protected GenerationPopulator baseGenerator;
    protected List<GenerationPopulator> genpop;
    protected List<Populator> pop;
    protected Map<BiomeType, BiomeGenerationSettings> biomeSettings;
    protected final World world;
    protected final VirtualMutableBiomeBuffer cachedBiomes;

    protected Random rand;
    private NoiseGeneratorPerlin noise4;
    private double[] stoneNoise;

    protected Map<String, Timing> populatorTimings = Maps.newHashMap();
    protected Timing chunkGeneratorTiming;

    public SpongeChunkGenerator(World world, GenerationPopulator base, BiomeGenerator biomegen) {
        this.world = checkNotNull(world, "world");
        this.baseGenerator = checkNotNull(base, "baseGenerator");
        this.biomeGenerator = checkNotNull(biomegen, "biomeGenerator");

        // Make initially empty biome cache
        this.cachedBiomes = new VirtualMutableBiomeBuffer(Vector2i.ZERO, CHUNK_AREA);

        this.genpop = Lists.newArrayList();
        this.pop = Lists.newArrayList();
        this.biomeSettings = Maps.newHashMap();
        this.rand = new Random(world.getSeed());
        this.noise4 = new NoiseGeneratorPerlin(this.rand, 4);
        this.stoneNoise = new double[256];

        this.world.provider.biomeProvider = CustomBiomeProvider.of(this.biomeGenerator);
        if (this.baseGenerator instanceof IChunkProviderOverworld) {
            ((IChunkProviderOverworld) this.baseGenerator).setBiomeGenerator(this.biomeGenerator);
        }

        if (!this.getClass().getSimpleName().equalsIgnoreCase("SpongeChunkProviderForge")) {
            String chunkGeneratorName = "";
            if (base instanceof SpongeGenerationPopulator) {
                chunkGeneratorName = "chunkGenerator (" + ((SpongeGenerationPopulator) base).getHandle(world).getClass().getSimpleName() + ")";
            } else {
                chunkGeneratorName = "chunkGenerator (" + base.getClass().getName() + ")";
            }
            this.chunkGeneratorTiming =
                    SpongeTimingsFactory.ofSafe(chunkGeneratorName, ((IMixinWorldServer) world).getTimingsHandler().chunkPopulate);
        }

    }

    @Override
    public GenerationPopulator getBaseGenerationPopulator() {
        return this.baseGenerator;
    }

    @Override
    public void setBaseGenerationPopulator(GenerationPopulator baseGenerationPopulator) {
        this.baseGenerator = baseGenerationPopulator;
        if (this.baseGenerator instanceof IChunkProviderOverworld) {
            ((IChunkProviderOverworld) this.baseGenerator).setBiomeGenerator(this.biomeGenerator);
        }
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators() {
        return this.genpop;
    }

    public void setGenerationPopulators(List<GenerationPopulator> generationPopulators) {
        this.genpop = Lists.newArrayList(generationPopulators);
    }

    @Override
    public List<Populator> getPopulators() {
        return this.pop;
    }

    public void setPopulators(List<Populator> populators) {
        this.pop = Lists.newArrayList(populators);
    }

    public Map<BiomeType, BiomeGenerationSettings> getBiomeOverrides() {
        return this.biomeSettings;
    }

    public void setBiomeOverrides(Map<BiomeType, BiomeGenerationSettings> biomeOverrides) {
        this.biomeSettings = Maps.newHashMap(biomeOverrides);
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomeGenerator) {
        this.biomeGenerator = biomeGenerator;
        this.world.provider.biomeProvider = CustomBiomeProvider.of(biomeGenerator);
        if (this.baseGenerator instanceof IChunkProviderOverworld) {
            ((IChunkProviderOverworld) this.baseGenerator).setBiomeGenerator(biomeGenerator);
        }
    }

    @Override
    public BiomeGenerationSettings getBiomeSettings(BiomeType type) {
        checkNotNull(type, "type");
        BiomeGenerationSettings settings = this.biomeSettings.get(type);
        if (settings == null) {
            settings = type.createDefaultGenerationSettings((org.spongepowered.api.world.World) this.world);
            this.biomeSettings.put(type, settings);
        }
        return settings;
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators(Class<? extends GenerationPopulator> type) {
        return this.genpop.stream().filter((p) -> type.isAssignableFrom(p.getClass())).collect(Collectors.toList());
    }

    @Override
    public List<Populator> getPopulators(Class<? extends Populator> type) {
        return this.pop.stream().filter((p) -> type.isAssignableFrom(p.getClass())).collect(Collectors.toList());
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {
        this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
        this.cachedBiomes.reuse(new Vector2i(chunkX * 16, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);
        ImmutableBiomeArea biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();

        // Generate base terrain
        ChunkPrimer chunkprimer = new ChunkPrimer();
        MutableBlockVolume blockBuffer = new ChunkPrimerBuffer(chunkprimer, chunkX, chunkZ);
        this.baseGenerator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);

        if (!(this.baseGenerator instanceof SpongeGenerationPopulator)) {
            replaceBiomeBlocks(this.world, this.rand, chunkX, chunkZ, chunkprimer, biomeBuffer);
        }

        // Apply the generator populators to complete the blockBuffer
        for (GenerationPopulator populator : this.genpop) {
            populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
        }

        // Get unique biomes to determine what generator populators to run
        List<BiomeType> uniqueBiomes = Lists.newArrayList();
        BiomeType biome;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                biome = this.cachedBiomes.getBiome(chunkX * 16 + x, chunkZ * 16 + z);
                if (!uniqueBiomes.contains(biome)) {
                    uniqueBiomes.add(biome);
                }
            }
        }

        // run our generator populators
        for (BiomeType type : uniqueBiomes) {
            if (!this.biomeSettings.containsKey(type)) {
                this.biomeSettings.put(type, type.createDefaultGenerationSettings((org.spongepowered.api.world.World) this.world));
            }
            for (GenerationPopulator populator : this.biomeSettings.get(type).getGenerationPopulators()) {
                populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
            }
        }

        // Assemble chunk
        Chunk chunk = new Chunk(this.world, chunkprimer, chunkX, chunkZ);
        this.cachedBiomes.fill(chunk.getBiomeArray());
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        IMixinWorldServer world = (IMixinWorldServer) this.world;
        world.getTimingsHandler().chunkPopulate.startTimingIfSync();
        this.chunkGeneratorTiming.startTimingIfSync();
        final CauseTracker causeTracker = world.getCauseTracker();
        final Cause populateCause = Cause.of(NamedCause.source(this));
        this.rand.setSeed(this.world.getSeed());
        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * i1 + chunkZ * j1 ^ this.world.getSeed());
        BlockFalling.fallInstantly = true;

        // Have to regeneate the biomes so that any virtual biomes can be passed
        // to the populator.
        this.cachedBiomes.reuse(new Vector2i(chunkX * 16, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);
        ImmutableBiomeArea biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();

        BlockPos blockpos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        BiomeType biome = (BiomeType) this.world.getBiome(blockpos.add(16, 0, 16));

        org.spongepowered.api.world.Chunk chunk = (org.spongepowered.api.world.Chunk) this.world.getChunkFromChunkCoords(chunkX, chunkZ);

        if (!this.biomeSettings.containsKey(biome)) {
            this.biomeSettings.put(biome, biome.createDefaultGenerationSettings((org.spongepowered.api.world.World) this.world));
        }

        List<Populator> populators = new ArrayList<>(this.pop);

        Populator snowPopulator = null;
        Iterator<Populator> itr = populators.iterator();
        while (itr.hasNext()) {
            Populator populator = itr.next();
            if (populator instanceof SnowPopulator) {
                itr.remove();
                snowPopulator = populator;
                break;
            }
        }

        populators.addAll(this.biomeSettings.get(biome).getPopulators());
        if (snowPopulator != null) {
            populators.add(snowPopulator);
        }

        Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPre(populateCause, populators, chunk));
        List<String> flags = Lists.newArrayList();
        Vector3i min = new Vector3i(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
        org.spongepowered.api.world.World spongeWorld = (org.spongepowered.api.world.World) this.world;
        Extent volume = new SoftBufferExtentViewDownsize(chunk.getWorld(), min, min.add(15, 255, 15), min.sub(8, 0, 8), min.add(23, 255, 23));
        for (Populator populator : populators) {
            final PopulatorType type = populator.getType();
            if (type == null) {
                System.err.printf("Found a populator with a null type: %s populator%n", populator);
            }
            if (Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPopulate(populateCause, populator, chunk))) {
                continue;
            }
            Timing timing = null;
            if (Timings.isTimingsEnabled()) {
                timing = this.populatorTimings.get(populator.getType().getId());
                if (timing == null) {
                    timing = SpongeTimingsFactory.ofSafe("populate - " + populator.getType().getId());// ,
                                                                                                      // this.chunkGeneratorTiming);
                    this.populatorTimings.put(populator.getType().getId(), timing);
                }
                timing.startTimingIfSync();
            }
            if (CauseTracker.ENABLED) {
                causeTracker.switchToPhase(GenerationPhase.State.POPULATOR_RUNNING, PhaseContext.start()
                        .add(NamedCause.of(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, type))
                        .addEntityCaptures()
                        .complete());
            }
            if (populator instanceof IFlaggedPopulator) {
                ((IFlaggedPopulator) populator).populate(spongeWorld, volume, this.rand, biomeBuffer, flags);
            } else {
                populator.populate(spongeWorld, volume, this.rand, biomeBuffer);
            }
            if (Timings.isTimingsEnabled()) {
                timing.stopTimingIfSync();
            }
            if (CauseTracker.ENABLED) {
                causeTracker.completePhase();
            }
        }

        // If we wrapped a custom chunk provider then we should call its
        // populate method so that its particular changes are used.
        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            Timing timing = null;
            if (Timings.isTimingsEnabled()) {
                IGenerationPopulator spongePopulator = (IGenerationPopulator) this.baseGenerator;
                timing = spongePopulator.getTimingsHandler();
                timing.startTimingIfSync();
            }
            ((SpongeGenerationPopulator) this.baseGenerator).getHandle(this.world).populate(chunkX, chunkZ);
            if (Timings.isTimingsEnabled()) {
                timing.stopTimingIfSync();
            }
        }

        PopulateChunkEvent.Post event = SpongeEventFactory.createPopulateChunkEventPost(populateCause, ImmutableList.copyOf(populators), chunk);
        SpongeImpl.postEvent(event);

        BlockFalling.fallInstantly = false;
        this.chunkGeneratorTiming.stopTimingIfSync();
        world.getTimingsHandler().chunkPopulate.stopTimingIfSync();
    }

    @Override
    public boolean generateStructures(Chunk chunk, int chunkX, int chunkZ) {
        boolean flag = false;
        if (chunk.getInhabitedTime() < 3600L) {
            for (Populator populator : this.pop) {
                if (populator instanceof StructureOceanMonument) {
                    final CauseTracker causeTracker = ((IMixinWorldServer) this.world).getCauseTracker();
                    if (CauseTracker.ENABLED) {
                        causeTracker.switchToPhase(GenerationPhase.State.POPULATOR_RUNNING, PhaseContext.start()
                                .add(NamedCause.of(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, populator.getType()))
                                .addEntityCaptures()
                                .complete());
                    }
                    flag |= ((StructureOceanMonument) populator).generateStructure(this.world, this.rand, new ChunkPos(chunkX, chunkZ));
                    if (CauseTracker.ENABLED) {
                        causeTracker.completePhase();
                    }
                }
            }
        }
        return flag;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (this.baseGenerator instanceof IChunkGenerator) {
            return ((IChunkGenerator) this.baseGenerator).getPossibleCreatures(creatureType, pos);
        }

        Biome biome = this.world.getBiome(pos);
        return biome.getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        if ("Stronghold".equals(structureName)) {
            for (GenerationPopulator gen : this.genpop) {
                if (gen instanceof MapGenStronghold) {
                    return ((MapGenStronghold) gen).getClosestStrongholdPos(worldIn, position);
                }
            }
        }
        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            return ((SpongeGenerationPopulator) this.baseGenerator).getHandle(this.world).getStrongholdGen(worldIn, structureName, position);
        }
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        if (this.baseGenerator instanceof IChunkGenerator) {
            ((IChunkGenerator) this.baseGenerator).recreateStructures(chunkIn, x, z);
            return;
        }
        for (GenerationPopulator populator : this.genpop) {
            if (populator instanceof MapGenStructure) {
                ((MapGenStructure) populator).generate(chunkIn.getWorld(), x, z, null);
            }
        }
    }

    public void replaceBiomeBlocks(World world, Random rand, int x, int z, ChunkPrimer chunk, ImmutableBiomeArea biomes) {
        double d0 = 0.03125D;
        this.stoneNoise = this.noise4.getRegion(this.stoneNoise, x * 16, z * 16, 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);
        Vector2i min = biomes.getBiomeMin();
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeType biomegenbase = biomes.getBiome(min.getX() + l, min.getY() + k);
                generateBiomeTerrain(world, rand, chunk, x * 16 + k, z * 16 + l, this.stoneNoise[l + k * 16],
                        getBiomeSettings(biomegenbase).getGroundCoverLayers());
            }
        }
    }

    public void generateBiomeTerrain(World worldIn, Random rand, ChunkPrimer chunk, int x, int z, double stoneNoise,
            List<GroundCoverLayer> groundcover) {
        if (groundcover.isEmpty()) {
            return;
        }
        int seaLevel = worldIn.getSeaLevel();
        IBlockState currentPlacement = null;
        int k = -1;
        int relativeX = x & 15;
        int relativeZ = z & 15;
        int i = 0;
        for (int currentY = 255; currentY >= 0; --currentY) {
            IBlockState nextBlock = chunk.getBlockState(relativeZ, currentY, relativeX);
            if (nextBlock.getMaterial() == Material.AIR) {
                k = -1;
            } else if (nextBlock.getBlock() == Blocks.STONE) {
                if (k == -1) {
                    if (groundcover.isEmpty()) {
                        k = 0;
                        continue;
                    }
                    i = 0;
                    GroundCoverLayer layer = groundcover.get(i);
                    currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                    k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                    if (k <= 0) {
                        continue;
                    }

                    if (currentY >= seaLevel - 1) {
                        chunk.setBlockState(relativeZ, currentY, relativeX, currentPlacement);
                        ++i;
                        if (i < groundcover.size()) {
                            layer = groundcover.get(i);
                            k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                            currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                        }
                    } else if (currentY < seaLevel - 7 - k) {
                        k = 0;
                        chunk.setBlockState(relativeZ, currentY, relativeX, Blocks.GRAVEL.getDefaultState());
                    } else {
                        ++i;
                        if (i < groundcover.size()) {
                            layer = groundcover.get(i);
                            k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                            currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                            chunk.setBlockState(relativeZ, currentY, relativeX, currentPlacement);
                        }
                    }
                } else if (k > 0) {
                    --k;
                    chunk.setBlockState(relativeZ, currentY, relativeX, currentPlacement);

                    if (k == 0) {
                        ++i;
                        if (i < groundcover.size()) {
                            GroundCoverLayer layer = groundcover.get(i);
                            k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                            currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                        }
                    }
                }
            }
        }
    }

}
