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
package org.spongepowered.common.mixin.core.server.level;

import co.aikar.timings.sponge.WorldTimingsHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.ChangeWeatherEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.WeatherTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.level.PlatformServerLevelBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.mixin.core.world.level.LevelMixin;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends LevelMixin implements ServerLevelBridge, PlatformServerLevelBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow @Final private ServerLevelData serverLevelData;

    @Shadow @Nonnull public abstract MinecraftServer shadow$getServer();
    @Shadow protected abstract void shadow$saveLevelData();
    // @formatter:on

    private LevelStorageSource.LevelStorageAccess impl$levelSave;
    private CustomBossEvents impl$bossBarManager;
    private SpongeRegistryHolder impl$registerHolder;
    private ChunkProgressListener impl$chunkStatusListener;
    private Map<Entity, Vector3d> impl$rotationUpdates;
    private Weather impl$prevWeather;

    private boolean impl$isManualSave = false;
    protected WorldTimingsHandler impl$timings = new WorldTimingsHandler((ServerLevel) (Object) this);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$cacheLevelSave(final MinecraftServer p_i241885_1_, final Executor p_i241885_2_, final LevelStorageSource.LevelStorageAccess p_i241885_3_,
            final ServerLevelData p_i241885_4_, final net.minecraft.resources.ResourceKey<Level> p_i241885_5_, final DimensionType p_i241885_6_, final ChunkProgressListener p_i241885_7_,
            final ChunkGenerator p_i241885_8_, final boolean p_i241885_9_, final long p_i241885_10_, final List<CustomSpawner> p_i241885_12_, final boolean p_i241885_13_,
            final CallbackInfo ci) {
        this.impl$levelSave = p_i241885_3_;
        this.impl$chunkStatusListener = p_i241885_7_;
        this.impl$rotationUpdates = new Object2ObjectOpenHashMap<>();
        this.impl$registerHolder = new SpongeRegistryHolder(((RegistryAccess.RegistryHolder) p_i241885_1_.registryAccess()));
        this.impl$prevWeather = ((ServerWorld) this).weather();
    }

    @Redirect(method = "getSeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;worldGenSettings()Lnet/minecraft/world/level/levelgen/WorldGenSettings;"))
    public WorldGenSettings impl$onGetSeed(final WorldData iServerConfiguration) {
        return ((PrimaryLevelData) this.serverLevelData).worldGenSettings();
    }

    @Override
    public LevelStorageSource.LevelStorageAccess bridge$getLevelSave() {
        return this.impl$levelSave;
    }

    @Override
    public ChunkProgressListener bridge$getChunkStatusListener() {
        return this.impl$chunkStatusListener;
    }

    @Override
    public boolean bridge$isLoaded() {
        if (((WorldBridge) this).bridge$isFake()) {
            return false;
        }

        final ServerLevel world = this.shadow$getServer().getLevel(this.shadow$dimension());
        if (world == null) {
            return false;
        }

        return world == (Object) this;
    }

    @Override
    public void bridge$adjustDimensionLogic(final DimensionType dimensionType) {
        if (this.bridge$isFake()) {
            return;
        }

        super.bridge$adjustDimensionLogic(dimensionType);

        // TODO Minecraft 1.16.2 - Rebuild level stems, get generator from type, set generator
        // TODO ...or cache generator on type?
    }

    @Override
    public CustomBossEvents bridge$getBossBarManager() {
        if (this.impl$bossBarManager == null) {
            if (Level.OVERWORLD.equals(this.shadow$dimension()) || this.bridge$isFake()) {
                this.impl$bossBarManager = this.shadow$getServer().getCustomBossEvents();
            } else {
                this.impl$bossBarManager = new CustomBossEvents();
            }
        }

        return this.impl$bossBarManager;
    }

    @Override
    public void bridge$addEntityRotationUpdate(final net.minecraft.world.entity.Entity entity, final Vector3d rotation) {
        this.impl$rotationUpdates.put(entity, rotation);
    }

    @Override
    public void bridge$updateRotation(final net.minecraft.world.entity.Entity entityIn) {
        final Vector3d rotationUpdate = this.impl$rotationUpdates.get(entityIn);
        if (rotationUpdate != null) {
            entityIn.xRot = (float) rotationUpdate.x();
            entityIn.yRot = (float) rotationUpdate.y();
        }
        this.impl$rotationUpdates.remove(entityIn);
    }

    @Override
    public void bridge$triggerExplosion(Explosion explosion) {
        // Sponge start
        // Set up the pre event
        if (ShouldFire.EXPLOSION_EVENT_PRE) {
            final ExplosionEvent.Pre
                    event =
                    SpongeEventFactory.createExplosionEventPre(PhaseTracker.getCauseStackManager().currentCause(),
                            explosion, (org.spongepowered.api.world.server.ServerWorld) this);
            if (SpongeCommon.postEvent(event)) {
                return;
            }
            explosion = event.explosion();
        }

        final net.minecraft.world.level.Explosion mcExplosion = (net.minecraft.world.level.Explosion) explosion;

        try (final PhaseContext<?> ignored = GeneralPhase.State.EXPLOSION.createPhaseContext(PhaseTracker.SERVER)
                .explosion((net.minecraft.world.level.Explosion) explosion)
                .source(explosion.sourceExplosive().isPresent() ? explosion.sourceExplosive() : this)) {
            ignored.buildAndSwitch();
            final boolean shouldBreakBlocks = explosion.shouldBreakBlocks();
            // Sponge End

            mcExplosion.explode();
            mcExplosion.finalizeExplosion(true);

            if (!shouldBreakBlocks) {
                mcExplosion.clearToBlow();
            }

            // Sponge Start - end processing
        }
        // Sponge End
    }

    @Override
    public void bridge$setManualSave(final boolean state) {
        this.impl$isManualSave = state;
    }

    @Override
    public RegistryHolder bridge$registries() {
        return this.impl$registerHolder;
    }

    @Override
    public BlockSnapshot bridge$createSnapshot(final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);

        if (!Level.isInWorldBounds(pos)) {
            return BlockSnapshot.empty();
        }

        if (!this.hasChunk(x >> 4, z >> 4)) {
            return BlockSnapshot.empty();
        }
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.world((ServerLevel) (Object) this).position(new Vector3i(x, y, z));
        final net.minecraft.world.level.chunk.LevelChunk chunk = this.shadow$getChunkAt(pos);
        final net.minecraft.world.level.block.state.BlockState state = chunk.getBlockState(pos);
        builder.blockState(state);
        final net.minecraft.world.level.block.entity.BlockEntity blockEntity = chunk.getBlockEntity(pos, net.minecraft.world.level.chunk.LevelChunk.EntityCreationType.CHECK);
        if (blockEntity != null) {
            TrackingUtil.addTileEntityToBuilder(blockEntity, builder);
        }
        ((LevelChunkBridge) chunk).bridge$getBlockCreatorUUID(pos).ifPresent(builder::creator);
        ((LevelChunkBridge) chunk).bridge$getBlockNotifierUUID(pos).ifPresent(builder::notifier);

        builder.flag(BlockChangeFlags.NONE);
        return builder.build();
    }

    @Override
    public ResourceKey bridge$getKey() {
        return (ResourceKey) (Object) this.shadow$dimension().location();
    }

    @Redirect(method = "saveLevelData", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorldData()Lnet/minecraft/world/level/storage/WorldData;"))
    private WorldData impl$usePerWorldLevelDataForDragonFight(final MinecraftServer server) {
        return (WorldData) this.shadow$getLevelData();
    }

    @Redirect(method = "setDefaultSpawnPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private void impl$respectKeepSpawnLoaded(final ServerChunkCache serverChunkProvider, final TicketType<Object> p_217228_1_, final ChunkPos p_217228_2_,
            final int p_217228_3_, final Object p_217228_4_) {
        if ((((ServerWorldProperties) this.shadow$getLevelData()).performsSpawnLogic())) {
            serverChunkProvider.addRegionTicket(p_217228_1_, p_217228_2_, p_217228_3_, p_217228_4_);
        }
    }

    /**
     * @author zidane - December 17th, 2020 - Minecraft 1.16.4
     * @reason Honor our serialization behavior in performing saves
     */
    @Overwrite
    public void save(@Nullable final ProgressListener progress, final boolean flush, final boolean skipSave) {

        final Cause currentCause = Sponge.server().causeStackManager().currentCause();

        if (Sponge.eventManager().post(SpongeEventFactory.createSaveWorldEventPre(currentCause, ((ServerWorld) this)))) {
            return; // cancelled save
        }

        final PrimaryLevelData levelData = (PrimaryLevelData) this.shadow$getLevelData();

        final ServerChunkCache chunkProvider = ((ServerLevel) (Object) this).getChunkSource();

        if (!skipSave) {

            final SerializationBehavior behavior = ((PrimaryLevelDataBridge) levelData).bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);

            if (progress != null) {
                progress.progressStartNoAbort(new TranslatableComponent("menu.savingLevel"));
            }

            // We always save the metadata unless it is NONE
            if (behavior != SerializationBehavior.NONE) {

                this.shadow$saveLevelData();

                // Sponge Start - We do per-world WorldInfo/WorldBorders/BossBars

                levelData.setWorldBorder(this.getWorldBorder().createSettings());

                levelData.setCustomBossEvents(((ServerLevelBridge) this).bridge$getBossBarManager().save());

                ((ServerLevelBridge) this).bridge$getLevelSave().saveDataTag(SpongeCommon.getServer().registryAccess()
                    , (PrimaryLevelData) this.shadow$getLevelData(), this.shadow$dimension() == Level.OVERWORLD ? SpongeCommon.getServer().getPlayerList()
                        .getSingleplayerData() : null);

                // Sponge End
            }
            if (progress != null) {
                progress.progressStage(new TranslatableComponent("menu.savingChunks"));
            }

            final boolean canAutomaticallySave = !this.impl$isManualSave && behavior == SerializationBehavior.AUTOMATIC;
            final boolean canManuallySave = this.impl$isManualSave && behavior == SerializationBehavior.MANUAL;

            if (canAutomaticallySave || canManuallySave) {
                chunkProvider.save(flush);
            }

            Sponge.eventManager().post(SpongeEventFactory.createSaveWorldEventPost(currentCause, ((ServerWorld) this)));
        }

        this.impl$isManualSave = false;
    }

    @Inject(method = "tick",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;oRainLevel:F", shift = At.Shift.BEFORE, ordinal = 1))
    public void impl$onSetWeatherParameters(final BooleanSupplier param0, final CallbackInfo ci, final ProfilerFiller var0, final boolean wasRaining) {
        final boolean isRaining = this.shadow$isRaining();
        if (this.oRainLevel != this.rainLevel || this.oThunderLevel != this.thunderLevel || wasRaining != isRaining) {
            Weather newWeather = ((ServerWorld) this).properties().weather();
            final Cause currentCause = Sponge.server().causeStackManager().currentCause();
            final Transaction<Weather> weatherTransaction = new Transaction<>(this.impl$prevWeather, newWeather);
            final ChangeWeatherEvent event = SpongeEventFactory.createChangeWeatherEvent(currentCause, ((ServerWorld) this), weatherTransaction);
            if (Sponge.eventManager().post(event)) {
                newWeather = event.weather().original();
            } else {
                newWeather = event.weather().finalReplacement();
            }

            // Set event results
            this.impl$prevWeather = newWeather;
            if (newWeather.type() == WeatherTypes.CLEAR.get()) {
                this.serverLevelData.setThunderTime(0);
                this.serverLevelData.setRainTime(0);
                this.serverLevelData.setClearWeatherTime((int) newWeather.remainingDuration().ticks());
                this.serverLevelData.setThundering(false);
                this.serverLevelData.setRaining(false);
            } else {
                final int newTime = (int) newWeather.remainingDuration().ticks();
                this.serverLevelData.setRaining(true);
                this.serverLevelData.setClearWeatherTime(0);
                this.serverLevelData.setRainTime(newTime);
                if (newWeather.type() == WeatherTypes.THUNDER.get()) {
                    this.serverLevelData.setThunderTime(newTime);
                    this.serverLevelData.setThundering(true);
                } else {
                    this.serverLevelData.setThunderTime(0);
                    this.serverLevelData.setThundering(false);
                }
            }
        }

    }

    @Override
    public WorldTimingsHandler bridge$getTimingsHandler() {
        return this.impl$timings;
    }

    @Override
    public String toString() {
        return new StringJoiner(",", ServerLevel.class.getSimpleName() + "[", "]")
                .add("key=" + this.shadow$dimension())
                .add("worldType=" + ((WorldType) this.shadow$dimensionType()).key(RegistryTypes.WORLD_TYPE))
                .toString();
    }
}
