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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;
import org.spongepowered.common.event.tracking.ItemDropData;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

/**
 * Contains default Vanilla implementations for features that are only
 * available in Forge. SpongeForge overwrites the methods in this class
 * with calls to the Forge methods.
 */
public final class SpongeImplHooks {

    public static boolean isVanilla() {
        return true;
    }

    public static boolean isDeobfuscatedEnvironment() {
        return true;
    }

    public static String getModIdFromClass(Class<?> clazz) {
        return clazz.getName().startsWith("net.minecraft.") ? "minecraft" : "unknown";
    }

    // Entity

    public static boolean isCreatureOfType(Entity entity, EnumCreatureType type) {
        return type.getCreatureClass().isAssignableFrom(entity.getClass());
    }

    public static boolean isFakePlayer(Entity entity) {
        return false;
    }

    public static void firePlayerJoinSpawnEvent(EntityPlayerMP playerMP) {
        // Overwritten in SpongeForge
    }

    public static void handlePostChangeDimensionEvent(EntityPlayerMP playerIn, WorldServer fromWorld, WorldServer toWorld) {
        // Overwritten in SpongeForge
    }

    public static boolean checkAttackEntity(EntityPlayer entityPlayer, Entity targetEntity) {
        return true;
    }

    public static double getBlockReachDistance(EntityPlayerMP player) {
        return 5.0d;
    }

    // Entity registry

    @Nullable
    public static Class<? extends Entity> getEntityClass(ResourceLocation name) {
        return EntityList.REGISTRY.getObject(name);
    }

    public static int getEntityId(Class<? extends Entity> entityClass) {
        return EntityList.REGISTRY.getIDForObject(entityClass);
    }

    // Block

    public static boolean isBlockFlammable(Block block, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return Blocks.FIRE.getFlammability(block) > 0;
    }

    public static int getBlockLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getLightOpacity();
    }

    // Tile entity

    @Nullable
    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider) block).createNewTileEntity(world, block.getMetaFromState(state));
        }
        return null;
    }

    public static boolean hasBlockTileEntity(Block block, IBlockState state) {
        return block instanceof ITileEntityProvider;
    }

    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public static void onTileChunkUnload(TileEntity te) {
        // Overwritten in SpongeForge
    }

    // World

    public static Iterator<Chunk> getChunkIterator(WorldServer world) {
        return world.getPlayerChunkMap().getChunkIterator();
    }

    public static void registerPortalAgentType(@Nullable Teleporter teleporter) {
        // Overwritten in SpongeForge
    }

    // World provider

    public static boolean canDoLightning(WorldProvider provider, net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public static boolean canDoRainSnowIce(WorldProvider provider, net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public static int getRespawnDimension(WorldProvider targetDimension, EntityPlayerMP player) {
        return 0;
    }

    public static BlockPos getRandomizedSpawnPoint(WorldServer world) {
        BlockPos ret = world.getSpawnPoint();

        boolean isAdventure = world.getWorldInfo().getGameType() == GameType.ADVENTURE;
        int spawnFuzz = Math.max(0, world.getMinecraftServer().getSpawnRadius(world));
        int border = MathHelper.floor(world.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if (border < spawnFuzz) spawnFuzz = border;

        if (!world.provider.hasNoSky() && !isAdventure && spawnFuzz != 0)
        {
            if (spawnFuzz < 2) spawnFuzz = 2;
            int spawnFuzzHalf = spawnFuzz / 2;
            ret = world.getTopSolidOrLiquidBlock(ret.add(world.rand.nextInt(spawnFuzzHalf) - spawnFuzz, 0, world.rand.nextInt(spawnFuzzHalf) - spawnFuzz));
        }

        return ret;
    }

    // Item stack merging

    public static void addItemStackToListForSpawning(Collection<ItemDropData> itemStacks, @Nullable ItemDropData itemStack) {
        // This is the hook that can be overwritten to handle merging the item stack into an already existing item stack
        if (itemStack != null) {
            itemStacks.add(itemStack);
        }
    }

    public static MapStorage getWorldMapStorage(World world) {
        return world.getMapStorage();
    }
}
