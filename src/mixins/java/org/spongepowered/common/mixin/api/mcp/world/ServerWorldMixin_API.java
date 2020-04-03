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
package org.spongepowered.common.mixin.api.mcp.world;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.merchant.IReputationTracking;
import net.minecraft.entity.merchant.IReputationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.Explosion;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.ServerTickList;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_API extends WorldMixin_API implements org.spongepowered.api.world.server.ServerWorld {


    @Shadow public abstract void shadow$tick(BooleanSupplier p_72835_1_);
    @Shadow public abstract void shadow$tickEnvironment(Chunk p_217441_1_, int p_217441_2_);
    @Shadow protected abstract BlockPos shadow$adjustPosToNearbyEntity(BlockPos p_175736_1_);
    @Shadow public abstract boolean shadow$isInsideTick();
    @Shadow public abstract void shadow$updateAllPlayersSleepingFlag();
    @Shadow public abstract ServerScoreboard shadow$getScoreboard();
    @Shadow private void shadow$resetRainAndThunder() { } // shadowed
    @Shadow public abstract void shadow$resetUpdateEntityTick();
    @Shadow private void shadow$tickFluid(NextTickListEntry<Fluid> p_205339_1_) { } // shadowed
    @Shadow private void shadow$tickBlock(NextTickListEntry<Block> p_205338_1_) { } // shadowed
    @Shadow public abstract void shadow$updateEntity(Entity p_217479_1_);
    @Shadow public abstract void shadow$func_217459_a(Entity p_217459_1_, Entity p_217459_2_);
    @Shadow public abstract void shadow$chunkCheck(Entity p_217464_1_);
    @Shadow public abstract boolean shadow$isBlockModifiable(PlayerEntity p_175660_1_, BlockPos p_175660_2_);
    @Shadow public abstract void shadow$createSpawnPosition(WorldSettings p_73052_1_) ;
    @Shadow protected abstract void shadow$createBonusChest();
    @Shadow public abstract @Nullable BlockPos shadow$getSpawnCoordinate();
    @Shadow public abstract void shadow$save(@Nullable IProgressUpdate p_217445_1_, boolean p_217445_2_, boolean p_217445_3_) throws SessionLockException;
    @Shadow protected abstract void shadow$saveLevel() throws SessionLockException;
    @Shadow public abstract List<Entity> shadow$getEntities(@Nullable EntityType<?> p_217482_1_, Predicate<? super Entity> p_217482_2_) ;
    @Shadow public abstract List<EnderDragonEntity> shadow$getDragons();
    @Shadow public abstract List<ServerPlayerEntity> shadow$getPlayers(Predicate<? super ServerPlayerEntity> p_217490_1_);
    @Shadow public abstract @Nullable ServerPlayerEntity shadow$getRandomPlayer();
    @Shadow public abstract Object2IntMap<EntityClassification> shadow$countEntities() ;
    @Shadow public abstract boolean shadow$addEntity(Entity p_217376_1_);
    @Shadow public abstract boolean shadow$summonEntity(Entity p_217470_1_);
    @Shadow public abstract void shadow$func_217460_e(Entity p_217460_1_);
    @Shadow public abstract void shadow$func_217446_a(ServerPlayerEntity p_217446_1_);
    @Shadow public abstract void shadow$func_217447_b(ServerPlayerEntity p_217447_1_) ;
    @Shadow public abstract void shadow$addNewPlayer(ServerPlayerEntity p_217435_1_);
    @Shadow public abstract void shadow$addRespawnedPlayer(ServerPlayerEntity p_217433_1_);
    @Shadow private void shadow$addPlayer(ServerPlayerEntity p_217448_1_) { }
    @Shadow private boolean shadow$addEntity0(Entity p_72838_1_) { return false; } // shadow
    @Shadow public abstract boolean shadow$addEntityIfNotDuplicate(Entity p_217440_1_) ;
    @Shadow private boolean shadow$hasDuplicateEntity(Entity p_27478_1_) { return false; } // shadow
    @Shadow public abstract void shadow$onChunkUnloading(Chunk p_217466_1_);
    @Shadow public abstract void shadow$onEntityRemoved(Entity p_217484_1_) ;
    @Shadow private void shadow$onEntityAdded(Entity p_217465_1_) { } // shadowed
    @Shadow public abstract void shadow$removeEntity(Entity p_217467_1_) ;
    @Shadow private void shadow$removeFromChunk(Entity p_217454_1_) { }
    @Shadow public abstract void shadow$removePlayer(ServerPlayerEntity p_217434_1_) ;
    @Shadow public abstract void shadow$addLightningBolt(LightningBoltEntity p_217468_1_) ;
    @Shadow public abstract void shadow$sendBlockBreakProgress(int p_175715_1_, BlockPos p_175715_2_, int p_175715_3_);
    @Shadow public abstract void shadow$playSound(@Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract void shadow$playMovingSound(@Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_);
    @Shadow public abstract void shadow$playBroadcastSound(int p_175669_1_, BlockPos p_175669_2_, int p_175669_3_);
    @Shadow public abstract void shadow$playEvent(@Nullable PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_);
    @Shadow public abstract void shadow$notifyBlockUpdate(BlockPos p_184138_1_, BlockState p_184138_2_, BlockState p_184138_3_, int p_184138_4_) ;
    @Shadow public abstract void shadow$setEntityState(Entity p_72960_1_, byte p_72960_2_);
    @Shadow public abstract ServerChunkProvider shadow$getChunkProvider();
    @Shadow public abstract Explosion shadow$createExplosion(@Nullable Entity p_217401_1_, DamageSource p_217401_2_, double p_217401_3_, double p_217401_5_, double p_217401_7_, float p_217401_9_, boolean p_217401_10_, Explosion.Mode p_217401_11_) ;
    @Shadow public abstract void shadow$addBlockEvent(BlockPos p_175641_1_, Block p_175641_2_, int p_175641_3_, int p_175641_4_);
    @Shadow private void shadow$sendQueuedBlockEvents() { }
    @Shadow private boolean shadow$fireBlockEvent(BlockEventData p_147485_1_) { return false; }
    @Shadow public abstract ServerTickList<Block> shadow$getPendingBlockTicks();
    @Shadow public abstract ServerTickList<Fluid> shadow$getPendingFluidTicks();
    @Nonnull
    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract Teleporter shadow$getDefaultTeleporter();
    @Shadow public abstract TemplateManager shadow$getStructureTemplateManager();
    @Shadow public abstract <T extends IParticleData> int shadow$spawnParticle(T p_195598_1_, double p_195598_2_, double p_195598_4_, double p_195598_6_, int p_195598_8_, double p_195598_9_, double p_195598_11_, double p_195598_13_, double p_195598_15_);
    @Shadow public abstract <T extends IParticleData> boolean shadow$spawnParticle(ServerPlayerEntity p_195600_1_, T p_195600_2_, boolean p_195600_3_, double p_195600_4_, double p_195600_6_, double p_195600_8_, int p_195600_10_, double p_195600_11_, double p_195600_13_, double p_195600_15_, double p_195600_17_);
    @Shadow private boolean shadow$sendPacketWithinDistance(ServerPlayerEntity p_195601_1_, boolean p_195601_2_, double p_195601_3_, double p_195601_5_, double p_195601_7_, IPacket<?> p_195601_9_) { return false; } // shadow
    @Nullable
    @Shadow public abstract Entity shadow$getEntityByID(int p_73045_1_);
    @Nullable
    @Shadow public abstract Entity shadow$getEntityByUuid(UUID p_217461_1_);
    @Nullable
    @Shadow public abstract BlockPos shadow$findNearestStructure(String p_211157_1_, BlockPos p_211157_2_, int p_211157_3_, boolean p_211157_4_);
    @Shadow public abstract RecipeManager shadow$getRecipeManager();
    @Shadow public abstract NetworkTagManager shadow$getTags();
    @Shadow public abstract void shadow$setGameTime(long p_82738_1_);
    @Shadow public abstract boolean shadow$isSaveDisabled();
    @Shadow public abstract void shadow$checkSessionLock() throws SessionLockException;
    @Shadow public abstract SaveHandler shadow$getSaveHandler();
    @Shadow public abstract DimensionSavedDataManager shadow$getSavedData();
    @Nullable
    @Shadow public abstract MapData shadow$getMapData(String p_217406_1_);
    @Shadow public abstract void shadow$registerMapData(MapData p_217399_1_);
    @Shadow public abstract int shadow$getNextMapId();
    @Shadow public abstract void shadow$setSpawnPoint(BlockPos p_175652_1_);
    @Shadow public abstract LongSet shadow$getForcedChunks();
    @Shadow public abstract boolean shadow$forceChunk(int p_217458_1_, int p_217458_2_, boolean p_217458_3_);
    @Shadow public abstract List<ServerPlayerEntity> shadow$getPlayers();
    @Shadow public abstract void shadow$func_217393_a(BlockPos p_217393_1_, BlockState p_217393_2_, BlockState p_217393_3_);
    @Shadow public abstract PointOfInterestManager shadow$getPointOfInterestManager();
    @Shadow public abstract boolean shadow$func_217483_b_(BlockPos p_217483_1_);
    @Shadow public abstract boolean shadow$func_222887_a(SectionPos p_222887_1_);
    @Shadow public abstract boolean shadow$func_217471_a(BlockPos p_217471_1_, int p_217471_2_);
    @Shadow public abstract int shadow$func_217486_a(SectionPos p_217486_1_);
    @Shadow public abstract RaidManager shadow$getRaids();
    @Nullable
    @Shadow public abstract Raid shadow$findRaid(BlockPos p_217475_1_);
    @Shadow public abstract boolean shadow$hasRaid(BlockPos p_217455_1_);
    @Shadow public abstract void shadow$updateReputation(IReputationType p_217489_1_, Entity p_217489_2_, IReputationTracking p_217489_3_);
    @Shadow public abstract void shadow$func_225322_a(Path p_225322_1_) throws IOException;
    @Shadow private static void shadow$func_225320_a(Writer p_225320_0_, Iterable<Entity> p_225320_1_) throws IOException {
        // do noghting
    }
    @Shadow private void shadow$func_225321_a(Writer p_225321_1_) throws IOException {
        // private method
    }

    @Shadow @Final private List<ServerPlayerEntity> players;



 @SuppressWarnings("unchecked")
 @Override
 public Collection<ServerPlayer> getPlayers() {
  return ImmutableList.copyOf((Collection<ServerPlayer>) (Collection<?>) this.shadow$getPlayers());
 }
}
