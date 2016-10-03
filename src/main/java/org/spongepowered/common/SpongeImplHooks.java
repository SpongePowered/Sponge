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
package org.spongepowered.common;


import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.ItemDropData;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Utility that fires events that normally Forge fires at (in spots). Typically
 * our penultimate goal is to not remove spots where events occur but sometimes
 * it happens (in @Overwrites typically). Normally events that are in Forge are
 * called themselves in SpongeVanilla but when it can't really occur, we fix
 * this issue with Sponge by overwriting this class
 */
public final class SpongeImplHooks {

    public static LoadWorldEvent createLoadWorldEvent(World world) {
        return SpongeEventFactory.createLoadWorldEvent(Cause.of(NamedCause.source(SpongeImpl.getGame().getServer())), world);
    }

    public static ClientConnectionEvent.Join createClientConnectionEventJoin(Cause cause, MessageChannel originalChannel,
            Optional<MessageChannel> channel, MessageEvent.MessageFormatter formatter, Player targetEntity, boolean messageCancelled) {
        return SpongeEventFactory.createClientConnectionEventJoin(cause, originalChannel, channel, formatter, targetEntity, messageCancelled);
    }

    public static RespawnPlayerEvent createRespawnPlayerEvent(Cause cause, Transform<World> fromTransform, Transform<World> toTransform,
            Player targetEntity, boolean bedSpawn) {
        return SpongeEventFactory.createRespawnPlayerEvent(cause, fromTransform, toTransform, targetEntity, bedSpawn);
    }

    public static ClientConnectionEvent.Disconnect createClientConnectionEventDisconnect(Cause cause, MessageChannel originalChannel,
            Optional<MessageChannel> channel, MessageEvent.MessageFormatter formatter, Player targetEntity, boolean messageCancelled) {
        return SpongeEventFactory.createClientConnectionEventDisconnect(cause, originalChannel, channel, formatter, targetEntity, messageCancelled);
    }

    public static boolean blockHasTileEntity(Block block, IBlockState state) {
        return block instanceof ITileEntityProvider;
    }

    public static int getBlockLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getLightOpacity();
    }

    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider)block).createNewTileEntity(world, block.getMetaFromState(state));
        }
        return null;
    }

    public static void addItemStackToListForSpawning(Collection<ItemDropData> itemStacks, @Nullable ItemDropData itemStack) {
        // This is the hook that can be overwritten to handle merging the item stack into an already existing item stack
        if (itemStack != null) {
            itemStacks.add(itemStack);
        }
    }

    /**
     * A simple method to check attacks for the forge event factory.
     *
     * @param entityPlayer
     * @param targetEntity
     * @return
     */
    public static boolean checkAttackEntity(EntityPlayer entityPlayer, Entity targetEntity) {
        final ItemStack item = entityPlayer.getHeldItemMainhand();
        if (item != null) {
            return true;
        }
        return true;
    }

    public static boolean isCreatureOfType(Entity entity, EnumCreatureType type) {
        if (entity instanceof EntityMob || entity instanceof EntitySlime) {
            return type == EnumCreatureType.MONSTER;
        } else if (entity instanceof EntityWaterMob) {
            return type == EnumCreatureType.WATER_CREATURE;
        } else if (entity instanceof EntityAmbientCreature) {
            return type == EnumCreatureType.AMBIENT;
        } else if (((entity instanceof EntityCreature))) {
            return type == EnumCreatureType.CREATURE;
        }

        return false;
    }

    public static boolean isFakePlayer(Entity entity) {
        return false;
    }

    public static String getModIdFromClass(Class<?> clazz) {
        return clazz.getName().startsWith("net.minecraft.") ? "minecraft" : "unknown";
    }

    public static void registerPortalAgentType(@Nullable Teleporter teleporter) {
        // plugins are required to register types
    }

    /**
     * Targeted specifically for a mixin to throw a method for Forge events.
     * @param playerIn
     * @param fromWorld
     * @param toWorld
     */
    public static void handlePostChangeDimensionEvent(EntityPlayerMP playerIn, WorldServer fromWorld, WorldServer toWorld) {

    }

    public static void firePlayerJoinSpawnEvent(EntityPlayerMP playerMP) {
        // Overwritten in Forge
    }

    public static boolean canDoLightning(WorldProvider provider, net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public static boolean canDoRainSnowIce(WorldProvider provider, net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public static boolean isDeobfuscatedEnvironment() {
        return true;
    }

    public static Iterator<Chunk> getChunkIterator(WorldServer world) {
        return world.getPlayerChunkMap().getChunkIterator();
    }

    public static int getChunkPosLight(IBlockState blockState, net.minecraft.world.World worldObj, BlockPos blockpos$mutableblockpos) {
        return blockState.getLightValue();
    }

    public static int getChunkBlockLightOpacity(IBlockState blockState, net.minecraft.world.World worldObj, BlockPos pos) {
        return blockState.getLightOpacity();
    }

    public static int getChunkBlockLightOpacity(IBlockState state, net.minecraft.world.World worldObj, int x, int y, int z) {
        return state.getLightOpacity();
    }

    public static BlockPos getRandomizedSpawnPoint(WorldServer worldServer) {
        BlockPos ret = worldServer.getSpawnPoint();

        boolean isAdventure = worldServer.getWorldInfo().getGameType() == GameType.ADVENTURE;
        int spawnFuzz = Math.max(0, worldServer.getMinecraftServer().getSpawnRadius(worldServer));
        int border = MathHelper.floor_double(worldServer.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if (border < spawnFuzz) spawnFuzz = border;

        if (!worldServer.provider.getHasNoSky() && !isAdventure && spawnFuzz != 0)
        {
            if (spawnFuzz < 2) spawnFuzz = 2;
            int spawnFuzzHalf = spawnFuzz / 2;
            ret = worldServer.getTopSolidOrLiquidBlock(ret.add(worldServer.rand.nextInt(spawnFuzzHalf) - spawnFuzz, 0, worldServer.rand.nextInt(spawnFuzzHalf) - spawnFuzz));
        }

        return ret;
    }
}
