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

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockLog;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.BlockTrackerCategory;
import org.spongepowered.common.config.category.BlockTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
@Implements(@Interface(iface = BlockType.class, prefix = "block$"))
public abstract class MixinBlock implements BlockType, IMixinBlock {

    private final boolean isVanilla = getClass().getName().startsWith("net.minecraft.");
    private boolean hasCollideLogic;
    private boolean hasCollideWithStateLogic;
    // Only needed for blocks that do not fire ChangeBlockEvent.Pre
    private boolean requiresBlockCapture = true;
    private static boolean canCaptureItems = true;
    private Timing timing;
    // Used by tracker config
    private boolean allowsBlockBulkCapture = true;
    private boolean allowsEntityBulkCapture = true;
    private boolean allowsBlockEventCreation = true;
    private boolean allowsEntityEventCreation = true;

    @Shadow private boolean needsRandomTick;
    @Shadow protected SoundType blockSoundType;
    @Shadow @Final protected BlockStateContainer blockState;

    @Shadow public abstract String getTranslationKey();
    @Shadow public abstract Material getMaterial(IBlockState state);
    @Shadow public abstract IBlockState shadow$getDefaultState();
    @Shadow public abstract boolean shadow$getTickRandomly();
    @Shadow public abstract void dropBlockAsItem(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int fortune);

    @Shadow public abstract BlockStateContainer getBlockState();

    @Shadow protected boolean enableStats;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstruction(CallbackInfo ci) {
        // Determine which blocks can avoid executing un-needed event logic
        // This will allow us to avoid running event logic for blocks that do nothing such as grass collisions
        // -- blood

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
        } catch (Throwable ex) {
            // ignore
        }

        Block block = (Block) (Object) this;
        if (block instanceof BlockLeaves || block instanceof BlockLog || block instanceof BlockGrass || block instanceof BlockLiquid) {
            this.requiresBlockCapture = false;
        }
    }

    @Inject(method = "registerBlock(ILnet/minecraft/util/ResourceLocation;Lnet/minecraft/block/Block;)V", at = @At("RETURN"))
    private static void onRegisterBlock(int id, ResourceLocation location, Block block, CallbackInfo ci) {
        BlockTypeRegistryModule.getInstance().registerFromGameData(location.toString(), (BlockType) block);
    }

    @Override
    public String getId() {
        return this.getNameFromRegistry();
    }

    @Override
    public String getName() {
        return this.getNameFromRegistry();
    }

    private String getNameFromRegistry() {
        // This should always succeed when things are working properly,
        // so we just catch the exception instead of doing a null check.
        try {
            return Block.REGISTRY.getNameForObject((Block) (Object) this).toString();
        } catch (NullPointerException e) {
            throw new RuntimeException(String.format("Block '%s' (class '%s') is not registered with the block registry! This is likely a bug in the corresponding mod.", this, this.getClass().getName()), e);
        }
    }

    @Override
    public BlockState getDefaultState() {
        return (BlockState) shadow$getDefaultState();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<BlockState> getAllBlockStates() {
        return (Collection<BlockState>) (Collection<?>) this.blockState.getValidStates();
    }

    @Override
    public Optional<ItemType> getItem() {
        ItemType itemType = (ItemType) Item.getItemFromBlock((Block) (Object) this);
        return Items.AIR.equals(itemType) ? Optional.empty() : Optional.of(itemType);
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getTranslationKey() + ".name");
    }

    @Intrinsic
    public boolean block$getTickRandomly() {
        return shadow$getTickRandomly();
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
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

    @Override
    public BlockState getDefaultBlockState() {
        return getDefaultState();
    }

    @Override
    public Collection<BlockTrait<?>> getTraits() {
        return getDefaultBlockState().getTraits();
    }

    @Override
    public Optional<BlockTrait<?>> getTrait(String blockTrait) {
        return getDefaultBlockState().getTrait(blockTrait);
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

        if (worldIn.isRemote || !SpongeImplHooks.isMainThread() || stack.isEmpty() || !doTileDrops) {
            return;
        }
        // Double check we aren't performing drops during restores.
        if (PhaseTracker.getInstance().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS) {
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
        if (!((IMixinWorld) worldIn).isFake()) {
            if (PhaseTracker.getInstance().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS) {
                ci.cancel();
                return;
            }

            final IMixinWorldServer mixinWorld = (IMixinWorldServer) worldIn;
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final IPhaseState<?> currentState = phaseTracker.getCurrentState();
            final PhaseContext<?> currentContext = phaseTracker.getCurrentContext();
            final boolean shouldEnterBlockDropPhase = !currentContext.isCapturingBlockItemDrops() && !currentState.alreadyProcessingBlockItemDrops() && !currentState.isWorldGeneration();
            if (shouldEnterBlockDropPhase) {
                // TODO: Change source to LocatableBlock
                PhaseContext<?> context = BlockPhase.State.BLOCK_DROP_ITEMS.createPhaseContext()
                        .source(mixinWorld.createSpongeBlockSnapshot(state, state, pos, BlockChangeFlags.PHYSICS_OBSERVER));
                // use current notifier and owner if available
                currentContext.applyNotifierIfAvailable(context::notifier);
                currentContext.applyOwnerIfAvailable(context::owner);
                context.buildAndSwitch();
            }
        }
    }

    @Nullable private PhaseData data = null; // Soft reference for the methods between this

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "RETURN"), cancellable = true)
    private void onDropBlockAsItemWithChanceReturn(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune,
        CallbackInfo ci) {
        if (!((IMixinWorld) worldIn).isFake()) {
            if (this.data == null) {
                // means that we didn't need to capture before
                return;
            }
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            if (phaseTracker.getCurrentPhaseData() != this.data) {
                // illegal state exception maybe?
                this.data = null;
                return;
            }
            final PhaseData data = this.data;
            final IPhaseState<?> currentState = data.state;
            final boolean shouldEnterBlockDropPhase = !data.context.isCapturingBlockItemDrops() && !currentState.alreadyProcessingBlockItemDrops() && !currentState.isWorldGeneration();
            if (shouldEnterBlockDropPhase) {
                phaseTracker.getCurrentContext().close();
            }
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
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getBlockTiming((net.minecraft.block.Block) (Object) this);
        }
        return this.timing;
    }

    @Override
    public boolean requiresBlockCapture() {
        return this.requiresBlockCapture;
    }

    @Override
    public BlockSoundGroup getSoundGroup() {
        return (BlockSoundGroup) this.blockSoundType;
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
    public void refreshCache() {
        // not needed
    }

    @Override
    public void initializeTrackerState() {
        SpongeConfig<TrackerConfig> trackerConfig = SpongeImpl.getTrackerConfig();
        BlockTrackerCategory blockTracker = trackerConfig.getConfig().getBlockTracker();
        String[] ids = this.getId().split(":");
        if (ids.length != 2) {
            final PrettyPrinter printer = new PrettyPrinter(60).add("Malformatted Block ID discovered!").centre().hr()
                .addWrapped(60, "Sponge has found a malformatted block id when trying to"
                                + " load configurations for the block id. The printed out block id"
                                + "is not originally from sponge, and should be brought up with the"
                                + "mod developer as the registration for this block is not likely"
                                + "to work with other systems and assumptions of having a properly"
                                + "formatted block id.")
                .add("%s : %s", "Malformed ID", this.getId())
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

        BlockTrackerModCategory modCapturing = blockTracker.getModMappings().get(modId);

        if (modCapturing == null) {
            modCapturing = new BlockTrackerModCategory();
            blockTracker.getModMappings().put(modId, modCapturing);
        }

        if (!modCapturing.isEnabled()) {
            this.allowsBlockBulkCapture = false;
            this.allowsEntityBulkCapture = false;
            this.allowsBlockEventCreation = false;
            this.allowsEntityEventCreation = false;
            modCapturing.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsBlockBulkCapture);
            modCapturing.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsEntityBulkCapture);
            modCapturing.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsBlockEventCreation);
            modCapturing.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsEntityEventCreation);
        } else {
            this.allowsBlockBulkCapture = modCapturing.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsEntityBulkCapture = modCapturing.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsBlockEventCreation = modCapturing.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsEntityEventCreation = modCapturing.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> true);
        }

        if (blockTracker.autoPopulateData()) {
            trackerConfig.save();
        }
    }
}
