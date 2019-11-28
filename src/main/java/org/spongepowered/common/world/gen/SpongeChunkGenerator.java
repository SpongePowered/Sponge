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

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.feature.EndCityStructure;
import net.minecraft.world.gen.feature.FortressStructure;
import net.minecraft.world.gen.feature.MineshaftStructure;
import net.minecraft.world.gen.feature.OceanMonumentStructure;
import net.minecraft.world.gen.feature.ScatteredStructure;
import net.minecraft.world.gen.feature.StrongholdStructure;
import net.minecraft.world.gen.feature.Structure;
import net.minecraft.world.gen.feature.VillageStructure;
import net.minecraft.world.gen.feature.WoodlandMansionStructure;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.PopulateChunkEvent;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.gen.ChunkGeneratorOverworldBridge;
import org.spongepowered.common.bridge.world.gen.FlaggedPopulatorBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.generation.PopulatorPhaseContext;
import org.spongepowered.common.mixin.core.world.WorldProviderAccessor;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.util.gen.ChunkPrimerBuffer;
import org.spongepowered.common.util.gen.ObjectArrayMutableBiomeBuffer;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
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
 * Similar class to {@link ChunkGeneratorOverworld}, but instead gets its blocks
 * from a custom chunk generator.
 */
public class SpongeChunkGenerator implements WorldGenerator, ChunkGenerator {

    private static final Vector3i CHUNK_AREA = new Vector3i(16, 1, 16);

    protected BiomeGenerator biomeGenerator;
    protected GenerationPopulator baseGenerator;
    protected List<GenerationPopulator> genpop;
    protected List<Populator> pop;
    protected Map<BiomeType, BiomeGenerationSettings> biomeSettings;
    protected final World world;
    protected final ObjectArrayMutableBiomeBuffer cachedBiomes;

    protected Random rand;
    private PerlinNoiseGenerator noise4;
    private double[] stoneNoise;

    protected Map<String, Timing> populatorTimings = Maps.newHashMap();
    protected Timing chunkGeneratorTiming;

    public SpongeChunkGenerator(World world, GenerationPopulator base, BiomeGenerator biomegen) {
        this.world = checkNotNull(world, "world");
        this.baseGenerator = checkNotNull(base, "baseGenerator");
        this.biomeGenerator = checkNotNull(biomegen, "biomeGenerator");

        // Make initially empty biome cache
        this.cachedBiomes = new ObjectArrayMutableBiomeBuffer(Vector3i.ZERO, CHUNK_AREA);

        this.genpop = Lists.newArrayList();
        this.pop = Lists.newArrayList();
        this.biomeSettings = Maps.newHashMap();
        this.rand = new Random(world.getSeed());
        this.noise4 = new PerlinNoiseGenerator(this.rand, 4);
        this.stoneNoise = new double[256];

        ((WorldProviderAccessor) this.world.dimension).accessor$setBiomeProvider(CustomBiomeProvider.of(this.biomeGenerator));
        if (this.baseGenerator instanceof ChunkGeneratorOverworldBridge) {
            ((ChunkGeneratorOverworldBridge) this.baseGenerator).bridge$setBiomeGenerator(this.biomeGenerator);
        }

        if (!this.getClass().getSimpleName().equalsIgnoreCase("SpongeChunkProviderForge")) {
            String chunkGeneratorName = "";
            if (base instanceof SpongeGenerationPopulator) {
                chunkGeneratorName = "chunkGenerator (" + ((SpongeGenerationPopulator) base).getHandle(world).getClass().getSimpleName() + ")";
            } else {
                chunkGeneratorName = "chunkGenerator (" + base.getClass().getName() + ")";
            }
            this.chunkGeneratorTiming =
                    SpongeTimingsFactory.ofSafe(chunkGeneratorName, ((WorldServerBridge) world).bridge$getTimingsHandler().chunkPopulate);
        }

    }

    @Override
    public GenerationPopulator getBaseGenerationPopulator() {
        return this.baseGenerator;
    }

    @Override
    public void setBaseGenerationPopulator(GenerationPopulator baseGenerationPopulator) {
        this.baseGenerator = baseGenerationPopulator;
        if (this.baseGenerator instanceof ChunkGeneratorOverworldBridge) {
            ((ChunkGeneratorOverworldBridge) this.baseGenerator).bridge$setBiomeGenerator(this.biomeGenerator);
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
        ((WorldProviderAccessor) this.world.dimension).accessor$setBiomeProvider(CustomBiomeProvider.of(biomeGenerator));
        if (this.baseGenerator instanceof ChunkGeneratorOverworldBridge) {
            ((ChunkGeneratorOverworldBridge) this.baseGenerator).bridge$setBiomeGenerator(biomeGenerator);
        }
    }

    @Override
    public BiomeGenerationSettings getBiomeSettings(BiomeType type) {
        checkNotNull(type, "type");
        BiomeGenerationSettings settings = this.biomeSettings.get(type);
        if (settings == null) {
            if (SpongeGenerationPopulator.class.isInstance(this.baseGenerator)) {
                // If the base generator was mod provided then we assume that it
                // will handle its own generation so we don't add the base game's generation
                settings = new SpongeBiomeGenerationSettings();
            } else {
                settings = type.createDefaultGenerationSettings((org.spongepowered.api.world.World) this.world);
            }
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
    public Chunk func_185932_a(int chunkX, int chunkZ) {
        this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
        this.cachedBiomes.reuse(new Vector3i(chunkX * 16, 0, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);
        ImmutableBiomeVolume biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();

        // Generate base terrain
        ChunkPrimer chunkprimer = new ChunkPrimer();
        MutableBlockVolume blockBuffer = new ChunkPrimerBuffer(chunkprimer, chunkX, chunkZ);
        this.baseGenerator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);

        if (!(this.baseGenerator instanceof SpongeGenerationPopulator)) {
            this.replaceBiomeBlocks(this.world, this.rand, chunkX, chunkZ, chunkprimer, biomeBuffer);
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
                biome = this.cachedBiomes.getBiome(chunkX * 16 + x, 0, chunkZ * 16 + z);
                if (!uniqueBiomes.contains(biome)) {
                    uniqueBiomes.add(biome);
                }
            }
        }

        // run our generator populators
        for (BiomeType type : uniqueBiomes) {
            BiomeGenerationSettings settings = this.getBiomeSettings(type);
            for (GenerationPopulator populator : settings.getGenerationPopulators()) {
                populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
            }
        }

        // Assemble chunk
        Chunk chunk;
        if (this.baseGenerator instanceof SpongeGenerationPopulator && ((SpongeGenerationPopulator) this.baseGenerator).getCachedChunk() != null) {
            chunk = ((SpongeGenerationPopulator) this.baseGenerator).getCachedChunk();
            ((ChunkBridge) chunk).bridge$fill(chunkprimer);
        } else {
            chunk = new Chunk(this.world, chunkprimer, chunkX, chunkZ);
            this.cachedBiomes.fill(chunk.func_76605_m());
        }
        chunk.func_76603_b();
        return chunk;
    }

    @Override
    public void func_185931_b(int chunkX, int chunkZ) {
        WorldServerBridge world = (WorldServerBridge) this.world;
        world.bridge$getTimingsHandler().chunkPopulate.startTimingIfSync();
        this.chunkGeneratorTiming.startTimingIfSync();
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        this.rand.setSeed(this.world.getSeed());
        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * i1 + chunkZ * j1 ^ this.world.getSeed());
        FallingBlock.field_149832_M = true;

        // Have to regeneate the biomes so that any virtual biomes can be passed
        // to the populator.
        this.cachedBiomes.reuse(new Vector3i(chunkX * 16, 0, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);
        ImmutableBiomeVolume biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();

        BlockPos blockpos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        BiomeType biome = (BiomeType) this.world.getBiome(blockpos.add(16, 0, 16));

        org.spongepowered.api.world.Chunk chunk = (org.spongepowered.api.world.Chunk) this.world.func_72964_e(chunkX, chunkZ);

        BiomeGenerationSettings settings = getBiomeSettings(biome);

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

        populators.addAll(settings.getPopulators());
        if (snowPopulator != null) {
            populators.add(snowPopulator);
        }

        Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPre(Sponge.getCauseStackManager().getCurrentCause(), populators, chunk));
        List<String> flags = Lists.newArrayList();
        final IPhaseState<?> currentState = phaseTracker.getCurrentState();
        final Vector3i min = currentState.getChunkPopulatorOffset(chunk, chunkX, chunkZ);
        org.spongepowered.api.world.World spongeWorld = (org.spongepowered.api.world.World) this.world;
        Extent volume = new SoftBufferExtentViewDownsize(chunk.getWorld(), min, min.add(15, 255, 15), min.sub(8, 0, 8), min.add(23, 255, 23));
        for (Populator populator : populators) {
            final PopulatorType type = populator.getType();
            if (Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPopulate(Sponge.getCauseStackManager().getCurrentCause(), populator, chunk))) {
                continue;
            }
            try (CauseStackManager.StackFrame ignored = Sponge.getCauseStackManager().pushCauseFrame()) {
                Timing timing = null;
                ignored.pushCause(populator);
                if (Timings.isTimingsEnabled()) {
                    timing = this.populatorTimings.get(populator.getType().getId());
                    if (timing == null) {
                        timing = SpongeTimingsFactory.ofSafe("populate - " + populator.getType().getId());// ,
                                                                                                          // this.chunkGeneratorTiming);
                        this.populatorTimings.put(populator.getType().getId(), timing);
                    }
                    timing.startTimingIfSync();
                }
                try (PhaseContext<?> context = GenerationPhase.State.POPULATOR_RUNNING.createPhaseContext()
                    .world((ServerWorld) world)
                    .populator(type)) {
                    context.buildAndSwitch();

                    if (populator instanceof FlaggedPopulatorBridge) {
                        ((FlaggedPopulatorBridge) populator).bridge$populate(spongeWorld, volume, this.rand, biomeBuffer, flags);
                    } else {
                        populator.populate(spongeWorld, volume, this.rand, biomeBuffer);
                    }
                    if (timing != null) { // It wouldn't be null if we are enabled or we set it up before hand.
                        timing.stopTimingIfSync();
                    }
                }
            }
        }

        // If we wrapped a custom chunk provider then we should call its
        // populate method so that its particular changes are used.
        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            Timing timing = null;
            if (Timings.isTimingsEnabled()) {
                TimingBridge spongePopulator = (TimingBridge) this.baseGenerator;
                timing = spongePopulator.bridge$getTimingsHandler();
                timing.startTimingIfSync();
            }
            ((SpongeGenerationPopulator) this.baseGenerator).getHandle(this.world).func_185931_b(chunkX, chunkZ);
            if (Timings.isTimingsEnabled()) {
                timing.stopTimingIfSync();
            }
        }

        PopulateChunkEvent.Post event = SpongeEventFactory.createPopulateChunkEventPost(Sponge.getCauseStackManager().getCurrentCause(), ImmutableList.copyOf(populators), chunk);
        SpongeImpl.postEvent(event);

        FallingBlock.field_149832_M = false;
        this.chunkGeneratorTiming.stopTimingIfSync();
        world.bridge$getTimingsHandler().chunkPopulate.stopTimingIfSync();
    }

    @Override
    @SuppressWarnings("try")
    public boolean func_185933_a(Chunk chunk, int chunkX, int chunkZ) {
        boolean flag = false;
        if (chunk.getInhabitedTime() < 3600L) {
            for (Populator populator : this.pop) {
                if (populator instanceof OceanMonumentStructure) {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                         GenerationContext<PopulatorPhaseContext> context = GenerationPhase.State.POPULATOR_RUNNING.createPhaseContext()
                             .world(this.world)
                             .populator(populator.getType())
                            .buildAndSwitch()) {
                        flag |= ((OceanMonumentStructure) populator).func_175794_a(this.world, this.rand, new ChunkPos(chunkX, chunkZ));
                    }
                }
            }
        }
        return flag;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos) {
        if (this.baseGenerator instanceof ChunkGenerator) {
            return ((ChunkGenerator) this.baseGenerator).getPossibleCreatures(creatureType, pos);
        }

        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            return ((SpongeGenerationPopulator) this.baseGenerator).getChunkGenerator().getPossibleCreatures(creatureType, pos);
        }

        Biome biome = this.world.getBiome(pos);
        return biome.getSpawns(creatureType);
    }

    @Nullable
    @Override
    public BlockPos func_180513_a(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
        Class<? extends Structure> target = null;
        switch (structureName) {
            case "Stronghold":
                target = StrongholdStructure.class;
                break;
            case "Mansion":
                target = WoodlandMansionStructure.class;
                break;
            case "Monument":
                target = OceanMonumentStructure.class;
                break;
            case "Village":
                target = VillageStructure.class;
                break;
            case "Mineshaft":
                target = MineshaftStructure.class;
                break;
            case "Temple":
                target = ScatteredStructure.class;
                break;
            case "Fortress":
                target = FortressStructure.class;
                break;
            case "EndCity":
                target = EndCityStructure.class;
                break;
        }
        if (target != null) {
            for (GenerationPopulator gen : this.genpop) {
                if (target.isInstance(gen)) {
                    return ((Structure) gen).func_180706_b(worldIn, position, p_180513_4_);
                }
            }
        }

        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            return ((SpongeGenerationPopulator) this.baseGenerator).getHandle(this.world).func_180513_a(worldIn, structureName, position, p_180513_4_);
        }

        return null;
    }

    @Override
    public boolean func_193414_a(World worldIn, String structureName, BlockPos position) {
        Class<? extends Structure> target = null;
        switch (structureName) {
            case "Stronghold":
                target = StrongholdStructure.class;
                break;
            case "Mansion":
                target = WoodlandMansionStructure.class;
                break;
            case "Monument":
                target = OceanMonumentStructure.class;
                break;
            case "Village":
                target = VillageStructure.class;
                break;
            case "Mineshaft":
                target = MineshaftStructure.class;
                break;
            case "Temple":
                target = ScatteredStructure.class;
                break;
            case "Fortress":
                target = FortressStructure.class;
                break;
            case "EndCity":
                target = EndCityStructure.class;
                break;
        }
        if (target != null) {
            for (GenerationPopulator gen : this.genpop) {
                if (target.isInstance(gen)) {
                    return ((Structure) gen).func_175795_b(position);
                }
            }
        }

        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            return ((SpongeGenerationPopulator) this.baseGenerator).getHandle(this.world).func_193414_a(worldIn, structureName, position);
        }

        return false;
}

    @Override
    public void func_180514_a(Chunk chunkIn, int x, int z) {
        if (!this.world.getWorldInfo().isMapFeaturesEnabled()) {
            return;
        }

        if (this.baseGenerator instanceof ChunkGenerator) {
            ((ChunkGenerator) this.baseGenerator).func_180514_a(chunkIn, x, z);
        }

        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            ((SpongeGenerationPopulator) this.baseGenerator).getHandle(this.world).func_180514_a(chunkIn, x, z);
        }

        for (GenerationPopulator populator : this.genpop) {
            if (populator instanceof Structure) {
                ((Structure) populator).func_186125_a(chunkIn.getWorld(), x, z, null);
            }
        }
    }

    public void replaceBiomeBlocks(World world, Random rand, int x, int z, ChunkPrimer chunk, ImmutableBiomeVolume biomes) {
        double d0 = 0.03125D;
        this.stoneNoise = this.noise4.func_151599_a(this.stoneNoise, x * 16, z * 16, 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);
        Vector3i min = biomes.getBiomeMin();
        for (int x0 = 0; x0 < 16; ++x0) {
            for (int z0 = 0; z0 < 16; ++z0) {
                BiomeType biomegenbase = biomes.getBiome(min.getX() + x0, 0, min.getZ() + z0);
                this.generateBiomeTerrain(world, rand, chunk, x * 16 + x0, z * 16 + z0, this.stoneNoise[x0 + z0 * 16],
                        this.getBiomeSettings(biomegenbase).getGroundCoverLayers());
            }
        }
    }

    public void generateBiomeTerrain(World worldIn, Random rand, ChunkPrimer chunk, int x, int z, double stoneNoise,
            List<GroundCoverLayer> groundcover) {
        if (groundcover.isEmpty()) {
            return;
        }
        int seaLevel = worldIn.getSeaLevel();
        BlockState currentPlacement = null;
        int layerProgress = -1;
        int relativeX = x & 15;
        int relativeZ = z & 15;
        int layerDepth = 0;
        for (int currentY = 255; currentY >= 0; --currentY) {
            BlockState nextBlock = chunk.func_177856_a(relativeX, currentY, relativeZ);
            if (nextBlock.getMaterial() == Material.AIR) {
                layerProgress = -1;
            } else if (nextBlock.getBlock() == Blocks.STONE) {
                if (layerProgress == -1) {
                    if (groundcover.isEmpty()) {
                        layerProgress = 0;
                        continue;
                    }
                    layerDepth = 0;
                    GroundCoverLayer layer = groundcover.get(layerDepth);
                    currentPlacement = (BlockState) layer.getBlockState().apply(stoneNoise);
                    layerProgress = layer.getDepth(currentY).getFlooredAmount(rand, stoneNoise);
                    if (layerProgress <= 0) {
                        continue;
                    }

                    if (currentY >= seaLevel - 1) {
                        chunk.func_177855_a(relativeX, currentY, relativeZ, currentPlacement);
                        ++layerDepth;
                        if (layerDepth < groundcover.size()) {
                            layer = groundcover.get(layerDepth);
                            layerProgress = layer.getDepth(currentY - 1).getFlooredAmount(rand, stoneNoise);
                            currentPlacement = (BlockState) layer.getBlockState().apply(stoneNoise);
                        }
                    } else if (currentY < seaLevel - 7 - layerProgress) {
                        layerProgress = 0;
                        chunk.func_177855_a(relativeX, currentY, relativeZ, Blocks.GRAVEL.getDefaultState());
                    } else {
                        ++layerDepth;
                        if (layerDepth < groundcover.size()) {
                            layer = groundcover.get(layerDepth);
                            layerProgress = layer.getDepth(currentY).getFlooredAmount(rand, stoneNoise);
                            currentPlacement = (BlockState) layer.getBlockState().apply(stoneNoise);
                            chunk.func_177855_a(relativeX, currentY, relativeZ, currentPlacement);
                        }
                    }
                } else if (layerProgress > 0) {
                    --layerProgress;
                    chunk.func_177855_a(relativeX, currentY, relativeZ, currentPlacement);

                    if (layerProgress == 0) {
                        ++layerDepth;
                        if (layerDepth < groundcover.size()) {
                            GroundCoverLayer layer = groundcover.get(layerDepth);
                            layerProgress = layer.getDepth(currentY - 1).getFlooredAmount(rand, stoneNoise);
                            currentPlacement = (BlockState) layer.getBlockState().apply(stoneNoise);
                        }
                    }
                }
            }
        }
    }

}
