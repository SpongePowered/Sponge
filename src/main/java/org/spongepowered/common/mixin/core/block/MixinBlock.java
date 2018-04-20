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
import com.google.common.collect.Multimap;
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
import net.minecraft.world.GameRules;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.VecHelper;

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

    @Shadow private boolean needsRandomTick;
    @Shadow protected SoundType blockSoundType;
    @Shadow @Final protected BlockStateContainer blockState;

    @Shadow public abstract String getUnlocalizedName();
    @Shadow public abstract Material getMaterial(IBlockState state);
    @Shadow public abstract IBlockState shadow$getDefaultState();
    @Shadow public abstract boolean shadow$getTickRandomly();
    @Shadow public abstract void dropBlockAsItem(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int fortune);

    @Shadow public abstract BlockStateContainer getBlockState();

    @Shadow protected boolean enableStats;

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
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

        // onEntityCollidedWithBlock (IBlockState)
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityCollidedWithBlock" : "func_180634_a";
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
        return Block.REGISTRY.getNameForObject((Block) (Object) this).toString();
    }

    @Override
    public String getName() {
        return Block.REGISTRY.getNameForObject((Block) (Object) this).toString();
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
        return new SpongeTranslation(getUnlocalizedName() + ".name");
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
    public void onHarvestBlockHead(net.minecraft.world.World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack, CallbackInfo ci) {
        // ExtraUtilities 2 uses a fake player to mine blocks for its Quantum Quarry and captures all block drops.
        // It also expects block drops to trigger an event during the quarry TE tick. As our captures are processed
        // post tick, we must avoid capturing to ensure the quarry can capture items properly.
        // If a fake player is detected with an item in hand, avoid captures
        if (stack != null && SpongeImplHooks.isFakePlayer(player) && player.getHeldItemMainhand() != null) {
            canCaptureItems = false;
        }
    }

    @Inject(method = "harvestBlock", at = @At(value = "RETURN"))
    public void onHarvestBlockReturn(net.minecraft.world.World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack, CallbackInfo ci) {
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

        if (worldIn.isRemote || !SpongeImpl.isMainThread() || stack.isEmpty() || doTileDrops) {
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
        worldIn.spawnEntity(entityitem);

    }

    // This method can be called directly by pistons, mods, etc. so the hook must go here
    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "HEAD"), cancellable = true)
    public void onDropBlockAsItemWithChanceHead(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune, CallbackInfo ci) {
        if (!((IMixinWorld) worldIn).isFake()) {
            if (PhaseTracker.getInstance().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS) {
                ci.cancel();
                return;
            }

            final IMixinWorldServer mixinWorld = (IMixinWorldServer) worldIn;
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final IPhaseState<?> currentState = phaseTracker.getCurrentState();
            final boolean shouldEnterBlockDropPhase = !phaseTracker.getCurrentContext().isCapturingBlockItemDrops() && !currentState.alreadyCapturingItemSpawns() && !currentState.isWorldGeneration();
            if (shouldEnterBlockDropPhase) {
                // TODO: Change source to LocatableBlock
                PhaseContext<?> context = BlockPhase.State.BLOCK_DROP_ITEMS.createPhaseContext()
                        .source(mixinWorld.createSpongeBlockSnapshot(state, state, pos, BlockChangeFlags.PHYSICS_OBSERVER));

                // unused, to be removed and re-located when phase context is cleaned up
                //.add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_FORTUNE, fortune))
                //.add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_POSITION, pos));
                // use current notifier and owner if available
                User notifier = phaseTracker.getCurrentContext().getNotifier().orElse(null);
                User owner = phaseTracker.getCurrentContext().getOwner().orElse(null);
                if (notifier != null) {
                    context.notifier(notifier);
                }
                if (owner != null) {
                    context.owner(owner);
                }
                context.buildAndSwitch();
            }
        }
    }

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "RETURN"), cancellable = true)
    private void onDropBlockAsItemWithChanceReturn(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune,
        CallbackInfo ci) {
        if (!((IMixinWorld) worldIn).isFake()) {
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final IPhaseState<?> currentState = phaseTracker.getCurrentState();
            final boolean shouldEnterBlockDropPhase = !phaseTracker.getCurrentContext().isCapturingBlockItemDrops() && !currentState.alreadyCapturingItemSpawns() && !currentState.isWorldGeneration();
            if (shouldEnterBlockDropPhase) {
                phaseTracker.completePhase(BlockPhase.State.BLOCK_DROP_ITEMS);
            }
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
}
