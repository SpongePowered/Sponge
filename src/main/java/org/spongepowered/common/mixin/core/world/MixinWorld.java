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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataManipulatorBuilder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.IMixinWorldSettings;
import org.spongepowered.common.interfaces.IMixinWorldType;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.scoreboard.SpongeScoreboard;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(1, 1, 1);
    private static final Vector3i BLOCK_SIZE = BLOCK_MAX.sub(BLOCK_MIN).add(1, 1, 1);
    private static final Vector2i BIOME_MIN = BLOCK_MIN.toVector2(true);
    private static final Vector2i BIOME_MAX = BLOCK_MAX.toVector2(true);
    private static final Vector2i BIOME_SIZE = BIOME_MAX.sub(BIOME_MIN).add(1, 1);
    private boolean keepSpawnLoaded;
    // TODO: what am I?
    private long weatherStartTime;
    public SpongeConfig<SpongeConfig.WorldConfig> worldConfig;
    private volatile Context worldContext;
    private ImmutableList<Populator> populators;
    private ImmutableList<GeneratorPopulator> generatorPopulators;

    protected SpongeScoreboard spongeScoreboard = new SpongeScoreboard();

    @Shadow public WorldProvider provider;
    @Shadow protected WorldInfo worldInfo;
    @Shadow public Random rand;
    @Shadow public List<net.minecraft.entity.Entity> loadedEntityList;
    @Shadow public Scoreboard worldScoreboard;

    @Shadow(prefix = "shadow$")
    public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();

    @Shadow
    public abstract boolean spawnEntityInWorld(net.minecraft.entity.Entity entityIn);

    @Shadow
    public abstract List<net.minecraft.entity.Entity> getEntities(Class<net.minecraft.entity.Entity> entityType,
            Predicate<net.minecraft.entity.Entity> filter);

    @Shadow
    public abstract void playSoundEffect(double x, double y, double z, String soundName, float volume, float pitch);

    @Shadow
    public abstract BiomeGenBase getBiomeGenForCoords(BlockPos pos);

    @Shadow
    public abstract IChunkProvider getChunkProvider();

    @Shadow
    public abstract WorldChunkManager getWorldChunkManager();

    @Shadow
    public abstract net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos);

    @Shadow
    public abstract boolean isBlockPowered(BlockPos pos);

    @Shadow
    public abstract IBlockState getBlockState(BlockPos pos);

    @Shadow
    public abstract net.minecraft.world.chunk.Chunk getChunkFromChunkCoords(int chunkX, int chunkZ);

    @Shadow
    public abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @SuppressWarnings("rawtypes")
    @Inject(method = "getCollidingBoundingBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"))
    public void onGetCollidingBoundingBoxes(net.minecraft.entity.Entity entity, net.minecraft.util.AxisAlignedBB axis,
            CallbackInfoReturnable<List> cir) {
        if (!entity.worldObj.isRemote && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
            cir.setReturnValue(new ArrayList());// Removing misbehaved living entities
        }
    }

    @Override
    public float getTemperature(Vector3i position) {
        return getTemperature(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public float getTemperature(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return getBiomeGenForCoords(pos).getFloatTemperature(pos);
    }

    @Override
    public UUID getUniqueId() {
        return ((WorldProperties) this.worldInfo).getUniqueId();
    }

    @Override
    public String getName() {
        return this.worldInfo.getWorldName();
    }

    @Override
    public Optional<Chunk> getChunk(Vector3i position) {
        return getChunk(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Optional<Chunk> getChunk(int x, int y, int z) {
        if (!SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return Optional.absent();
        }
        WorldServer worldserver = (WorldServer) (Object) this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(x, z)) {
            chunk = worldserver.theChunkProviderServer.provideChunk(x, z);
        }
        return Optional.fromNullable((Chunk) chunk);
    }

    @Override
    public Optional<Chunk> loadChunk(Vector3i position, boolean shouldGenerate) {
        return loadChunk(position.getX(), position.getY(), position.getZ(), shouldGenerate);
    }

    @Override
    public Optional<Chunk> loadChunk(int x, int y, int z, boolean shouldGenerate) {
        if (!SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return Optional.absent();
        }
        WorldServer worldserver = (WorldServer) (Object) this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(x, z) || shouldGenerate) {
            chunk = worldserver.theChunkProviderServer.loadChunk(x, z);
        }
        return Optional.fromNullable((Chunk) chunk);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        // avoid intermediate object creation from using BlockState
        return (BlockType) getChunkFromChunkCoords(x >> 4, z >> 4).getBlock(x, y, z);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        checkBlockBounds(x, y, z);
        SpongeHooks.setBlockState(((net.minecraft.world.World) (Object) this), x, y, z, block);
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkBiomeBounds(x, z);
        return (BiomeType) this.getBiomeGenForCoords(new BlockPos(x, 0, z));
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkBiomeBounds(x, z);
        ((Chunk) getChunkFromChunkCoords(x >> 4, z >> 4)).setBiome(x, z, biome);
    }

    @Override
    public boolean isBlockPowered(int x, int y, int z) {
        return isBlockPowered(new BlockPos(x, y, z));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Entity> getEntities() {
        return (Collection<Entity>) (Object) this.loadedEntityList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Entity> getEntities(Predicate<Entity> filter) {
        return (Collection<Entity>) (Object) this.getEntities(net.minecraft.entity.Entity.class,
                (Predicate<net.minecraft.entity.Entity>) (Object) filter);
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3d position) {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");

        Entity entity = null;

        try {
            entity = ConstructorUtils.invokeConstructor(type.getEntityClass(), this);
            entity.setLocation(entity.getLocation().setPosition(position));
        } catch (Exception e) {
            Sponge.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
        return Optional.fromNullable(entity);
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3i position) {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");
        Entity entity = null;

        try {
            entity = ConstructorUtils.invokeConstructor(type.getEntityClass(), this);
            entity.setLocation(entity.getLocation().setPosition(position.toDouble()));
        } catch (Exception e) {
            Sponge.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
        return Optional.fromNullable(entity);
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer) {
        // TODO once entity containers are implemented
        return Optional.absent();
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");
        return spawnEntityInWorld(((net.minecraft.entity.Entity) entity));
    }

    @Override
    public WorldBorder getWorldBorder() {
        return (WorldBorder) shadow$getWorldBorder();
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();

            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            for (Packet packet : packets) {
                manager.sendToAllNear(x, y, z, radius, this.provider.getDimensionId(), packet);
            }
        }
    }

    @Override
    public Weather getWeather() {
        if (this.worldInfo.isThundering()) {
            return Weathers.THUNDER_STORM;
        } else if (this.worldInfo.isRaining()) {
            return Weathers.RAIN;
        } else {
            return Weathers.CLEAR;
        }
    }

    @Override
    public long getRemainingDuration() {
        Weather weather = getWeather();
        if (weather.equals(Weathers.CLEAR)) {
            if (this.worldInfo.getCleanWeatherTime() > 0) {
                return this.worldInfo.getCleanWeatherTime();
            } else {
                return Math.min(this.worldInfo.getThunderTime(), this.worldInfo.getRainTime());
            }
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            return this.worldInfo.getThunderTime();
        } else if (weather.equals(Weathers.RAIN)) {
            return this.worldInfo.getRainTime();
        }
        return 0;
    }

    @Override
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - this.weatherStartTime;
    }

    @Override
    public void forecast(Weather weather) {
        this.forecast(weather, (300 + this.rand.nextInt(600)) * 20);
    }

    @Override
    public void forecast(Weather weather, long duration) {
        if (weather.equals(Weathers.CLEAR)) {
            this.worldInfo.setCleanWeatherTime((int) duration);
            this.worldInfo.setRainTime(0);
            this.worldInfo.setThunderTime(0);
            this.worldInfo.setRaining(false);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.RAIN)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(true);
        }
    }

    @Override
    public Dimension getDimension() {
        return (Dimension) this.provider;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public void setKeepSpawnLoaded(boolean keepLoaded) {
        this.keepSpawnLoaded = keepLoaded;
    }

    @Override
    public SpongeConfig<SpongeConfig.WorldConfig> getWorldConfig() {
        return this.worldConfig;
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {
        this.playSound(sound, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {
        this.playSound(sound, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {
        this.playSoundEffect(position.getX(), position.getY(), position.getZ(), sound.getName(), (float) Math.max(minVolume, volume), (float) pitch);
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        World spongeWorld = this;
        if (spongeWorld instanceof WorldServer) {
            return Optional.fromNullable((Entity) ((WorldServer) (Object) this).getEntityFromUuid(uuid));
        }
        for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of((Entity) entity);
            }
        }
        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return ((ChunkProviderServer) this.getChunkProvider()).loadedChunks;
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        if (chunk == null) {
            return false;
        }
        return chunk.unloadChunk();
    }

    @Override
    public WorldCreationSettings getCreationSettings() {
        WorldProperties properties = this.getProperties();

        // Create based on WorldProperties
        WorldSettings settings = new WorldSettings(this.worldInfo);
        IMixinWorldSettings mixin = (IMixinWorldSettings) (Object) settings;
        mixin.setDimensionType(properties.getDimensionType());
        mixin.setGeneratorSettings(properties.getGeneratorSettings());
        mixin.setGeneratorModifiers(properties.getGeneratorModifiers());
        mixin.setEnabled(true);
        mixin.setKeepSpawnLoaded(this.keepSpawnLoaded);
        mixin.setLoadOnStartup(properties.loadOnStartup());

        return (WorldCreationSettings) (Object) settings;
    }

    @Override
    public void updateWorldGenerator() {
        IMixinWorldType worldType = (IMixinWorldType) this.getProperties().getGeneratorType();

        // Get the default generator for the world type
        DataContainer generatorSettings = this.getProperties().getGeneratorSettings();
        if (generatorSettings.contains(IMixinWorldType.STRING_VALUE)) {
            String options = generatorSettings.getString(IMixinWorldType.STRING_VALUE).get();
            if (options.equals("")) {
                return;
            }
        }
        SpongeWorldGenerator newGenerator = worldType.createGenerator(this, generatorSettings);

        // Re-apply all world generator modifiers
        WorldCreationSettings creationSettings = this.getCreationSettings();

        for (WorldGeneratorModifier modifier : this.getProperties().getGeneratorModifiers()) {
            modifier.modifyWorldGenerator(creationSettings, generatorSettings, newGenerator);
        }

        // Set this world generator
        this.setWorldGenerator(newGenerator);
    }


    @Override
    public ImmutableList<Populator> getPopulators() {
        if (this.populators == null) {
            this.populators = ImmutableList.of();
        }
        return this.populators;
    }

    @Override
    public ImmutableList<GeneratorPopulator> getGeneratorPopulators() {
        if (this.generatorPopulators == null) {
            this.generatorPopulators = ImmutableList.of();
        }
        return this.generatorPopulators;
    }


    @Override
    public void setWorldInfo(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }


    @Override
    public WorldProperties getProperties() {
        return (WorldProperties) this.worldInfo;
    }

    @Override
    public Location getSpawnLocation() {
        return new Location(this, this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());
    }

    @Override
    public Context getContext() {
        if (this.worldContext == null) {
            this.worldContext = new Context(Context.WORLD_KEY, getName());
        }
        return this.worldContext;
    }

    @Override
    public Optional<TileEntity> getTileEntity(int x, int y, int z) {
        net.minecraft.tileentity.TileEntity tileEntity = getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null) {
            return Optional.absent();
        } else {
            return Optional.of((TileEntity) tileEntity);
        }
    }

    @Override
    public <T extends DataManipulator<T>> Optional<T> getData(int x, int y, int z, Class<T> dataClass) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil(dataClass);
        if (blockUtilOptional.isPresent()) {
            return blockUtilOptional.get().fromBlockPos((net.minecraft.world.World) (Object) this, new BlockPos(x, y, z));
        }
        return Optional.absent();
    }


    @Override
    public <T extends DataManipulator<T>> Optional<T> getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil(manipulatorClass);
        if (blockUtilOptional.isPresent()) {
            Optional<T> data = blockUtilOptional.get().fromBlockPos((net.minecraft.world.World) (Object) this, new BlockPos(x, y, z));
            if (!data.isPresent() && blockUtilOptional.get() instanceof DataManipulatorBuilder) {
                data = Optional.<T>fromNullable(((DataManipulatorBuilder<T>) blockUtilOptional.get()).create());
            }
            return data;
        }
        return Optional.absent();
    }

    @Override
    public <T extends DataManipulator<T>> boolean remove(int x, int y, int z, Class<T> manipulatorClass) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil(manipulatorClass);
        if (blockUtilOptional.isPresent()) {
            return blockUtilOptional.get().remove((net.minecraft.world.World) ((Object) this), new BlockPos(x, y, z));
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(int x, int y, int z, T manipulatorData, DataPriority priority) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil((Class<T>) (Class) manipulatorData
                .getClass());
        if (blockUtilOptional.isPresent()) {
            return blockUtilOptional.get().setData((net.minecraft.world.World) ((Object) this), new BlockPos(x, y, z), manipulatorData, priority);
        }
        return builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public Collection<DataManipulator<?>> getManipulators(int x, int y, int z) {
        final BlockPos blockPos = new BlockPos(x, y, z);
        return ((IMixinBlock) getBlock(x, y, z).getType()).getManipulators((net.minecraft.world.World) ((Object) this), blockPos);
    }

    @Override
    public Vector2i getBiomeMin() {
        return BIOME_MIN;
    }

    @Override
    public Vector2i getBiomeMax() {
        return BIOME_MAX;
    }

    @Override
    public Vector2i getBiomeSize() {
        return BIOME_SIZE;
    }

    @Override
    public Vector3i getBlockMin() {
        return BLOCK_MIN;
    }

    @Override
    public Vector3i getBlockMax() {
        return BLOCK_MAX;
    }

    @Override
    public Vector3i getBlockSize() {
        return BLOCK_SIZE;
    }

    @Override
    public boolean containsBiome(int x, int z) {
        return VecHelper.inBounds(x, z, BIOME_MIN, BIOME_MAX);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, BLOCK_MIN, BLOCK_MAX);
    }

    private void checkBiomeBounds(int x, int z) {
        if (!containsBiome(x, z)) {
            throw new PositionOutOfBoundsException(new Vector2i(x, z), BIOME_MIN, BIOME_MAX);
        }
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BLOCK_MIN, BLOCK_MAX);
        }
    }

    @Override
    public org.spongepowered.api.scoreboard.Scoreboard getScoreboard() {
        return this.spongeScoreboard;
    }

    @Override
    public void setScoreboard(org.spongepowered.api.scoreboard.Scoreboard scoreboard) {
        this.spongeScoreboard = checkNotNull(((SpongeScoreboard) scoreboard), "Scoreboard cannot be null!");
        this.worldScoreboard = ((SpongeScoreboard) scoreboard).createScoreboard(scoreboard);
    }

}
