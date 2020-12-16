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
package org.spongepowered.common.mixin.core.world.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.accessor.world.server.ServerChunkProviderAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.PlatformServerWorldBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.mixin.core.world.WorldMixin;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends WorldMixin implements ServerWorldBridge, PlatformServerWorldBridge, ResourceKeyBridge {

    // @formatter:off
    @Shadow @Final private IServerWorldInfo serverLevelData;
    @Shadow @Nonnull public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract List<ServerPlayerEntity> shadow$players();
    @Shadow protected abstract void shadow$saveLevelData();
    // @formatter:on

    private CustomServerBossInfoManager impl$bossBarManager;
    private boolean impl$isManualSave = false;

    @Override
    public boolean bridge$isLoaded() {
        if (((WorldBridge) this).bridge$isFake()) {
            return false;
        }

        final ServerWorld world = ((SpongeWorldManager) ((Server) this.shadow$getServer()).getWorldManager()).getWorld(this.shadow$getDimension().getType());
        if (world == null) {
            return false;
        }

        return world == (Object) this;
    }

    @Override
    public void bridge$adjustDimensionLogic(final SpongeDimensionType dimensionType) {
        if (this.bridge$isFake()) {
            return;
        }

        super.bridge$adjustDimensionLogic(dimensionType);

        final ChunkGenerator<?> chunkGenerator = this.dimension.createChunkGenerator();
        ((ServerChunkProviderAccessor) this.chunkProvider).accessor$generator(chunkGenerator);
        ((ChunkManagerAccessor) ((ServerChunkProvider) this.chunkProvider).chunkMap).accessor$generator(chunkGenerator);

        for (final ServerPlayerEntity player : this.shadow$players()) {
            ((ServerPlayerEntityBridge) player).bridge$sendViewerEnvironment(dimensionType);
        }
    }

    @Override
    public CustomServerBossInfoManager bridge$getBossBarManager() {

        if (this.impl$bossBarManager == null) {
            if (this.dimension.getType() == DimensionType.OVERWORLD || this.bridge$isFake()) {
                this.impl$bossBarManager = this.shadow$getServer().getCustomBossEvents();
            } else {
                this.impl$bossBarManager = new CustomServerBossInfoManager(this.shadow$getServer());
            }
        }

        return this.impl$bossBarManager;
    }

    private final Map<Entity, Vector3d> impl$rotationUpdates = new HashMap<>();

    @Override
    public void bridge$addEntityRotationUpdate(final net.minecraft.entity.Entity entity, final Vector3d rotation) {
        this.impl$rotationUpdates.put(entity, rotation);
    }

    @Override
    public void bridge$updateRotation(final net.minecraft.entity.Entity entityIn) {
        final Vector3d rotationUpdate = this.impl$rotationUpdates.get(entityIn);
        if (rotationUpdate != null) {
            entityIn.xRot = (float) rotationUpdate.getX();
            entityIn.yRot = (float) rotationUpdate.getY();
        }
        this.impl$rotationUpdates.remove(entityIn);
    }

    @Override
    public void bridge$setWeather(Weather weather, long ticks) {
        if (weather == Weathers.CLEAR.get()) {
            this.serverLevelData.setClearWeatherTime((int) Math.max(Integer.MAX_VALUE, ticks));
            this.serverLevelData.setRainTime(0);
            this.serverLevelData.setThunderTime(0);
            this.serverLevelData.setRaining(false);
            this.serverLevelData.setThundering(false);
        } else if (weather == Weathers.RAIN.get()) {
            this.serverLevelData.setClearWeatherTime(0);
            this.serverLevelData.setRainTime((int) Math.max(Integer.MAX_VALUE, ticks));
            this.serverLevelData.setThunderTime((int) Math.max(Integer.MAX_VALUE, ticks));
            this.serverLevelData.setRaining(true);
            this.serverLevelData.setThundering(false);
        } else if (weather == Weathers.THUNDER.get()) {
            this.serverLevelData.setClearWeatherTime(0);
            this.serverLevelData.setRainTime((int) Math.max(Integer.MAX_VALUE, ticks));
            this.serverLevelData.setThunderTime((int) Math.max(Integer.MAX_VALUE, ticks));
            this.serverLevelData.setRaining(true);
            this.serverLevelData.setThundering(true);
        }
    }

    @Override
    public long bridge$getDurationInTicks() {
        if (this.shadow$isThundering()) {
            return this.serverLevelData.getThunderTime();
        }
        if (this.shadow$isRaining()) {
            return this.serverLevelData.getRainTime();
        }
        if (this.serverLevelData.getClearWeatherTime() > 0) {
            return this.serverLevelData.getClearWeatherTime();
        }
        return Math.min(this.serverLevelData.getThunderTime(), this.serverLevelData.getRainTime());
    }

    @Override
    public void bridge$triggerExplosion(Explosion explosion) {
        // Sponge start
        // Set up the pre event
        final ExplosionEvent.Pre
                event =
                SpongeEventFactory.createExplosionEventPre(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        explosion, (org.spongepowered.api.world.server.ServerWorld) this);
        if (SpongeCommon.postEvent(event)) {
            return;
        }
        explosion = event.getExplosion();
        final net.minecraft.world.Explosion mcExplosion;
        try {
            // Since we already have the API created implementation Explosion, let's use it.
            mcExplosion = (net.minecraft.world.Explosion) explosion;
        } catch (final Exception e) {
            new PrettyPrinter(60).add("Explosion not compatible with this implementation").centre().hr()
                    .add("An explosion that was expected to be used for this implementation does not")
                    .add("originate from this implementation.")
                    .add(e)
                    .trace();
            return;
        }

        try (final PhaseContext<?> ignored = GeneralPhase.State.EXPLOSION.createPhaseContext(PhaseTracker.SERVER)
                .explosion((net.minecraft.world.Explosion) explosion)
                .source(explosion.getSourceExplosive().isPresent() ? explosion.getSourceExplosive() : this)) {
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
    public void bridge$setManualSave(boolean state) {
        this.impl$isManualSave = state;
    }

    @Override
    public IServerWorldInfo bridge$getServerLevelData() {
        return this.serverLevelData;
    }

    @Override
    public ResourceKey bridge$getKey() {
        return ((ResourceKeyBridge) this.shadow$getWorldInfo()).bridge$getKey();
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        ((ResourceKeyBridge) this.shadow$getWorldInfo()).bridge$setKey(key);
    }

    /**
     * @author zidane - November 24th, 2020 - Minecraft 1.15.2
     * @reason Honor our serialization behavior in performing saves
     */
    @Overwrite
    public void save(@Nullable IProgressUpdate progress, boolean flush, boolean skipSave) {
        final ServerWorld this$ = (ServerWorld) (Object) this;
        final ServerWorldInfo levelData = (ServerWorldInfo) this.shadow$getLevelData();

        ServerChunkProvider serverchunkprovider = this$.getChunkSource();

        if (!skipSave) {

            final SerializationBehavior behavior = ((IServerWorldInfoBridge) levelData).bridge$getSerializationBehavior();

            if (progress != null) {
                progress.progressStartNoAbort(new TranslationTextComponent("menu.savingLevel"));
            }

            // We always save the metadata unless it is NONE
            if (behavior != SerializationBehavior.NONE) {

                this.shadow$saveLevelData();

                // Sponge Start - We do per-world WorldInfo/WorldBorders/BossBars

                this.getWorldBorder().copyTo(this$.getWorldInfo());

                levelData.setCustomBossEvents(((ServerWorldBridge) this$).bridge$getBossBarManager().save());

                ((MinecraftServerAccessor) SpongeCommon.getServer()).accessor$storageSource().saveDataTag(SpongeCommon.getServer().registryAccess()
                    , (ServerWorldInfo) this.levelData, this.shadow$dimension() == World.OVERWORLD ? SpongeCommon.getServer().getPlayerList().getSingleplayerData() : null);

                // Sponge End
            }
            if (progress != null) {
                progress.progressStage(new TranslationTextComponent("menu.savingChunks"));
            }

            final boolean canAutomaticallySave = !this.impl$isManualSave && behavior == SerializationBehavior.AUTOMATIC;
            final boolean canManuallySave = this.impl$isManualSave && behavior == SerializationBehavior.MANUAL;

            if (canAutomaticallySave || canManuallySave) {
                serverchunkprovider.save(flush);
            }
        }

        this.impl$isManualSave = false;
    }

    @Override
    public String toString() {
        return new StringJoiner(",", ServerWorld.class.getSimpleName() + "[", "]")
                .add("key=" + this.shadow$dimension())
                .add("dimensionType=" + this.shadow$dimensionType())
                .toString();
    }
}
