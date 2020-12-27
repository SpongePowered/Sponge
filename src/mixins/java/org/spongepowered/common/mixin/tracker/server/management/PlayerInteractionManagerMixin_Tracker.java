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
package org.spongepowered.common.mixin.tracker.server.management;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@Mixin(PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin_Tracker {

    @Shadow public ServerPlayerEntity player;
    @Shadow public net.minecraft.world.server.ServerWorld level;

    @Shadow private GameType gameModeForPlayer;

    @Shadow public abstract boolean isCreative();

    @Inject(method = "useItem", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getCount()I", ordinal = 0))
    public void impl$callInteractItemSecondary(final ServerPlayerEntity player, final World p_187250_2_, final ItemStack stack, final Hand hand,
        final CallbackInfoReturnable<ActionResultType> cir
    ) {
        final InteractItemEvent.Secondary event = SpongeCommonEventFactory.callInteractItemEventSecondary(player, stack, hand);
        if (event.isCancelled()) {
            cir.setReturnValue(ActionResultType.FAIL);
        }
    }

    /**
     * @author Morph
     * @reason Fire interact block event.
     */
    @Overwrite
    public ActionResultType useItemOn(final ServerPlayerEntity playerIn, final World worldIn, final ItemStack stackIn, final Hand handIn, final BlockRayTraceResult blockRaytraceResultIn) {
        final BlockPos blockpos = blockRaytraceResultIn.getBlockPos();
        final BlockState blockstate = worldIn.getBlockState(blockpos);
        // Sponge start
        final BlockSnapshot snapshot = ((ServerWorld) (worldIn)).createSnapshot(VecHelper.toVector3i(blockpos));
        final Vector3d hitVec = Vector3d.from(blockRaytraceResultIn.getBlockPos().getX(), blockRaytraceResultIn.getBlockPos().getY(), blockRaytraceResultIn.getBlockPos().getZ());
        final org.spongepowered.api.util.Direction direction = DirectionFacingProvider.INSTANCE.getKey(blockRaytraceResultIn.getDirection()).get();
        final InteractBlockEvent.Secondary event = SpongeCommonEventFactory.callInteractBlockEventSecondary(playerIn, stackIn, hitVec, snapshot, direction, handIn);
        if (event.isCancelled()) {
            return ActionResultType.FAIL;
        }
        final Tristate useItem = event.getUseItemResult();
        final Tristate useBlock = event.getUseBlockResult();
        // Sponge end
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            final INamedContainerProvider inamedcontainerprovider = blockstate.getMenuProvider(worldIn, blockpos);
            if (inamedcontainerprovider != null) {
                playerIn.openMenu(inamedcontainerprovider);
                final Vector3i pos = VecHelper.toVector3i(blockRaytraceResultIn.getBlockPos());
                final ServerLocation location = ServerLocation.of((ServerWorld) worldIn, pos);
                try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    frame.pushCause(playerIn);
                    frame.addContext(EventContextKeys.BLOCK_HIT, ((ServerWorld)(worldIn)).createSnapshot(pos));
                    ((ContainerBridge) playerIn.containerMenu).bridge$setOpenLocation(location);
                    if (!InventoryEventFactory.callInteractContainerOpenEvent(((ServerPlayerEntity) playerIn))) {
                        return ActionResultType.SUCCESS;
                    }
                }
                return ActionResultType.SUCCESS;
            } else {
                return ActionResultType.PASS;
            }
        } else {
            final boolean flag = !playerIn.getMainHandItem().isEmpty() || !playerIn.getOffhandItem().isEmpty();
            final boolean flag1 = playerIn.isSecondaryUseActive() && flag;
            final ItemStack copiedStack = stackIn.copy();
            if (useBlock != Tristate.FALSE && !flag1) { // Sponge check useBlock
                final Container lastOpenContainer = playerIn.containerMenu;
                final ActionResultType result = blockstate.use(worldIn, playerIn, handIn, blockRaytraceResultIn);
                if (result.consumesAction() && lastOpenContainer != playerIn.containerMenu) {
                    final Vector3i pos = VecHelper.toVector3i(blockRaytraceResultIn.getBlockPos());
                    final ServerLocation location = ServerLocation.of((ServerWorld) worldIn, pos);
                    try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(playerIn);
                        frame.addContext(EventContextKeys.BLOCK_HIT, ((ServerWorld) (worldIn)).createSnapshot(pos));
                        ((ContainerBridge) playerIn.containerMenu).bridge$setOpenLocation(location);
                        if (!InventoryEventFactory.callInteractContainerOpenEvent(playerIn)) {
                            return ActionResultType.FAIL;
                        }
                    }
                }
                if (result.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(playerIn, blockpos, copiedStack);
                    return result;
                }
            }

            if (!stackIn.isEmpty() && !playerIn.getCooldowns().isOnCooldown(stackIn.getItem())) {
                // Sponge start
                if (useItem == Tristate.FALSE) {
                    return ActionResultType.PASS;
                }
                // Sponge end
                final ItemUseContext itemusecontext = new ItemUseContext(playerIn, handIn, blockRaytraceResultIn);
                final ActionResultType result;
                if (this.isCreative()) {
                    final int i = stackIn.getCount();
                    result = stackIn.useOn(itemusecontext);
                    stackIn.setCount(i);
                } else {
                    result = stackIn.useOn(itemusecontext);
                }

                if (result.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(playerIn, blockpos, copiedStack);
                }

                return result;
            } else {
                return ActionResultType.PASS;
            }
        }
    }


}
