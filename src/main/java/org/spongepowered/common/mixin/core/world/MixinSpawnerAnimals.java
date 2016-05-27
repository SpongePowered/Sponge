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
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

@Mixin(SpawnerAnimals.class)
public abstract class MixinSpawnerAnimals {

    private static final String WORLD_CAN_SPAWN_CREATURE = "Lnet/minecraft/world/WorldServer;canCreatureTypeSpawnHere("
        + "Lnet/minecraft/entity/EnumCreatureType;Lnet/minecraft/world/biome/BiomeGenBase$SpawnListEntry;Lnet/minecraft/util/BlockPos;)Z";

    private static final String BIOME_CAN_SPAWN_ANIMAL =
        "Lnet/minecraft/world/SpawnerAnimals;canCreatureTypeSpawnAtLocation(Lnet/minecraft/entity/EntityLiving$SpawnPlacementType;"
        + "Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)Z";

    private static final String WEIGHTED_RANDOM_GET = "Lnet/minecraft/util/WeightedRandom;getRandomItem(Ljava/util/Random;Ljava/util/Collection;)"
        + "Lnet/minecraft/util/WeightedRandom$Item;";
    private static final String WORLD_SERVER_SPAWN_ENTITY = "Lnet/minecraft/world/WorldServer;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z";
    private static final String WORLD_SPAWN_ENTITY = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z";

    private static boolean spawnerStart;
    private static EntityType spawnerEntityType;
    private static Class<? extends Entity> spawnerEntityClass;

    @ModifyConstant(method = "findChunksForSpawning", constant = @Constant(intValue = 8))
    public int adjustCheckRadiusForServerView(int originalValue, WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs,
            boolean p_77192_4_) {
        // TODO Adjust for when per-world view distances are a thing
        return Math.min(((IMixinWorld) worldServerIn).getActiveConfig().getConfig().getWorld().getMobSpawnRange(), MinecraftServer
                .getServer()
                .getConfigurationManager().getViewDistance());
    }

    @Inject(method = "findChunksForSpawning", at = @At(value = "HEAD"))
    public void onFindChunksForSpawningHead(WorldServer worldServer, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnedOnSetTickRate, CallbackInfoReturnable<Integer> ci) {
        IMixinWorld spongeWorld = ((IMixinWorld) worldServer);
        CauseTracker causeTracker = spongeWorld.getCauseTracker();
        causeTracker.addCause(Cause.of(NamedCause.source(SpawnCause.builder().type(SpawnTypes.WORLD_SPAWNER).build())));
        causeTracker.setWorldSpawnerRunning(true);
        causeTracker.setCaptureSpawnedEntities(true);
        spawnerStart = true;
        spongeWorld.getTimingsHandler().mobSpawn.startTiming();
    }

    @Inject(method = "findChunksForSpawning", at = @At(value = "RETURN"))
    public void onFindChunksForSpawningReturn(WorldServer worldServer, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnedOnSetTickRate, CallbackInfoReturnable<Integer> ci) {
        IMixinWorld spongeWorld = ((IMixinWorld) worldServer);
        CauseTracker causeTracker = spongeWorld.getCauseTracker();
        causeTracker.handleSpawnedEntities();
        causeTracker.setWorldSpawnerRunning(false);
        causeTracker.setCaptureSpawnedEntities(false);
        causeTracker.removeCurrentCause();
        if (spawnerStart) {
            spawnerStart = false;
            spawnerEntityClass = null;
            spawnerEntityType = null;
        }
        spongeWorld.getTimingsHandler().mobSpawn.stopTiming();
    }

    @Inject(method = "performWorldGenSpawning", at = @At(value = "HEAD"))
    private static void onPerformWorldGenSpawningHead(World worldServer, BiomeGenBase biome, int j, int k, int l, int m, Random rand, CallbackInfo ci) {
        IMixinWorld spongeWorld = ((IMixinWorld) worldServer);
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        causeTracker.addCause(Cause.of(NamedCause.source(SpawnCause.builder().type(SpawnTypes.WORLD_SPAWNER).build()), NamedCause.of("Biome", biome)));
        causeTracker.setChunkSpawnerRunning(true);
        causeTracker.setCaptureSpawnedEntities(true);
        spawnerStart = true;
    }

    @Inject(method = "performWorldGenSpawning", at = @At(value = "RETURN"))
    private static void onPerformWorldGenSpawningReturn(World worldServer, BiomeGenBase biome, int j, int k, int l, int m, Random rand, CallbackInfo ci) {
        IMixinWorld spongeWorld = (IMixinWorld) worldServer;
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        causeTracker.handleSpawnedEntities();
        causeTracker.setChunkSpawnerRunning(false);
        causeTracker.setCaptureSpawnedEntities(false);
        causeTracker.removeCurrentCause();
        if (spawnerStart) {
            spawnerStart = false;
            spawnerEntityClass = null;
            spawnerEntityType = null;
        }
    }

    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean onFindChunksForSpawningEligiblePlayer(EntityPlayer player) {
        // We treat players who do not affect spawning as "spectators"
        return !((IMixinEntityPlayer) player).affectsSpawning() || player.isSpectator();

    }


    /**
     * Basically, this is redirecting the boolean check to where the worldserver is checked first, then
     * our event is thrown. Note that the {@link #setEntityType(Class)} needs to be called first to
     * actively set the entity type being used.
     *
     * @param worldServer The world server
     * @param creatureType The creature type
     * @param spawnListEntry The spawner list entry containing the entity class
     * @param pos The position
     * @return True if the worldserver check was valid and if our event wasn't cancelled
     */
    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = WORLD_CAN_SPAWN_CREATURE))
    public boolean onCanSpawn(WorldServer worldServer, EnumCreatureType creatureType, BiomeGenBase.SpawnListEntry spawnListEntry, BlockPos pos) {
        setEntityType(spawnListEntry.entityClass);
        return worldServer.canCreatureTypeSpawnHere(creatureType, spawnListEntry, pos) && check(pos, worldServer);
    }

    /**
     * Redirects the canCreatureTypeSpawnAtLocation to add our event check after. This requires that the {@link #onGetRandom(Random, Collection)}
     * is called before this to actively set the proper entity class.
     * @param type
     * @param worldIn
     * @param pos
     * @return
     */
    @Redirect(method = "performWorldGenSpawning", at = @At(value = "INVOKE", target = BIOME_CAN_SPAWN_ANIMAL))
    private static boolean onCanGenerate(EntityLiving.SpawnPlacementType type, World worldIn, BlockPos pos) {
        return SpawnerAnimals.canCreatureTypeSpawnAtLocation(type, worldIn, pos) && check(pos, worldIn);
    }

    /**
     * Redirects the method call to get the spawn list entry for world gen spawning so that we can
     * "capture" the {@link net.minecraft.entity.Entity} class that is about to attempt to be spawned.
     *
     * @param random
     * @param collection
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "performWorldGenSpawning", at = @At(value = "INVOKE", target = WEIGHTED_RANDOM_GET))
    private static WeightedRandom.Item onGetRandom(Random random, Collection collection) {
        BiomeGenBase.SpawnListEntry entry = (BiomeGenBase.SpawnListEntry) WeightedRandom.getRandomItem(random, collection);
        setEntityType(entry.entityClass);
        return entry;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setEntityType(Class entityclass) {
        spawnerEntityClass = entityclass;
        Optional<EntityType> entityType = EntityTypeRegistryModule.getInstance().getEntity(spawnerEntityClass);
        if (!entityType.isPresent()) {
            SpongeImpl.getLogger().warn("There's an unknown Entity class that isn't registered with Sponge!" + spawnerEntityClass);
        } else {
            spawnerEntityType = entityType.get();
        }
    }

    private static boolean check(BlockPos pos, World world) {
        EntityType entityType = spawnerEntityType;
        if (entityType == null) {
            return true; // Basically, we can't throw our own event.
        }
        Vector3d vector3d = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
        Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) world, vector3d);
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(world)), entityType, transform);
        SpongeImpl.postEvent(event);
        return !event.isCancelled();
    }

    /**
     * @author gabizou - January 30th, 2016
     *
     * Redirects the spawning here to note that it's from the world spawner.
     *
     * @param worldServer The world server coming in
     * @param nmsEntity The nms entity
     * @return True if the world spawn was successful
     */
    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = WORLD_SERVER_SPAWN_ENTITY))
    private boolean onSpawnEntityInWorld(WorldServer worldServer, net.minecraft.entity.Entity nmsEntity) {
        return ((org.spongepowered.api.world.World) worldServer).spawnEntity(((Entity) nmsEntity),
                ((IMixinWorld) worldServer).getCauseTracker().getCurrentCause());
    }

    @Redirect(method = "performWorldGenSpawning", at = @At(value = "INVOKE", target = WORLD_SPAWN_ENTITY))
    private static boolean onSpawnEntityInWorldGen(World world, net.minecraft.entity.Entity entity) {
        return ((org.spongepowered.api.world.World) world).spawnEntity((Entity) entity,
                ((IMixinWorld) world).getCauseTracker().getCurrentCause());
    }

}
