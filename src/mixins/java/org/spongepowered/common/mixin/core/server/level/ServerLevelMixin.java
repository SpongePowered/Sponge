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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTicks;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.sound.PlaySoundEvent;
import org.spongepowered.api.event.world.ChangeWeatherEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.WeatherTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.entity.MobAccessor;
import org.spongepowered.common.accessor.world.entity.item.FallingBlockEntityAccessor;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.bridge.world.level.border.WorldBorderBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.level.storage.DimensionDataStorageBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.bridge.world.level.storage.ServerLevelDataBridge;
import org.spongepowered.common.bridge.world.ticks.LevelTicksBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.world.level.LevelMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.world.server.SpongeRegistryData;
import org.spongepowered.common.world.server.SpongeServerLevelData;
import org.spongepowered.common.world.weather.SpongeWeather;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends LevelMixin implements ServerLevelBridge {

    // @formatter:off
    @Shadow @Final private ServerLevelData serverLevelData;
    @Shadow @Final private PersistentEntitySectionManager<Entity> entityManager;
    @Shadow @Final private LevelTicks<Block> blockTicks;
    @Shadow @Final private LevelTicks<Fluid> fluidTicks;
    @Shadow private int emptyTime;

    @Shadow @NonNull public abstract MinecraftServer shadow$getServer();
    @Shadow @Final private MinecraftServer server;

    @Shadow @Nullable private EnderDragonFight dragonFight;
    @Shadow @Final List<ServerPlayer> players;
    @Shadow public abstract SavedDataStorage shadow$getDataStorage();

    @Shadow public abstract DifficultyInstance shadow$getCurrentDifficultyAt(BlockPos p_175649_1_);
    @Shadow public abstract WeatherData shadow$getWeatherData();
    // @formatter:on


    private final long[] impl$recentTickTimes = new long[100];

    private LevelStorageSource.LevelStorageAccess impl$levelSave;
    private CustomBossEvents impl$bossBarManager;
    private Weather impl$prevWeather;
    private boolean impl$isManualSave = false;
    private long impl$preTickTime = 0L;
    @Unique private @Nullable GameRules impl$gameRules;

    /**
     * Initializes per-world {@link WeatherData} via vanilla's {@link SavedDataStorage}.
     * This intercepts the {@code server.getWeatherData()} call used by {@code prepareWeather}
     * in the constructor, ensuring each world has independent weather state from creation.
     *
     * <p>Since each dimension has its own {@link SavedDataStorage} (created by
     * {@link ServerChunkCache} at {@code getDimensionPath(dim).resolve("data")}),
     * {@code computeIfAbsent(WeatherData.TYPE)} creates a per-dimension {@code weather.dat}
     * file automatically.</p>
     */
    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWeatherData()Lnet/minecraft/world/level/saveddata/WeatherData;")
    )
    private WeatherData impl$initPerWorldWeatherData(final MinecraftServer instance, final Operation<WeatherData> original) {
        return this.shadow$getDataStorage().computeIfAbsent(WeatherData.TYPE);
    }

    /**
     * Redirects {@link ServerLevel#getWeatherData()} to return the per-world {@link WeatherData}
     * from this dimension's {@link SavedDataStorage} instead of the global server weather data.
     * This enables independent weather cycles, commands, and API access for each world.
     */
    @WrapOperation(
        method = "getWeatherData",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWeatherData()Lnet/minecraft/world/level/saveddata/WeatherData;")
    )
    private WeatherData impl$usePerWorldWeatherData(final MinecraftServer instance, final Operation<WeatherData> original) {
        return this.shadow$getDataStorage().computeIfAbsent(WeatherData.TYPE);
    }

    /**
     * Overrides {@link ServerLevel#getGameRules()} to return per-dimension game rules
     * backed by a per-dimension {@link GameRuleMap} stored via {@link SavedDataStorage}.
     *
     * <p>The per-dimension {@code GameRuleMap} is loaded (or created) via
     * {@code computeIfAbsent(GameRuleMap.TYPE)}, which stores/reads from
     * each dimension's own {@code data/} directory as {@code game_rules.dat}.</p>
     */
    @WrapOperation(
        method = "getGameRules",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/gamerules/GameRules;")
    )
    private GameRules impl$perWorldGameRules(final MinecraftServer instance, final Operation<GameRules> original) {
        if (this.impl$gameRules == null) {
            final GameRuleMap perWorldMap = this.shadow$getDataStorage().computeIfAbsent(GameRuleMap.TYPE);
            final boolean isNewWorld = perWorldMap.size() == 0;
            this.impl$gameRules = new GameRules(
                instance.getWorldData().enabledFeatures(),
                perWorldMap
            );
            if (isNewWorld) {
                // New world — copy the server's current game rule values as initial defaults
                this.impl$gameRules.setAll(original.call(instance), null);
            }
        }
        return this.impl$gameRules;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$onInit(
        final MinecraftServer server,
        final Executor executor,
        final LevelStorageSource.LevelStorageAccess storage,
        final ServerLevelData levelData,
        final net.minecraft.resources.ResourceKey<Level> dimension,
        final LevelStem levelStem,
        final boolean isDebug,
        final long biomeZoomSeed,
        final List<CustomSpawner> customSpawners,
        final boolean tickTime,
        final CallbackInfo ci
    ) {
        final SpongeServerLevelData spongeData = ((ServerLevelDataBridge) levelData).bridge$spongeData();

        final ResourceKey worldKey = ((ServerWorld) this).key();

        // Load per-dimension identity from SpongeRegistryData (SavedDataStorage)
        final SpongeRegistryData registryData = this.shadow$getDataStorage().computeIfAbsent(SpongeRegistryData.TYPE);
        if (registryData.key() == null) {
            registryData.setKey(worldKey);
        }
        // Populate SpongeServerLevelData from registry data
        spongeData.setUniqueId(registryData.uniqueId());
        if (spongeData.key() == null) {
            spongeData.setKey(worldKey);
        } else if (!spongeData.key().equals(worldKey)) {
            SpongeCommon.logger().warn("Level data ({}) has key {} but is used by world {} ({}).", levelData.getClass().getSimpleName(), spongeData.key(), worldKey, this.getClass().getSimpleName());
        }

        if (spongeData.configAdapter() == null) {
            final DimensionType dimensionType = levelStem.type().value();
            spongeData.setConfigAdapter(SpongeGameConfigs.load(dimensionType, spongeData.key()));
        }

        this.impl$levelSave = storage;
        this.impl$prevWeather = SpongeWeather.of(this.shadow$getWeatherData());
        ((LevelTicksBridge<?>) this.blockTicks).bridge$level((ServerLevel) (Object) this);
        ((LevelTicksBridge<?>) this.fluidTicks).bridge$level((ServerLevel) (Object) this);

        final Boolean createDragonFight = ((DimensionTypeBridge) (Object) this.shadow$dimensionType()).bridge$createDragonFight();
        if (createDragonFight != null) {
            if (createDragonFight) {
                final long seed = spongeData.worldGenOptions().seed();
                this.dragonFight = this.shadow$getDataStorage().computeIfAbsent(EnderDragonFight.TYPE);
                this.dragonFight.init((ServerLevel) (Object) this, seed, BlockPos.ZERO);
            } else {
                this.dragonFight = null;
            }
        }

        this.bridge$adjustDimensionLogic(levelStem.type().value());
    }

    @Override
    public LevelStorageSource.LevelStorageAccess bridge$getLevelSave() {
        return this.impl$levelSave;
    }

    @Override
    public boolean bridge$isLoaded() {
        if (((LevelBridge) this).bridge$isFake()) {
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

        ((DimensionDataStorageBridge) this.shadow$getDataStorage()).bridge$dimensionKey(
            this.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).getKey(dimensionType));

        // TODO Minecraft 1.16.2 - Rebuild level stems, get generator from type, set generator
        // TODO ...or cache generator on type?

        this.impl$setWorldOnBorder();
    }

    @Override
    public CustomBossEvents bridge$getBossBarManager() {
        if (this.impl$bossBarManager == null) {
            if (this.bridge$isFake()) {
                this.impl$bossBarManager = this.shadow$getServer().getCustomBossEvents();
            } else {
                this.impl$bossBarManager = this.shadow$getDataStorage().computeIfAbsent(CustomBossEvents.TYPE);
            }
        }

        return this.impl$bossBarManager;
    }

    @Override
    public void bridge$triggerExplosion(Explosion explosion) {
        // Set up the pre event
        final PhaseTracker phaseTracker = PhaseTracker.getWorldInstance((ServerLevel) (Object) this);
        if (ShouldFire.EXPLOSION_EVENT_PRE) {
            final var cause = phaseTracker.currentCause();
            final ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(cause, explosion, (ServerWorld) this);
            if (SpongeCommon.post(event)) {
                return;
            }
            explosion = event.explosion();
        }

        final ServerExplosion mcExplosion = (ServerExplosion) explosion;

        try (final PhaseContext<?> ignored = GeneralPhase.State.EXPLOSION.createPhaseContext(phaseTracker)
            .explosion(mcExplosion)
            .source(explosion.sourceExplosive().isPresent() ? explosion.sourceExplosive() : this)) {
            ignored.buildAndSwitch();

            mcExplosion.explode();

            // see ServerLevel#explode/Level#explode
            ParticleOptions particle = mcExplosion.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;
            var sound = SoundEvents.GENERIC_EXPLODE;
            final var explosionSource = mcExplosion.getDirectSourceEntity();
            final boolean playSound = explosionSource == null || !explosionSource.isSilent();
            for (ServerPlayer player : this.players) {
                if (player.distanceToSqr(mcExplosion.center()) < 4096.0) {
                    Optional<Vec3> kb = Optional.ofNullable(mcExplosion.getHitPlayers().get(player));
                    final var packet = new ClientboundExplodePacket(mcExplosion.center(), explosion.radius(), 1, kb, particle, sound, WeightedList.of(), playSound);
                    this.bridge$handleExplosionPacket(player.connection, explosion, packet);
                    player.connection.send(packet);
                }
            }
        }
    }

    @Override
    public void bridge$handleExplosionPacket(final ServerGamePacketListenerImpl instance, Explosion apiExplosion, final ClientboundExplodePacket packet) {
        // TODO control particle in API
        ParticleOptions particleData = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AIR.defaultBlockState()); // "no" particle
        particleData = packet.explosionParticle();
        // TODO control sound in API
        var soundEvent = Holder.direct(new SoundEvent(Identifier.parse("sponge:none"), Optional.of(0f))); // "no" sound
        soundEvent = packet.explosionSound();
        // TODO apiExplosion.shouldPlaySmoke() is not initialized correctly
        var newPacket = new ClientboundExplodePacket(packet.center(), packet.radius(), packet.blockCount(), packet.playerKnockback(), particleData, soundEvent, packet.blockParticles(), packet.playSound());
        instance.send(newPacket);
    }

    @Override
    public void bridge$setManualSave(final boolean state) {
        this.impl$isManualSave = state;
    }

    @Override
    public BlockSnapshot bridge$createSnapshot(final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);

        if (((ServerLevel) (Object) this).isOutsideBuildHeight(pos)) {
            return BlockSnapshot.empty();
        }

        if (!this.hasChunk(x >> 4, z >> 4)) {
            return BlockSnapshot.empty();
        }
        final SpongeBlockSnapshot.BuilderImpl builder = SpongeBlockSnapshot.BuilderImpl.pooled();
        builder.world((ServerLevel) (Object) this).position(new Vector3i(x, y, z));
        final LevelChunk chunk = this.shadow$getChunkAt(pos);
        final BlockState state = chunk.getBlockState(pos);
        builder.blockState(state);
        final BlockEntity blockEntity = chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        if (blockEntity != null) {
            TrackingUtil.addTileEntityToBuilder(blockEntity, builder);
        }
        ((LevelChunkBridge) chunk).bridge$getBlockCreatorUUID(pos).ifPresent(builder::creator);
        ((LevelChunkBridge) chunk).bridge$getBlockNotifierUUID(pos).ifPresent(builder::notifier);

        builder.flag(BlockChangeFlags.NONE);
        return builder.build();
    }

    @Override
    public long[] bridge$recentTickTimes() {
        return this.impl$recentTickTimes;
    }

    @Redirect(method = {
        "saveLevelData",
        "findNearestMapStructure",
        "isFlat",
        "getSeed",
        "enabledFeatures"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorldData()Lnet/minecraft/world/level/storage/WorldData;"))
    private WorldData impl$usePerWorldLevelData(final MinecraftServer server) {
        final LevelData levelData = this.shadow$getLevelData();
        if (levelData instanceof final WorldData worldData) {
            return worldData;
        }
        return server.getWorldData();
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    public void impl$postSaveWorldEventPre(final CallbackInfo ci, final @Share("manualSave") LocalBooleanRef manualSave) {
        manualSave.set(this.impl$isManualSave);
        this.impl$isManualSave = false;

        final Cause currentCause = PhaseTracker.getInstance().currentCause();
        if (Sponge.eventManager().post(SpongeEventFactory.createSaveWorldEventPre(currentCause, ((ServerWorld) this)))) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;saveLevelData(Z)V"))
    public void impl$wrapSaveLevelData(final ServerLevel self, final boolean flush, final Operation<Void> original) {
        final ServerLevelData levelData = (ServerLevelData) this.shadow$getLevelData();
        final SerializationBehavior behavior = ((ServerLevelDataBridge) levelData).bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);

        if (behavior != SerializationBehavior.NONE) {
            original.call(self, flush);
        }
    }

    @WrapWithCondition(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;save(Z)V"))
    public boolean impl$wrapChunkCacheSave(final ServerChunkCache chunkCache, final boolean flush, final @Share("manualSave") LocalBooleanRef manualSave) {
        final ServerLevelData levelData = (ServerLevelData) this.shadow$getLevelData();
        final SerializationBehavior behavior = ((ServerLevelDataBridge) levelData).bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
        return behavior == SerializationBehavior.AUTOMATIC || (manualSave.get() && behavior == SerializationBehavior.MANUAL);
    }

    @Inject(method = "save", at = @At("TAIL"))
    public void impl$postSaveWorldEventPost(final CallbackInfo ci) {
        final Cause currentCause = PhaseTracker.getInstance().currentCause();
        Sponge.eventManager().post(SpongeEventFactory.createSaveWorldEventPost(currentCause, (ServerWorld) this));
    }

    @WrapOperation(method = "advanceWeatherCycle",
        at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/WeatherData;isThundering()Z"),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/WeatherData;isRaining()Z")
        }
    )
    private boolean impl$handleIsThunderingIfHasCeiling(final WeatherData instance, final Operation<Boolean> original) {
        if (this.levelData instanceof PrimaryLevelDataBridge pldb && pldb.bridge$dimensionType() != null) {
            // Dimensions with a ceiling (e.g. nether) should not have visible weather effects
            return !pldb.bridge$dimensionType().hasCeiling() && original.call(instance);
        }
        return original.call(instance);
    }

    // We need to stick with the local capture and coercion in 26.1 due to Forge stripping the LVT
    @Inject(method = "advanceWeatherCycle",
        locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;oRainLevel:F", shift = At.Shift.BEFORE, ordinal = 1))
    public void impl$onSetWeatherParameters(final CallbackInfo ci, final @Coerce boolean wasRaining) {
        final boolean isRaining = this.shadow$isRaining();
        if (this.oRainLevel != this.rainLevel || this.oThunderLevel != this.thunderLevel || wasRaining != isRaining) {
            Weather newWeather = ((ServerWorld) this).properties().weather();
            final Cause currentCause = PhaseTracker.getInstance().currentCause();
            final Transaction<Weather> weatherTransaction = new Transaction<>(this.impl$prevWeather, newWeather);
            final ChangeWeatherEvent event = SpongeEventFactory.createChangeWeatherEvent(currentCause, ((ServerWorld) this), weatherTransaction);
            if (Sponge.eventManager().post(event)) {
                newWeather = event.weather().original();
            } else {
                newWeather = event.weather().finalReplacement();
            }

            // Set event results
            // TODO - 26.1-snapshot-6 revamp weather with a timelines api.
            this.impl$prevWeather = newWeather;
            if (newWeather.type() == WeatherTypes.CLEAR.get()) {
                final var weatherData = this.shadow$getWeatherData();
                weatherData.setClearWeatherTime(SpongeTicks.toSaturatedIntOrInfinite(newWeather.remainingDuration()));
                weatherData.setRainTime(0);
                weatherData.setRaining(false);
                weatherData.setThunderTime(0);
                weatherData.setThundering(false);
            } else {
                final int newTime = SpongeTicks.toSaturatedIntOrInfinite(newWeather.remainingDuration());
                final var weatherData = this.shadow$getWeatherData();
                weatherData.setRaining(true);
                weatherData.setRainTime(newTime);
                weatherData.setClearWeatherTime(0);
                if (newWeather.type() == WeatherTypes.THUNDER.get()) {
                    weatherData.setThundering(true);
                    weatherData.setThunderTime(newTime);
                } else {
                    weatherData.setThunderTime(0);
                    weatherData.setThundering(false);
                }
            }
        }

    }

    @WrapOperation(method = "tickThunder",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isRainingAt(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean impl$onBeforeThunder(
        final ServerLevel serverLevel, final BlockPos param0, final Operation<Boolean> wrapped
    ) {
        final boolean rainingAt = wrapped.call(serverLevel, param0);
        if (rainingAt) {
            final LightningEvent.Pre strike = SpongeEventFactory.createLightningEventPre(PhaseTracker.getInstance().currentCause());
            if (Sponge.eventManager().post(strike)) {
                return false;
            }
        }
        return rainingAt;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void impl$capturePreTickTime(final BooleanSupplier param0, final CallbackInfo ci) {
        this.impl$preTickTime = Util.getNanos();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void impl$capturePostTickTime(final BooleanSupplier param0, final CallbackInfo ci) {
        final long postTickTime = Util.getNanos();

        this.impl$recentTickTimes[this.shadow$getServer().getTickCount() % 100] = postTickTime - this.impl$preTickTime;
    }

    private void impl$setWorldOnBorder() {
        ((WorldBorderBridge) this.getWorldBorder()).bridge$setAssociatedWorld(((ServerWorld) this).key());
    }

    @Inject(method = "globalLevelEvent", at = @At("HEAD"), cancellable = true)
    private void impl$throwBroadcastGlobalEvent(int effectID, BlockPos pos, int pitch, CallbackInfo ci) {
        if (!this.bridge$isFake() && ShouldFire.PLAY_SOUND_EVENT_BROADCAST) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getWorldInstance((ServerLevel) (Object) this).pushCauseFrame()) {
                final PlaySoundEvent.Broadcast event = SpongeCommonEventFactory.callPlaySoundBroadcastEvent(frame, this, pos, effectID);
                if (event != null && event.isCancelled()) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void impl$throwBroadcastEvent(final Entity player, final int eventID, final BlockPos pos, final int dataID, CallbackInfo ci) {
        if (eventID == Constants.WorldEvents.PLAY_RECORD_EVENT && ShouldFire.PLAY_SOUND_EVENT_FROM_JUKEBOX) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
                final BlockEntity tileEntity = this.shadow$getBlockEntity(pos);
                if (tileEntity instanceof JukeboxBlockEntity) {
                    final JukeboxBlockEntity jukebox = (JukeboxBlockEntity) tileEntity;
                    final ItemStack record = jukebox.getItem(0);
                    frame.pushCause(jukebox);
                    frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(record));
                    if (!record.isEmpty()) {
                        final Optional<MusicDisc> recordProperty = ((org.spongepowered.api.item.inventory.ItemStack) (Object) record).get(Keys.MUSIC_DISC);
                        if (!recordProperty.isPresent()) {
                            //Safeguard for https://github.com/SpongePowered/SpongeCommon/issues/2337
                            return;
                        }
                        final MusicDisc recordType = recordProperty.get();
                        final PlaySoundEvent.FromJukebox event = SpongeCommonEventFactory.callPlaySoundFromJukeboxEvent(frame.currentCause(), jukebox, recordType, dataID);
                        if (event.isCancelled()) {
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void impl$ignoreGameEventsForVanishedEntities(final Holder<GameEvent> $$0, final Vec3 $$1, final GameEvent.Context $$2, final CallbackInfo ci) {
        if ($$2.sourceEntity() instanceof VanishableBridge bridge && !bridge.bridge$vanishState().triggerVibrations()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "lambda$updatePOIOnBlockStateChange$3",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;add(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/entity/ai/village/poi/PoiRecord;"
        )
    )
    private PoiRecord impl$avoidAddingPoiUpdatesOnUnloadedWorld(
        final PoiManager instance, final BlockPos pos, final Holder<PoiType> type, final Operation<PoiRecord> original
    ) {
        // Unloaded worlds should not notify PoiManager of changes
        if (!SpongeCommon.server().levelKeys().contains(this.shadow$dimension())) {
            return null;
        }
        return original.call(instance, pos, type);
    }

    @Inject(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/level/ServerLevel;emptyTime:I",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void impl$unloadBlockEntities(final BooleanSupplier param0, final CallbackInfo ci) {
        /*
         * This code fixes block entity memory leak when the level hasn't online players
         * and forced chunks. For the first 300 ticks the level can still clean up removed
         * block entities on its own (ticks are performed for block entities). After it
         * this mixin code is responsible for the subsequent unloading of block entities.
         * Such a memory leak occurs when a plugin writes a lot of blocks, but the level
         * is without players.
         */
        if (this.emptyTime >= 300 && !this.blockEntityTickers.isEmpty()) {
            this.blockEntityTickers.removeIf(TickingBlockEntity::isRemoved);
        }
    }

    @Override
    public String toString() {
        final Optional<ResourceKey> worldTypeKey = Optional.ofNullable(this.server.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).getKey(this.shadow$dimensionType())).map(ResourceKey.class::cast);
        return new StringJoiner(",", ServerLevel.class.getSimpleName() + "[", "]")
            .add("key=" + this.shadow$dimension())
            .add("worldType=" + worldTypeKey.map(ResourceKey::toString).orElse("inline"))
            .toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends org.spongepowered.api.entity.Entity> E bridge$createEntity(final EntityType<E> type, final Vector3d position, final boolean naturally) throws IllegalArgumentException, IllegalStateException {
        if (type == net.minecraft.world.entity.EntityTypes.PLAYER) {
            // Unable to construct these
            throw new IllegalArgumentException("A Player cannot be created by the API!");
        }

        Entity entity = null;
        final double x = position.x();
        final double y = position.y();
        final double z = position.z();
        final Level thisWorld = (Level) (Object) this;
        // Not all entities have a single World parameter as their constructor
        if (type == net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT) {
            entity = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(thisWorld, EntitySpawnReason.EVENT);
            entity.snapTo(x, y, z);
            ((LightningBolt) entity).setVisualOnly(false);
        }
        // TODO - archetypes should solve the problem of calling the correct constructor
        if (type == net.minecraft.world.entity.EntityTypes.ENDER_PEARL) {
            final ArmorStand tempEntity = new ArmorStand(thisWorld, x, y, z);
            tempEntity.setPos(tempEntity.getX(), tempEntity.getY() - tempEntity.getEyeHeight(), tempEntity.getZ());
            entity = new ThrownEnderpearl(thisWorld, tempEntity, Items.ENDER_PEARL.getDefaultInstance());
            ((EnderPearl) entity).offer(Keys.SHOOTER, UnknownProjectileSource.UNKNOWN);
        }
        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (type == net.minecraft.world.entity.EntityTypes.FALLING_BLOCK) {
            entity = FallingBlockEntityAccessor.invoker$new(thisWorld, x, y, z, Blocks.SAND.defaultBlockState());
        }
        if (type == net.minecraft.world.entity.EntityTypes.ITEM) {
            entity = new ItemEntity(thisWorld, x, y, z, new ItemStack(Blocks.STONE));
        }

        if (entity == null) {
            final ResourceKey key = (ResourceKey) (Object) SpongeCommon.vanillaRegistry(Registries.ENTITY_TYPE).getKey((net.minecraft.world.entity.EntityType<?>) type);
            try {
                entity = ((net.minecraft.world.entity.EntityType) type).create(thisWorld, EntitySpawnReason.EVENT);
                entity.snapTo(x, y, z);
            } catch (final Exception e) {
                throw new RuntimeException("There was an issue attempting to construct " + key, e);
            }
        }

        // TODO - replace this with an actual check

        if (entity instanceof HangingEntity) {
            if (!((HangingEntity) entity).survives()) {
                throw new IllegalArgumentException("Hanging entity does not survive at the given position: " + position);
            }
        }

        if (naturally && entity instanceof Mob) {
            // Adding the default equipment
            final DifficultyInstance difficulty = this.shadow$getCurrentDifficultyAt(new BlockPos((int) x, (int) y, (int) z));
            ((MobAccessor) entity).invoker$populateDefaultEquipmentSlots(this.random, difficulty);
        }

        if (entity instanceof Painting) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            // TODO ? ((Painting) entity).motive = Motive.KEBAB;
        }

        return (E) entity;
    }

}
