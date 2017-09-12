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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerInteractionManager;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = PlayerInteractionManager.class)
public abstract class MixinPlayerInteractionManager implements IMixinPlayerInteractionManager {

    @Shadow public EntityPlayerMP player;
    @Shadow public net.minecraft.world.World world;
    @Shadow private GameType gameType;

    @Shadow public abstract boolean isCreative();

    /**
     * @author Aaron1011
     * @author gabizou - May 28th, 2016 - Rewritten for 1.9.4
     *
     * @reason Fire interact block event.
     */
    @Overwrite
    public EnumActionResult processRightClickBlock(EntityPlayer player, net.minecraft.world.World worldIn, @Nullable ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float offsetX, float offsetY, float offsetZ) {
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
        @Nullable final ItemStack oldStack = stack.copy();
        final BlockSnapshot currentSnapshot = ((World) worldIn).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final InteractBlockEvent.Secondary event = SpongeCommonEventFactory.callInteractBlockEventSecondary(player, oldStack,
                Optional.of(new Vector3d(offsetX, offsetY, offsetZ)), currentSnapshot,
                DirectionFacingProvider.getInstance().getKey(facing).get(), hand);
        if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
            SpongeCommonEventFactory.playerInteractItemChanged = true;
        }

        if (event.isCancelled()) {
            final IBlockState state = (IBlockState) currentSnapshot.getState();

            if (state.getBlock() == Blocks.COMMAND_BLOCK) {
                // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
                this.player.connection.sendPacket(new SPacketCloseWindow(0));

            } else if (state.getProperties().containsKey(BlockDoor.HALF)) {
                // Stopping a door from opening while `g the top part will allow the door to open, we need to update the
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

            SpongeCommonEventFactory.interactBlockEventCancelled = true;
            return EnumActionResult.FAIL;
        }
        // Sponge End

        if (!player.isSneaking() || player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) {
            // Sponge start - check event useBlockResult, and revert the client if it's FALSE.
            // Also, store the result instead of returning immediately
            if (event.getUseBlockResult() != Tristate.FALSE) {
                IBlockState iblockstate = (IBlockState) currentSnapshot.getState();
                Container lastOpenContainer = player.openContainer;

                EnumActionResult result = iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, offsetX, offsetY, offsetZ)
                         ? EnumActionResult.SUCCESS
                         : EnumActionResult.PASS;
                // if itemstack changed, avoid restore
                if (!ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
                    SpongeCommonEventFactory.playerInteractItemChanged = true;
                }

                result = this.handleOpenEvent(lastOpenContainer, this.player, currentSnapshot, result);

                if (result != EnumActionResult.PASS) {

                    return result;
                }
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

            if (block instanceof BlockCommandBlock || block instanceof BlockStructure)
            {
                return EnumActionResult.FAIL;
            }
        } // else if (this.isCreative()) { // Sponge - Rewrite this to handle an isCreative check after the result, since we have a copied stack at the top of this method.
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
        final EnumActionResult result = stack.onItemUse(player, worldIn, pos, hand, facing, offsetX, offsetY, offsetZ);
        if (this.isCreative()) {
            stack.setItemDamage(oldStack.getItemDamage());
            stack.setCount(oldStack.getCount());
        }

        if (!ItemStack.areItemStacksEqual(player.getHeldItem(hand), oldStack) || result != EnumActionResult.SUCCESS) {
            player.openContainer.detectAndSendChanges();
        }

        return result;
        // Sponge end
        // } // Sponge - Remove unecessary else bracket
    }

    @Override
    public EnumActionResult handleOpenEvent(Container lastOpenContainer, EntityPlayerMP player, BlockSnapshot blockSnapshot, EnumActionResult result) {

        if (lastOpenContainer != player.openContainer) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.BLOCK_HIT, blockSnapshot);
            if (!SpongeCommonEventFactory.callInteractInventoryOpenEvent(player)) {
                result = EnumActionResult.FAIL;
                SpongeCommonEventFactory.interactBlockEventCancelled = true;
            }
            Sponge.getCauseStackManager().popCause();
        }
        return result;
    }
}
