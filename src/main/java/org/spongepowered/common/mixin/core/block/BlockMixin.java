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
import net.minecraft.item.ItemStack;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.BlockTrackerCategory;
import org.spongepowered.common.config.category.BlockTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Block.class)
public abstract class BlockMixin implements BlockBridge, TrackableBridge, TimingBridge {

    private final boolean impl$isVanilla = getClass().getName().startsWith("net.minecraft.");
    private boolean impl$hasCollideLogic;
    private boolean impl$hasCollideWithStateLogic;
    // Used to determine if this block needs to be handled in WorldServer#addBlockEvent
    private boolean impl$shouldFireBlockEvents = true;
    @Nullable private Timing impl$timing;
    // Used by tracker config
    private boolean impl$allowsBlockBulkCapture = true;
    private boolean impl$allowsEntityBulkCapture = true;
    private boolean impl$allowsBlockEventCreation = true;
    private boolean impl$allowsEntityEventCreation = true;
    private boolean impl$hasNeighborOverride = false;

    @Shadow @Final protected BlockStateContainer blockState;

    @Shadow public abstract String getTranslationKey();
    @Shadow public abstract Material getMaterial(IBlockState state);
    @Shadow public abstract IBlockState shadow$getDefaultState();
    @Shadow public abstract void dropBlockAsItem(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int fortune);
    @Shadow public abstract BlockStateContainer getBlockState();
    @Shadow protected abstract Block setTickRandomly(boolean shouldTick);

    @Inject(method = "registerBlock(ILnet/minecraft/util/ResourceLocation;Lnet/minecraft/block/Block;)V", at = @At("RETURN"))
    private static void onRegisterBlock(final int id, final ResourceLocation location, final Block block, final CallbackInfo ci) {
        BlockTypeRegistryModule.getInstance().registerFromGameData(location.toString(), (BlockType) block);
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return false;
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        return Optional.empty();
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        return Optional.empty(); // By default, all blocks just have a single state unless otherwise dictated.
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        return ImmutableList.of();
    }

    @Override
    public ImmutableMap<Class<? extends Property<?, ?>>, Property<?, ?>> bridge$getProperties(final IBlockState blockState) {
        return populateSpongeProperties(ImmutableMap.builder(), blockState).build();
    }

    @SuppressWarnings("unchecked")
    private ImmutableMap.Builder<Class<? extends Property<?, ?>>, Property<?, ?>> populateSpongeProperties(
        final ImmutableMap.Builder<Class<? extends Property<?, ?>>, Property<?, ?>> builder, final IBlockState blockState) {
        for (final Property<?, ?> property : SpongeImpl.getPropertyRegistry().getPropertiesFor((BlockState) blockState)) {
            builder.put((Class<? extends Property<?, ?>>) property.getClass(), property);
        }
        return builder;
    }

    @Inject(method = "dropBlockAsItem", at = @At("HEAD"), cancellable = true)
    private void checkBlockDropForTransactions(final net.minecraft.world.World worldIn, final BlockPos pos, final IBlockState state, final int fortune,
        final CallbackInfo ci) {
        if (((WorldBridge) worldIn).bridge$isFake()) {
            return;
        }
        final SpongeProxyBlockAccess proxyAccess = ((WorldServerBridge) worldIn).bridge$getProxyAccess();
        if (proxyAccess.hasProxy() && proxyAccess.isProcessingTransactionWithNextHavingBreak(pos, state)) {
            ci.cancel();
        }
    }


    // Please, for the love of all that is good, do NOT re-order the following two injections, if you do, you end up causing errors
    // from Mixin complaining about a leaked CallbackInfo. More can be read here: https://github.com/SpongePowered/Mixin/issues/337
    @Inject(method = "spawnAsEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;setDefaultPickupDelay()V", shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true)
    private static void impl$attemptCaptureOrAllowSpawn(final net.minecraft.world.World worldIn, final BlockPos pos, final ItemStack stack,
        final CallbackInfo ci, final float unused, final double xOffset, final double yOffset, final double zOffset,
        final EntityItem toSpawn) {
        // Sponge Start - Tell the phase state to track this position, and then unset it.
        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();

        if (context.allowsBulkEntityCaptures() && context.allowsBlockPosCapturing()) {
            context.getCaptureBlockPos().setPos(pos);
            worldIn.func_72838_d(toSpawn);
            context.getCaptureBlockPos().setPos(null);
            ci.cancel();
        }
    }

    /**
     * @author gabizou - July 23rd, 2019 - 1.12
     * @reason Because adding a few redirects for the massive if
     * statement is less performant than doing the fail fast check
     * of "is main thread or are we restoring", before we reach the
     * {@link net.minecraft.world.World#isRemote} check or
     * {@link ItemStack#isEmpty()} check, we can eliminate a larger
     * majority of the hooks that would otherwise be required for
     * doing an overwrite of this method.
     *
     * @param worldIn The world
     * @param pos The position
     * @param stack The stack
     * @param ci Callbackinfo to cancel if we're not on the main thread or we're restoring
     */
    @Inject(method = "spawnAsEntity", at = @At("HEAD"), cancellable = true)
    private static void impl$checkMainThreadAndRestoring(final net.minecraft.world.World worldIn, final BlockPos pos, final ItemStack stack,
        final CallbackInfo ci) {
        if (!SpongeImplHooks.isMainThread() || PhaseTracker.getInstance().getCurrentState().isRestoring()) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnAsEntity",
        at = @At(value = "NEW", target = "net/minecraft/entity/item/EntityItem"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILSOFT,
        require = 0,
        expect = 0
    )
    private static void impl$throwConstructPreEvent(
        final net.minecraft.world.World worldIn, final BlockPos pos, final ItemStack stack, final CallbackInfo ci,
        final float unused, final double xOffset, final double yOffset, final double zOffset) {
        if (!ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            return;
        }
        final double xPos = (double) pos.func_177958_n() + xOffset;
        final double yPos = (double) pos.func_177956_o() + yOffset;
        final double zPos = (double) pos.func_177952_p() + zOffset;
        // Go ahead and throw the construction event
        final Transform<World> position = new Transform<>((World) worldIn, new Vector3d(xPos, yPos, zPos));
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(worldIn.func_180495_p(pos));
            final ConstructEntityEvent.Pre
                eventPre =
                SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), EntityTypes.ITEM, position);
            SpongeImpl.postEvent(eventPre);
            if (eventPre.isCancelled()) {
                ci.cancel();
            }
        }
    }

    // This method can be called directly by pistons, mods, etc. so the hook must go here
    @Inject(method = "dropBlockAsItemWithChance", at = @At("HEAD"), cancellable = true)
    private void onDropBlockAsItemWithChanceHead(final net.minecraft.world.World worldIn, final BlockPos pos, final IBlockState state,
        final float chance, final int fortune,
        final CallbackInfo ci) {
        if (!((WorldBridge) worldIn).bridge$isFake() && !SpongeImplHooks.isRestoringBlocks(worldIn)) {
            if (PhaseTracker.getInstance().getCurrentState().isRestoring()) {
                ci.cancel();
                return;
            }

            final WorldServerBridge mixinWorld = (WorldServerBridge) worldIn;
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final IPhaseState<?> currentState = phaseTracker.getCurrentState();
            final PhaseContext<?> currentContext = phaseTracker.getCurrentContext();
            final boolean shouldEnterBlockDropPhase = !currentContext.isCapturingBlockItemDrops() && !currentState.alreadyProcessingBlockItemDrops() && !currentState.isWorldGeneration();
            if (shouldEnterBlockDropPhase) {
                // TODO: Change source to LocatableBlock
                final PhaseContext<?> context = BlockPhase.State.BLOCK_DROP_ITEMS.createPhaseContext()
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
    private void onDropBlockAsItemWithChanceReturn(final net.minecraft.world.World worldIn, final BlockPos pos, final IBlockState state, final float chance, final int fortune,
        final CallbackInfo ci) {
        if (!((WorldBridge) worldIn).bridge$isFake() && !SpongeImplHooks.isRestoringBlocks(worldIn)) {
            if (this.data == null) {
                // means that we didn't need to capture before
                return;
            }
            this.data.close();
            this.data = null;
        }
    }

    @Override
    public boolean bridge$isVanilla() {
        return this.impl$isVanilla;
    }

    @Override
    public boolean bridge$hasCollideLogic() {
        return this.impl$hasCollideLogic;
    }

    @Override
    public boolean bridge$hasCollideWithStateLogic() {
        return this.impl$hasCollideWithStateLogic;
    }

    @Override
    public boolean bridge$hasNeighborChangedLogic() {
        return this.impl$hasNeighborOverride;
    }

    @Override
    public Timing bridge$getTimingsHandler() {
        if (this.impl$timing == null) {
            this.impl$timing = SpongeTimings.getBlockTiming((net.minecraft.block.Block) (Object) this);
        }
        return this.impl$timing;
    }

    @Override
    public boolean bridge$allowsBlockBulkCapture() {
        return this.impl$allowsBlockBulkCapture;
    }

    @Override
    public boolean bridge$allowsEntityBulkCapture() {
        return this.impl$allowsEntityBulkCapture;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.impl$allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.impl$allowsEntityEventCreation;
    }

    @Override
    public void bridge$refreshTrackerStates() {
        // not needed
    }

    /**
     * Used to determine if this block should fire
     * sponge events during WorldServer#addBlockEvent.
     */
    @Override
    public boolean bridge$shouldFireBlockEvents() {
        return this.impl$shouldFireBlockEvents;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void bridge$initializeTrackerState() {
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
        if ((Block) (Object) this instanceof BlockMobSpawner || (Block) (Object) this instanceof BlockEnderChest
            || (Block) (Object) this instanceof BlockChest || (Block) (Object) this instanceof BlockShulkerBox
            || (Block) (Object) this instanceof BlockEndGateway || (Block) (Object) this instanceof BlockBeacon) {
            this.impl$shouldFireBlockEvents = false;
        }
        // Determine which blocks can avoid executing un-needed event logic
        // This will allow us to avoid running event logic for blocks that do nothing such as grass collisions
        // -- blood
        // @author gabizou - October 9th, 2018
        // @reason Due to early class initialization and object instantiation, a lot of the reflection access
        // logic can be delayed until actual type registration with sponge. This will at the very least allow
        // mod type registrations to go through without getting the overall cost of reflection during object construction.

        this.impl$hasCollideLogic = true;
        this.impl$hasCollideWithStateLogic = true;

        // onEntityCollidedWithBlock
        try {
            final String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityWalk" : "func_176199_a";
            final Class<?>[] argTypes = {net.minecraft.world.World.class, BlockPos.class, Entity.class };
            final Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.impl$hasCollideLogic = false;
            }
        } catch (final NoClassDefFoundError err) {
            //noinspection EqualsBetweenInconvertibleTypes
            this.impl$hasCollideLogic = !this.getClass().equals(Block.class);
        } catch (final Throwable ex) {
            // ignore
        }

        // onEntityCollision (IBlockState)
        try {
            final String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityCollision" : "func_180634_a";
            final Class<?>[] argTypes = {net.minecraft.world.World.class, BlockPos.class, IBlockState.class, Entity.class };
            final Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.impl$hasCollideWithStateLogic = false;
            }
        } catch (final NoClassDefFoundError err) {
            //noinspection EqualsBetweenInconvertibleTypes
            this.impl$hasCollideWithStateLogic = !this.getClass().equals(Block.class);
        } catch (final Throwable ex) {
            // ignore
        }
        // neighborChanged
        try {
            final String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "neighborChanged" : "func_189540_a";
            final Class<?>[] argTypes = {IBlockState.class, net.minecraft.world.World.class, BlockPos.class, Block.class, BlockPos.class};
            final Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            this.impl$hasNeighborOverride = !clazz.equals(Block.class);
        } catch (final Throwable e) {
            if (e instanceof NoClassDefFoundError) {
                // fall back to checking if class equals Block.
                // Fixes https://github.com/SpongePowered/SpongeForge/issues/2770
                //noinspection EqualsBetweenInconvertibleTypes
                this.impl$hasNeighborOverride = !this.getClass().equals(Block.class);
            }
        }

        if (!blockTrackerModCat.isEnabled()) {
            this.impl$allowsBlockBulkCapture = false;
            this.impl$allowsEntityBulkCapture = false;
            this.impl$allowsBlockEventCreation = false;
            this.impl$allowsEntityEventCreation = false;
            blockTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsBlockBulkCapture);
            blockTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsEntityBulkCapture);
            blockTrackerModCat.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsBlockEventCreation);
            blockTrackerModCat.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsEntityEventCreation);
        } else {
            this.impl$allowsBlockBulkCapture = blockTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
            this.impl$allowsEntityBulkCapture = blockTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
            this.impl$allowsBlockEventCreation = blockTrackerModCat.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
            this.impl$allowsEntityEventCreation = blockTrackerModCat.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
        }

        if (blockTrackerCat.autoPopulateData()) {
            trackerConfigAdapter.save();
        }
    }
}
