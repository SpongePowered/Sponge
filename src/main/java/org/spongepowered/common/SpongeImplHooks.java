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
import net.minecraft.item.crafting.IRecipe;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.command.SpongeCommandFactory;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.block.BlockFireAccessor;
import org.spongepowered.common.mixin.core.world.WorldAccessor;
import org.spongepowered.common.mixin.plugin.tileentityactivation.TileEntityActivation;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.util.SpawnerSpawnType;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

/**
 * Contains default Vanilla implementations for features that are only
 * available in Forge. SpongeForge overwrites the methods in this class
 * with calls to the Forge methods.
 */
public class SpongeImplHooks {

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
        String className = clazz.getName();
        if (className.startsWith("net.minecraft.")) {
            return "minecraft";
        }
        if (className.startsWith("org.spongepowered.")) {
            return "sponge";
        }
        return "unknown";
    }

    // Entity

    public static boolean isCreatureOfType(Entity entity, EntityClassification type) {
        return type.getCreatureClass().isAssignableFrom(entity.getClass());
    }

    public static boolean isFakePlayer(Entity entity) {
        return false;
    }

    public static void fireServerConnectionEvent(NetworkManager manager) {
        // Implemented in SF
    }

    public static void firePlayerJoinSpawnEvent(ServerPlayerEntity player) {
        // Overwritten in SpongeForge
    }

    public static void handlePostChangeDimensionEvent(ServerPlayerEntity player, ServerWorld fromWorld, ServerWorld toWorld) {
        // Overwritten in SpongeForge
    }

    public static boolean checkAttackEntity(PlayerEntity player, Entity victim) {
        return true;
    }

    public static double getBlockReachDistance(ServerPlayerEntity player) {
        return 5.0d;
    }

    // Entity registry

    @Nullable
    public static Class<? extends Entity> getEntityClass(ResourceLocation name) {
        return EntityList.REGISTRY.getOrDefault(name);
    }

    @Nullable
    public static String getEntityTranslation(ResourceLocation name) {
        return EntityList.getTranslationName(name);
    }

    public static int getEntityId(Class<? extends Entity> entityClass) {
        return EntityList.REGISTRY.getId(entityClass);
    }

    // Block

    public static boolean isBlockFlammable(Block block, IBlockAccess world, BlockPos pos, Direction face) {
        return ((BlockFireAccessor) Blocks.FIRE).accessor$getBlockFlamability(block) > 0;
    }

    public static int getBlockLightOpacity(BlockState state, IBlockAccess world, BlockPos pos) {
        return state.getLightOpacity();
    }

	public static int getChunkPosLight(BlockState blockState, World world, BlockPos pos) {
		return blockState.getLightValue();
	}
    // Tile entity

    @Nullable
    public static TileEntity createTileEntity(Block block, World world, BlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider) block).createNewTileEntity(world, block.getMetaFromState(state));
        }
        return null;
    }

    public static boolean hasBlockTileEntity(Block block, BlockState state) {
        return block instanceof ITileEntityProvider;
    }

    public static boolean shouldRefresh(TileEntity tileEntity, World world, BlockPos pos, BlockState oldState, BlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public static void onTileChunkUnload(TileEntity tileEntity) {
        // Overwritten in SpongeForge
    }

    // World

    public static Iterator<Chunk> getChunkIterator(ServerWorld world) {
        return world.getPlayerChunkMap().getChunkIterator();
    }

    public static void registerPortalAgentType(@Nullable ForgeITeleporterBridge teleporter) {
        // Overwritten in SpongeForge
    }

    // World provider

    public static boolean canDoLightning(Dimension dimension, Chunk chunk) {
        return true;
    }

    public static boolean canDoRainSnowIce(Dimension dimension, Chunk chunk) {
        return true;
    }

    public static DimensionType getRespawnDimensionType(Dimension dimension, ServerPlayerEntity player) {
        return DimensionType.OVERWORLD;
    }

    public static BlockPos getRandomizedSpawnPoint(ServerWorld world) {
        BlockPos ret = world.getSpawnPoint();

        final boolean isAdventure = world.getWorldInfo().getGameType() == GameType.ADVENTURE;
        int spawnFuzz = Math.max(0, world.getServer().getSpawnRadius(world));
        final int border = MathHelper.floor(world.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if (border < spawnFuzz) {
            spawnFuzz = border;
        }

        if (!world.dimension.isNether() && !isAdventure && spawnFuzz != 0)
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

    public static void addItemStackToListForSpawning(Collection<ItemDropData> dropData, @Nullable ItemDropData drop) {
        // This is the hook that can be overwritten to handle merging the item stack into an already existing item stack
        if (drop != null) {
            dropData.add(drop);
        }
    }

    public static MapStorage getWorldMapStorage(World world) {
        return world.getMapStorage();
    }

    public static int countEntities(ServerWorld world, EntityClassification type, boolean forSpawnCount) {
        return world.countEntities(type.getCreatureClass());
    }

    public static int getMaxSpawnPackSize(MobEntity mob) {
        return mob.getMaxSpawnedInChunk();
    }

    public static SpawnerSpawnType canEntitySpawnHere(MobEntity mob, boolean entityNotColliding) {
        if (mob.getCanSpawnHere() && entityNotColliding) {
            return SpawnerSpawnType.NORMAL;
        }
        return SpawnerSpawnType.NONE;
    }

    @Nullable
    public static Object onUtilRunTask(FutureTask<?> task, Logger logger) {
        PhaseTracker phaseTracker = PhaseTracker.getInstance();
        try (BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(task))  {
            context.buildAndSwitch();
            Object o = Util.runTask(task, logger);
            return o;
        } catch (Exception e) {
            phaseTracker
                .printMessageWithCaughtException("Exception during phase body", "Something happened trying to run the main body of a phase", e);

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

    public static void onTileEntityChunkUnload(TileEntity tileEntity) {
        // forge only method
    }

    public static boolean canConnectRedstone(Block block, BlockState state, IBlockAccess world, BlockPos pos, @Nullable Direction side) {
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
        IRecipe recipe = CraftingManager.REGISTRY.getOrDefault(new ResourceLocation(id));
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
    public static RayTraceResult rayTraceEyes(LivingEntity entity, double length) {
        Vec3d startPos = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        Vec3d endPos = startPos.add(entity.getLookVec().scale(length));
        return entity.world.rayTraceBlocks(startPos, endPos);
    }

    public static boolean shouldKeepSpawnLoaded(DimensionType dimensionType) {
        final ServerWorld world = SpongeImpl.getWorldManager().getWorld(dimensionType);
        return world != null && ((WorldInfoBridge) world.getWorldInfo()).bridge$doesKeepSpawnLoaded();

    }

    public static void setKeepSpawnLoaded(DimensionType dimensionType, boolean keepSpawnLoaded) {
        // This is only used in SpongeForge
    }

    @Nullable
    public static BlockPos getBedLocation(PlayerEntity player, DimensionType dimensionType) {
        return ((PlayerEntityBridge) player).bridge$getBedLocation(dimensionType);
    }

    public static boolean isSpawnForced(PlayerEntity player, DimensionType dimensionType) {
        return ((PlayerEntityBridge) player).bridge$isSpawnForced(dimensionType);
    }

    public static Inventory toInventory(Object inventory, @Nullable Object forgeItemHandler) {
        SpongeImpl.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
        return null;
    }

    public static InventoryAdapter findInventoryAdapter(Object inventory) {
        SpongeImpl.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
        throw new IllegalArgumentException("Unknown inventory " + inventory.getClass().getName() + " report this to Sponge");
    }

    public static void onTileEntityInvalidate(TileEntity tileEntity) {
        te.remove();
    }

    public static void capturePerEntityItemDrop(PhaseContext<?> phaseContext, Entity owner, ItemEntity item) {
        phaseContext.getPerEntityItemEntityDropSupplier().get().put(owner.getUniqueID(), item);
    }

    /**
     * Gets the enchantment modifier for looting on the entity living base from the damage source, but in forge cases, we need to use their hooks.
     */
    public static int getLootingEnchantmentModifier(LivingEntity target, LivingEntity entity, DamageSource cause) {
        return EnchantmentHelper.getLootingModifier(entity);
    }

    public static double getWorldMaxEntityRadius(ServerWorld world) {
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

    public static void onUseItemTick(LivingEntity living, ItemStack stack, int activeItemStackUseCount) {
    }

    public static void onTETickStart(TileEntity tileEntity) {

    }

    public static void onTETickEnd(TileEntity tileEntity) {

    }

    public static void onEntityTickStart(Entity entity) {

    }

    public static void onEntityTickEnd(Entity entity) {

    }

    public static boolean isMainThread() {
        // Return true when the server isn't yet initialized, this means on a client
        // that the game is still being loaded. This is needed to support initialization
        // events with cause tracking.
        return !Sponge.isServerAvailable() || Sponge.getServer().onMainThread();
    }

    // Overridden by SpongeImplHooksMixin_ItemNameOverflowPrevention for exploit check
    public static boolean creativeExploitCheck(IPacket<?> packet, ServerPlayerEntity player) {
        return false;
    }

    public static String getImplementationId() {
        throw new UnsupportedOperationException("SpongeCommon does not have it's own ecosystem, this needs to be mixed into for implementations depending on SpongeCommon");
    }

    public static BlockEntityType getTileEntityType(Class<? extends TileEntity> tileEntityClass) {
        return SpongeImpl.getRegistry().getTranslated(tileEntityClass, BlockEntityType.class);
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
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
    public static void shouldCloseScreen(World world, BlockPos pos, @Nullable Object eventData, ServerPlayerEntity player) {
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     *
     * @param forgeEventObject The forge event object, if it was created
     * @return The result as a result of the event data
     */
    public static ActionResultType getInteractionCancellationResult(@Nullable Object forgeEventObject) {
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
    public static boolean doesItemSneakBypass(World worldIn, BlockPos pos, PlayerEntity player, net.minecraft.item.ItemStack heldItemMainhand,
        net.minecraft.item.ItemStack heldItemOffhand) {
        return heldItemMainhand.isEmpty() && heldItemOffhand.isEmpty();
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     */
    @Nullable
    public static ActionResultType getEnumResultForProcessRightClickBlock(ServerPlayerEntity player, InteractBlockEvent.Secondary event,
        ActionResultType result, World world, BlockPos pos, Hand hand) {
        return ActionResultType.FAIL;
    }

    /**
     * Eliminate an extra overwrite for SpongeForge for PlayerInteractionManagerMixin#processRightClickBlock.
     */
    public static ActionResultType onForgeItemUseFirst(PlayerEntity player, ItemStack stack, World world, BlockPos pos,
        Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return ActionResultType.PASS;
    }

    /**
     * Forge events are getting wrapped in various cases that end up causing corner cases where the effective side ends up being the client.
     *
     * <p>
     *     If this returns true, the event manager will handle the client event differently (or not at all)
     * </p>>
     */
    public static boolean isClientEvent(Object object) {
        return false;
    }


    /**
     * Forge has custom items, and normally, we throw an event for any and all listeners. The problem is that since Forge just blindly calls
     * events, and Sponge only throws events if there are listeners, the custom item hook does not get called for direct spawned entities,
     * so we need to explicitly call the custom item creation hooks here.
     */
    @Nullable
    public static Entity getCustomEntityIfItem(Entity entity) {
        return null;
    }

    /**
     * For use with {@link TileEntityActivation}.
     *
     * @param tileEntity The tile to tick
     * @return True whether to tick or false, not to
     */
    public static boolean shouldTickTile(ITickableTileEntity tileEntity) {
        return true;
    }

    /**
     * Used for compatibility with Forge where Forge uses wrapped Items since they allow for registry replacements.
     *
     * @param item The item
     * @return The resource location id
     */
    @Nullable
    public static ResourceLocation getItemResourceLocation(Item item) {
        return Item.REGISTRY.getKey(mixinItem_api);
    }

    public static void registerItemForSpongeRegistry(int id, ResourceLocation textualID, Item item) {
        ItemTypeRegistryModule.getInstance().registerAdditionalCatalog((ItemType) item);
    }

    public static void writeItemStackCapabilitiesToDataView(DataContainer container, ItemStack stack) {

    }

    public static boolean canEnchantmentBeAppliedToItem(Enchantment enchantment, ItemStack stack) {
        return enchantment.canApply(stack);
    }

    public static void setCapabilitiesFromSpongeBuilder(ItemStack stack, CompoundNBT compound) {

    }

    public static TileEntity onChunkGetTileDuringRemoval(ServerWorld world, BlockPos pos) {
        if (((WorldAccessor) world).accessor$getIsOutsideBuildHeight(pos)) {
            return null;
        } else {
            TileEntity tileentity2 = null;

            if (((WorldAccessor) world).accessor$getProcessingLoadedTiles()) {
                tileentity2 = ((WorldAccessor) world).accessPendingTileEntityAt(pos);
            }

            if (tileentity2 == null) {
                // Sponge - Instead of creating the tile entity, just check if it's there. If the
                // tile entity doesn't exist, don't create it since we're about to just wholesale remove it...
                // tileentity2 = this.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
                tileentity2 = world.getChunkAt(pos).getTileEntity(pos, Chunk.CreateEntityType.CHECK);
            }

            if (tileentity2 == null) {
                tileentity2 =  ((WorldAccessor) world).accessPendingTileEntityAt(pos);
            }

            return tileentity2;
        }
    }
}
