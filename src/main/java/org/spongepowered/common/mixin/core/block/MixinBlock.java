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
package org.spongepowered.common.mixin.core.block;

import co.aikar.timings.Timing;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEndGateway;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.BlockTrackerCategory;
import org.spongepowered.common.config.category.BlockTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
public abstract class MixinBlock implements BlockBridge {

    private final boolean isVanilla = getClass().getName().startsWith("net.minecraft.");
    private boolean hasCollideLogic;
    private boolean hasCollideWithStateLogic;
    // Used to determine if this block needs to be handled in WorldServer#addBlockEvent
    private boolean shouldFireBlockEvents = true;
    private static boolean canCaptureItems = true;
    private Timing timing;
    // Used by tracker config
    private boolean allowsBlockBulkCapture = true;
    private boolean allowsEntityBulkCapture = true;
    private boolean allowsBlockEventCreation = true;
    private boolean allowsEntityEventCreation = true;
    private boolean hasNeighborOverride = false;

    @Shadow @Final protected BlockStateContainer blockState;

    @Shadow public abstract String getTranslationKey();
    @Shadow public abstract Material getMaterial(IBlockState state);
    @Shadow public abstract IBlockState shadow$getDefaultState();
    @Shadow public abstract void dropBlockAsItem(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int fortune);
    @Shadow public abstract BlockStateContainer getBlockState();
    @Shadow protected abstract Block setTickRandomly(boolean shouldTick);

    @Inject(method = "registerBlock(ILnet/minecraft/util/ResourceLocation;Lnet/minecraft/block/Block;)V", at = @At("RETURN"))
    private static void onRegisterBlock(int id, ResourceLocation location, Block block, CallbackInfo ci) {
        BlockTypeRegistryModule.getInstance().registerFromGameData(location.toString(), (BlockType) block);
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return false;
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        return Optional.empty();
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        return Optional.empty(); // By default, all blocks just have a single state unless otherwise dictated.
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.of();
    }

    @Override
    public ImmutableMap<Class<? extends Property<?, ?>>, Property<?, ?>> getProperties(IBlockState blockState) {
        return populateSpongeProperties(ImmutableMap.builder(), blockState).build();
    }

    @SuppressWarnings("unchecked")
    private ImmutableMap.Builder<Class<? extends Property<?, ?>>, Property<?, ?>> populateSpongeProperties(
        ImmutableMap.Builder<Class<? extends Property<?, ?>>, Property<?, ?>> builder, IBlockState blockState) {
        for (Property<?, ?> property : SpongeImpl.getPropertyRegistry().getPropertiesFor((BlockState) blockState)) {
            builder.put((Class<? extends Property<?, ?>>) property.getClass(), property);
        }
        return builder;
    }

    @Inject(method = "dropBlockAsItem", at = @At("HEAD"), cancellable = true)
    private void checkBlockDropForTransactions(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int fortune, CallbackInfo ci) {
        if (((WorldBridge) worldIn).isFake()) {
            return;
        }
        final SpongeProxyBlockAccess proxyAccess = ((ServerWorldBridge) worldIn).bridge$getProxyAccess();
        if (proxyAccess.hasProxy() && proxyAccess.isProcessingTransactionWithNextHavingBreak(pos, state)) {
            ci.cancel();
        }
    }

    @Inject(method = "harvestBlock", at = @At(value = "HEAD"))
    private void onHarvestBlockHead(net.minecraft.world.World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te,
        @Nullable ItemStack stack, CallbackInfo ci) {
        // ExtraUtilities 2 uses a fake player to mine blocks for its Quantum Quarry and captures all block drops.
        // It also expects block drops to trigger an event during the quarry TE tick. As our captures are processed
        // post tick, we must avoid capturing to ensure the quarry can capture items properly.
        // If a fake player is detected with an item in hand, avoid captures
        if (stack != null && SpongeImplHooks.isFakePlayer(player) && player.getHeldItemMainhand() != null && !player.getHeldItemMainhand().isEmpty()) {
            canCaptureItems = false;
        }
    }

    @Inject(method = "harvestBlock", at = @At(value = "RETURN"))
    private void onHarvestBlockReturn(net.minecraft.world.World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
        @Nullable TileEntity te, @Nullable ItemStack stack, CallbackInfo ci) {
        canCaptureItems = true;
    }

    /**
     * @author gabizou - April 19th, 2018
     * @reason With the amount of redirects and events needed to be thrown here,
     * we overwrite the method in it's entirety (also bypassing forge's block captures
     * to sync up with sponge's captures).
     *
     * @param worldIn
     * @param pos
     * @param stack
     */
    @Overwrite
    public static void spawnAsEntity(net.minecraft.world.World worldIn, BlockPos pos, ItemStack stack) {
        // Sponge Start - short circuit up top to reduce indentation as necessary
        final boolean doTileDrops = worldIn.getGameRules().getBoolean("doTileDrops");

        if (worldIn.isRemote || !SpongeImplHooks.isMainThread() || stack.isEmpty() || !doTileDrops || SpongeImplHooks.isRestoringBlocks(worldIn)) {
            return;
        }
        // Double check we aren't performing drops during restores.
        if (PhaseTracker.getInstance().getCurrentState().isRestoring()) {
            return;
        }
        // Sponge Start - make some of these local variables so we have them prepped already.
        double xOffset = (double) (worldIn.rand.nextFloat() * 0.5F) + 0.25D;
        double yOffset = (double) (worldIn.rand.nextFloat() * 0.5F) + 0.25D;
        double zOffset = (double) (worldIn.rand.nextFloat() * 0.5F) + 0.25D;
        final double xPos = (double) pos.getX() + xOffset;
        final double yPos = (double) pos.getY() + yOffset;
        final double zPos = (double) pos.getZ() + zOffset;

        // TODO - Determine whether DropItemEvent.Pre is supposed to spawn here.

        // Go ahead and throw the construction event
        Transform<World> position = new Transform<>((World) worldIn, new Vector3d(xPos, yPos, zPos));
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(worldIn.getBlockState(pos));
            final ConstructEntityEvent.Pre eventPre = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), EntityTypes.ITEM, position);
            SpongeImpl.postEvent(eventPre);
            if (eventPre.isCancelled()) {
                return;
            }
        }
        EntityItem entityitem = new EntityItem(worldIn, xPos, yPos, zPos, stack);
        entityitem.setDefaultPickupDelay();
        // Sponge Start - Tell the phase state to track this position, and then unset it.
        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();

        if (context.allowsBulkEntityCaptures() && context.allowsBlockPosCapturing()) {
            context.getCaptureBlockPos().setPos(pos);
            worldIn.spawnEntity(entityitem);
            context.getCaptureBlockPos().setPos(null);
            return;
        }
        // Sponge End - if we're not capturing positions, then just go ahead and proceed as normal
        worldIn.spawnEntity(entityitem);

    }

    // This method can be called directly by pistons, mods, etc. so the hook must go here
    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "HEAD"), cancellable = true)
    private void onDropBlockAsItemWithChanceHead(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune,
        CallbackInfo ci) {
        if (!((WorldBridge) worldIn).isFake() && !SpongeImplHooks.isRestoringBlocks(worldIn)) {
            if (PhaseTracker.getInstance().getCurrentState().isRestoring()) {
                ci.cancel();
                return;
            }

            final ServerWorldBridge mixinWorld = (ServerWorldBridge) worldIn;
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final IPhaseState<?> currentState = phaseTracker.getCurrentState();
            final PhaseContext<?> currentContext = phaseTracker.getCurrentContext();
            final boolean shouldEnterBlockDropPhase = !currentContext.isCapturingBlockItemDrops() && !currentState.alreadyProcessingBlockItemDrops() && !currentState.isWorldGeneration();
            if (shouldEnterBlockDropPhase) {
                // TODO: Change source to LocatableBlock
                PhaseContext<?> context = BlockPhase.State.BLOCK_DROP_ITEMS.createPhaseContext()
                        .source(mixinWorld.bridge$createSnapshot(state, state, pos, BlockChangeFlags.PHYSICS_OBSERVER));
                // use current notifier and owner if available
                currentContext.applyNotifierIfAvailable(context::notifier);
                currentContext.applyOwnerIfAvailable(context::owner);
                context.buildAndSwitch();
                this.data = context;
            }
        }
    }

    @Nullable private PhaseContext<?> data = null; // Soft reference for the methods between this

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "RETURN"), cancellable = true)
    private void onDropBlockAsItemWithChanceReturn(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune,
        CallbackInfo ci) {
        if (!((WorldBridge) worldIn).isFake() && !SpongeImplHooks.isRestoringBlocks(worldIn)) {
            if (this.data == null) {
                // means that we didn't need to capture before
                return;
            }
            this.data.close();
            this.data = null;
        }
    }

    @Override
    public boolean isVanilla() {
        return this.isVanilla;
    }

    @Override
    public boolean hasCollideLogic() {
        return this.hasCollideLogic;
    }

    @Override
    public boolean hasCollideWithStateLogic() {
        return this.hasCollideWithStateLogic;
    }

    @Override
    public boolean hasNeighborChangedLogic() {
        return this.hasNeighborOverride;
    }

    @Override
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getBlockTiming((net.minecraft.block.Block) (Object) this);
        }
        return this.timing;
    }

    @Override
    public boolean allowsBlockBulkCapture() {
        return this.allowsBlockBulkCapture;
    }

    @Override
    public boolean allowsEntityBulkCapture() {
        return this.allowsEntityBulkCapture;
    }

    @Override
    public boolean allowsBlockEventCreation() {
        return this.allowsBlockEventCreation;
    }

    @Override
    public boolean allowsEntityEventCreation() {
        return this.allowsEntityEventCreation;
    }

    @Override
    public void refreshTrackerStates() {
        // not needed
    }

    /**
     * Used to determine if this block should fire 
     * sponge events during WorldServer#addBlockEvent.
     */
    @Override 
    public boolean shouldFireBlockEvents() {
        return this.shouldFireBlockEvents;
    }

    @Override
    public void initializeTrackerState() {
        final SpongeConfig<TrackerConfig> trackerConfigAdapter = SpongeImpl.getTrackerConfigAdapter();
        final BlockTrackerCategory blockTrackerCat = trackerConfigAdapter.getConfig().getBlockTracker();
        String[] ids = ((BlockType) this).getId().split(":");
        if (ids.length != 2) {
            final PrettyPrinter printer = new PrettyPrinter(60).add("Malformatted Block ID discovered!").centre().hr()
                .addWrapped(60, "Sponge has found a malformatted block id when trying to"
                                + " load configurations for the block id. The printed out block id"
                                + "is not originally from sponge, and should be brought up with the"
                                + "mod developer as the registration for this block is not likely"
                                + "to work with other systems and assumptions of having a properly"
                                + "formatted block id.")
                .add("%s : %s", "Malformed ID", ((BlockType) this).getId())
                .add("%s : %s", "Discovered id array", ids)
                .add();
            final String id = ids[0];
            ids = new String[]{"unknown", id};
            printer
                .add("Sponge will attempt to work around this by using the provided generated id:")
                .add("%s : %s", "Generated ID", Arrays.toString(ids))
                .log(SpongeImpl.getLogger(), Level.WARN);

        }
        final String modId = ids[0];
        final String name = ids[1];

        BlockTrackerModCategory blockTrackerModCat = blockTrackerCat.getModMappings().get(modId);

        if (blockTrackerModCat == null) {
            blockTrackerModCat = new BlockTrackerModCategory();
            blockTrackerCat.getModMappings().put(modId, blockTrackerModCat);
        }

        // Determine if this block needs to be handled during WorldServer#addBlockEvent
        if (((Block) (Object) this) instanceof BlockMobSpawner || ((Block) (Object) this) instanceof BlockEnderChest
            || ((Block) (Object) this) instanceof BlockChest || ((Block) (Object) this) instanceof BlockShulkerBox
            || ((Block) (Object) this)instanceof BlockEndGateway || ((Block) (Object) this) instanceof BlockBeacon) {
            this.shouldFireBlockEvents = false;
        }
        // Determine which blocks can avoid executing un-needed event logic
        // This will allow us to avoid running event logic for blocks that do nothing such as grass collisions
        // -- blood
        // @author gabizou - October 9th, 2018
        // @reason Due to early class initialization and object instantiation, a lot of the reflection access
        // logic can be delayed until actual type registration with sponge. This will at the very least allow
        // mod type registrations to go through without getting the overall cost of reflection during object construction.

        this.hasCollideLogic = true;
        this.hasCollideWithStateLogic = true;

        // onEntityCollidedWithBlock
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityWalk" : "func_176199_a";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, Entity.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.hasCollideLogic = false;
            }
        } catch (NoClassDefFoundError err) {
            //noinspection EqualsBetweenInconvertibleTypes
            this.hasCollideLogic = !this.getClass().equals(Block.class);
        } catch (Throwable ex) {
            // ignore
        }

        // onEntityCollision (IBlockState)
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityCollision" : "func_180634_a";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, IBlockState.class, Entity.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.hasCollideWithStateLogic = false;
            }
        } catch (NoClassDefFoundError err) {
            //noinspection EqualsBetweenInconvertibleTypes
            this.hasCollideWithStateLogic = !this.getClass().equals(Block.class);
        } catch (Throwable ex) {
            // ignore
        }
        // neighborChanged
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "neighborChanged" : "func_189540_a";
            Class<?>[] argTypes = {IBlockState.class, net.minecraft.world.World.class, BlockPos.class, Block.class, BlockPos.class};
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            this.hasNeighborOverride = !clazz.equals(Block.class);
        } catch (Throwable e) {
            if (e instanceof NoClassDefFoundError) {
                // fall back to checking if class equals Block.
                // Fixes https://github.com/SpongePowered/SpongeForge/issues/2770
                //noinspection EqualsBetweenInconvertibleTypes
                this.hasNeighborOverride = !this.getClass().equals(Block.class);
            }
        }

        if (!blockTrackerModCat.isEnabled()) {
            this.allowsBlockBulkCapture = false;
            this.allowsEntityBulkCapture = false;
            this.allowsBlockEventCreation = false;
            this.allowsEntityEventCreation = false;
            blockTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsBlockBulkCapture);
            blockTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsEntityBulkCapture);
            blockTrackerModCat.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsBlockEventCreation);
            blockTrackerModCat.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsEntityEventCreation);
        } else {
            this.allowsBlockBulkCapture = blockTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsEntityBulkCapture = blockTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsBlockEventCreation = blockTrackerModCat.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsEntityEventCreation = blockTrackerModCat.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> true);
        }

        if (blockTrackerCat.autoPopulateData()) {
            trackerConfigAdapter.save();
        }
    }
}
