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
import com.google.common.collect.Sets;
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
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.util.SpawnerSpawnType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {

    @Nullable
    private static EntityType impl$spawnerEntityType;
    private final Set<Chunk> impl$eligibleSpawnChunks = Sets.newIdentityHashSet();

    /**
     * @author blood - February 18th, 2017
     * @reason Refactor entire method for optimizations and spawn limits.
     *
     * @param world The world
     * @param spawnHostileMobs If hostile entities can spawn
     * @param spawnPeacefulMobs If passive entities can spawn
     * @param spawnOnSetTickRate If tickrate has been reached for spawning passives
     * @return The amount of entities spawned
     */
    @Overwrite
    public int findChunksForSpawning(final WorldServer world, final boolean spawnHostileMobs, final boolean spawnPeacefulMobs, final boolean spawnOnSetTickRate) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return 0;
        }

        try (final PhaseContext<?> context = GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext()
                .world(world)) {
            context.buildAndSwitch();
            Iterator<Chunk> chunkIterator = this.impl$eligibleSpawnChunks.iterator();
            while (chunkIterator.hasNext()) {
                final Chunk chunk = chunkIterator.next();
                ((ChunkBridge) chunk).bridge$setIsSpawning(false);
                chunkIterator.remove();
            }

            final WorldServerBridge spongeWorld = (WorldServerBridge) world;
            spongeWorld.bridge$getTimingsHandler().mobSpawn.startTiming();

            int chunkSpawnCandidates = 0;
            final int mobSpawnRange = Math.min(((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter().getConfig().getWorld().getMobSpawnRange(),
                    ((org.spongepowered.api.world.World) world).getViewDistance());
            // Vanilla uses a div count of 289 (17x17) which assumes the view distance is 8.
            // Since we allow for custom ranges, we need to adjust the div count based on the
            // mob spawn range set by server.
            final int MOB_SPAWN_COUNT_DIV = (2 * mobSpawnRange + 1) * (2 * mobSpawnRange + 1);

            for (final EntityPlayer entityplayer : world.playerEntities) {
                // We treat players who do not affect spawning as "spectators"
                if (!((EntityPlayerBridge) entityplayer).bridge$affectsSpawning() || entityplayer.isSpectator()) {
                    continue;
                }

                final int playerPosX = MathHelper.floor(entityplayer.posX / 16.0D);
                final int playerPosZ = MathHelper.floor(entityplayer.posZ / 16.0D);

                for (int i = -mobSpawnRange; i <= mobSpawnRange; ++i) {
                    for (int j = -mobSpawnRange; j <= mobSpawnRange; ++j) {
                        final boolean flag = i == -mobSpawnRange || i == mobSpawnRange || j == -mobSpawnRange || j == mobSpawnRange;
                        final Chunk
                            chunk =
                            ((ChunkProviderBridge) world.getChunkProvider())
                                .bridge$getLoadedChunkWithoutMarkingActive(i + playerPosX, j + playerPosZ);
                        if (chunk == null || (chunk.unloadQueued && !((ChunkBridge) chunk).bridge$isPersistedChunk())) {
                            // Don't attempt to spawn in an unloaded chunk
                            continue;
                        }
                        if (this.impl$eligibleSpawnChunks.contains(chunk)) {
                            continue;
                        }

                        final ChunkBridge spongeChunk = (ChunkBridge) chunk;
                        ++chunkSpawnCandidates;
                        final ChunkPos chunkPos = chunk.getPos();
                        if (!flag && world.getWorldBorder().contains(chunkPos)) {
                            final PlayerChunkMapEntry playerchunkmapentry = world.getPlayerChunkMap().getEntry(chunkPos.x, chunkPos.z);

                            if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers() && !spongeChunk.bridge$isSpawning()) {
                                this.impl$eligibleSpawnChunks.add(chunk);
                                spongeChunk.bridge$setIsSpawning(true);
                            }
                        }
                    }
                }
            }

            // If there are no eligible chunks, return early
            if (this.impl$eligibleSpawnChunks.isEmpty()) {
                spongeWorld.bridge$getTimingsHandler().mobSpawn.stopTiming();
                return 0;
            }

            int totalSpawned = 0;
            final long worldTotalTime = world.getTotalWorldTime();
            final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();

            labelOuterLoop:
            for (final EnumCreatureType enumCreatureType : EnumCreatureType.values()) {
                int limit = 0;
                int tickRate = 0;
                if (enumCreatureType == EnumCreatureType.MONSTER) {
                    limit = configAdapter.getConfig().getSpawner().getMonsterSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getMonsterTickRate();
                } else if (enumCreatureType == EnumCreatureType.CREATURE) {
                    limit = configAdapter.getConfig().getSpawner().getAnimalSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getAnimalTickRate();
                } else if (enumCreatureType == EnumCreatureType.WATER_CREATURE) {
                    limit = configAdapter.getConfig().getSpawner().getAquaticSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getAquaticTickRate();
                } else if (enumCreatureType == EnumCreatureType.AMBIENT) {
                    limit = configAdapter.getConfig().getSpawner().getAmbientSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getAmbientTickRate();
                }

                if (limit == 0 || tickRate == 0 || (worldTotalTime % tickRate) != 0L) {
                    continue;
                }

                if ((!enumCreatureType.getPeacefulCreature() || spawnPeacefulMobs) && (enumCreatureType.getPeacefulCreature() || spawnHostileMobs)) {
                    final int entityCount = SpongeImplHooks.countEntities(world, enumCreatureType, true);
                    final int maxCount = limit * chunkSpawnCandidates / MOB_SPAWN_COUNT_DIV;
                    if (entityCount > maxCount) {
                        continue labelOuterLoop;
                    }

                    chunkIterator = this.impl$eligibleSpawnChunks.iterator();
                    int mobLimit = maxCount - entityCount + 1;
                    labelChunkStart:
                    while (chunkIterator.hasNext() && mobLimit > 0) {
                        final Chunk chunk = chunkIterator.next();
                        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                        final BlockPos blockpos = getRandomChunkPosition(world, chunk);
                        final int k1 = blockpos.getX();
                        final int l1 = blockpos.getY();
                        final int i2 = blockpos.getZ();
                        final IBlockState iblockstate = chunk.getBlockState(blockpos);

                        if (!iblockstate.isNormalCube()) {
                            int spawnCount = 0;
                            for (int spawnLimit = 0; spawnLimit < 3; ++spawnLimit) {
                                int l2 = k1;
                                int i3 = l1;
                                int j3 = i2;
                                Biome.SpawnListEntry spawnListEntry = null;
                                IEntityLivingData ientitylivingdata = null;
                                final int l3 = MathHelper.ceil(Math.random() * 4.0D);

                                for (int i4 = 0; i4 < l3; ++i4) {
                                    l2 += world.rand.nextInt(6) - world.rand.nextInt(6);
                                    i3 += world.rand.nextInt(1) - world.rand.nextInt(1);
                                    j3 += world.rand.nextInt(6) - world.rand.nextInt(6);
                                    mutableBlockPos.setPos(l2, i3, j3);
                                    final double spawnX = l2 + 0.5F;
                                    final double spawnY = i3;
                                    final double spawnZ = j3 + 0.5F;

                                    if (!world.isAnyPlayerWithinRangeAt(spawnX, spawnY, spawnZ, 24.0D)
                                        && world.getSpawnPoint().distanceSq(spawnX, spawnY, spawnZ) >= 576.0D) {
                                        if (spawnListEntry == null) {
                                            spawnListEntry = world.getSpawnListEntryForTypeAt(enumCreatureType, mutableBlockPos);

                                            if (spawnListEntry == null) {
                                                break;
                                            }
                                        }

                                        final EntityType entityType = EntityTypeRegistryModule.getInstance().getForClass(spawnListEntry.entityClass);
                                        if (entityType != null) {
                                            final Vector3d vector3d = new Vector3d(spawnX, spawnY, spawnZ);
                                            final Transform<org.spongepowered.api.world.World>
                                                transform =
                                                new Transform<>((org.spongepowered.api.world.World) world, vector3d);
                                            final ConstructEntityEvent.Pre
                                                event =
                                                SpongeEventFactory
                                                    .createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), entityType,
                                                        transform);
                                            if (SpongeImpl.postEvent(event)) {
                                                continue;
                                            }
                                        }

                                        if (world.canCreatureTypeSpawnHere(enumCreatureType, spawnListEntry, mutableBlockPos)
                                            && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
                                            EntitySpawnPlacementRegistry.getPlacementForEntity(spawnListEntry.entityClass), world,
                                            mutableBlockPos)) {
                                            final EntityLiving entityliving;

                                            try {
                                                entityliving =
                                                    spawnListEntry.entityClass.getConstructor(new Class<?>[]{World.class}).newInstance(world);
                                            } catch (final Exception exception) {
                                                exception.printStackTrace();
                                                continue labelOuterLoop;
                                            }

                                            entityliving.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0.0F);
                                            boolean entityNotColliding = entityliving.isNotColliding();

                                            final SpawnerSpawnType type = SpongeImplHooks.canEntitySpawnHere(entityliving, entityNotColliding);
                                            if (type != SpawnerSpawnType.NONE) {
                                                if (type == SpawnerSpawnType.NORMAL) {
                                                    ientitylivingdata = entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);
                                                    entityNotColliding = entityliving.isNotColliding();
                                                }

                                                if (entityNotColliding) {
                                                    ++spawnCount;
                                                    world.spawnEntity(entityliving);
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

            spongeWorld.bridge$getTimingsHandler().mobSpawn.stopTiming();

            return totalSpawned;
        }
    }

    private static BlockPos getRandomChunkPosition(final World worldIn, final Chunk chunk)
    {
        final int i = chunk.x * 16 + worldIn.rand.nextInt(16);
        final int j = chunk.z * 16 + worldIn.rand.nextInt(16);
        final int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        final int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    /**
     * @author aikar - February 20th, 2017 - Optimizes light level check.
     * @author blood - February 20th, 2017 - Avoids checking unloaded chunks and chunks with pending light updates.
     *
     * @reason Avoids checking unloaded chunks and chunks with pending light updates.
     */
    @Overwrite
    private static BlockPos getRandomChunkPosition(final World worldIn, final int x, final int z)
    {
        // Sponge start
        final Chunk chunk = ((ChunkProviderBridge) worldIn.getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(x, z);
        if (chunk == null || (chunk.unloadQueued && !((ChunkBridge) chunk).bridge$isPersistedChunk())) {
            // Don't attempt to spawn in an unloaded chunk
            return null;
        }
        // Sponge end

        final int i = x * 16 + worldIn.rand.nextInt(16);
        final int j = z * 16 + worldIn.rand.nextInt(16);
        final int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        final int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    @Inject(method = "performWorldGenSpawning", at = @At("HEAD"))
    private static void onPerformWorldGenSpawningHead(final World worldServer, final Biome biome, final int j, final int k, final int l, final int m, final Random rand, final CallbackInfo ci) {
        GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext()
                .source(worldServer)
                .world(worldServer)
                .buildAndSwitch();
    }

    @Inject(method = "performWorldGenSpawning", at = @At("RETURN"))
    private static void onPerformWorldGenSpawningReturn(final World worldServer, final Biome biome, final int j, final int k, final int l, final int m, final Random rand, final CallbackInfo ci) {
        PhaseTracker.getInstance().getCurrentContext().close();
        WorldEntitySpawnerMixin.impl$spawnerEntityType = null;
    }

    /**
     * Redirects the canCreatureTypeSpawnAtLocation to add our event check after. This requires that the {@link #onGetRandom(Random, List)}
     * is called before this to actively set the proper entity class.
     * @param type
     * @param worldIn
     * @param pos
     * @return
     */
    @Redirect(method = "performWorldGenSpawning", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/WorldEntitySpawner;canCreatureTypeSpawnAtLocation(Lnet/minecraft/entity/EntityLiving$SpawnPlacementType;"
        + "Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z"))
    private static boolean onCanGenerate(final EntityLiving.SpawnPlacementType type, final World worldIn, final BlockPos pos) {
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
    @Redirect(method = "performWorldGenSpawning", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/util/WeightedRandom;getRandomItem(Ljava/util/Random;Ljava/util/List;)"
            + "Lnet/minecraft/util/WeightedRandom$Item;"))
    private static WeightedRandom.Item onGetRandom(final Random random, final List<Biome.SpawnListEntry> collection) {
        final Biome.SpawnListEntry entry = WeightedRandom.getRandomItem(random, collection);
        setEntityType(entry.entityClass);
        return entry;
    }

    private static void setEntityType(final Class<? extends net.minecraft.entity.Entity> entityclass) {
        WorldEntitySpawnerMixin.impl$spawnerEntityType = EntityTypeRegistryModule.getInstance().getForClass(entityclass);
    }

    private static boolean check(final BlockPos pos, final World world) {
        final EntityType entityType = WorldEntitySpawnerMixin.impl$spawnerEntityType;
        if (entityType == null) {
            return true; // Basically, we can't throw our own event.
        }
        final Vector3d vector3d = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
        final Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) world, vector3d);
        Sponge.getCauseStackManager().pushCause(world);
        final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), entityType, transform);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        return !event.isCancelled();
    }
}
