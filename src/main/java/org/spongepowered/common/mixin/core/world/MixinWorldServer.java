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

import co.aikar.timings.TimingHistory;
import co.aikar.timings.WorldTimingsHandler;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.ConfigRenderOptions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Explosion;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.cause.entity.spawn.WeatherSpawnCause;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.data.persistence.ConfigurateTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinNextTickListEntry;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerChunkMap;
import org.spongepowered.common.interfaces.world.IMixinExplosion;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.interfaces.world.gen.IPopulatorProvider;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.IModData_Collisions;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.border.PlayerBorderListener;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeGenerationPopulator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.type.SpongeWorldType;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(WorldServer.class)
@Implements(@Interface(iface = IMixinWorldServer.class, prefix = "worldServer$", unique = true))
public abstract class MixinWorldServer extends MixinWorld implements IMixinWorldServer {

    private static final String PROFILER_SS = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V";
    private static final String PROFILER_ESS = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V";

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(1, 1, 1);

    private final CauseTracker causeTracker = new CauseTracker((WorldServer) (Object) this);
    private final Map<net.minecraft.entity.Entity, Vector3d> rotationUpdates = new HashMap<>();
    private SpongeChunkGenerator spongegen;
    private SpongeConfig<?> activeConfig;
    protected long weatherStartTime;
    protected Weather prevWeather;
    protected WorldTimingsHandler timings = new WorldTimingsHandler((WorldServer) (Object) this);
    private int chunkGCTickCount = 0;
    private int chunkGCLoadThreshold = 0;
    private int chunkGCTickInterval = 600;
    private long chunkUnloadDelay = 30000;
    private boolean weatherThunderEnabled = true;
    private boolean weatherIceAndSnowEnabled = true;

    @Shadow @Final private MinecraftServer mcServer;
    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow @Final private PlayerChunkMap thePlayerManager;
    @Shadow @Final @Mutable private Teleporter worldTeleporter;
    @Shadow @Final private WorldServer.ServerBlockEventList[] blockEventQueue;
    @Shadow private int blockEventCacheIndex;
    @Shadow private int updateEntityTick;

    @Shadow public abstract boolean fireBlockEvent(BlockEventData event);
    @Shadow protected abstract void createBonusChest();
    @Shadow @Nullable public abstract net.minecraft.entity.Entity getEntityFromUuid(UUID uuid);
    @Shadow public abstract PlayerChunkMap getPlayerChunkMap();
    @Shadow @Override public abstract ChunkProviderServer getChunkProvider();
    @Shadow protected abstract void playerCheckLight();
    @Shadow protected abstract BlockPos adjustPosToNearbyEntity(BlockPos pos);
    @Shadow private boolean canAddEntity(net.minecraft.entity.Entity entityIn) {
        return false; // Shadowed
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, CallbackInfo callbackInfo) {
        this.activeConfig = SpongeHooks.getActiveConfig((WorldServer)(Object) this);
        this.prevWeather = getWeather();
        this.weatherStartTime = this.worldInfo.getWorldTotalTime();
        ((World) (Object) this).getWorldBorder().addListener(new PlayerBorderListener(this.getMinecraftServer(), dimensionId));
        PortalAgentType portalAgentType = ((WorldProperties) this.worldInfo).getPortalAgentType();
        if (!portalAgentType.equals(PortalAgentTypes.DEFAULT)) {
            try {
                this.worldTeleporter = (Teleporter) portalAgentType.getPortalAgentClass().getConstructor(new Class<?>[] {WorldServer.class})
                        .newInstance(new Object[] {this});
            } catch (Exception e) {
                SpongeImpl.getLogger().log(Level.ERROR, "Could not create PortalAgent of type " + portalAgentType.getId()
                        + " for world " + this.getName() + ": " + e.getMessage() + ". Falling back to default...");
            }
        }

        // Turn on capturing
        updateWorldGenerator();
        // Need to set the active config before we call it.
        this.chunkGCLoadThreshold = SpongeHooks.getActiveConfig((WorldServer) (Object) this).getConfig().getWorld().getChunkLoadThreadhold();
        this.chunkGCTickInterval = this.getActiveConfig().getConfig().getWorld().getTickInterval();
        this.weatherIceAndSnowEnabled = this.getActiveConfig().getConfig().getWorld().getWeatherIceAndSnow();
        this.weatherThunderEnabled = this.getActiveConfig().getConfig().getWorld().getWeatherThunder();
        this.updateEntityTick = 0;
    }

    @Inject(method = "createBonusChest", at = @At(value = "HEAD"))
    public void onCreateBonusChest(CallbackInfo ci) {
        if (CauseTracker.ENABLED) {
            this.getCauseTracker().switchToPhase(GenerationPhase.State.TERRAIN_GENERATION, PhaseContext.start()
                    .add(NamedCause.source(this))
                    .addCaptures()
                    .complete());
        }
    }


    @Inject(method = "createBonusChest", at = @At(value = "RETURN"))
    public void onCreateBonusChestEnd(CallbackInfo ci) {
        if (CauseTracker.ENABLED) {
            this.getCauseTracker().completePhase();
        }
    }

    @Inject(method = "createSpawnPosition(Lnet/minecraft/world/WorldSettings;)V", at = @At("HEAD"), cancellable = true)
    public void onCreateSpawnPosition(WorldSettings settings, CallbackInfo ci) {
        GeneratorType generatorType = (GeneratorType) settings.getTerrainType();

        // Allow bonus chest generation for non-Overworld worlds
        if (!this.provider.canRespawnHere() && this.getProperties().doesGenerateBonusChest()) {
            this.createBonusChest();
        }

        if ((generatorType != null && generatorType.equals(GeneratorTypes.THE_END)) || ((((WorldServer) (Object) this)).getChunkProvider().chunkGenerator instanceof ChunkProviderEnd)) {
            this.worldInfo.setSpawn(new BlockPos(100, 50, 0));
            ci.cancel();
        }
    }

    @Redirect(method = "createSpawnPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldSettings;isBonusChestEnabled()Z"))
    public boolean onIsBonusChestEnabled(WorldSettings settings) {
        return this.getProperties().doesGenerateBonusChest();
    }

    @Override
    public boolean isProcessingExplosion() {
        return this.processingExplosion;
    }

    @Override
    public boolean isMinecraftChunkLoaded(int x, int z, boolean allowEmpty) {
        return this.isChunkLoaded(x, z, allowEmpty);
    }

    @Override
    public SpongeConfig<WorldConfig> getWorldConfig() {
        return ((IMixinWorldInfo) this.worldInfo).getWorldConfig();
    }


    @Override
    public SpongeConfig<?> getActiveConfig() {
        return this.activeConfig;
    }

    @Override
    public void setActiveConfig(SpongeConfig<?> config) {
        this.activeConfig = config;
        // update cached settings
        this.chunkGCLoadThreshold = this.activeConfig.getConfig().getWorld().getChunkLoadThreadhold();
        this.chunkGCTickInterval = this.activeConfig.getConfig().getWorld().getTickInterval();
        this.weatherIceAndSnowEnabled = this.activeConfig.getConfig().getWorld().getWeatherIceAndSnow();
        this.weatherThunderEnabled = this.activeConfig.getConfig().getWorld().getWeatherThunder();
        this.chunkUnloadDelay = this.activeConfig.getConfig().getWorld().getChunkUnloadDelay() * 1000;
        if (this.getChunkProvider() != null) {
            final IMixinChunkProviderServer mixinChunkProvider = (IMixinChunkProviderServer) this.getChunkProvider();
            final int maxChunkUnloads = this.activeConfig.getConfig().getWorld().getMaxChunkUnloads();
            mixinChunkProvider.setMaxChunkUnloads(maxChunkUnloads < 1 ? 1 : maxChunkUnloads);
//            ((ChunkProviderServer) this.getChunkProvider()).chunkLoadOverride = !this.activeConfig.getConfig().getWorld().getDenyChunkRequests();
            for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
                if (entity instanceof IModData_Activation) {
                    ((IModData_Activation) entity).requiresActivationCacheRefresh(true);
                }
                if (entity instanceof IModData_Collisions) {
                    ((IModData_Collisions) entity).requiresCollisionsCacheRefresh(true);
                }
            }
        }
    }

    @Override
    public boolean isLoaded() {
        return WorldManager.getWorldByDimensionId(getDimensionId()).isPresent();
    }

    @Override
    public void updateWorldGenerator() {

        // Get the default generator for the world type
        DataContainer generatorSettings = this.getProperties().getGeneratorSettings();

        SpongeWorldGenerator newGenerator = createWorldGenerator(generatorSettings);
        // If the base generator is an IChunkProvider which implements
        // IPopulatorProvider we request that it add its populators not covered
        // by the base generation populator
        if (newGenerator.getBaseGenerationPopulator() instanceof IChunkGenerator) {
            // We check here to ensure that the IPopulatorProvider is one of our mixed in ones and not
            // from a mod chunk provider extending a provider that we mixed into
            if (WorldGenConstants.isValid((IChunkGenerator) newGenerator.getBaseGenerationPopulator(), IPopulatorProvider.class)) {
                ((IPopulatorProvider) newGenerator.getBaseGenerationPopulator()).addPopulators(newGenerator);
            }
        } else if (newGenerator.getBaseGenerationPopulator() instanceof IPopulatorProvider) {
            // If its not a chunk provider but is a populator provider then we call it as well
            ((IPopulatorProvider) newGenerator.getBaseGenerationPopulator()).addPopulators(newGenerator);
        }

        for (WorldGeneratorModifier modifier : this.getProperties().getGeneratorModifiers()) {
            modifier.modifyWorldGenerator(this.getProperties(), generatorSettings, newGenerator);
        }

        this.spongegen = createChunkGenerator(newGenerator);
        this.spongegen.setGenerationPopulators(newGenerator.getGenerationPopulators());
        this.spongegen.setPopulators(newGenerator.getPopulators());
        this.spongegen.setBiomeOverrides(newGenerator.getBiomeSettings());

        ChunkProviderServer chunkProviderServer = this.getChunkProvider();
        chunkProviderServer.chunkGenerator = this.spongegen;
    }

    @Override
    public SpongeChunkGenerator createChunkGenerator(SpongeWorldGenerator newGenerator) {
        return new SpongeChunkGenerator((net.minecraft.world.World) (Object) this, newGenerator.getBaseGenerationPopulator(),
                newGenerator.getBiomeGenerator());
    }

    @Override
    public SpongeWorldGenerator createWorldGenerator(DataContainer settings) {
        // Minecraft uses a string for world generator settings
        // This string can be a JSON string, or be a string of a custom format

        // Try to convert to custom format
        Optional<String> optCustomSettings = settings.getString(DataQueries.WORLD_CUSTOM_SETTINGS);
        if (optCustomSettings.isPresent()) {
            return this.createWorldGenerator(optCustomSettings.get());
        }

        final StringWriter writer = new StringWriter();
        try {
            HoconConfigurationLoader.builder().setRenderOptions(ConfigRenderOptions.concise().setJson(true))
                    .setSink(() -> new BufferedWriter(writer)).build().save(ConfigurateTranslator.instance().translateData(settings));
        } catch (Exception e) {
            SpongeImpl.getLogger().warn("Failed to convert settings from [{}] for GeneratorType [{}] used by World [{}].", settings,
                    ((net.minecraft.world.World) (Object) this).getWorldType(), this, e);
        }

        return this.createWorldGenerator(writer.toString());
    }

    @Override
    public SpongeWorldGenerator createWorldGenerator(String settings) {
        final WorldServer worldServer = (WorldServer) (Object) this;
        final WorldType worldType = worldServer.getWorldType();
        final IChunkGenerator chunkGenerator;
        final BiomeProvider biomeProvider;
        if (worldType instanceof SpongeWorldType) {
            chunkGenerator = ((SpongeWorldType) worldType).getChunkGenerator(worldServer, settings);
            biomeProvider = ((SpongeWorldType) worldType).getBiomeProvider(worldServer);
        } else {
            final IChunkGenerator currentGenerator = this.getChunkProvider().chunkGenerator;
            if (currentGenerator != null) {
                chunkGenerator = currentGenerator;
            } else {
                final WorldProvider worldProvider = worldServer.provider;
                ((IMixinWorldProvider) worldProvider).setGeneratorSettings(settings);
                chunkGenerator = worldProvider.createChunkGenerator();
            }
            biomeProvider = worldServer.provider.biomeProvider;
        }
        return new SpongeWorldGenerator(worldServer, (BiomeGenerator) biomeProvider, SpongeGenerationPopulator.of(worldServer, chunkGenerator));
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        return this.spongegen;
    }

    @Override
    public CauseTracker getCauseTracker() {
        return this.causeTracker;
    }


    /**
     * @author blood - July 1st, 2016
     * @author gabizou - July 1st, 2016 - Update to 1.10 and cause tracking
     *
     * @reason Added chunk and block tick optimizations, timings, cause tracking, and pre-construction events.
     */
    @Override
    @Overwrite
    protected void updateBlocks() {
        this.playerCheckLight();

        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            Iterator<net.minecraft.world.chunk.Chunk> iterator1 = this.thePlayerManager.getChunkIterator();

            while (iterator1.hasNext())
            {
                iterator1.next().onTick(false);
            }
            return; // Sponge: Add return
        }
        // else // Sponge - Remove unnecessary else
        // { //

        int i = this.shadow$getGameRules().getInt("randomTickSpeed");
        boolean flag = this.isRaining();
        boolean flag1 = this.isThundering();
        this.theProfiler.startSection("pollingChunks");

        final CauseTracker causeTracker = this.getCauseTracker(); // Sponge - get the cause tracker

        // Sponge: Use SpongeImplHooks for Forge
        for (Iterator<net.minecraft.world.chunk.Chunk> iterator =
             SpongeImplHooks.getChunkIterator((WorldServer) (Object) this); iterator.hasNext(); this.theProfiler.endSection())
        {
            this.theProfiler.startSection("getChunk");
            net.minecraft.world.chunk.Chunk chunk = iterator.next();
            int j = chunk.xPosition * 16;
            int k = chunk.zPosition * 16;
            this.theProfiler.endStartSection("checkNextLight");
            this.timings.updateBlocksCheckNextLight.startTiming(); // Sponge - Timings
            chunk.enqueueRelightChecks();
            this.timings.updateBlocksCheckNextLight.stopTiming(); // Sponge - Timings
            this.theProfiler.endStartSection("tickChunk");
            this.timings.updateBlocksChunkTick.startTiming(); // Sponge - Timings
            chunk.onTick(false);
            this.timings.updateBlocksChunkTick.stopTiming(); // Sponge - Timings
            // Sponge start - if surrounding neighbors are not loaded, skip
            if (!((IMixinChunk) chunk).areNeighborsLoaded()) {
                continue;
            }
            // Sponge end
            this.theProfiler.endStartSection("thunder");
            // Sponge start
            this.timings.updateBlocksThunder.startTiming();

            //if (this.provider.canDoLightning(chunk) && flag && flag1 && this.rand.nextInt(100000) == 0) // Sponge - Add SpongeImplHooks for forge
            if (this.weatherThunderEnabled && SpongeImplHooks.canDoLightning(this.provider, chunk) && flag && flag1 && this.rand.nextInt(100000) == 0)
            {
                if (CauseTracker.ENABLED) {
                    causeTracker.switchToPhase(TickPhase.Tick.WEATHER, PhaseContext.start()
                            .addCaptures()
                            .add(NamedCause.source(this))
                            .complete());
                }
                // Sponge end
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                int l = this.updateLCG >> 2;
                BlockPos blockpos = this.adjustPosToNearbyEntity(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));

                if (this.isRainingAt(blockpos))
                {
                    DifficultyInstance difficultyinstance = this.getDifficultyForLocation(blockpos);

                    // Sponge - create a transform to be used for events
                    final Transform<org.spongepowered.api.world.World> transform = new Transform<>(this, VecHelper.toVector3d(blockpos).toDouble());

                    if (this.rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.05D)
                    {
                        // Sponge Start - Throw construction events
                        SpawnCause horseCause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
                        ConstructEntityEvent.Pre constructEntityEvent = SpongeEventFactory.createConstructEntityEventPre(Cause.source(horseCause).build(), EntityTypes.HORSE, transform);
                        SpongeImpl.postEvent(constructEntityEvent);
                        if (!constructEntityEvent.isCancelled()) {
                            // Sponge End
                            EntityHorse entityhorse = new EntityHorse((WorldServer) (Object) this);
                            entityhorse.setType(HorseType.SKELETON);
                            entityhorse.setSkeletonTrap(true);
                            entityhorse.setGrowingAge(0);
                            entityhorse.setPosition((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                            this.spawnEntityInWorld(entityhorse);
                            // Sponge Start - Throw a construct event for the lightning
                        }

                        SpawnCause lightiningCause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
                        ConstructEntityEvent.Pre lightning = SpongeEventFactory.createConstructEntityEventPre(Cause.source(lightiningCause).build(), EntityTypes.LIGHTNING, transform);
                        SpongeImpl.postEvent(lightning);
                        if (!lightning.isCancelled()) {
                            // Sponge End
                            this.addWeatherEffect(new EntityLightningBolt((WorldServer) (Object) this, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), true));
                        } // Sponge - Brackets.
                    }
                    else
                    {
                        // Sponge start - Throw construction event for lightningbolts

                        SpawnCause cause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
                        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)),
                                EntityTypes.LIGHTNING, transform);
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            // Sponge End
                            this.addWeatherEffect(new EntityLightningBolt((WorldServer) (Object) this, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), false));
                        } // Sponge - Brackets.
                    }
                }
                // Sponge Start - Cause tracker unwind
                if (CauseTracker.ENABLED) {
                    causeTracker.completePhase();
                }
                // Sponge End

            }

            this.timings.updateBlocksThunder.stopTiming(); // Sponge - Stop thunder timing
            this.timings.updateBlocksIceAndSnow.startTiming(); // Sponge - Start thunder timing
            this.theProfiler.endStartSection("iceandsnow");

            // if (this.rand.nextInt(16) == 0) // Sponge - Rewrite to use our boolean, and forge hook
            if (this.weatherIceAndSnowEnabled && SpongeImplHooks.canDoRainSnowIce(this.provider, chunk) && this.rand.nextInt(16) == 0)
            {
                // Sponge Start - Enter weather phase for snow and ice and flooding.
                causeTracker.switchToPhase(TickPhase.Tick.WEATHER, PhaseContext.start()
                        .addCaptures()
                        .add(NamedCause.source(this))
                        .complete());
                // Sponge End
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                int j2 = this.updateLCG >> 2;
                BlockPos blockpos1 = this.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
                BlockPos blockpos2 = blockpos1.down();

                if (this.canBlockFreezeNoWater(blockpos2))
                {
                    this.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
                }

                if (flag && this.canSnowAt(blockpos1, true))
                {
                    this.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState());
                }

                if (flag && this.getBiome(blockpos2).canRain())
                {
                    this.getBlockState(blockpos2).getBlock().fillWithRain((WorldServer) (Object) this, blockpos2);
                }
                causeTracker.completePhase(); // Sponge - complete weather phase
            }

            this.timings.updateBlocksIceAndSnow.stopTiming(); // Sponge - Stop ice and snow timing
            this.timings.updateBlocksRandomTick.startTiming(); // Sponge - Start random block tick timing
            this.theProfiler.endStartSection("tickBlocks");

            if (i > 0)
            {
                for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray())
                {
                    if (extendedblockstorage != net.minecraft.world.chunk.Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.getNeedsRandomTick())
                    {
                        for (int i1 = 0; i1 < i; ++i1)
                        {
                            this.updateLCG = this.updateLCG * 3 + 1013904223;
                            int j1 = this.updateLCG >> 2;
                            int k1 = j1 & 15;
                            int l1 = j1 >> 8 & 15;
                            int i2 = j1 >> 16 & 15;
                            IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
                            Block block = iblockstate.getBlock();
                            this.theProfiler.startSection("randomTick");

                            if (block.getTickRandomly())
                            {
                                // Sponge start - capture random tick
                                // Remove the random tick for cause tracking
                                // block.randomTick(this, new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), iblockstate, this.rand);

                                BlockPos pos = new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k);
                                IMixinBlock spongeBlock = (IMixinBlock) block;
                                spongeBlock.getTimingsHandler().startTiming();
                                final PhaseData currentTuple = causeTracker.getCurrentPhaseData();
                                final IPhaseState phaseState = currentTuple.state;
                                if (!CauseTracker.ENABLED || phaseState.getPhase().alreadyCapturingBlockTicks(phaseState, currentTuple.context)) {
                                    block.randomTick((WorldServer) (Object) this, pos, iblockstate, this.rand);
                                } else {
                                    TrackingUtil.randomTickBlock(causeTracker, block, pos, iblockstate, this.rand);
                                }
                                spongeBlock.getTimingsHandler().stopTiming();
                                // Sponge end
                            }

                            this.theProfiler.endSection();
                        }
                    }
                }
            }
        }

        this.timings.updateBlocksRandomTick.stopTiming(); // Sponge - Stop random block timing
        this.theProfiler.endSection();
        // } // Sponge- Remove unecessary else
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target= "Lnet/minecraft/world/WorldServer;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean onBlockTickIsAreaLoaded(WorldServer worldIn, BlockPos fromPos, BlockPos toPos) {
        int posX = fromPos.getX() + 8;
        int posZ = fromPos.getZ() + 8;
        // Forge passes the same block position for forced chunks
        if (fromPos.equals(toPos)) {
            posX = fromPos.getX();
            posZ = fromPos.getZ();
        }
        final net.minecraft.world.chunk.Chunk chunk = this.getChunkProvider().getLoadedChunk(posX >> 4, posZ >> 4);
        if (chunk == null || !((IMixinChunk) chunk).areNeighborsLoaded()) {
            return false;
        }

        return true;
    }

    /**
     * @author blood - August 30th, 2016
     *
     * @reason Always allow entity cleanup to occur. This prevents issues such as a plugin 
     *         generating chunks with no players causing entities not getting cleaned up.
     */
    @Override
    @Overwrite
    public void updateEntities() {
        // Sponge start
        /* 
        if (this.playerEntities.isEmpty()) {
            if (this.updateEntityTick++ >= 300) {
                return;
            }
        } else {
            this.resetUpdateEntityTick();
        }*/
        // Sponge end

        if (CauseTracker.ENABLED) {
            TrackingUtil.tickWorldProvider(this);
        } else {
            this.provider.onWorldUpdateEntities();
        }
        super.updateEntities();
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlockTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.onUpdateTick(block, worldIn, pos, state, rand);
    }

    // This ticks pending updates to blocks, Requires mixin for NextTickListEntry so we use the correct tracking
    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final PhaseData phaseData = causeTracker.getCurrentPhaseData();
        final IPhaseState phaseState = phaseData.state;
        if (phaseState.getPhase().alreadyCapturingBlockTicks(phaseState, phaseData.context) || phaseState.getPhase().ignoresBlockUpdateTick(phaseData)) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        IMixinBlock spongeBlock = (IMixinBlock) block;
        spongeBlock.getTimingsHandler().startTiming();
        TrackingUtil.updateTickBlock(causeTracker, block, pos, state, rand);
        spongeBlock.getTimingsHandler().stopTiming();
    }

    @Redirect(method = "addBlockEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer$ServerBlockEventList;add(Ljava/lang/Object;)Z", remap = false))
    public boolean onAddBlockEvent(WorldServer.ServerBlockEventList list, Object obj, BlockPos pos, Block blockIn, int eventId, int eventParam) {
        if (CauseTracker.ENABLED) {
            final CauseTracker causeTracker = this.getCauseTracker();
            final PhaseData currentPhase = causeTracker.getCurrentPhaseData();
            final IPhaseState phaseState = currentPhase.state;
            if (phaseState.getPhase().ignoresBlockEvent(phaseState)) {
                return list.add((BlockEventData) obj);
            }
            final BlockEventData blockEventData = (BlockEventData) obj;
            final PhaseContext context = currentPhase.context;

            IMixinBlockEventData blockEvent = (IMixinBlockEventData) blockEventData;
            phaseState.getPhase().addNotifierToBlockEvent(phaseState, context, causeTracker, pos, blockEvent);
        }
        return list.add((BlockEventData) obj);
    }

    // special handling for Pistons since they use their own event system
    @Redirect(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/WorldServer;fireBlockEvent(Lnet/minecraft/block/BlockEventData;)Z"))
    public boolean onFireBlockEvent(net.minecraft.world.WorldServer worldIn, BlockEventData event) {
        if (!CauseTracker.ENABLED) {
            fireBlockEvent(event);
        }
        final CauseTracker causeTracker = this.getCauseTracker();
        final IPhaseState phaseState = causeTracker.getCurrentState();
        if (phaseState.getPhase().ignoresBlockEvent(phaseState)) {
            return fireBlockEvent(event);
        }
        return TrackingUtil.fireMinecraftBlockEvent(causeTracker, worldIn, event);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTickEnd(CallbackInfo ci) {
        // Clean up any leaked chunks
        this.doChunkGC();
    }

    // Chunk GC
    private void doChunkGC() {
        this.chunkGCTickCount++;

        ChunkProviderServer chunkProviderServer = this.getChunkProvider();
        int chunkLoadCount = this.getChunkProvider().getLoadedChunkCount();
        if (chunkLoadCount >= this.chunkGCLoadThreshold && this.chunkGCLoadThreshold > 0) {
            chunkLoadCount = 0;
        } else if (this.chunkGCTickCount >= this.chunkGCTickInterval && this.chunkGCTickInterval > 0) {
            this.chunkGCTickCount = 0;
        } else {
            return;
        }

        long now = System.currentTimeMillis();
        long unloadAfter = this.chunkUnloadDelay;
        for (net.minecraft.world.chunk.Chunk chunk : chunkProviderServer.getLoadedChunks()) {
            IMixinChunk spongeChunk = (IMixinChunk) chunk;
            if (spongeChunk.getScheduledForUnload() != null && (now - spongeChunk.getScheduledForUnload()) > unloadAfter) {
                spongeChunk.setScheduledForUnload(null);
            }
            // If a player is currently using the chunk, skip it
            if (spongeChunk.getScheduledForUnload() != null || ((IMixinPlayerChunkMap) this.getPlayerChunkMap()).isChunkInUse(chunk.xPosition, chunk.zPosition)) {
                continue;
            }

            // Queue chunk for unload
            chunkProviderServer.unload(chunk);
            SpongeHooks.logChunkGCQueueUnload(chunkProviderServer.worldObj, chunk);
        }
    }

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;canSave()Z"))
    public boolean canChunkProviderSave(ChunkProviderServer chunkProviderServer) {
        if (chunkProviderServer.canSave()) {
            Sponge.getEventManager().post(SpongeEventFactory.createSaveWorldEventPre(Cause.of(NamedCause.source(SpongeImpl.getServer())), this));
            return true;
        }

        return false;
    }

    @Inject(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;getLoadedChunks()Ljava/util/Collection;"), cancellable = true)
    public void onSaveAllChunks(boolean saveAllChunks, IProgressUpdate progressCallback, CallbackInfo ci) {
        Sponge.getEventManager().post(SpongeEventFactory.createSaveWorldEventPost(Cause.of(NamedCause.source(SpongeImpl.getServer())), this));
        // The chunk GC handles all queuing for chunk unloads so we cancel here to avoid it during a save.
        if (this.chunkGCTickInterval > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "addBlockEvent", at = @At("HEAD"), cancellable = true)
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam, CallbackInfo ci) {
        // We fire a Pre event to make sure our captures do not get stuck in a loop.
        // This is very common with pistons as they add block events while blocks are being notified.
        if (SpongeCommonEventFactory.handleChangeBlockEventPre(this, pos)) {
            ci.cancel();
        }
    }

    @Redirect(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"), expect = 0, require = 0)
    private int onGetDimensionIdForBlockEvents(DimensionType dimensionType) {
        return this.getDimensionId();
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        ImmutableList.Builder<ScheduledBlockUpdate> builder = ImmutableList.builder();
        for (NextTickListEntry sbu : this.pendingTickListEntriesTreeSet) {
            if (sbu.position.equals(position)) {
                builder.add((ScheduledBlockUpdate) sbu);
            }
        }
        return builder.build();
    }

    @Nullable
    private NextTickListEntry tmpScheduledObj;

    @Redirect(method = "updateBlockTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/NextTickListEntry;setPriority(I)V"))
    private void onUpdateScheduledBlock(NextTickListEntry sbu, int priority) {
        this.onCreateScheduledBlockUpdate(sbu, priority);
    }

    @Redirect(method = "scheduleBlockUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/NextTickListEntry;setPriority(I)V"))
    private void onCreateScheduledBlockUpdate(NextTickListEntry sbu, int priority) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final IPhaseState phaseState = causeTracker.getCurrentState();

        if (phaseState.getPhase().ignoresScheduledUpdates(phaseState)) {
            this.tmpScheduledObj = sbu;
            return;
        }

        sbu.setPriority(priority);
        ((IMixinNextTickListEntry) sbu).setWorld((WorldServer) (Object) this);
        if (!((WorldServer) (Object) this).isBlockLoaded(sbu.position)) {
            this.tmpScheduledObj = sbu;
            return;
        }

        this.tmpScheduledObj = sbu;
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        BlockPos pos = new BlockPos(x, y, z);
        this.scheduleBlockUpdate(pos, getBlockState(pos).getBlock(), ticks, priority);
        ScheduledBlockUpdate sbu = (ScheduledBlockUpdate) this.tmpScheduledObj;
        this.tmpScheduledObj = null;
        return sbu;
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        // Note: Ignores position argument
        this.pendingTickListEntriesHashSet.remove(update);
        this.pendingTickListEntriesTreeSet.remove(update);
    }

    @Redirect(method = "updateAllPlayersSleepingFlag()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean isSpectatorOrIgnored(EntityPlayer entityPlayer) {
        // spectators are excluded from the sleep tally in vanilla
        // this redirect expands that check to include sleep-ignored players as well
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return ignore || entityPlayer.isSpectator();
    }

    @Redirect(method = "areAllPlayersAsleep()Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isPlayerFullyAsleep()Z"))
    public boolean isPlayerFullyAsleep(EntityPlayer entityPlayer) {
        // if isPlayerFullyAsleep() returns false areAllPlayerAsleep() breaks its loop and returns false
        // this redirect forces it to return true if the player is sleep-ignored even if they're not sleeping
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return ignore || entityPlayer.isPlayerFullyAsleep();
    }

    @Redirect(method = "areAllPlayersAsleep()Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean isSpectatorAndNotIgnored(EntityPlayer entityPlayer) {
        // if a player is marked as a spectator areAllPlayersAsleep() breaks its loop and returns false
        // this redirect forces it to return false if a player is sleep-ignored even if they're a spectator
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return !ignore && entityPlayer.isSpectator();
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        return Optional.ofNullable((Entity) this.getEntityFromUuid(uuid));
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState blockState, BlockChangeFlag flag, Cause cause) {
        checkBlockBounds(x, y, z);
        final CauseTracker causeTracker = this.getCauseTracker();
        final PhaseData peek = causeTracker.getCurrentPhaseData();
        boolean isWorldGen = CauseTracker.ENABLED && peek.state.getPhase().isWorldGeneration(peek.state);
        boolean handlesOwnCompletion = CauseTracker.ENABLED && peek.state.getPhase().handlesOwnPhaseCompletion(peek.state);
        if (!isWorldGen) {
            checkArgument(cause != null, "Cause cannot be null!");
            checkArgument(cause.root() instanceof PluginContainer, "PluginContainer must be at the ROOT of a cause!");
            checkArgument(flag != null, "BlockChangeFlag cannot be null!");
        }
        if (!isWorldGen && !handlesOwnCompletion) {
            final PhaseContext context = PhaseContext.start()
                    .add(NamedCause.of(InternalNamedCauses.General.PLUGIN_CAUSE, cause))
                    .addCaptures()
                    .add(NamedCause.of(InternalNamedCauses.General.BLOCK_CHANGE, flag))
                    .add(NamedCause.source(cause.root()));
            for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
                context.add(NamedCause.of(entry.getKey(), entry.getValue()));
            }
            context.complete();
            causeTracker.switchToPhase(PluginPhase.State.BLOCK_WORKER, context);
        }
        if (handlesOwnCompletion) {
            peek.context.firstNamed(InternalNamedCauses.General.BLOCK_CHANGE, PhaseContext.CaptureFlag.class)
                    .ifPresent(captureFlag -> captureFlag.addFlag(flag));
        }
        final boolean state = setBlockState(new BlockPos(x, y, z), (IBlockState) blockState, flag);
        if (!isWorldGen && !handlesOwnCompletion) {
            causeTracker.completePhase();
        }
        return state;
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BLOCK_MIN, BLOCK_MAX);
        }
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState currentState = this.getBlockState(pos);
        return this.createSpongeBlockSnapshot(currentState, currentState.getActualState((WorldServer) (Object) this, pos), pos, 2);
    }

    @Override
    public boolean spawnEntities(Iterable<? extends Entity> entities, Cause cause) {
        checkArgument(cause != null, "Cause cannot be null!");
        checkArgument(cause.root() instanceof SpawnCause, "SpawnCause must be at the ROOT of a cause!");
        checkArgument(cause.containsType(PluginContainer.class), "PluginContainer must be within the cause!");
        List<Entity> entitiesToSpawn = new ArrayList<>();
        entities.forEach(entitiesToSpawn::add);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(cause, entitiesToSpawn, this);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntities()) {
                this.forceSpawnEntity(entity);
            }
        }
        return event.isCancelled();
    }

    /**
     * @author gabizou - April 24th, 2016
     * @reason Needs to redirect the dimension id for the packet being sent to players
     * so that the dimension is correctly adjusted
     *
     * @param id The world provider's dimension id
     * @return True if the spawn was successful and the effect is played.
     */
    // We expect 0 because forge patches it correctly
    @Redirect(method = "addWeatherEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"), expect = 0, require = 0)
    public int getDimensionIdForWeatherEffect(DimensionType id) {
        return this.getDimensionId();
    }

    /**
     * @author gabizou - February 7th, 2016
     * @author gabizou - September 3rd, 2016 - Moved from MixinWorld since WorldServer overrides the method.
     *
     * This will short circuit all other patches such that we control the
     * entities being loaded by chunkloading and can throw our bulk entity
     * event. This will bypass Forge's hook for individual entity events,
     * but the SpongeModEventManager will still successfully throw the
     * appropriate event and cancel the entities otherwise contained.
     *
     * @param entities The entities being loaded
     * @param callbackInfo The callback info
     */
    @Final
    @Inject(method = "loadEntities", at = @At("HEAD"), cancellable = true)
    private void spongeLoadEntities(Collection<net.minecraft.entity.Entity> entities, CallbackInfo callbackInfo) {
        if (entities.isEmpty()) {
            // just return, no entities to load!
            callbackInfo.cancel();
            return;
        }
        List<Entity> entityList = new ArrayList<>();
        for (net.minecraft.entity.Entity entity : entities) {
            if (this.canAddEntity(entity)) {
                entityList.add((Entity) entity);
            }
        }
        SpawnCause cause = SpawnCause.builder().type(InternalSpawnTypes.CHUNK_LOAD).build();
        List<NamedCause> causes = new ArrayList<>();
        causes.add(NamedCause.source(cause));
        causes.add(NamedCause.of("World", this));
        SpawnEntityEvent.ChunkLoad chunkLoad = SpongeEventFactory.createSpawnEntityEventChunkLoad(Cause.of(causes), entityList, this);
        SpongeImpl.postEvent(chunkLoad);
        if (!chunkLoad.isCancelled() && chunkLoad.getEntities().size() > 0) {
            for (Entity successful : chunkLoad.getEntities()) {
                this.loadedEntityList.add((net.minecraft.entity.Entity) successful);
                this.onEntityAdded((net.minecraft.entity.Entity) successful);
            }
        }
        callbackInfo.cancel();
    }

    @Override
    public void triggerExplosion(org.spongepowered.api.world.explosion.Explosion explosion, Cause cause) {
        checkNotNull(explosion, "explosion");
        Location<org.spongepowered.api.world.World> origin = explosion.getLocation();
        checkNotNull(origin, "location");
        checkNotNull(cause, "Cause cannot be null!");
        checkArgument(cause.containsType(PluginContainer.class), "Cause must contain a PluginContainer!");
        if (CauseTracker.ENABLED) {
            final PhaseContext phaseContext = PhaseContext.start()
                    .add(NamedCause.source(cause))
                    .explosion()
                    .addEntityCaptures()
                    .addEntityDropCaptures()
                    .addBlockCaptures();
            phaseContext.getCaptureExplosion().addExplosion(explosion);
            phaseContext.complete();
            this.causeTracker.switchToPhase(PluginPhase.State.CUSTOM_EXPLOSION, phaseContext);
        }
        final Explosion mcExplosion;
        try {
            // Since we already have the API created implementation Explosion, let's use it.
            mcExplosion = (Explosion) explosion;
        } catch (Exception e) {
            new PrettyPrinter(60).add("Explosion not compatible with this implementation").centre().hr()
                    .add("An explosion that was expected to be used for this implementation does not")
                    .add("originate from this implementation.")
                    .add(e)
                    .trace();
            return;
        }
        final double x = mcExplosion.explosionX;
        final double y = mcExplosion.explosionY;
        final double z = mcExplosion.explosionZ;
        final boolean isSmoking = mcExplosion.isSmoking;
        final float strength = explosion.getRadius();

        // Set up the pre event
        final ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(cause, explosion, this);
        if (SpongeImpl.postEvent(event)) {
            this.processingExplosion = false;
            if (CauseTracker.ENABLED) {
                this.causeTracker.completePhase();
            }
            return;
        }
        // Sponge End

        mcExplosion.doExplosionA();
        mcExplosion.doExplosionB(false);

        if (!isSmoking) {
            mcExplosion.clearAffectedBlockPositions();
        }

        for (EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                ((EntityPlayerMP) entityplayer).connection.sendPacket(new SPacketExplosion(x, y, z, strength, mcExplosion.getAffectedBlockPositions(),
                        mcExplosion.getPlayerKnockbackMap().get(entityplayer)));
            }
        }

        // Sponge Start - end processing
        this.processingExplosion = false;
        if (CauseTracker.ENABLED) {
            this.causeTracker.completePhase();
        }
        // Sponge End
    }

    @Override
    public void triggerInternalExplosion(org.spongepowered.api.world.explosion.Explosion explosion) {
        checkNotNull(explosion, "explosion");
        Location<org.spongepowered.api.world.World> origin = explosion.getLocation();
        checkNotNull(origin, "location");
        newExplosion(EntityUtil.toNullableNative(explosion.getSourceExplosive().orElse(null)), origin.getX(),
                origin.getY(), origin.getZ(), explosion.getRadius(), explosion.canCauseFire(),
                explosion.shouldBreakBlocks()
        );
    }

    // ------------------------- Start Cause Tracking overrides of Minecraft World methods ----------

    /**
     * @author gabizou March 11, 2016
     *
     * The train of thought for how spawning is handled:
     * 1) This method is called in implementation
     * 2) handleVanillaSpawnEntity is called to associate various contextual SpawnCauses
     * 3) {@link CauseTracker#spawnEntity(Entity)} is called to check if the entity is to
     *    be "collected" or "captured" in the current {@link PhaseContext} of the current phase
     * 4) If the entity is forced or is captured, {@code true} is returned, otherwise, the entity is
     *    passed along normal spawning handling.
     */
    @Override
    public boolean spawnEntityInWorld(net.minecraft.entity.Entity entity) {
        return canAddEntity(entity) && getCauseTracker().spawnEntity(EntityUtil.fromNative(entity));
    }


    /**
     * @author gabizou, March 12th, 2016
     *
     * Move this into WorldServer as we should not be modifying the client world.
     *
     * Purpose: Rewritten to support capturing blocks
     */
    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (!this.isValid(pos)) {
            return false;
        } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) { // isRemote is always false since this is WorldServer
            return false;
        } else {
            // Sponge - reroute to the CauseTracker
            return this.getCauseTracker().setBlockState(pos, newState, flags);
        }
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, BlockChangeFlag flag) {
        if (!this.isValid(pos)) {
            return false;
        } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) { // isRemote is always false since this is WorldServer
            return false;
        } else {
            // Sponge - reroute to the CauseTracker
            return this.getCauseTracker().setBlockStateWithFlag(pos, state, flag);
        }
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        this.scheduledUpdatesAreImmediate = true;
        // Sponge start - Cause tracking
        final PhaseData peek = this.causeTracker.getCurrentPhaseData();
        if (!CauseTracker.ENABLED || peek.state.getPhase().ignoresBlockUpdateTick(peek)) {
            state.getBlock().updateTick((WorldServer) (Object) this, pos, state, random);
            // THIS NEEDS TO BE SET BACK TO FALSE OR ELSE ALL HELL BREAKS LOOSE!
            // No seriously, if this is not set back to false, all future updates are processed immediately
            // and various things get caught under the Unwinding Phase.
            this.scheduledUpdatesAreImmediate = false;
            return;
        }
        TrackingUtil.updateTickBlock(this.causeTracker, state.getBlock(), pos, state, random);
        // Sponge end
        this.scheduledUpdatesAreImmediate = false;
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void notifyBlockOfStateChange(BlockPos pos, Block blockIn) {
        this.getCauseTracker().notifyBlockOfStateChange(pos, blockIn, null);
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        if (!isValid(pos)) {
            return;
        }

        EnumSet<EnumFacing> directions = EnumSet.allOf(EnumFacing.class);
        directions.remove(skipSide);
        final NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, directions);
        if (event == null || !event.isCancelled()) {
            final CauseTracker causeTracker = this.getCauseTracker();
            for (EnumFacing facing : EnumFacing.values()) {
                if (event != null) {
                    final Direction direction = DirectionFacingProvider.getInstance().getKey(facing).get();
                    if (!event.getNeighbors().keySet().contains(direction)) {
                        continue;
                    }
                }

                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
            }
        }
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType) {
        if (!isValid(pos)) {
            return;
        }

        final NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, EnumSet.allOf(EnumFacing.class));
        if (event == null || !event.isCancelled()) {
            final CauseTracker causeTracker = this.getCauseTracker();
            for (EnumFacing facing : EnumFacing.values()) {
                if (event != null) {
                    final Direction direction = DirectionFacingProvider.getInstance().getKey(facing).get();
                    if (!event.getNeighbors().keySet().contains(direction)) {
                        continue;
                    }
                }

                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void onUpdateWeatherEffect(net.minecraft.entity.Entity entityIn) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final IPhaseState state = causeTracker.getCurrentState();
        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingEntityTicks(state)) {
            entityIn.onUpdate();
            return;
        }
        TrackingUtil.tickEntity(causeTracker, entityIn);
        updateRotation(entityIn);
    }

    @Override
    protected void onUpdateTileEntities(ITickable tile) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final IPhaseState state = causeTracker.getCurrentState();
        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingTileTicks(state)) {
            tile.update();
            return;
        }

        TrackingUtil.tickTileEntity(causeTracker, tile);
    }

    @Override
    protected void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final IPhaseState state = causeTracker.getCurrentState();
        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingEntityTicks(state)) {
            entity.onUpdate();
            return;
        }

        TrackingUtil.tickEntity(causeTracker, entity);
        updateRotation(entity);
    }

    @Override
    protected void onCallEntityRidingUpdate(net.minecraft.entity.Entity entity) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final IPhaseState state = causeTracker.getCurrentState();
        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingEntityTicks(state)) {
            entity.updateRidden();
            return;
        }

        TrackingUtil.tickRidingEntity(causeTracker, entity);
        updateRotation(entity);
    }

    @Redirect(method = "wakeAllPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;wakeUpPlayer(ZZZ)V"))
    private void spongeWakeUpPlayer(EntityPlayer player, boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        if (CauseTracker.ENABLED) {
            this.causeTracker.switchToPhase(EntityPhase.State.PLAYER_WAKE_UP, PhaseContext.start()
                    .add(NamedCause.source(player))
                    .addCaptures()
                    .complete()
            );
        }
        player.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
        if (CauseTracker.ENABLED) {
            this.causeTracker.completePhase();
        }
    }

    // ------------------------ End of Cause Tracking ------------------------------------

    // IMixinWorld method
    @Override
    public void spongeNotifyNeighborsPostBlockChange(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        if ((flags & 1) != 0) {
            this.notifyNeighborsRespectDebug(pos, newState.getBlock());

            if (newState.hasComparatorInputOverride()) {
                this.updateComparatorOutputLevel(pos, newState.getBlock());
            }
        }
    }

    // IMixinWorld method
    @Override
    public void addEntityRotationUpdate(net.minecraft.entity.Entity entity, Vector3d rotation) {
        this.rotationUpdates.put(entity, rotation);
    }

    // IMixinWorld method
    @Override
    public void updateRotation(net.minecraft.entity.Entity entityIn) {
        Vector3d rotationUpdate = this.rotationUpdates.get(entityIn);
        if (rotationUpdate != null) {
            entityIn.rotationPitch = (float) rotationUpdate.getX();
            entityIn.rotationYaw = (float) rotationUpdate.getY();
        }
        this.rotationUpdates.remove(entityIn);
    }

    @Override
    public void onSpongeEntityAdded(net.minecraft.entity.Entity entity) {
        this.onEntityAdded(entity);
    }

    @Override
    public void onSpongeEntityRemoved(net.minecraft.entity.Entity entity) {
        this.onEntityRemoved(entity);
    }

    @Override
    public boolean spawnEntity(Entity entity, Cause cause) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final IPhaseState state = causeTracker.getCurrentState();
        if (CauseTracker.ENABLED && !state.getPhase().alreadyCapturingEntitySpawns(state)) {
            causeTracker.switchToPhase(PluginPhase.State.CUSTOM_SPAWN, PhaseContext.start()
                .add(NamedCause.source(cause))
                .addCaptures()
                .complete());
            causeTracker.spawnEntityWithCause(entity, cause);
            causeTracker.completePhase();
            return true;
        }
        return causeTracker.spawnEntityWithCause(entity, cause);
    }

    @Override
    public boolean forceSpawnEntity(Entity entity) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final int x = minecraftEntity.getPosition().getX();
        final int z = minecraftEntity.getPosition().getZ();
        return forceSpawnEntity(minecraftEntity, x >> 4, z >> 4);
    }

    private boolean forceSpawnEntity(net.minecraft.entity.Entity entity, int chunkX, int chunkZ) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entity;
            this.playerEntities.add(entityplayer);
            this.updateAllPlayersSleepingFlag();
        }

        if (entity instanceof EntityLightningBolt) {
            this.addWeatherEffect(entity);
            return true;
        }

        this.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(entity);
        this.loadedEntityList.add(entity);
        this.onSpongeEntityAdded(entity);
        return true;
    }


    @Override
    public SpongeBlockSnapshot createSpongeBlockSnapshot(IBlockState state, IBlockState extended, BlockPos pos, int updateFlag) {
        this.builder.reset();
        this.builder.blockState((BlockState) state)
                .extendedState((BlockState) extended)
                .worldId(this.getUniqueId())
                .position(VecHelper.toVector3i(pos));
        Optional<UUID> creator = getCreator(pos.getX(), pos.getY(), pos.getZ());
        Optional<UUID> notifier = getNotifier(pos.getX(), pos.getY(), pos.getZ());
        if (creator.isPresent()) {
            this.builder.creator(creator.get());
        }
        if (notifier.isPresent()) {
            this.builder.notifier(notifier.get());
        }
        if (state.getBlock() instanceof ITileEntityProvider) {
            net.minecraft.tileentity.TileEntity te = getTileEntity(pos);
            if (te != null) {
                TileEntity tile = (TileEntity) te;
                for (DataManipulator<?, ?> manipulator : tile.getContainers()) {
                    this.builder.add(manipulator);
                }
                NBTTagCompound nbt = new NBTTagCompound();
                te.writeToNBT(nbt);
                this.builder.unsafeNbt(nbt);
            }
        }
        return new SpongeBlockSnapshot(this.builder, BlockChangeFlag.ALL.setUpdateNeighbors((updateFlag & 1) != 0), updateFlag);
    }

    /**
     * @author gabizou - September 10th, 2016
     * @reason Due to the amount of changes, and to ensure that Forge's events are being properly
     * thrown, we must overwrite to have our hooks in place where we need them to be and when.
     *
     * @param entityIn The entity that caused the explosion
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param strength The strength of the explosion, determines what blocks can be destroyed
     * @param isFlaming Whether fire will be caused from the explosion
     * @param isSmoking Whether blocks will break
     * @return The explosion
     */
    @Overwrite
    @Override
    public Explosion newExplosion(@Nullable net.minecraft.entity.Entity entityIn, double x, double y, double z, float strength, boolean isFlaming,
            boolean isSmoking) {
        // Sponge Start - Cause tracking
        this.processingExplosion = true;
        if (CauseTracker.ENABLED) {
            final PhaseContext phaseContext = PhaseContext.start()
                    .explosion()
                    .addEntityCaptures()
                    .addEntityDropCaptures()
                    .addBlockCaptures();
            final PhaseData currentPhaseData = this.causeTracker.getCurrentPhaseData();
            currentPhaseData.state.getPhase().appendContextPreExplosion(phaseContext, currentPhaseData);
            phaseContext.complete();
            this.causeTracker.switchToPhase(GeneralPhase.State.EXPLOSION, phaseContext);
        }
        // Sponge End

        Explosion explosion = new Explosion((WorldServer) (Object) this, entityIn, x, y, z, strength, isFlaming, isSmoking);

        // Sponge Start - More cause tracking
        if (CauseTracker.ENABLED) {
            try {
                this.causeTracker.getCurrentContext().getCaptureExplosion().addExplosion(((org.spongepowered.api.world.explosion.Explosion) explosion));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Set up the pre event
        final ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(((IMixinExplosion) explosion).createCause(),
                (org.spongepowered.api.world.explosion.Explosion) explosion, this);
        if (SpongeImpl.postEvent(event)) {
            this.processingExplosion = false;
            if (CauseTracker.ENABLED) {
                this.causeTracker.completePhase();
            }
            return explosion;
        }
        // Sponge End

        explosion.doExplosionA();
        explosion.doExplosionB(false);

        if (!isSmoking) {
            explosion.clearAffectedBlockPositions();
        }

        for (EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                ((EntityPlayerMP) entityplayer).connection.sendPacket(new SPacketExplosion(x, y, z, strength, explosion.getAffectedBlockPositions(),
                        explosion.getPlayerKnockbackMap().get(entityplayer)));
            }
        }

        // Sponge Start - end processing
        this.processingExplosion = false;
        if (CauseTracker.ENABLED) {
            this.causeTracker.completePhase();
        }
        // Sponge End
        return explosion;
    }

    /**
     * @author amaranth - April 25th, 2016
     * @reason Avoid 25 chunk map lookups per entity per tick by using neighbor pointers
     *
     * @param xStart X block start coordinate
     * @param yStart Y block start coordinate
     * @param zStart Z block start coordinate
     * @param xEnd X block end coordinate
     * @param yEnd Y block end coordinate
     * @param zEnd Z block end coordinate
     * @param allowEmpty Whether empty chunks should be accepted
     * @return If the chunks for the area are loaded
     */
    @Override
    public boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
        if (yEnd < 0 || yStart > 255) {
            return false;
        }

        xStart = xStart >> 4;
        zStart = zStart >> 4;
        xEnd = xEnd >> 4;
        zEnd = zEnd >> 4;

        Chunk base = (Chunk) this.getChunkProvider().getLoadedChunk(xStart, zStart);
        if (base == null) {
            return false;
        }

        Optional<Chunk> currentColumn = Optional.of(base);
        for (int i = xStart; i <= xEnd; i++) {
            if (!currentColumn.isPresent()) {
                return false;
            }

            Chunk column = currentColumn.get();

            Optional<Chunk> currentRow = column.getNeighbor(Direction.SOUTH);
            for (int j = zStart; j <= zEnd; j++) {
                if (!currentRow.isPresent()) {
                    return false;
                }

                Chunk row = currentRow.get();

                if (!allowEmpty && ((net.minecraft.world.chunk.Chunk) row).isEmpty()) {
                    return false;
                }

                currentRow = row.getNeighbor(Direction.SOUTH);
            }

            currentColumn = column.getNeighbor(Direction.EAST);
        }

        return true;
    }

    @Override
    public WorldStorage getWorldStorage() {
        return (WorldStorage) ((WorldServer) (Object) this).getChunkProvider();
    }

    @Override
    public PortalAgent getPortalAgent() {
        return (PortalAgent) this.worldTeleporter;
    }

    /**************************** TIMINGS ***************************************/
    /*
    The remaining of these overridden methods are all injectors into World#updateEntities() to where
    the exact fine tuning of where the methods are invoked, the call stack is precisely emulated as
    if this were an overwrite. The injections themselves are sensitive in some regards, but mostly
    will remain just fine.
     */


    @Override
    protected void startEntityGlobalTimings() {
        this.timings.entityTick.startTiming();
        co.aikar.timings.TimingHistory.entityTicks += this.loadedEntityList.size();
    }

    @Override
    protected void stopTimingForWeatherEntityTickCrash(net.minecraft.entity.Entity updatingEntity) {
        EntityUtil.toMixin(updatingEntity).getTimingsHandler().stopTiming();
    }

    @Override
    protected void stopEntityTickTimingStartEntityRemovalTiming() {
        this.timings.entityTick.stopTiming();
        this.timings.entityRemoval.startTiming();
    }

    @Override
    protected void stopEntityRemovalTiming() {
        this.timings.entityRemoval.stopTiming();
    }

    @Override
    protected void startEntityTickTiming() {
        this.timings.entityTick.startTiming();
    }

    @Override
    protected void stopTimingTickEntityCrash(net.minecraft.entity.Entity updatingEntity) {
        EntityUtil.toMixin(updatingEntity).getTimingsHandler().stopTiming();
    }

    @Override
    protected void stopEntityTickSectionBeforeRemove() {
       this.timings.entityTick.stopTiming();
    }

    @Override
    protected void startEntityRemovalTick() {
        this.timings.entityRemoval.startTiming();
    }

    @Override
    protected void startTileTickTimer() {
        this.timings.tileEntityTick.startTiming();
    }

    @Override
    protected void stopTimingTickTileEntityCrash(net.minecraft.tileentity.TileEntity updatingTileEntity) {
        ((IMixinTileEntity) updatingTileEntity).getTimingsHandler().stopTiming();
    }

    @Override
    protected void stopTileEntityAndStartRemoval() {
        this.timings.tileEntityTick.stopTiming();
        this.timings.tileEntityRemoval.startTiming();
    }

    @Override
    protected void stopTileEntityRemovelInWhile() {
        this.timings.tileEntityRemoval.stopTiming();
    }

    @Override
    protected void startPendingTileEntityTimings() {
        this.timings.tileEntityPending.startTiming();
    }

    @Override
    protected void endPendingTileEntities() {
        this.timings.tileEntityPending.stopTiming();
        TimingHistory.tileEntityTicks += this.loadedTileEntityList.size();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=tickPending") )
    private void onBeginTickBlockUpdate(CallbackInfo ci) {
        this.timings.scheduledBlocks.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=tickBlocks") )
    private void onAfterTickBlockUpdate(CallbackInfo ci) {
        this.timings.scheduledBlocks.stopTiming();
        this.timings.updateBlocks.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=chunkMap") )
    private void onBeginUpdateBlocks(CallbackInfo ci) {
        this.timings.updateBlocks.stopTiming();
        this.timings.doChunkMap.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=village") )
    private void onBeginUpdateVillage(CallbackInfo ci) {
        this.timings.doChunkMap.stopTiming();
        this.timings.doVillages.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=portalForcer"))
    private void onBeginUpdatePortal(CallbackInfo ci) {
        this.timings.doVillages.stopTiming();
        this.timings.doPortalForcer.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
    private void onEndUpdatePortal(CallbackInfo ci) {
        this.timings.doPortalForcer.stopTiming();
    }
    // TIMINGS
    @Inject(method = "tickUpdates", at = @At(value = "INVOKE_STRING", target = PROFILER_SS, args = "ldc=cleaning"))
    private void onTickUpdatesCleanup(boolean flag, CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksCleanup.startTiming();

    }

    @Inject(method = "tickUpdates", at = @At(value = "INVOKE_STRING", target = PROFILER_SS, args = "ldc=ticking"))
    private void onTickUpdatesTickingStart(boolean flag, CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksCleanup.stopTiming();
        this.timings.scheduledBlocksTicking.startTiming();
    }

    @Inject(method = "tickUpdates", at = @At("RETURN"))
    private void onTickUpdatesTickingEnd(CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksTicking.stopTiming();
    }

    @Override
    public WorldTimingsHandler getTimingsHandler() {
        return this.timings;
    }

    /**************************** EFFECT ****************************************/

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {
        this.playSound(sound, category, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound,  SoundCategory category, Vector3d position, double volume, double pitch) {
        this.playSound(sound, category, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound,  SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        this.playSound(null, position.getX(), position.getY(), position.getZ(), SoundEvents.getRegisteredSoundEvent(sound.getId()), (net.minecraft.util.SoundCategory) (Object) category, (float) Math.max(minVolume, volume), (float) pitch);
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

        List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            PlayerList playerList = this.mcServer.getPlayerList();

            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            for (Packet<?> packet : packets) {
                playerList.sendToAllNearExcept(null, x, y, z, radius, this.getDimensionId(), packet);
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
    public void setWeather(Weather weather) {
        if (weather.equals(Weathers.CLEAR)) {
            this.setWeather(weather, (300 + this.rand.nextInt(600)) * 20);
        } else {
            this.setWeather(weather, 0);
        }
    }

    @Override
    public void setWeather(Weather weather, long duration) {
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

    @Inject(method = "updateWeather", at = @At(value = "RETURN"))
    public void onUpdateWeatherReturn(CallbackInfo ci) {
        Weather weather = getWeather();
        int duration = (int) getRemainingDuration();
        if (this.prevWeather != weather && duration > 0) {
            ChangeWorldWeatherEvent event = SpongeEventFactory.createChangeWorldWeatherEvent(Cause.of(NamedCause.source(this)), duration, duration,
                    weather, weather, this.prevWeather, this);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                this.setWeather(this.prevWeather);
            } else {
                this.setWeather(event.getWeather(), event.getDuration());
                this.prevWeather = event.getWeather();
                this.weatherStartTime = this.worldInfo.getWorldTotalTime();
            }
        }
    }

    @Override
    public long getWeatherStartTime() {
        return this.weatherStartTime;
    }

    @Override
    public void setWeatherStartTime(long weatherStartTime) {
        this.weatherStartTime = weatherStartTime;
    }

    @Override
    public int getChunkGCTickInterval() {
        return this.chunkGCTickInterval;
    }

    @Override
    public long getChunkUnloadDelay() {
        return this.chunkUnloadDelay;
    }
}
