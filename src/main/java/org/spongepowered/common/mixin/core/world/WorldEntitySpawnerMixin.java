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
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
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

import javax.annotation.Nullable;

@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {

    @Nullable
    private static EntityType impl$spawnerEntityType;
    private final List<Chunk> impl$eligibleSpawnChunks = new ArrayList<>();

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
    public int findChunksForSpawning(final ServerWorld world, final boolean spawnHostileMobs, final boolean spawnPeacefulMobs, final boolean spawnOnSetTickRate) {
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
            final int mobSpawnRange = Math.min(((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter().getConfig().getWorld().getMobSpawnRange(),
                    ((org.spongepowered.api.world.World) world).getViewDistance());
            // Vanilla uses a div count of 289 (17x17) which assumes the view distance is 8.
            // Since we allow for custom ranges, we need to adjust the div count based on the
            // mob spawn range set by server.
            final int MOB_SPAWN_COUNT_DIV = (2 * mobSpawnRange + 1) * (2 * mobSpawnRange + 1);

            for (final PlayerEntity entityplayer : world.field_73010_i) {
                // We treat players who do not affect spawning as "spectators"
                if (!((EntityPlayerBridge) entityplayer).bridge$affectsSpawning() || entityplayer.func_175149_v()) {
                    continue;
                }

                final int playerPosX = MathHelper.func_76128_c(entityplayer.field_70165_t / 16.0D);
                final int playerPosZ = MathHelper.func_76128_c(entityplayer.field_70161_v / 16.0D);

                for (int i = -mobSpawnRange; i <= mobSpawnRange; ++i) {
                    for (int j = -mobSpawnRange; j <= mobSpawnRange; ++j) {
                        final boolean flag = i == -mobSpawnRange || i == mobSpawnRange || j == -mobSpawnRange || j == mobSpawnRange;
                        final Chunk
                            chunk =
                            ((ChunkProviderBridge) world.func_72863_F())
                                .bridge$getLoadedChunkWithoutMarkingActive(i + playerPosX, j + playerPosZ);
                        if (chunk == null || (chunk.field_189550_d && !((ChunkBridge) chunk).bridge$isPersistedChunk())) {
                            // Don't attempt to spawn in an unloaded chunk
                            continue;
                        }

                        final ChunkBridge spongeChunk = (ChunkBridge) chunk;
                        ++chunkSpawnCandidates;
                        final ChunkPos chunkPos = chunk.func_76632_l();
                        if (!flag && world.func_175723_af().func_177730_a(chunkPos)) {
                            final PlayerChunkMapEntry playerchunkmapentry = world.func_184164_w().func_187301_b(chunkPos.field_77276_a, chunkPos.field_77275_b);

                            if (playerchunkmapentry != null && playerchunkmapentry.func_187274_e() && !spongeChunk.bridge$isSpawning()) {
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
            final long worldTotalTime = world.func_82737_E();
            final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter();

            labelOuterLoop:
            for (final EntityClassification enumCreatureType : EntityClassification.values()) {
                int limit = 0;
                int tickRate = 0;
                if (enumCreatureType == EntityClassification.MONSTER) {
                    limit = configAdapter.getConfig().getSpawner().getMonsterSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getMonsterTickRate();
                } else if (enumCreatureType == EntityClassification.CREATURE) {
                    limit = configAdapter.getConfig().getSpawner().getAnimalSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getAnimalTickRate();
                } else if (enumCreatureType == EntityClassification.WATER_CREATURE) {
                    limit = configAdapter.getConfig().getSpawner().getAquaticSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getAquaticTickRate();
                } else if (enumCreatureType == EntityClassification.AMBIENT) {
                    limit = configAdapter.getConfig().getSpawner().getAmbientSpawnLimit();
                    tickRate = configAdapter.getConfig().getSpawner().getAmbientTickRate();
                }

                if (limit == 0 || tickRate == 0 || (worldTotalTime % tickRate) != 0L) {
                    continue;
                }

                if ((!enumCreatureType.func_75599_d() || spawnPeacefulMobs) && (enumCreatureType.func_75599_d() || spawnHostileMobs)) {
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
                        final int k1 = blockpos.func_177958_n();
                        final int l1 = blockpos.func_177956_o();
                        final int i2 = blockpos.func_177952_p();
                        final BlockState iblockstate = world.func_180495_p(blockpos);

                        if (!iblockstate.func_185915_l()) {
                            int spawnCount = 0;
                            for (int spawnLimit = 0; spawnLimit < 3; ++spawnLimit) {
                                int l2 = k1;
                                int i3 = l1;
                                int j3 = i2;
                                Biome.SpawnListEntry spawnListEntry = null;
                                ILivingEntityData ientitylivingdata = null;
                                final int l3 = MathHelper.func_76143_f(Math.random() * 4.0D);

                                for (int i4 = 0; i4 < l3; ++i4) {
                                    l2 += world.field_73012_v.nextInt(6) - world.field_73012_v.nextInt(6);
                                    i3 += world.field_73012_v.nextInt(1) - world.field_73012_v.nextInt(1);
                                    j3 += world.field_73012_v.nextInt(6) - world.field_73012_v.nextInt(6);
                                    mutableBlockPos.func_181079_c(l2, i3, j3);
                                    final double spawnX = l2 + 0.5F;
                                    final double spawnY = i3;
                                    final double spawnZ = j3 + 0.5F;

                                    if (!world.func_175636_b(spawnX, spawnY, spawnZ, 24.0D)
                                        && world.func_175694_M().func_177954_c(spawnX, spawnY, spawnZ) >= 576.0D) {
                                        if (spawnListEntry == null) {
                                            spawnListEntry = world.func_175734_a(enumCreatureType, mutableBlockPos);

                                            if (spawnListEntry == null) {
                                                break;
                                            }
                                        }

                                        final EntityType entityType = EntityTypeRegistryModule.getInstance().getForClass(spawnListEntry.field_76300_b);
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

                                        if (world.func_175732_a(enumCreatureType, spawnListEntry, mutableBlockPos)
                                            && WorldEntitySpawner.func_180267_a(
                                            EntitySpawnPlacementRegistry.func_180109_a(spawnListEntry.field_76300_b), world,
                                            mutableBlockPos)) {
                                            final MobEntity entityliving;

                                            try {
                                                entityliving =
                                                    spawnListEntry.field_76300_b.getConstructor(new Class<?>[]{World.class}).newInstance(world);
                                            } catch (final Exception exception) {
                                                exception.printStackTrace();
                                                continue labelOuterLoop;
                                            }

                                            entityliving.func_70012_b(spawnX, spawnY, spawnZ, world.field_73012_v.nextFloat() * 360.0F, 0.0F);
                                            final boolean entityNotColliding = entityliving.func_70058_J();

                                            final SpawnerSpawnType type = SpongeImplHooks.canEntitySpawnHere(entityliving, entityNotColliding);
                                            if (type != SpawnerSpawnType.NONE) {
                                                if (type == SpawnerSpawnType.NORMAL) {
                                                    ientitylivingdata = entityliving.func_180482_a(world.func_175649_E(new BlockPos(entityliving)), ientitylivingdata);
                                                }

                                                if (entityNotColliding) {
                                                    ++spawnCount;
                                                    world.func_72838_d(entityliving);
                                                } else {
                                                    entityliving.func_70106_y();
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
        final int i = chunk.field_76635_g * 16 + worldIn.field_73012_v.nextInt(16);
        final int j = chunk.field_76647_h * 16 + worldIn.field_73012_v.nextInt(16);
        final int k = MathHelper.func_154354_b(chunk.func_177433_f(new BlockPos(i, 0, j)) + 1, 16);
        final int l = worldIn.field_73012_v.nextInt(k > 0 ? k : chunk.func_76625_h() + 16 - 1);
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
        final Chunk chunk = ((ChunkProviderBridge) worldIn.func_72863_F()).bridge$getLoadedChunkWithoutMarkingActive(x, z);
        if (chunk == null || (chunk.field_189550_d && !((ChunkBridge) chunk).bridge$isPersistedChunk())) {
            // Don't attempt to spawn in an unloaded chunk
            return null;
        }
        // Sponge end

        final int i = x * 16 + worldIn.field_73012_v.nextInt(16);
        final int j = z * 16 + worldIn.field_73012_v.nextInt(16);
        final int k = MathHelper.func_154354_b(chunk.func_177433_f(new BlockPos(i, 0, j)) + 1, 16);
        final int l = worldIn.field_73012_v.nextInt(k > 0 ? k : chunk.func_76625_h() + 16 - 1);
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
    private static boolean onCanGenerate(final MobEntity.SpawnPlacementType type, final World worldIn, final BlockPos pos) {
        return WorldEntitySpawner.func_180267_a(type, worldIn, pos) && check(pos, worldIn);
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
        final Biome.SpawnListEntry entry = WeightedRandom.func_76271_a(random, collection);
        setEntityType(entry.field_76300_b);
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
        final Vector3d vector3d = new Vector3d(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
        final Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) world, vector3d);
        Sponge.getCauseStackManager().pushCause(world);
        final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), entityType, transform);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        return !event.isCancelled();
    }
}
