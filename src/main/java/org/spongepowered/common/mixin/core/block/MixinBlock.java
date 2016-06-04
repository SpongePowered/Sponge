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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.ItemDropData;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
@Implements(@Interface(iface = BlockType.class, prefix = "block$"))
public abstract class MixinBlock implements BlockType, IMixinBlock {

    private final boolean isVanilla = getClass().getName().startsWith("net.minecraft.");
    private Timing timing;

    @Shadow private boolean needsRandomTick;

    @Shadow public abstract String getUnlocalizedName();
    @Shadow public abstract Material getMaterial(IBlockState state);
    @Shadow(prefix = "shadow$")
    public abstract IBlockState shadow$getDefaultState();

    @Inject(method = "registerBlock", at = @At("RETURN"))
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

    @Override
    public Optional<ItemType> getItem() {
        return Optional.ofNullable((ItemType) Item.getItemFromBlock((Block) (Object) this));
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getUnlocalizedName() + ".name");
    }

    @Intrinsic
    public boolean block$getTickRandomly() {
        return this.getTickRandomly();
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callRandomTickEvent(net.minecraft.world.World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (world.isRemote) {
            return;
        }

        BlockSnapshot blockSnapshot = ((World) world).createSnapshot(VecHelper.toVector3i(pos));
        final TickBlockEvent event = SpongeEventFactory.createTickBlockEvent(Cause.of(NamedCause.source(world)), blockSnapshot);
        SpongeImpl.postEvent(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
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

    @Redirect(method = "dropBlockAsItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropBlockAsItemWithChance(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;FI)V"))
    private void onDropItemWithFortuneChances(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (world instanceof IMixinWorldServer) {
            final IMixinWorldServer mixinWorld = (IMixinWorldServer) world;
            final CauseTracker causeTracker = mixinWorld.getCauseTracker();
            final IPhaseState currentState = causeTracker.getStack().peekState();
            if (!currentState.getPhase().alreadyCapturingItemSpawns(currentState)) {
                causeTracker.switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.BLOCK_DROP_ITEMS, PhaseContext.start()
                        .add(NamedCause.source(mixinWorld.createSpongeBlockSnapshot(state, state, pos, 4)))
                        .addBlockCaptures()
                        .addEntityCaptures()
                        .add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_FORTUNE, fortune))
                        .add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_POSITION, pos))
                        .complete());
            }
            block.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
            if (!currentState.getPhase().alreadyCapturingItemSpawns(currentState)) {
                causeTracker.completePhase();
            }
            return;
        }
        block.dropBlockAsItemWithChance(world, pos, state, chance, fortune);

    }

    @Inject(method = "spawnAsEntity", at = @At(value = "NEW", args = {"class=net/minecraft/entity/item/EntityItem"}), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void checkSpawnAsEntity(net.minecraft.world.World worldIn, BlockPos pos, ItemStack stack, CallbackInfo callbackInfo, float chance, double x, double y, double z) {
        Transform<World> position = new Transform<>((World) worldIn, new Vector3d(x, y, z));
        EventConsumer.event(SpongeEventFactory.createConstructEntityEventPre(Cause.source(worldIn.getBlockState(pos)).build(), EntityTypes.ITEM, position))
            .cancelled(event -> callbackInfo.cancel())
            .process();
    }

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "HEAD"), cancellable = true)
    public void onDropBlockAsItemWithChance(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune, CallbackInfo ci) {
        if (!worldIn.isRemote && ((IMixinWorldServer) worldIn).getCauseTracker().getStack().peekState() == BlockPhase.State.RESTORING_BLOCKS) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnAsEntity", at = @At(value = "HEAD"), cancellable = true)
    private static void onSpawnAsEntity(net.minecraft.world.World worldIn, BlockPos pos, net.minecraft.item.ItemStack stack, CallbackInfo ci) {
        if (!worldIn.isRemote && ((IMixinWorldServer) worldIn).getCauseTracker().getStack().peekState() == BlockPhase.State.RESTORING_BLOCKS) {
            ci.cancel();
        }
    }

    @Redirect(method = "spawnAsEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private static boolean redirectGameRulesToCaptureItemDrops(GameRules gameRules, String argument, net.minecraft.world.World worldIn, BlockPos pos, ItemStack stack) {
        final boolean allowTileDrops = gameRules.getBoolean(argument);
        if (allowTileDrops && worldIn instanceof IMixinWorldServer) {
            final IMixinWorldServer mixin = (IMixinWorldServer) worldIn;
            final PhaseData currentPhase = mixin.getCauseTracker().getStack().peek();
            final IPhaseState currentState = currentPhase.getState();
            if (currentState.tracksBlockSpecificDrops()) {
                final PhaseContext context = currentPhase.getContext();
                final Multimap<BlockPos, ItemDropData> multimap = context.getCapturedBlockDrops();
                final Collection<ItemDropData> itemStacks = multimap.get(pos);
                SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.item(stack).position(VecHelper.toVector3d(pos)).build());
                return false;
            }
        }
        return allowTileDrops;
    }

    @Override
    public boolean isVanilla() {
        return this.isVanilla;
    }

    @Override
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getBlockTiming((net.minecraft.block.Block)(Object) this);
        }
        return this.timing;
    }
}
