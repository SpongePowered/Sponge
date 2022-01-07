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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.command.SpongeCommandFactory;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.util.InventoryUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.block.BlockFireAccessor;
import org.spongepowered.common.mixin.core.world.WorldAccessor;
import org.spongepowered.common.mixin.plugin.tileentityactivation.TileEntityActivation;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.util.SpawnerSpawnType;
import org.spongepowered.common.world.WorldManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    public static boolean isClientAvailable() {
        return false;
    }

    public static boolean isDeobfuscatedEnvironment() {
        return true;
    }

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

    public static boolean isCreatureOfType(final Entity entity, final EnumCreatureType type) {
        return type.getCreatureClass().isAssignableFrom(entity.getClass());
    }

    public static boolean isFakePlayer(final Entity entity) {
        return false;
    }

    public static void fireServerConnectionEvent(final NetworkManager netManager) {
        // Implemented in SF
    }

    public static void firePlayerJoinSpawnEvent(final EntityPlayerMP playerMP) {
        // Implemented in SF
    }

    public static void handlePostChangeDimensionEvent(final EntityPlayerMP playerIn, final WorldServer fromWorld, final WorldServer toWorld) {
        // Overwritten in SpongeForge
    }

    public static boolean checkAttackEntity(final EntityPlayer entityPlayer, final Entity targetEntity) {
        return true;
    }

    public static double getBlockReachDistance(final EntityPlayerMP player) {
        return 5.0d;
    }

    /**
     * @author Polyacov_Yury
     * @reason Forge reachDistance attribute compatibility
     * @param player the player whose reach is being checked
     * @param entity the entity that is being reached
     * @return square of maximum player reach distance
     */
    public static double getEntityReachDistanceSq(final EntityPlayerMP player, Entity entity) {
        double d0 = 36.0d; // 6 blocks
        if (!player.canEntityBeSeen(entity)) {  // TODO: this check introduces MC-107103
            d0 = 9.0D; // 3 blocks
        }
        return d0;
    }

    // Entity registry

    @Nullable
    public static Class<? extends Entity> getEntityClass(final ResourceLocation name) {
        return EntityList.REGISTRY.getObject(name);
    }

    @Nullable
    public static String getEntityTranslation(final ResourceLocation name) {
        return EntityList.getTranslationName(name);
    }

    public static int getEntityId(final Class<? extends Entity> entityClass) {
        return EntityList.REGISTRY.getIDForObject(entityClass);
    }

    // Block

    public static boolean isBlockFlammable(final Block block, final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        return ((BlockFireAccessor) Blocks.FIRE).accessor$getBlockFlamability(block) > 0;
    }

    public static int getBlockLightOpacity(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return state.getLightOpacity();
    }

	public static int getChunkPosLight(final IBlockState blockState, final World world, final BlockPos pos) {
		return blockState.getLightValue();
	}
    // Tile entity

    @Nullable
    public static TileEntity createTileEntity(final Block block, final net.minecraft.world.World world, final IBlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider) block).createNewTileEntity(world, block.getMetaFromState(state));
        }
        return null;
    }

    public static boolean hasBlockTileEntity(final Block block, final IBlockState state) {
        return block instanceof ITileEntityProvider;
    }

    public static boolean shouldRefresh(final TileEntity tile, final net.minecraft.world.World world, final BlockPos pos, final IBlockState oldState, final IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public static void onTileChunkUnload(final TileEntity te) {
        // Overwritten in SpongeForge
    }

    // World

    public static Iterator<Chunk> getChunkIterator(final WorldServer world) {
        return world.getPlayerChunkMap().getChunkIterator();
    }

    public static void registerPortalAgentType(@Nullable final ForgeITeleporterBridge teleporter) {
        // Overwritten in SpongeForge
    }

    // World provider

    public static boolean canDoLightning(final WorldProvider provider, final net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public static boolean canDoRainSnowIce(final WorldProvider provider, final net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public static int getRespawnDimension(final WorldProvider targetDimension, final EntityPlayerMP player) {
        return 0;
    }

    public static BlockPos getRandomizedSpawnPoint(final WorldServer world) {
        BlockPos ret = world.getSpawnPoint();

        final boolean isAdventure = world.getWorldInfo().getGameType() == GameType.ADVENTURE;
        int spawnFuzz = Math.max(0, world.getMinecraftServer().getSpawnRadius(world));
        final int border = MathHelper.floor(world.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if (border < spawnFuzz) {
            spawnFuzz = border;
        }

        if (!world.provider.isNether() && !isAdventure && spawnFuzz != 0)
        {
            if (spawnFuzz < 2) {
                spawnFuzz = 2;
            }
            final int spawnFuzzHalf = spawnFuzz / 2;
            ret = world.getTopSolidOrLiquidBlock(ret.add(world.rand.nextInt(spawnFuzzHalf) - spawnFuzz, 0, world.rand.nextInt(spawnFuzzHalf) - spawnFuzz));
        }

        return ret;
    }

    // Item stack merging

    public static void addItemStackToListForSpawning(final Collection<ItemDropData> itemStacks, @Nullable final ItemDropData itemStack) {
        // This is the hook that can be overwritten to handle merging the item stack into an already existing item stack
        if (itemStack != null) {
            itemStacks.add(itemStack);
        }
    }

    public static MapStorage getWorldMapStorage(final World world) {
        return world.getMapStorage();
    }

    public static int countEntities(final WorldServer worldServer, final net.minecraft.entity.EnumCreatureType type, final boolean forSpawnCount) {
        return worldServer.countEntities(type.getCreatureClass());
    }

    public static int getMaxSpawnPackSize(final EntityLiving entityLiving) {
        return entityLiving.getMaxSpawnedInChunk();
    }

    public static SpawnerSpawnType canEntitySpawnHere(final EntityLiving entityLiving, final boolean entityNotColliding) {
        if (entityLiving.getCanSpawnHere() && entityNotColliding) {
            return SpawnerSpawnType.NORMAL;
        }
        return SpawnerSpawnType.NONE;
    }

    @Nullable
    public static Object onUtilRunTask(final FutureTask<?> task, final Logger logger) {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        try (final BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(task))  {
            context.buildAndSwitch();
            final Object o = Util.runTask(task, logger);
            return o;
        } catch (Exception e) {
            phaseTracker
                .printMessageWithCaughtException("Exception during phase body", "Something happened trying to run the main body of a phase", e);

            return null;
        }
    }

    public static void onEntityError(final Entity entity, final CrashReport crashReport) {
        throw new ReportedException(crashReport);
    }

    public static void onTileEntityError(final TileEntity tileEntity, final CrashReport crashReport) {
        throw new ReportedException(crashReport);
    }

    public static void blockExploded(final Block block, final World world, final BlockPos blockpos, final Explosion explosion) {
        world.setBlockToAir(blockpos);
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
    @SuppressWarnings("unused") // overridden to be used in MixinSpongeImplHooks.
    public static boolean isRestoringBlocks(final World world) {
        return PhaseTracker.getInstance().getCurrentState().isRestoring();

    }

    public static void onTileEntityChunkUnload(final net.minecraft.tileentity.TileEntity tileEntity) {
        // forge only method
    }

    public static boolean canConnectRedstone(
        final Block block, final IBlockState state, final IBlockAccess world, final BlockPos pos, @Nullable final EnumFacing side) {
        return state.canProvidePower() && side != null;
    }
    // Crafting

    public static Optional<ItemStack> getContainerItem(final ItemStack itemStack) {
        checkNotNull(itemStack, "The itemStack must not be null");

        final net.minecraft.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);

        if (nmsStack.isEmpty()) {
            return Optional.empty();
        }

        final Item nmsItem = nmsStack.getItem();

        if (nmsItem.hasContainerItem()) {
            final Item nmsContainerItem = nmsItem.getContainerItem();
            final net.minecraft.item.ItemStack nmsContainerStack = new net.minecraft.item.ItemStack(nmsContainerItem);
            final ItemStack containerStack = ItemStackUtil.fromNative(nmsContainerStack);

            return Optional.of(containerStack);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<CraftingRecipe> findMatchingRecipe(final CraftingGridInventory inventory, final org.spongepowered.api.world.World world) {
        final IRecipe recipe = CraftingManager.findMatchingRecipe(InventoryUtil.toNativeInventory(inventory), ((net.minecraft.world.World) world));
        return Optional.ofNullable(((CraftingRecipe) recipe));
    }

    public static Collection<CraftingRecipe> getCraftingRecipes() {
        return Streams.stream(CraftingManager.REGISTRY.iterator()).map(CraftingRecipe.class::cast).collect(ImmutableList.toImmutableList());
    }

    public static Optional<CraftingRecipe> getRecipeById(final String id) {
        final IRecipe recipe = CraftingManager.REGISTRY.getObject(new ResourceLocation(id));
        if (recipe == null) {
            return Optional.empty();
        }
        return Optional.of(((CraftingRecipe) recipe));
    }

    public static void register(final ResourceLocation name, final IRecipe recipe) {
        CraftingManager.register(name, recipe);
    }

    @Nullable
    public static PluginContainer getActiveModContainer() {
        return null;
    }

    public static Text getAdditionalCommandDescriptions() {
        return Text.EMPTY;
    }

    public static void registerAdditionalCommands(final ChildCommandElementExecutor flagChildren, final ChildCommandElementExecutor nonFlagChildren) {
        // Overwritten in SpongeForge
    }

    public static Predicate<? super PluginContainer> getPluginFilterPredicate() {
        return plugin -> !SpongeCommandFactory.CONTAINER_LIST_STATICS.contains(plugin.getId());
    }

    // Borrowed from Forge, with adjustments by us

    @Nullable
    public static RayTraceResult rayTraceEyes(final EntityLivingBase entity, final double length) {
        final Vec3d startPos = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        final Vec3d endPos = startPos.add(entity.getLookVec().scale(length));
        return entity.world.rayTraceBlocks(startPos, endPos);
    }

    public static boolean shouldKeepSpawnLoaded(final net.minecraft.world.DimensionType dimensionType, final int dimensionId) {
        final WorldServer worldServer = WorldManager.getWorldByDimensionId(dimensionId).orElse(null);
        return worldServer != null && ((WorldProperties) worldServer.getWorldInfo()).doesKeepSpawnLoaded();

    }

    public static void setShouldLoadSpawn(final net.minecraft.world.DimensionType dimensionType, final boolean keepSpawnLoaded) {
        // This is only used in SpongeForge
    }

    public static BlockPos getBedLocation(final EntityPlayer playerIn, final int dimension) {
        return ((EntityPlayerBridge) playerIn).bridge$getBedLocation(dimension);
    }

    public static boolean isSpawnForced(final EntityPlayer playerIn, final int dimension) {
        return ((EntityPlayerBridge) playerIn).bridge$isSpawnForced(dimension);
    }

    public static Inventory toInventory(final Object inventory, @Nullable final Object forgeItemHandler) {
        SpongeImpl.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
        return null;
    }

    public static InventoryAdapter findInventoryAdapter(final Object inventory) {
        SpongeImpl.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
        throw new IllegalArgumentException("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
    }

    public static void onTileEntityInvalidate(final TileEntity te) {
        te.invalidate();
    }

    public static void capturePerEntityItemDrop(final PhaseContext<?> phaseContext, final Entity owner,
        final EntityItem entityitem) {
        phaseContext.getPerEntityItemEntityDropSupplier().get().put(owner.getUniqueID(), entityitem);
    }

    /**
     * @author gabizou - April 21st, 2018
     * Gets the enchantment modifier for looting on the entity living base from the damage source, but in forge cases, we need to use their hooks.
     *
     * @param target
     * @param entity
     * @param cause
     * @return
     */
    public static int getLootingEnchantmentModifier(final EntityLivingBase target, final EntityLivingBase entity, final DamageSource cause) {
        return EnchantmentHelper.getLootingModifier(entity);
    }

    public static double getWorldMaxEntityRadius(final WorldServer worldServer) {
        return 2.0D;
    }

    /**
     * Provides the {@link Profession} to set onto the villager. Since forge has it's own
     * villager profession system, sponge has to bridge the compatibility and
     * the profession may not be "properly" registered.
     * @param professionId
     * @return
     */
    public static Profession validateProfession(final int professionId) {
        final List<Profession> professions = (List<Profession>) ProfessionRegistryModule.getInstance().getAll();
        for (final Profession profession : professions) {
            if (profession instanceof SpongeProfession) {
                if (professionId == ((SpongeProfession) profession).type) {
                    return profession;
                }
            }
        }
        throw new IllegalStateException("Invalid Villager profession id is present! Found: " + professionId
                                        + " when the expected contain: " + professions);

    }

    public static void onUseItemTick(final EntityLivingBase entity, final net.minecraft.item.ItemStack stack, final int activeItemStackUseCount) {
    }

    public static void onTETickStart(final TileEntity te) {

    }

    public static void onTETickEnd(final TileEntity te) {

    }

    public static void onEntityTickStart(final Entity entity) {

    }

    public static void onEntityTickEnd(final Entity entity) {

    }

    public static boolean isMainThread() {
        // Return true when the server isn't yet initialized, this means on a client
        // that the game is still being loaded. This is needed to support initialization
        // events with cause tracking.
        return !Sponge.isServerAvailable() || Sponge.getServer().isMainThread();
    }

    // Overridden by SpongeImplHooksMixin_ItemNameOverflowPrevention for exploit check
    public static boolean creativeExploitCheck(final Packet<?> packetIn, final EntityPlayerMP playerMP) {
        return false;
    }

    public static String getImplementationId() {
        throw new UnsupportedOperationException("SpongeCommon does not have it's own ecosystem, this needs to be mixed into for implementations depending on SpongeCommon");
    }

    public static TileEntityType getTileEntityType(final Class<? extends TileEntity> aClass) {
        return SpongeImpl.getRegistry().getTranslated(aClass, TileEntityType.class);
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param spongeEvent The sponge event
     * @return The forge event
     */
    @Nullable
    public static Object postForgeEventDataCompatForSponge(final InteractBlockEvent.Secondary spongeEvent) {
        SpongeImpl.postEvent(spongeEvent);
        return null;
    }

    // Some mods such as OpenComputers open a GUI on client-side
    // To workaround this, we will always send a SPacketCloseWindow to client if interacting with a TE
    // However, we skip closing under two circumstances:
    //
    // * If an inventory has already been opened on the server (e.g. by a plugin),
    // since we don't want to undo that

    // * If the event was cancelled by a Forge mod. In this case, we adhere to Forge's normal
    // bheavior, which is to leave any GUIs open on the client. Some mods, like Quark, modify
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
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param worldIn The world
     * @param pos The position
     * @param eventData The event data, if it was created
     * @param player The player
     */
    public static void shouldCloseScreen(final World worldIn, final BlockPos pos, @Nullable final Object eventData, final EntityPlayerMP player) {
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param forgeEventObject The forge event object, if it was created
     * @return The result as a result of the event data
     */
    public static EnumActionResult getInteractionCancellationResult(@Nullable final Object forgeEventObject) {
        return EnumActionResult.FAIL;
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param worldIn The world in
     * @param pos The position
     * @param player The player
     * @param heldItemMainhand The main hand item
     * @param heldItemOffhand The offhand item
     * @return Whether to bypass sneaking state, forge has an extra hook on the item class
     */
    public static boolean doesItemSneakBypass(final World worldIn, final BlockPos pos, final EntityPlayer player, final net.minecraft.item.ItemStack heldItemMainhand,
        final net.minecraft.item.ItemStack heldItemOffhand) {
        return heldItemMainhand.isEmpty() && heldItemOffhand.isEmpty();
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param player The player interacting
     * @param event The sponge event
     * @param result The current result
     * @param worldIn The world
     * @param pos the position
     * @param hand the hand used
     * @return Null so that the rest of the method continues processing? TODO - Zidane and Morph, please check this...
     */
    @Nullable
    public static EnumActionResult getEnumResultForProcessRightClickBlock(final EntityPlayerMP player,
        final InteractBlockEvent.Secondary event, final EnumActionResult result, final World worldIn, final BlockPos pos,
        final EnumHand hand) {
        return EnumActionResult.FAIL;
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param player The player
     * @param stack The item stack to check
     * @param worldIn The world
     * @param pos the position
     * @param hand the hand used
     * @param facing Facing direction
     * @param hitX hit x pos
     * @param hitY hit y pos
     * @param hitZ hit z pos
     * @return The result of the item stack's hook method
     */
    public static EnumActionResult onForgeItemUseFirst(
        final EntityPlayer player, final net.minecraft.item.ItemStack stack, final World worldIn, final BlockPos pos,
        final EnumHand hand, final EnumFacing facing, final float hitX,
        final float hitY, final float hitZ) {
        return EnumActionResult.PASS;
    }

    /**
     * @author gabizou - May 10th, 2019 - 1.12.2
     * @reason Forge events are getting wrapped in various cases that end up causing corner cases where the effective side
     * @param object The event
     * @return False by default, means all server sided events or common events are allowed otherwise.
     */
    public static boolean isEventClientEvent(final Object object) {
        return false;
    }


    /**
     * @author gabizou - May 28th, 2019 - 1.12.2
     * @reason Forge has custom items, and normally, we throw an event for any
     * and all listeners. The problem is that since Forge just blindly calls
     * events, and Sponge only throws events if there are listeners, the custom
     * item hook does not get called for direct spawned entities, so, we need
     * to explicitly call the custom item creation hooks here.
     *
     * @param entity The vanilla entity item
     * @return The custom item entity for the dropped item
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static Entity getCustomEntityIfItem(final Entity entity) {
        return null;
    }

    /**
     * For use with {@link TileEntityActivation}.
     *
     * @param tile The tile to tick
     * @return True whether to tick or false, not to
     */
    public static boolean shouldTickTile(final ITickable tile) {
        return true;
    }

    /**
     * Used for compatibility with Forge where Forge uses wrapped Items
     * since they allow for registry replacements.
     *
     * @param mixinItem_api The item
     * @return The resource location id
     */
    @Nullable
    public static ResourceLocation getItemResourceLocation(final Item mixinItem_api) {
        return Item.REGISTRY.getNameForObject(mixinItem_api);
    }

    public static void registerItemForSpongeRegistry(final int id, final ResourceLocation textualID, final Item itemIn) {
        ItemTypeRegistryModule.getInstance().registerAdditionalCatalog((ItemType) itemIn);
    }

    public static void writeItemStackCapabilitiesToDataView(final DataContainer container, final net.minecraft.item.ItemStack stack) {

    }

    public static boolean canEnchantmentBeAppliedToItem(final Enchantment enchantment, final net.minecraft.item.ItemStack stack) {
        return enchantment.canApply(stack);
    }

    public static void setCapabilitiesFromSpongeBuilder(final ItemStack stack, final NBTTagCompound compoundTag) {

    }

    public static TileEntity onChunkGetTileDuringRemoval(final WorldServer worldServer, final BlockPos pos) {
        if (((WorldAccessor) worldServer).accessor$getIsOutsideBuildHeight(pos)) {
            return null;
        } else {
            TileEntity tileentity2 = null;

            if (((WorldAccessor) worldServer).accessor$getProcessingLoadedTiles()) {
                tileentity2 = ((WorldAccessor) worldServer).accessPendingTileEntityAt(pos);
            }

            if (tileentity2 == null) {
                // Sponge - Instead of creating the tile entity, just check if it's there. If the
                // tile entity doesn't exist, don't create it since we're about to just wholesale remove it...
                // tileentity2 = this.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
                tileentity2 = worldServer.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
            }

            if (tileentity2 == null) {
                tileentity2 =  ((WorldAccessor) worldServer).accessPendingTileEntityAt(pos);
            }

            return tileentity2;
        }
    }

    /**
     * @author JBYoshi
     * @reason Forge compatibility
     * @param world The world in which the event takes place
     * @param entityIn The entity that called collisions
     * @param aabb The bounding box
     * @param collided The entities that were collided with
     */
    public static void onForgeCollision(final World world, @Nullable final Entity entityIn, final AxisAlignedBB aabb,
            final List<AxisAlignedBB> collided) {
    }
}
