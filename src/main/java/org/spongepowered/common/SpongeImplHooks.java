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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.command.SpongeCommandFactory;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.item.inventory.util.InventoryUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.SpawnerSpawnType;
import org.spongepowered.common.world.WorldManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

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
        final String className = clazz.getName();
        if (className.startsWith("net.minecraft.")) {
            return "minecraft";
        }
        if (className.startsWith("org.spongepowered.")) {
            return "sponge";
        }
        return "unknown";
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

    @Nullable
    public static String getEntityTranslation(ResourceLocation name) {
        return EntityList.getTranslationName(name);
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

	public static int getChunkPosLight(IBlockState blockState, World world, BlockPos pos) {
		return blockState.getLightValue();
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
        if (border < spawnFuzz) {
            spawnFuzz = border;
        }

        if (!world.provider.isNether() && !isAdventure && spawnFuzz != 0)
        {
            if (spawnFuzz < 2) {
                spawnFuzz = 2;
            }
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

    public static int countEntities(WorldServer worldServer, net.minecraft.entity.EnumCreatureType type, boolean forSpawnCount) {
        return worldServer.countEntities(type.getCreatureClass());
    }

    public static int getMaxSpawnPackSize(EntityLiving entityLiving) {
        return entityLiving.getMaxSpawnedInChunk();
    }

    public static SpawnerSpawnType canEntitySpawnHere(EntityLiving entityLiving, boolean entityNotColliding) {
        if (entityLiving.getCanSpawnHere() && entityNotColliding) {
            return SpawnerSpawnType.NORMAL;
        }
        return SpawnerSpawnType.NONE;
    }

    @Nullable
    public static Object onUtilRunTask(FutureTask<?> task, Logger logger) {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        try (final BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(task)
                .buildAndSwitch())  {
            final Object o = Util.runTask(task, logger);
            return o;
        } catch (Exception e) {
            phaseTracker.printExceptionFromPhase(e);
            return null;
        }
    }

    public static void onEntityError(Entity entity, CrashReport crashReport) {
        throw new ReportedException(crashReport);
    }

    public static void onTileEntityError(TileEntity tileEntity, CrashReport crashReport) {
        throw new ReportedException(crashReport);
    }

    public static void blockExploded(Block block, World world, BlockPos blockpos, Explosion explosion) {
        world.setBlockToAir(blockpos);
        block.onBlockDestroyedByExplosion(world, blockpos, explosion);
    }

    public static boolean isRestoringBlocks(World world) {
        if (PhaseTracker.getInstance().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS) {
            return true;
        }

        return false;
    }

    public static void onTileEntityChunkUnload(net.minecraft.tileentity.TileEntity tileEntity) {
        // forge only method
    }

    public static boolean canConnectRedstone(Block block, IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return state.canProvidePower() && side != null;
    }
    // Crafting

    public static Optional<ItemStack> getContainerItem(ItemStack itemStack) {
        checkNotNull(itemStack, "The itemStack must not be null");

        net.minecraft.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);

        if (nmsStack.isEmpty()) {
            return Optional.empty();
        }

        Item nmsItem = nmsStack.getItem();

        if (nmsItem.hasContainerItem()) {
            Item nmsContainerItem = nmsItem.getContainerItem();
            net.minecraft.item.ItemStack nmsContainerStack = new net.minecraft.item.ItemStack(nmsContainerItem);
            ItemStack containerStack = ItemStackUtil.fromNative(nmsContainerStack);

            return Optional.of(containerStack);
        } else {
            return Optional.empty();
        }
    }

    public static void onCraftingRecipeRegister(CraftingRecipe recipe) {
        // Overridden in SF
        CraftingManager.register(recipe.getId(), ((IRecipe) recipe));
    }

    public static Optional<CraftingRecipe> findMatchingRecipe(CraftingGridInventory inventory, org.spongepowered.api.world.World world) {
        IRecipe recipe = CraftingManager.findMatchingRecipe(InventoryUtil.toNativeInventory(inventory), ((net.minecraft.world.World) world));
        return Optional.ofNullable(((CraftingRecipe) recipe));
    }

    public static Collection<CraftingRecipe> getCraftingRecipes() {
        return Streams.stream(CraftingManager.REGISTRY.iterator()).map(CraftingRecipe.class::cast).collect(ImmutableList.toImmutableList());
    }

    public static Optional<CraftingRecipe> getRecipeById(String id) {
        IRecipe recipe = CraftingManager.REGISTRY.getObject(new ResourceLocation(id));
        if (recipe == null) {
            return Optional.empty();
        }
        return Optional.of(((CraftingRecipe) recipe));
    }

    public static Text getAdditionalCommandDescriptions() {
        return Text.EMPTY;
    }

    public static void registerAdditionalCommands(ChildCommandElementExecutor flagChildren, ChildCommandElementExecutor nonFlagChildren) {
        // Overwritten in SpongeForge
    }

    public static Predicate<? super PluginContainer> getPluginFilterPredicate() {
        return plugin -> !SpongeCommandFactory.CONTAINER_LIST_STATICS.contains(plugin.getId());
    }

    // Borrowed from Forge, with adjustments by us

    @Nullable
    public static RayTraceResult rayTraceEyes(EntityLivingBase entity, double length) {
        final Vec3d startPos = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        final Vec3d endPos = startPos.add(entity.getLookVec().scale(length));
        return entity.world.rayTraceBlocks(startPos, endPos);
    }

    public static boolean shouldKeepSpawnLoaded(net.minecraft.world.DimensionType dimensionType, int dimensionId) {
        final WorldServer worldServer = WorldManager.getWorldByDimensionId(dimensionId).orElse(null);
        return worldServer != null && ((WorldProperties) worldServer.getWorldInfo()).doesKeepSpawnLoaded();

    }

    public static void setShouldLoadSpawn(net.minecraft.world.DimensionType dimensionType, boolean keepSpawnLoaded) {
        // This is only used in SpongeForge
    }
}
