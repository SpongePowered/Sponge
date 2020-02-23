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
package org.spongepowered.common.mixin.core.server.management;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.bridge.server.management.PlayerInteractionManagerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import javax.annotation.Nullable;

@Mixin(PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin implements PlayerInteractionManagerBridge {

    @Shadow public ServerPlayerEntity player;
    @Shadow public net.minecraft.world.World world;
    @Shadow private GameType gameType;
    @Shadow private int initialDamage;
    @Shadow private int curblockDamage;
    @Shadow private boolean isDestroyingBlock;
    @Shadow private BlockPos destroyPos;
    @Shadow private int durabilityRemainingOnBlock;

    @Shadow public abstract boolean isCreative();
    @Shadow public abstract boolean tryHarvestBlock(BlockPos pos);

    private boolean impl$interactBlockLeftClickEventCancelled = false;
    private boolean impl$interactBlockRightClickEventCancelled = false;
    private boolean impl$lastInteractItemOnBlockCancelled = false;

    @Override
    public boolean bridge$isInteractBlockRightClickCancelled() {
        return this.impl$interactBlockRightClickEventCancelled;
    }

    @Override
    public void bridge$setInteractBlockRightClickCancelled(final boolean cancelled) {
        this.impl$interactBlockRightClickEventCancelled = cancelled;
    }

    @Override
    public boolean bridge$isLastInteractItemOnBlockCancelled() {
        return this.impl$lastInteractItemOnBlockCancelled;
    }

    @Override
    public void bridge$setLastInteractItemOnBlockCancelled(final boolean lastInteractItemOnBlockCancelled) {
        this.impl$lastInteractItemOnBlockCancelled = lastInteractItemOnBlockCancelled;
    }

    /*
                We have to check for cancelled left click events because they occur from different packets
                or processing branches such that there's no clear "context" of where we can store these variables.
                So, we store it to the interaction manager's fields, to avoid contaminating other interaction
                manager's processes.
                 */
    @Inject(method = "blockRemoving", at = @At("HEAD"), cancellable = true)
    private void onBlockRemovingSpongeCheckForCancelledBlockEvent(final BlockPos pos, final CallbackInfo ci) {
        if (this.impl$interactBlockLeftClickEventCancelled) {
            this.impl$interactBlockLeftClickEventCancelled = false;
            ci.cancel();
        }
    }

    /**
     * @author morpheus - December 15th, 2018
     *
     * @reason Fire interact block event.
     */
    @Overwrite
    public void onBlockClicked(final BlockPos pos, final Direction side) {
        // Sponge start - Fire interact block event
        // This was an @inject in SpongeVanilla and Forge is also firing its event.
        // To achieve compatibility and standardize this method, we use an @Overwrite
        final BlockSnapshot blockSnapshot = Location.of((World) this.player.world, VecHelper.toVector3d(pos)).createSnapshot();
        final RayTraceResult result = SpongeImplHooks.rayTraceEyes(this.player, SpongeImplHooks.getBlockReachDistance(this.player));
        final Vector3d vec = result == null ? null : VecHelper.toVector3d(result.hitResult);
        final ItemStack stack = this.player.getHeldItemMainhand();

        final InteractBlockEvent.Primary blockEvent =
                SpongeCommonEventFactory.callInteractBlockEventPrimary(this.player, stack, blockSnapshot, Hand.MAIN_HAND, side, vec);

        final boolean isCancelled = blockEvent.isCancelled();
        this.impl$interactBlockLeftClickEventCancelled = isCancelled;

        if (isCancelled) {

            final BlockState state = this.player.world.getBlockState(pos);
            ((ServerPlayerEntityBridge) this.player).bridge$sendBlockChange(pos, state);
            this.player.world.notifyBlockUpdate(pos, this.player.world.getBlockState(pos), state, 3);
            return;
        }
        // Sponge End

        if (this.isCreative()) {
            if (!this.world.extinguishFire((PlayerEntity)null, pos, side)) {
                this.tryHarvestBlock(pos);
            }
        } else {
            final BlockState iblockstate = this.world.getBlockState(pos);
            final Block block = iblockstate.getBlock();

            if (this.gameType.hasLimitedInteractions()) {
                if (this.gameType == GameType.SPECTATOR) {
                    return;
                }

                if (!this.player.isAllowEdit()) {
                    final ItemStack itemstack = this.player.getHeldItemMainhand();

                    if (itemstack.isEmpty()) {
                        return;
                    }

                    if (!itemstack.canDestroy(block)) {
                        return;
                    }
                }
            }

            this.world.extinguishFire((PlayerEntity)null, pos, side);
            this.initialDamage = this.curblockDamage;
            float f = 1.0F;

            if (iblockstate.getMaterial() != Material.AIR) {
                block.onBlockClicked(this.world, pos, this.player);
                f = iblockstate.getPlayerRelativeBlockHardness(this.player, this.player.world, pos);
            }

            if (iblockstate.getMaterial() != Material.AIR && f >= 1.0F) {
                this.tryHarvestBlock(pos);
            } else {
                this.isDestroyingBlock = true;
                this.destroyPos = pos;
                final int i = (int)(f * 10.0F);
                this.world.sendBlockBreakProgress(this.player.getEntityId(), pos, i);
                this.durabilityRemainingOnBlock = i;
            }
        }
    }


    /**
     * @author gabizou - September 5th, 2018
     * @author morpheus - December 15th, 2018 - Move the @redirect inside the @Overwrite and fire InteractItemEvent
     *
     * @reason Due to the way that buckets and the like can be handled
     * on the client, often times we need to cancel the item stack usage
     * due to server side cancellation logic that may not exist on the client.
     * Therefor, the cancellation of possible block changes doesn't take
     * effect, and therefor requires telling the client to set back the item
     * in hand.
     * @reason Fire interact item event.
     */
    @Overwrite
    public ActionResultType processRightClick(
        final PlayerEntity player, final net.minecraft.world.World worldIn, final ItemStack stack, final Hand hand) {
        // Sponge start - Fire interact item event
        // This is modified by Forge to fire its own event.
        // To achieve compatibility and standardize this method, we use an @Overwrite
        if (this.gameType == GameType.SPECTATOR) {
            return ActionResultType.PASS;
        }

        // Sponge - start
        final ItemStack oldStack = stack.copy();
        final BlockSnapshot currentSnapshot = BlockSnapshot.empty();
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final InteractItemEvent.Secondary event = SpongeCommonEventFactory.callInteractItemEventSecondary(frame, player, oldStack, hand, null, currentSnapshot);

            if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
                ((PacketContext<?>) PhaseTracker.getInstance().getCurrentContext()).interactItemChanged(true);
            }

            this.bridge$setLastInteractItemOnBlockCancelled(event.isCancelled()); //|| event.getUseItemResult() == Tristate.FALSE;

            if (event.isCancelled()) {
                this.impl$interactBlockRightClickEventCancelled = true;

                ((ServerPlayerEntity) player).sendContainerToPlayer(player.container);
                return ActionResultType.FAIL;
            }
        }
        // Sponge End

        if (stack.isEmpty()) {
            return ActionResultType.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return ActionResultType.PASS;
        }


        final int i = stack.getCount();
        final int j = stack.getMetadata();
        final ActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
        final ItemStack itemstack = actionresult.getResult();

        if (itemstack == stack && itemstack.getCount() == i && itemstack.getUseDuration() <= 0 && itemstack.getMetadata() == j) {

            // Sponge - start

            // Sanity checks on the world being used (hey, i don't know the rules about clients...
            // and if the world is in fact a responsible server world.
            final ActionResultType result = actionresult.getType();
            if (!(worldIn instanceof WorldBridge) || ((WorldBridge) worldIn).bridge$isFake()) {
                return result;
            }

            // Otherwise, let's find out if it's a failed result
            if (result == ActionResultType.FAIL && player instanceof ServerPlayerEntity) {
                // Then, go ahead and tell the client about the change.
                // A few comments about this:
                // window id of -2 sets the player's inventory slot instead of the "held cursor"
                // Then, we need to get the slot index for the held item, which is always
                // playerMP.inventory.currentItem
                final ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
                final SSetSlotPacket packetToSend;
                if (hand == Hand.MAIN_HAND) {
                    // And here, my friends, is why the offhand slot is so stupid....
                    packetToSend = new SSetSlotPacket(-2, player.inventory.currentItem, actionresult.getResult());
                } else {
                    // This is the type of stupidity that comes from finding out that offhand slots
                    // are always the last remaining slot index remaining of the player's overall inventory.
                    // And this has to be done to avoid duplications by inadvertently setting the main hand
                    // item.
                    final int offhandSlotIndex = player.inventory.getSizeInventory() - 1;
                    packetToSend = new SSetSlotPacket(-2, offhandSlotIndex, actionresult.getResult());
                }
                // And finally, set the packet.
                playerMP.connection.sendPacket(packetToSend);
                // this is a full stop re-sync to the client, code above might not actually matter.
                playerMP.sendContainerToPlayer(player.container);
            }
            // Sponge - end

            return result;

        } else if (actionresult.getType() == ActionResultType.FAIL && itemstack.getUseDuration() > 0 && !player.isHandActive()) {
            return actionresult.getType();
        } else {
            player.setHeldItem(hand, itemstack);

            if (this.isCreative()) {
                itemstack.setCount(i);

                if (itemstack.isDamageable()) {
                    itemstack.setItemDamage(j);
                }
            }

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }

            if (!player.isHandActive()) {
                ((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
            }

            return actionresult.getType();
        }
    }

}
