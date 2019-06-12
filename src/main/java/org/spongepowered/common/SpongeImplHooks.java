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
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.command.SpongeCommandFactory;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.world.IMixinITeleporter;
import org.spongepowered.common.item.inventory.util.InventoryUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.plugin.tileentityactivation.TileEntityActivation;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.util.SpawnerSpawnType;
import org.spongepowered.common.world.WorldManager;

import java.util.Collection;
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

    public static void fireServerConnectionEvent(NetworkManager netManager) {
        // Implemented in SF
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

    public static void registerPortalAgentType(@Nullable IMixinITeleporter teleporter) {
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
                .source(task))  {
            context.buildAndSwitch();
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
    public static boolean isRestoringBlocks(World world) {
        return PhaseTracker.getInstance().getCurrentState().isRestoring();

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

    public static void register(ResourceLocation name, IRecipe recipe) {
        CraftingManager.register(name, recipe);
    }

    @Nullable
    public static PluginContainer getActiveModContainer() {
        return null;
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

    public static BlockPos getBedLocation(EntityPlayer playerIn, int dimension) {
        return ((IMixinEntityPlayer) playerIn).getBedLocation(dimension);
    }

    public static boolean isSpawnForced(EntityPlayer playerIn, int dimension) {
        return ((IMixinEntityPlayer) playerIn).isSpawnForced(dimension);
    }

    public static Inventory toInventory(Object inventory, @Nullable Object forgeItemHandler) {
        SpongeImpl.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
        return null;
    }

    public static void onTileEntityInvalidate(TileEntity te) {
        te.invalidate();
    }

    public static void capturePerEntityItemDrop(PhaseContext<?> phaseContext, Entity owner,
        EntityItem entityitem) {
        phaseContext.getPerEntityItemEntityDropSupplier().get().put(owner.getUniqueID(), entityitem);
    }

    /**
     * @author gabizou - April 21st, 2018
     * Gets the enchantment modifier for looting on the entity living base from the damage source, but in forge cases, we need to use their hooks.
     *
     * @param mixinEntityLivingBase
     * @param entity
     * @param cause
     * @return
     */
    public static int getLootingEnchantmentModifier(IMixinEntityLivingBase mixinEntityLivingBase, EntityLivingBase entity, DamageSource cause) {
        return EnchantmentHelper.getLootingModifier(entity);
    }

    public static double getWorldMaxEntityRadius(WorldServer worldServer) {
        return 2.0D;
    }

    /**
     * Provides the {@link Profession} to set onto the villager. Since forge has it's own
     * villager profession system, sponge has to bridge the compatibility and
     * the profession may not be "properly" registered.
     * @param professionId
     * @return
     */
    public static Profession validateProfession(int professionId) {
        List<Profession> professions = (List<Profession>) ProfessionRegistryModule.getInstance().getAll();
        for (Profession profession : professions) {
            if (profession instanceof SpongeProfession) {
                if (professionId == ((SpongeProfession) profession).type) {
                    return profession;
                }
            }
        }
        throw new IllegalStateException("Invalid Villager profession id is present! Found: " + professionId
                                        + " when the expected contain: " + professions);

    }

    public static void onUseItemTick(EntityLivingBase entity, net.minecraft.item.ItemStack stack, int activeItemStackUseCount) {
    }

    public static void onTETickStart(TileEntity te) {

    }

    public static void onTETickEnd(TileEntity te) {

    }

    public static void onEntityTickStart(Entity entity) {

    }

    public static void onEntityTickEnd(Entity entity) {

    }

    public static boolean isMainThread() {
        // Return true when the server isn't yet initialized, this means on a client
        // that the game is still being loaded. This is needed to support initialization
        // events with cause tracking.
        return !Sponge.isServerAvailable() || Sponge.getServer().isMainThread();
    }

    // Overridden by MixinSpongeImplHooks_ItemNameOverflowPrevention for exploit check
    public static boolean creativeExploitCheck(Packet<?> packetIn, EntityPlayerMP playerMP) {
        return false;
    }

    public static String getImplementationId() {
        throw new UnsupportedOperationException("SpongeCommon does not have it's own ecosystem, this needs to be mixed into for implementations depending on SpongeCommon");
    }

    public static TileEntityType getTileEntityType(Class<? extends TileEntity> aClass) {
        return SpongeImpl.getRegistry().getTranslated(aClass, TileEntityType.class);
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for MixinPlayerInteractionManager#processRightClickBlock.
     *
     * @param spongeEvent The sponge event
     * @return The forge event
     */
    @Nullable
    public static Object postForgeEventDataCompatForSponge(InteractBlockEvent.Secondary spongeEvent) {
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
     * @reason Eliminate an extra overwrite for SpongeForge for MixinPlayerInteractionManager#processRightClickBlock.
     *
     * @param worldIn The world
     * @param pos The position
     * @param eventData The event data, if it was created
     * @param player The player
     */
    public static void shouldCloseScreen(World worldIn, BlockPos pos, @Nullable Object eventData, EntityPlayerMP player) {
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for MixinPlayerInteractionManager#processRightClickBlock.
     *
     * @param forgeEventObject The forge event object, if it was created
     * @return The result as a result of the event data
     */
    public static EnumActionResult getInteractionCancellationResult(@Nullable Object forgeEventObject) {
        return EnumActionResult.FAIL;
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for MixinPlayerInteractionManager#processRightClickBlock.
     *
     * @param worldIn The world in
     * @param pos The position
     * @param player The player
     * @param heldItemMainhand The main hand item
     * @param heldItemOffhand The offhand item
     * @return Whether to bypass sneaking state, forge has an extra hook on the item class
     */
    public static boolean doesItemSneakBypass(World worldIn, BlockPos pos, EntityPlayer player, net.minecraft.item.ItemStack heldItemMainhand,
        net.minecraft.item.ItemStack heldItemOffhand) {
        return heldItemMainhand.isEmpty() && heldItemOffhand.isEmpty();
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for MixinPlayerInteractionManager#processRightClickBlock.
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
    public static EnumActionResult getEnumResultForProcessRightClickBlock(EntityPlayerMP player,
        InteractBlockEvent.Secondary event, EnumActionResult result, World worldIn, BlockPos pos,
        EnumHand hand) {
        return EnumActionResult.FAIL;
    }

    /**
     * @author gabizou - April 23rd, 2019 - 1.12.2
     * @reason Eliminate an extra overwrite for SpongeForge for MixinPlayerInteractionManager#processRightClickBlock.
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
    public static EnumActionResult onForgeItemUseFirst(EntityPlayer player, net.minecraft.item.ItemStack stack, World worldIn, BlockPos pos,
        EnumHand hand, EnumFacing facing, float hitX,
        float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }

    /**
     * @author gabizou - May 10th, 2019 - 1.12.2
     * @reason Forge events are getting wrapped in various cases that end up causing corner cases where the effective side
     * @param object The event
     * @return False by default, means all server sided events or common events are allowed otherwise.
     */
    public static boolean isEventClientEvent(Object object) {
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
    @Nullable
    public static Entity getCustomEntityIfItem(Entity entity) {
        return entity;
    }

    /**
     * For use with {@link TileEntityActivation}.
     *
     * @param tile The tile to tick
     * @return True whether to tick or false, not to
     */
    public static boolean shouldTickTile(ITickable tile) {
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
    public static ResourceLocation getItemResourceLocation(Item mixinItem_api) {
        return Item.REGISTRY.getNameForObject(mixinItem_api);
    }

    public static void registerItemForSpongeRegistry(int id, ResourceLocation textualID, Item itemIn) {
        ItemTypeRegistryModule.getInstance().registerAdditionalCatalog((ItemType) itemIn);
    }

    public static void writeItemStackCapabilitiesToDataView(DataContainer container, net.minecraft.item.ItemStack stack) {

    }

    public static boolean canEnchantmentBeAppliedToItem(Enchantment enchantment, net.minecraft.item.ItemStack stack) {
        return enchantment.canApply(stack);
    }

    public static void setCapabilitiesFromSpongeBuilder(ItemStack stack, NBTTagCompound compoundTag) {

    }
}
