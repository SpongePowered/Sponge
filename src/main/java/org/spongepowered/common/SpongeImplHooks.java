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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.accessor.world.WorldAccessor;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.world.server.SpongeWorldManager;

/**
 * Contains default Vanilla implementations for features that are only
 * available in Forge. SpongeForge overwrites the methods in this class
 * with calls to the Forge methods.
 */
public class SpongeImplHooks {

    public static String getModIdFromClass(final Class<?> clazz) {
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

    public static boolean isCreatureOfType(final Entity entity, final EntityClassification type) {
        return entity.getType().getClassification() == type;
    }

    public static boolean isFakePlayer(final Entity entity) {
        return false;
    }

    public static void fireServerConnectionEvent(final NetworkManager manager) {
        // Implemented in SF
    }

    public static void firePlayerJoinSpawnEvent(final ServerPlayerEntity player) {
        // Overwritten in SpongeForge
    }

    public static void handlePostChangeDimensionEvent(final ServerPlayerEntity player, final ServerWorld fromWorld, final ServerWorld toWorld) {
        // Overwritten in SpongeForge
    }

    public static boolean checkAttackEntity(final PlayerEntity player, final Entity victim) {
        return true;
    }

    public static double getBlockReachDistance(final ServerPlayerEntity player) {
        return 5.0d;
    }

    // Block

    public static int getBlockLightOpacity(final BlockState state, final IBlockReader world, final BlockPos pos) {
        return state.getLightValue();
    }

	public static int getChunkPosLight(final BlockState blockState, final World world, final BlockPos pos) {
		return blockState.getLightValue();
	}
    // Tile entity

    @Nullable
    public static TileEntity createTileEntity(final Block block, final World world, final BlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider) block).createNewTileEntity(world);
        }
        return null;
    }

    public static boolean hasBlockTileEntity(final BlockState state) {
        return state.getBlock() instanceof ITileEntityProvider;
    }


    public static void onTileChunkUnload(final TileEntity tileEntity) {
        // Overwritten in SpongeForge
    }

    // World provider

    public static boolean canDoLightning(final Dimension dimension, final Chunk chunk) {
        return true;
    }

    public static boolean canDoRainSnowIce(final Dimension dimension, final Chunk chunk) {
        return true;
    }

    public static DimensionType getRespawnDimensionType(final Dimension dimension, final ServerPlayerEntity player) {
        return DimensionType.OVERWORLD;
    }

    public static BlockPos getRandomizedSpawnPoint(final ServerWorld world) {
        BlockPos ret = world.getSpawnPoint();

        final boolean isAdventure = world.getWorldInfo().getGameType() == GameType.ADVENTURE;
        int spawnFuzz = Math.max(0, world.getServer().getSpawnProtectionSize());
        final int border = MathHelper.floor(world.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if (border < spawnFuzz) {
            spawnFuzz = border;
        }

        throw new UnsupportedOperationException("Implement the rest of this please");
//        if (!world.dimension.isNether() && !isAdventure && spawnFuzz != 0)
//        {
//            if (spawnFuzz < 2) {
//                spawnFuzz = 2;
//            }
//            final int spawnFuzzHalf = spawnFuzz / 2;
//            ret = world.getSpawnCoordinate(ret.add(world.rand.nextInt(spawnFuzzHalf) - spawnFuzz, 0, world.rand.nextInt(spawnFuzzHalf) - spawnFuzz));
//        }

//        return ret;
    }

    // Item stack merging

    public static int getMaxSpawnPackSize(final MobEntity mob) {
        return mob.getMaxSpawnedInChunk();
    }


    public static void onEntityError(final Entity entity, final CrashReport crashReport) {
        throw new ReportedException(crashReport);
    }

    public static void onTileEntityError(final TileEntity tileEntity, final CrashReport crashReport) {
        throw new ReportedException(crashReport);
    }

    public static void blockExploded(final Block block, final World world, final BlockPos blockpos, final Explosion explosion) {
        world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3);
        block.onExplosionDestroy(world, blockpos, explosion);
    }

    /**
     * A method for forge compatibility when mods tend to set the flag
     * to true to mark a world restoring so entity item drops don't
     * get spawned (other entities do get spawned).
     *
     * @param world The world to check
     * @return True if the current phase state is restoring, or the world is restoring in forge.
     */
    @SuppressWarnings("unused") // overridden to be used with the PhaseTracker specific code.
    public static boolean isRestoringBlocks(final World world) {
        return false;
    }

    public static void onTileEntityChunkUnload(final TileEntity tileEntity) {
        // forge only method
    }

    public static boolean canConnectRedstone(final Block block, final BlockState state, final IWorldReader world, final BlockPos pos, @Nullable final Direction side) {
        return state.canProvidePower() && side != null;
    }

    // Borrowed from Forge, with adjustments by us

    @Nullable
    public static RayTraceResult rayTraceEyes(final LivingEntity entity, final double length) {
        return null;
    }

    public static boolean shouldKeepSpawnLoaded(final Server server, final DimensionType dimensionType) {
        final ServerWorld world = ((SpongeWorldManager) server.getWorldManager()).getWorld(dimensionType);
        return world != null && ((WorldInfoBridge) world.getWorldInfo()).bridge$doesKeepSpawnLoaded();

    }

    public static void setKeepSpawnLoaded(final DimensionType dimensionType, final boolean keepSpawnLoaded) {
        // This is only used in SpongeForge
    }

    @Nullable
    public static BlockPos getBedLocation(final PlayerEntity player, final DimensionType dimensionType) {
        return ((PlayerEntityBridge) player).bridge$getBedLocation(dimensionType);
    }

    public static boolean isSpawnForced(final PlayerEntity player, final DimensionType dimensionType) {
        return ((PlayerEntityBridge) player).bridge$isSpawnForced(dimensionType);
    }

    public static Inventory toInventory(final Object inventory, @Nullable final Object forgeItemHandler) {
        SpongeCommon.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
        return null;
    }

    public static InventoryAdapter findInventoryAdapter(final Object inventory) {
        SpongeCommon.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
        throw new IllegalArgumentException("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
    }

    public static void onTileEntityInvalidate(final TileEntity tileEntity) {
        tileEntity.remove();
    }

    public static void capturePerEntityItemDrop(final PhaseContext<?> phaseContext, final Entity owner, final ItemEntity item) {
        phaseContext.getPerEntityItemEntityDropSupplier().get().put(owner.getUniqueID(), item);
    }

    /**
     * Gets the enchantment modifier for looting on the entity living base from the damage source, but in forge cases, we need to use their hooks.
     */
    public static int getLootingEnchantmentModifier(final LivingEntity target, final LivingEntity entity, final DamageSource cause) {
        return EnchantmentHelper.getLootingModifier(entity);
    }

    public static double getWorldMaxEntityRadius(final ServerWorld world) {
        return 2.0D;
    }


    public static void onUseItemTick(final LivingEntity living, final ItemStack stack, final int activeItemStackUseCount) {
    }

    public static void onTETickStart(final TileEntity tileEntity) {

    }

    public static void onTETickEnd(final TileEntity tileEntity) {

    }

    public static void onEntityTickStart(final Entity entity) {

    }

    public static void onEntityTickEnd(final Entity entity) {

    }

    public static boolean onServerThread() {
        // Return true when the server isn't yet initialized, this means on a client
        // that the game is still being loaded. This is needed to support initialization
        // events with cause tracking.
        return !Sponge.isServerAvailable() || Sponge.getServer().onMainThread();
    }

    // Overridden by SpongeImplHooksMixin_ItemNameOverflowPrevention for exploit check
    public static boolean creativeExploitCheck(final IPacket<?> packet, final ServerPlayerEntity player) {
        return false;
    }

    public static String getImplementationId() {
        throw new UnsupportedOperationException("SpongeCommon does not have it's own ecosystem, this needs to be mixed into for implementations depending on SpongeCommon");
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     */
    @Nullable
    public static Object postForgeEventDataCompatForSponge(final InteractBlockEvent.Secondary spongeEvent) {
        SpongeCommon.postEvent(spongeEvent);
        return null;
    }

    // Some mods such as OpenComputers open a GUI on client-side
    // To workaround this, we will always send a SPacketCloseWindow to client if interacting with a TE
    // However, we skip closing under two circumstances:
    //
    // * If an inventory has already been opened on the server (e.g. by a plugin),
    // since we don't want to undo that

    // * If the event was cancelled by a Forge mod. In this case, we adhere to Forge's normal
    // behavior, which is to leave any GUIs open on the client. Some mods, like Quark, modify
    // Vanilla blocks (such as noteblocks) by opening a custom GUI on the client interaction event,
    // and then cancelling the interaction event on the server.
    //
    // In the second case, we have two conflicting goals. First, we want to ensure that Sponge protection
    // plugins are ablee to fully prevent interactions with a block. This means sending a close
    // window packet to the client when the event is cancelled, since we can't know what
    // client-side only GUIs (no Container) a mod may have opened.
    //
    // However, we don't want to break mods that rely on the fact that cancelling
    // a server-side interaction events leaves any client GUIs open.
    //
    // To resolve this issue, we only send a close window packet if the event was not cancelled
    // by a Forge event listener.
    // SpongeForge - end
    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     */
    public static void shouldCloseScreen(final World world, final BlockPos pos, @Nullable final Object eventData, final ServerPlayerEntity player) {
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param forgeEventObject The forge event object, if it was created
     * @return The result as a result of the event data
     */
    public static ActionResultType getInteractionCancellationResult(@Nullable final Object forgeEventObject) {
        return ActionResultType.FAIL;
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param worldIn The world in
     * @param pos The position
     * @param player The player
     * @param heldItemMainhand The main hand item
     * @param heldItemOffhand The offhand item
     * @return Whether to bypass sneaking state, forge has an extra hook on the item class
     */
    public static boolean doesItemSneakBypass(final World worldIn, final BlockPos pos, final PlayerEntity player, final net.minecraft.item.ItemStack heldItemMainhand,
                                              final net.minecraft.item.ItemStack heldItemOffhand) {
        return heldItemMainhand.isEmpty() && heldItemOffhand.isEmpty();
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     */
    @Nullable
    public static ActionResultType getEnumResultForProcessRightClickBlock(final ServerPlayerEntity player, final InteractBlockEvent.Secondary event,
                                                                          final ActionResultType result, final World world, final BlockPos pos, final Hand hand) {
        return ActionResultType.FAIL;
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     */
    public static ActionResultType onForgeItemUseFirst(final PlayerEntity player, final ItemStack stack, final World world, final BlockPos pos,
                                                       final Hand hand, final Direction facing, final float hitX, final float hitY, final float hitZ) {
        return ActionResultType.PASS;
    }

    /**
     * Forge events are getting wrapped in various cases that end up causing corner cases where the effective side ends up being the client.
     *
     * <p>
     *     If this returns true, the event manager will handle the client event differently (or not at all)
     * </p>>
     */
    public static boolean isClientEvent(final Object object) {
        return false;
    }


    /**
     * Forge has custom items, and normally, we throw an event for any and all listeners. The problem is that since Forge just blindly calls
     * events, and Sponge only throws events if there are listeners, the custom item hook does not get called for direct spawned entities,
     * so we need to explicitly call the custom item creation hooks here.
     */
    @Nullable
    public static Entity getCustomEntityIfItem(final Entity entity) {
        return null;
    }

    /**
     * For use with TileActivation.
     *
     * @param tileEntity The tile to tick
     * @return True whether to tick or false, not to
     */
    public static boolean shouldTickTile(final ITickableTileEntity tileEntity) {
        return true;
    }

    /**
     * Used for compatibility with Forge where Forge uses wrapped Items since they allow for registry replacements.
     *
     * @param item The item
     * @return The resource location id
     */
    @Nullable
    public static ResourceLocation getItemResourceLocation(final Item item) {
        return Registry.ITEM.getKey(item);
    }

    /**
     * Used in game dictionaries
     * @param key
     * @param item
     */
    public static void registerItemForSpongeRegistry(final ResourceLocation key, final Item item) {
        // todo
    }

    public static void writeItemStackCapabilitiesToDataView(final DataContainer container, final ItemStack stack) {

    }

    public static boolean canEnchantmentBeAppliedToItem(final Enchantment enchantment, final ItemStack stack) {
        return enchantment.canApply(stack);
    }

    public static void setCapabilitiesFromSpongeBuilder(final ItemStack stack, final CompoundNBT compound) {

    }

    public static TileEntity onChunkGetTileDuringRemoval(final ServerWorld world, final BlockPos pos) {
        if (WorldAccessor.accessor$isOutsideBuildHeight(pos)) {
            return null;
        } else {
            TileEntity tileentity2 = null;

            if (((WorldAccessor) world).accessor$getProcessingLoadedTiles()) {
                tileentity2 = ((WorldAccessor) world).accessor$getPendingTileEntityAt(pos);
            }

            if (tileentity2 == null) {
                // Sponge - Instead of creating the tile entity, just check if it's there. If the
                // tile entity doesn't exist, don't create it since we're about to just wholesale remove it...
                // tileentity2 = this.shadow$getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
                tileentity2 = world.getChunkAt(pos).getTileEntity(pos, Chunk.CreateEntityType.CHECK);
            }

            if (tileentity2 == null) {
                tileentity2 =  ((WorldAccessor) world).accessor$getPendingTileEntityAt(pos);
            }

            return tileentity2;
        }
    }

    /**
     * A Forge event bridge for us to use
     * @param entity
     * @param world
     * @return
     */
    public static boolean canEntityJoinWorld(final Entity entity, final ServerWorld world) {
        return false;
    }

    public static TileEntity createTileEntity(final BlockState newState, final World world) {
        return ((ITileEntityProvider) newState.getBlock()).createNewTileEntity(world);
    }
}
