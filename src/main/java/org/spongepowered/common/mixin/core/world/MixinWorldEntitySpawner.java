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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.util.SpawnerSpawnType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(WorldEntitySpawner.class)
public abstract class MixinWorldEntitySpawner {

    private static final String BIOME_CAN_SPAWN_ANIMAL =
        "Lnet/minecraft/world/WorldEntitySpawner;canCreatureTypeSpawnAtLocation(Lnet/minecraft/entity/EntityLiving$SpawnPlacementType;"
        + "Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z";

    private static final String WEIGHTED_RANDOM_GET = "Lnet/minecraft/util/WeightedRandom;getRandomItem(Ljava/util/Random;Ljava/util/List;)"
        + "Lnet/minecraft/util/WeightedRandom$Item;";
    @Nullable
    private static EntityType spawnerEntityType;
    private List<Chunk> eligibleSpawnChunks = new ArrayList<>();

    /**
     * @author blood - February 18th, 2017
     * @reason Refactor entire method for optimizations and spawn limits.
     *
     * @param worldServerIn The world
     * @param spawnHostileMobs If hostile entities can spawn
     * @param spawnPeacefulMobs If passive entities can spawn
     * @param spawnOnSetTickRate If tickrate has been reached for spawning passives
     * @return The amount of entities spawned
     */
    @Overwrite
    public int findChunksForSpawning(WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return 0;
        }

        try (PhaseContext<?> context = GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext()
                .world(worldServerIn)
                .buildAndSwitch()) {
            Iterator<Chunk> chunkIterator = this.eligibleSpawnChunks.iterator();
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                ((IMixinChunk) chunk).setIsSpawning(false);
                chunkIterator.remove();
            }

            IMixinWorldServer spongeWorld = ((IMixinWorldServer) worldServerIn);
            spongeWorld.getTimingsHandler().mobSpawn.startTiming();

            int chunkSpawnCandidates = 0;
            final int mobSpawnRange = Math.min(((IMixinWorldServer) worldServerIn).getActiveConfig().getConfig().getWorld().getMobSpawnRange(),
                SpongeImpl.getServer().getPlayerList().getViewDistance());
            // Vanilla uses a div count of 289 (17x17) which assumes the view distance is 8.
            // Since we allow for custom ranges, we need to adjust the div count based on the
            // mob spawn range set by server.
            final int MOB_SPAWN_COUNT_DIV = (2 * mobSpawnRange + 1) * (2 * mobSpawnRange + 1);

            for (EntityPlayer entityplayer : worldServerIn.playerEntities) {
                // We treat players who do not affect spawning as "spectators"
                if (!((IMixinEntityPlayer) entityplayer).affectsSpawning() || entityplayer.isSpectator()) {
                    continue;
                }

                int playerPosX = MathHelper.floor(entityplayer.posX / 16.0D);
                int playerPosZ = MathHelper.floor(entityplayer.posZ / 16.0D);

                for (int i = -mobSpawnRange; i <= mobSpawnRange; ++i) {
                    for (int j = -mobSpawnRange; j <= mobSpawnRange; ++j) {
                        boolean flag = i == -mobSpawnRange || i == mobSpawnRange || j == -mobSpawnRange || j == mobSpawnRange;
                        final Chunk
                            chunk =
                            ((IMixinChunkProviderServer) worldServerIn.getChunkProvider())
                                .getLoadedChunkWithoutMarkingActive(i + playerPosX, j + playerPosZ);
                        if (chunk == null || (chunk.unloadQueued && !((IMixinChunk) chunk).isPersistedChunk())) {
                            // Don't attempt to spawn in an unloaded chunk
                            continue;
                        }

                        final IMixinChunk spongeChunk = (IMixinChunk) chunk;
                        ++chunkSpawnCandidates;
                        final ChunkPos chunkPos = chunk.getPos();
                        if (!flag && worldServerIn.getWorldBorder().contains(chunkPos)) {
                            PlayerChunkMapEntry playerchunkmapentry = worldServerIn.getPlayerChunkMap().getEntry(chunkPos.x, chunkPos.z);

                            if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers() && !spongeChunk.isSpawning()) {
                                this.eligibleSpawnChunks.add(chunk);
                                spongeChunk.setIsSpawning(true);
                            }
                        }
                    }
                }
            }

            // If there are no eligible chunks, return early
            if (this.eligibleSpawnChunks.size() == 0) {
                spongeWorld.getTimingsHandler().mobSpawn.stopTiming();
                return 0;
            }

            int totalSpawned = 0;
            final long worldTotalTime = worldServerIn.getTotalWorldTime();
            final SpongeConfig<?> activeConfig = ((IMixinWorldServer) worldServerIn).getActiveConfig();

            labelOuterLoop:
            for (EnumCreatureType enumCreatureType : EnumCreatureType.values()) {
                int limit = 0;
                int tickRate = 0;
                if (enumCreatureType == EnumCreatureType.MONSTER) {
                    limit = activeConfig.getConfig().getSpawner().getMonsterSpawnLimit();
                    tickRate = activeConfig.getConfig().getSpawner().getMonsterTickRate();
                } else if (enumCreatureType == EnumCreatureType.CREATURE) {
                    limit = activeConfig.getConfig().getSpawner().getAnimalSpawnLimit();
                    tickRate = activeConfig.getConfig().getSpawner().getAnimalTickRate();
                } else if (enumCreatureType == EnumCreatureType.WATER_CREATURE) {
                    limit = activeConfig.getConfig().getSpawner().getAquaticSpawnLimit();
                    tickRate = activeConfig.getConfig().getSpawner().getAquaticTickRate();
                } else if (enumCreatureType == EnumCreatureType.AMBIENT) {
                    limit = activeConfig.getConfig().getSpawner().getAmbientSpawnLimit();
                    tickRate = activeConfig.getConfig().getSpawner().getAmbientTickRate();
                }

                if (limit == 0 || tickRate == 0 || (worldTotalTime % tickRate) != 0L) {
                    continue;
                }

                if ((!enumCreatureType.getPeacefulCreature() || spawnPeacefulMobs) && (enumCreatureType.getPeacefulCreature() || spawnHostileMobs)) {
                    int entityCount = SpongeImplHooks.countEntities(worldServerIn, enumCreatureType, true);
                    int maxCount = limit * chunkSpawnCandidates / MOB_SPAWN_COUNT_DIV;
                    if (entityCount > maxCount) {
                        continue labelOuterLoop;
                    }

                    chunkIterator = this.eligibleSpawnChunks.iterator();
                    int mobLimit = maxCount - entityCount + 1;
                    labelChunkStart:
                    while (chunkIterator.hasNext() && mobLimit > 0) {
                        final Chunk chunk = chunkIterator.next();
                        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                        final BlockPos blockpos = getRandomChunkPosition(worldServerIn, chunk);
                        int k1 = blockpos.getX();
                        int l1 = blockpos.getY();
                        int i2 = blockpos.getZ();
                        IBlockState iblockstate = worldServerIn.getBlockState(blockpos);

                        if (!iblockstate.isNormalCube()) {
                            int spawnCount = 0;
                            for (int spawnLimit = 0; spawnLimit < 3; ++spawnLimit) {
                                int l2 = k1;
                                int i3 = l1;
                                int j3 = i2;
                                Biome.SpawnListEntry spawnListEntry = null;
                                IEntityLivingData ientitylivingdata = null;
                                int l3 = MathHelper.ceil(Math.random() * 4.0D);

                                for (int i4 = 0; i4 < l3; ++i4) {
                                    l2 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                    i3 += worldServerIn.rand.nextInt(1) - worldServerIn.rand.nextInt(1);
                                    j3 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                    mutableBlockPos.setPos(l2, i3, j3);
                                    final double spawnX = l2 + 0.5F;
                                    final double spawnY = i3;
                                    final double spawnZ = j3 + 0.5F;

                                    if (!worldServerIn.isAnyPlayerWithinRangeAt(spawnX, spawnY, spawnZ, 24.0D)
                                        && worldServerIn.getSpawnPoint().distanceSq(spawnX, spawnY, spawnZ) >= 576.0D) {
                                        if (spawnListEntry == null) {
                                            spawnListEntry = worldServerIn.getSpawnListEntryForTypeAt(enumCreatureType, mutableBlockPos);

                                            if (spawnListEntry == null) {
                                                break;
                                            }
                                        }

                                        final EntityType entityType = EntityTypeRegistryModule.getInstance().getForClass(spawnListEntry.entityClass);
                                        if (entityType != null) {
                                            Vector3d vector3d = new Vector3d(spawnX, spawnY, spawnZ);
                                            Transform<org.spongepowered.api.world.World>
                                                transform =
                                                new Transform<>((org.spongepowered.api.world.World) worldServerIn, vector3d);
                                            ConstructEntityEvent.Pre
                                                event =
                                                SpongeEventFactory
                                                    .createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), entityType,
                                                        transform);
                                            if (SpongeImpl.postEvent(event)) {
                                                continue;
                                            }
                                        }

                                        if (worldServerIn.canCreatureTypeSpawnHere(enumCreatureType, spawnListEntry, mutableBlockPos)
                                            && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
                                            EntitySpawnPlacementRegistry.getPlacementForEntity(spawnListEntry.entityClass), worldServerIn,
                                            mutableBlockPos)) {
                                            EntityLiving entityliving;

                                            try {
                                                entityliving =
                                                    spawnListEntry.entityClass.getConstructor(new Class<?>[]{World.class}).newInstance(worldServerIn);
                                            } catch (Exception exception) {
                                                exception.printStackTrace();
                                                continue labelOuterLoop;
                                            }

                                            entityliving.setLocationAndAngles(spawnX, spawnY, spawnZ, worldServerIn.rand.nextFloat() * 360.0F, 0.0F);
                                            final boolean entityNotColliding = entityliving.isNotColliding();

                                            final SpawnerSpawnType type = SpongeImplHooks.canEntitySpawnHere(entityliving, entityNotColliding);
                                            if (type != SpawnerSpawnType.NONE) {
                                                if (type == SpawnerSpawnType.NORMAL) {
                                                    ientitylivingdata = entityliving.onInitialSpawn(worldServerIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);
                                                }

                                                if (entityNotColliding) {
                                                    ++spawnCount;
                                                    worldServerIn.spawnEntity(entityliving);
                                                } else {
                                                    entityliving.setDead();
                                                }

                                                mobLimit--;
                                                if (mobLimit <= 0 || spawnCount >= SpongeImplHooks.getMaxSpawnPackSize(entityliving)) {
                                                    continue labelChunkStart;
                                                }
                                            }

                                            totalSpawned += spawnCount;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            spongeWorld.getTimingsHandler().mobSpawn.stopTiming();

            return totalSpawned;
        }
    }

    private static BlockPos getRandomChunkPosition(World worldIn, Chunk chunk)
    {
        int i = chunk.x * 16 + worldIn.rand.nextInt(16);
        int j = chunk.z * 16 + worldIn.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    /**
     * @author aikar - February 20th, 2017 - Optimizes light level check.
     * @author blood - February 20th, 2017 - Avoids checking unloaded chunks and chunks with pending light updates.
     *
     * @reason Avoids checking unloaded chunks and chunks with pending light updates.
     */
    @Overwrite
    private static BlockPos getRandomChunkPosition(World worldIn, int x, int z)
    {
        // Sponge start
        final Chunk chunk = ((IMixinChunkProviderServer) worldIn.getChunkProvider()).getLoadedChunkWithoutMarkingActive(x, z);
        if (chunk == null || (chunk.unloadQueued && !((IMixinChunk) chunk).isPersistedChunk())) {
            // Don't attempt to spawn in an unloaded chunk
            return null;
        }
        // Sponge end

        int i = x * 16 + worldIn.rand.nextInt(16);
        int j = z * 16 + worldIn.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    @Inject(method = "performWorldGenSpawning", at = @At(value = "HEAD"))
    private static void onPerformWorldGenSpawningHead(World worldServer, Biome biome, int j, int k, int l, int m, Random rand, CallbackInfo ci) {
        GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext()
                .source(worldServer)
                .world(worldServer)
                .buildAndSwitch();
    }

    @Inject(method = "performWorldGenSpawning", at = @At(value = "RETURN"))
    private static void onPerformWorldGenSpawningReturn(World worldServer, Biome biome, int j, int k, int l, int m, Random rand, CallbackInfo ci) {
        CauseTracker.getInstance().completePhase(GenerationPhase.State.WORLD_SPAWNER_SPAWNING);
        spawnerEntityType = null;
    }

    /**
     * Redirects the canCreatureTypeSpawnAtLocation to add our event check after. This requires that the {@link #onGetRandom(Random, List)}
     * is called before this to actively set the proper entity class.
     * @param type
     * @param worldIn
     * @param pos
     * @return
     */
    @Redirect(method = "performWorldGenSpawning", at = @At(value = "INVOKE", target = BIOME_CAN_SPAWN_ANIMAL))
    private static boolean onCanGenerate(EntityLiving.SpawnPlacementType type, World worldIn, BlockPos pos) {
        return WorldEntitySpawner.canCreatureTypeSpawnAtLocation(type, worldIn, pos) && check(pos, worldIn);
    }

    /**
     * Redirects the method call to get the spawn list entry for world gen spawning so that we can
     * "capture" the {@link net.minecraft.entity.Entity} class that is about to attempt to be spawned.
     *
     * @param random
     * @param collection
     * @return
     */
    @Redirect(method = "performWorldGenSpawning", at = @At(value = "INVOKE", target = WEIGHTED_RANDOM_GET))
    private static WeightedRandom.Item onGetRandom(Random random, List<Biome.SpawnListEntry> collection) {
        Biome.SpawnListEntry entry = WeightedRandom.getRandomItem(random, collection);
        setEntityType(entry.entityClass);
        return entry;
    }

    private static void setEntityType(Class<? extends net.minecraft.entity.Entity> entityclass) {
        spawnerEntityType = EntityTypeRegistryModule.getInstance().getForClass(entityclass);
    }

    private static boolean check(BlockPos pos, World world) {
        EntityType entityType = spawnerEntityType;
        if (entityType == null) {
            return true; // Basically, we can't throw our own event.
        }
        Vector3d vector3d = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
        Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) world, vector3d);
        Sponge.getCauseStackManager().pushCause(world);
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), entityType, transform);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        return !event.isCancelled();
    }
}
