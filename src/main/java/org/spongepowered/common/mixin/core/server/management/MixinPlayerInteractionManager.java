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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.slot.EquipmentSlot;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerInteractionManager;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;

@Mixin(value = PlayerInteractionManager.class)
public abstract class MixinPlayerInteractionManager implements IMixinPlayerInteractionManager {

    @Shadow public EntityPlayerMP player;
    @Shadow public net.minecraft.world.World world;
    @Shadow private GameType gameType;
    @Shadow private int initialDamage;
    @Shadow private int curblockDamage;
    @Shadow private boolean isDestroyingBlock;
    @Shadow private BlockPos destroyPos;
    @Shadow private int durabilityRemainingOnBlock;

    @Shadow public abstract boolean isCreative();
    @Shadow public abstract boolean tryHarvestBlock(BlockPos pos);

    @Inject(method = "blockRemoving", at = @At("HEAD"), cancellable = true)
    public void onBlockRemoving(final BlockPos pos, final CallbackInfo ci) {
        if (SpongeCommonEventFactory.interactBlockLeftClickEventCancelled) {
            SpongeCommonEventFactory.interactBlockLeftClickEventCancelled = false;
            ci.cancel();
        }
    }

    /**
     * @author morpheus - December 15th, 2018
     *
     * @reason Fire interact block event.
     */
    @Overwrite
    public void onBlockClicked(BlockPos pos, EnumFacing side) {
        // Sponge start - Fire interact block event
        // This was an @inject in SpongeVanilla and Forge is also firing its event.
        // To achieve compatibility and standardize this method, we use an @Overwrite
        final BlockSnapshot blockSnapshot = new Location<>((World) this.player.world, VecHelper.toVector3d(pos)).createSnapshot();
        final RayTraceResult result = SpongeImplHooks.rayTraceEyes(this.player, SpongeImplHooks.getBlockReachDistance(this.player));
        final Vector3d vec = result == null ? null : VecHelper.toVector3d(result.hitVec);
        final ItemStack stack = this.player.getHeldItemMainhand();

        final InteractBlockEvent.Primary blockEvent =
                SpongeCommonEventFactory.callInteractBlockEventPrimary(this.player, stack, blockSnapshot, EnumHand.MAIN_HAND, side, vec);

        boolean isCancelled = blockEvent.isCancelled();
        SpongeCommonEventFactory.interactBlockLeftClickEventCancelled = isCancelled;

        if (isCancelled) {
            SpongeCommonEventFactory.interactBlockLeftClickEventCancelled = true;

            final IBlockState state = this.player.world.getBlockState(pos);
            ((IMixinEntityPlayerMP) this.player).sendBlockChange(pos, state);
            this.player.world.notifyBlockUpdate(pos, this.player.world.getBlockState(pos), state, 3);
            return;
        }
        // Sponge End

        if (this.isCreative()) {
            if (!this.world.extinguishFire((EntityPlayer)null, pos, side)) {
                this.tryHarvestBlock(pos);
            }
        } else {
            IBlockState iblockstate = this.world.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if (this.gameType.hasLimitedInteractions()) {
                if (this.gameType == GameType.SPECTATOR) {
                    return;
                }

                if (!this.player.isAllowEdit()) {
                    ItemStack itemstack = this.player.getHeldItemMainhand();

                    if (itemstack.isEmpty()) {
                        return;
                    }

                    if (!itemstack.canDestroy(block)) {
                        return;
                    }
                }
            }

            this.world.extinguishFire((EntityPlayer)null, pos, side);
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
                int i = (int)(f * 10.0F);
                this.world.sendBlockBreakProgress(this.player.getEntityId(), pos, i);
                this.durabilityRemainingOnBlock = i;
            }
        }
    }

    /**
     * @author Aaron1011
     * @author gabizou - May 28th, 2016 - Rewritten for 1.9.4
     *
     * @reason Fire interact block event.
     */
    @Overwrite
    public EnumActionResult processRightClickBlock(EntityPlayer player, net.minecraft.world.World worldIn, ItemStack stack, EnumHand hand, BlockPos
            pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Overwritten in SpongeForge. Make sure to keep the two methods consistent.
        if (this.gameType == GameType.SPECTATOR) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer) {
                Block block = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    // TODO - fire event
                    player.displayGUIChest(ilockablecontainer);
                    return EnumActionResult.SUCCESS;
                }
            } else if (tileentity instanceof IInventory) {
                // TODO - fire event
                player.displayGUIChest((IInventory) tileentity);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;

        } // else { // Sponge - Remove unecessary else
        // Sponge Start - Create an interact block event before something happens.
        // Store reference of current player's itemstack in case it changes
        final ItemStack oldStack = stack.copy();
        final Vector3d hitVec = VecHelper.toVector3d(pos.add(hitX, hitY, hitZ));
        final BlockSnapshot currentSnapshot = ((org.spongepowered.api.world.World) worldIn).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final InteractBlockEvent.Secondary event = SpongeCommonEventFactory.createInteractBlockEventSecondary(player, oldStack,
                hitVec, currentSnapshot, DirectionFacingProvider.getInstance().getKey(facing).get(), hand);

        SpongeImpl.postEvent(event);

        if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
            final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
            ((PacketContext<?>) peek.context).interactItemChanged(true);
        }

        SpongeCommonEventFactory.lastInteractItemOnBlockCancelled = event.isCancelled() || event.getUseItemResult() == Tristate.FALSE;

        if (event.isCancelled()) {
            final IBlockState state = (IBlockState) currentSnapshot.getState();

            if (state.getBlock() == Blocks.COMMAND_BLOCK) {
                // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
                this.player.connection.sendPacket(new SPacketCloseWindow(0));

            } else if (state.getProperties().containsKey(BlockDoor.HALF)) {
                // Stopping a door from opening while interacting the top part will allow the door to open, we need to update the
                // client to resolve this
                if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
                    this.player.connection.sendPacket(new SPacketBlockChange(worldIn, pos.up()));
                } else {
                    this.player.connection.sendPacket(new SPacketBlockChange(worldIn, pos.down()));
                }

            } else if (!oldStack.isEmpty()) {
                // Stopping the placement of a door or double plant causes artifacts (ghosts) on the top-side of the block. We need to remove it
                final Item item = oldStack.getItem();
                if (item instanceof ItemDoor || (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(Blocks.DOUBLE_PLANT))) {
                    this.player.connection.sendPacket(new SPacketBlockChange(worldIn, pos.up(2)));
                }
            }

            SpongeCommonEventFactory.interactBlockRightClickEventCancelled = true;

            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            return EnumActionResult.FAIL;
        }
        // Sponge End

        EnumActionResult result = EnumActionResult.PASS;

        if (!player.isSneaking() || (player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) || event.getUseBlockResult() == Tristate.TRUE) {
            // Sponge start - check event useBlockResult, and revert the client if it's FALSE.
            // also, store the result instead of returning immediately
            if (event.getUseBlockResult() != Tristate.FALSE) {
                IBlockState iblockstate = (IBlockState) currentSnapshot.getState();
                Container lastOpenContainer = player.openContainer;

                // Don't close client gui based on the result of Block#onBlockActivated
                // See https://github.com/SpongePowered/SpongeForge/commit/a684cccd0355d1387a30a7fee08d23fa308273c9
                if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
                    result = EnumActionResult.SUCCESS;
                }

                // if itemstack changed, avoid restore
                if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
                    final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
                    ((PacketContext<?>) peek.context).interactItemChanged(true);
                }

                result = this.handleOpenEvent(lastOpenContainer, this.player, currentSnapshot, result);
            } else {
                // Need to send a block change to the client, because otherwise, they are not
                // going to be told about the block change.
                this.player.connection.sendPacket(new SPacketBlockChange(this.world, pos));
                // Since the event was explicitly set to fail, we need to respect it and treat it as if
                // it wasn't cancelled, but perform no further processing.
                return EnumActionResult.FAIL;
            }
            // Sponge End
        }

        if (stack.isEmpty()) {
            return EnumActionResult.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
        } else if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();

            if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                return EnumActionResult.FAIL;
            }
        }
        // else if (this.isCreative()) { // Sponge - Rewrite this to handle an isCreative check after the result, since we have a copied stack at the top of this method.
        //    int j = stack.getMetadata();
        //    int i = stack.stackSize;
        //    EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, offsetX, offsetY, offsetZ);
        //    stack.setItemDamage(j);
        //    stack.stackSize = i;
        //    return enumactionresult;
        // } else {
        //    return stack.onItemUse(player, worldIn, pos, hand, facing, offsetX, offsetY, offsetZ);
        // }
        // } // Sponge - Remove unecessary else bracket
        // Sponge Start - complete the method with the micro change of resetting item damage and quantity from the copied stack.

        if (event.getUseItemResult() != Tristate.FALSE) {
            result = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (this.isCreative()) {
                stack.setItemDamage(oldStack.getItemDamage());
                stack.setCount(oldStack.getCount());
            }
        }

        if (!ItemStack.areItemStacksEqual(player.getHeldItem(hand), oldStack) || result != EnumActionResult.SUCCESS) {
            player.openContainer.detectAndSendChanges();
        }

        return result;
        // Sponge end
        // } // Sponge - Remove unecessary else bracket
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
    public EnumActionResult processRightClick(EntityPlayer player, net.minecraft.world.World worldIn, ItemStack stack, EnumHand hand) {
        // Sponge start - Fire interact item event
        // This is modified by Forge to fire its own event.
        // To achieve compatibility and standardize this method, we use an @Overwrite
        if (this.gameType == GameType.SPECTATOR) {
            return EnumActionResult.PASS;
        }

        // Sponge - start
        final ItemStack oldStack = stack.copy();
        final BlockSnapshot currentSnapshot = BlockSnapshot.NONE;
        final InteractItemEvent.Secondary event = SpongeCommonEventFactory.callInteractItemEventSecondary(player, oldStack, hand, null, currentSnapshot);

        if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
            final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
            ((PacketContext<?>) peek.context).interactItemChanged(true);
        }

        SpongeCommonEventFactory.lastInteractItemOnBlockCancelled = event.isCancelled(); //|| event.getUseItemResult() == Tristate.FALSE;

        if (event.isCancelled()) {
            SpongeCommonEventFactory.interactBlockRightClickEventCancelled = true;

            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            return EnumActionResult.FAIL;
        }
        // Sponge End

        if (stack.isEmpty()) {
            return EnumActionResult.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
        }


        int i = stack.getCount();
        int j = stack.getMetadata();
        ActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
        ItemStack itemstack = actionresult.getResult();

        if (itemstack == stack && itemstack.getCount() == i && itemstack.getMaxItemUseDuration() <= 0 && itemstack.getMetadata() == j) {

            // Sponge - start

            // Sanity checks on the world being used (hey, i don't know the rules about clients...
            // and if the world is in fact a responsible server world.
            final EnumActionResult result = actionresult.getType();
            if (!(worldIn instanceof IMixinWorld) || ((IMixinWorld) worldIn).isFake()) {
                return result;
            }

            // Otherwise, let's find out if it's a failed result
            if (result == EnumActionResult.FAIL && player instanceof EntityPlayerMP) {
                // Then, go ahead and tell the client about the change.
                // A few comments about this:
                // window id of -2 sets the player's inventory slot instead of the "held cursor"
                // Then, we need to get the slot index for the held item, which is always
                // playerMP.inventory.currentItem
                final EntityPlayerMP playerMP = (EntityPlayerMP) player;
                final SPacketSetSlot packetToSend;
                if (hand == EnumHand.MAIN_HAND) {
                    // And here, my friends, is why the offhand slot is so stupid....
                    packetToSend = new SPacketSetSlot(-2, player.inventory.currentItem, actionresult.getResult());
                } else {
                    // This is the type of stupidity that comes from finding out that offhand slots
                    // are always the last remaining slot index remaining of the player's overall inventory.
                    // And this has to be done to avoid duplications by inadvertently setting the main hand
                    // item.
                    final int offhandSlotIndex = player.inventory.getSizeInventory() - 1;
                    packetToSend = new SPacketSetSlot(-2, offhandSlotIndex, actionresult.getResult());
                }
                // And finally, set the packet.
                playerMP.connection.sendPacket(packetToSend);
                // this is a full stop re-sync to the client, code above might not actually matter.
                playerMP.sendContainerToPlayer(player.inventoryContainer);
            }
            // Sponge - end

            return result;

        } else if (actionresult.getType() == EnumActionResult.FAIL && itemstack.getMaxItemUseDuration() > 0 && !player.isHandActive()) {
            return actionresult.getType();
        } else {
            player.setHeldItem(hand, itemstack);

            if (this.isCreative()) {
                itemstack.setCount(i);

                if (itemstack.isItemStackDamageable()) {
                    itemstack.setItemDamage(j);
                }
            }

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }

            if (!player.isHandActive()) {
                ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
            }

            return actionresult.getType();
        }
    }

    @Override
    public EnumActionResult handleOpenEvent(Container lastOpenContainer, EntityPlayerMP player, BlockSnapshot blockSnapshot, EnumActionResult result) {
        if (lastOpenContainer != player.openContainer) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(player);
                frame.addContext(EventContextKeys.BLOCK_HIT, blockSnapshot);
                ((IMixinContainer) player.openContainer).setOpenLocation(blockSnapshot.getLocation().orElse(null));
                if (!SpongeCommonEventFactory.callInteractInventoryOpenEvent(player)) {
                    result = EnumActionResult.FAIL;
                    SpongeCommonEventFactory.interactBlockRightClickEventCancelled = true;
                }
            }
        }
        return result;
    }
}
