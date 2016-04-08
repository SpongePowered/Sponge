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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.TristateUtil;

import java.util.Optional;

@Mixin(value = PlayerInteractionManager.class, priority = 1000)
public abstract class MixinPlayerInteractionManager {

    @Shadow public EntityPlayerMP thisPlayerMP;
    @Shadow public net.minecraft.world.World theWorld;
    @Shadow private WorldSettings.GameType gameType;

    @Shadow public abstract boolean isCreative();
    @Shadow public abstract EnumActionResult processRightClick(EntityPlayer player, net.minecraft.world.World worldIn, ItemStack stack, EnumHand hand);

    /**
     * Activate the clicked on block, otherwise use the held item.
     */
    @Overwrite
    public EnumActionResult processRightClickBlock(EntityPlayer player, net.minecraft.world.World worldIn, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float offsetX, float offsetY, float offsetZ) {
        if (this.gameType == WorldSettings.GameType.SPECTATOR) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer) {
                Block block = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    player.displayGUIChest(ilockablecontainer);
                    return EnumActionResult.SUCCESS;
                }
            } else if (tileentity instanceof IInventory) {
                player.displayGUIChest((IInventory) tileentity);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;
        } else {
            // Sponge Start - fire event, and revert the client if cancelled

            ItemStack oldStack = ItemStack.copyItemStack(stack);

            BlockSnapshot currentSnapshot = ((World) worldIn).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            InteractBlockEvent.Secondary event = SpongeCommonEventFactory.callInteractBlockEventSecondary(Cause.of(NamedCause.source(player)),
                        Optional.of(new Vector3d(offsetX, offsetY, offsetZ)), currentSnapshot,
                        DirectionFacingProvider.getInstance().getKey(facing).get(), hand);

            if (event.isCancelled()) {
                final IBlockState state = worldIn.getBlockState(pos);

                if (state.getBlock() == Blocks.command_block) {
                    // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
                    ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new SPacketCloseWindow(0));

                } else if (state.getProperties().containsKey(BlockDoor.HALF)) {
                    // Stopping a door from opening while interacting the top part will allow the door to open, we need to update the
                    // client to resolve this
                    if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
                        ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new SPacketBlockChange(worldIn, pos.up()));
                    } else {
                        ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new SPacketBlockChange(worldIn, pos.down()));
                    }

                } else if (stack != null) {
                    // Stopping the placement of a door or double plant causes artifacts (ghosts) on the top-side of the block. We need to remove it
                    if (stack.getItem() instanceof ItemDoor || (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock()
                            .equals(Blocks.double_plant))) {
                        ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new SPacketBlockChange(worldIn, pos.up(2)));
                    }
                }

                return EnumActionResult.FAIL;
            }
            // Sponge end

            EnumActionResult result = EnumActionResult.FAIL;

            if (!player.isSneaking() || player.getHeldItemMainhand() == null && player.getHeldItemOffhand() == null) {
                // Sponge start - check event useBlockResult, and revert the client if it's FALSE.
                // Also, store the result instead of returning immediately
                if (event.getUseBlockResult() != Tristate.FALSE) {
                    IBlockState iblockstate = worldIn.getBlockState(pos);
                    // TODO - should this always be called if event.getUseBlockResult() is Tristate.TRUE?
                    result = iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, stack, facing, offsetX, offsetY, offsetZ) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
                } else {
                    thisPlayerMP.playerNetServerHandler.sendPacket(new SPacketBlockChange(theWorld, pos));
                    result = TristateUtil.toActionResult(event.getUseItemResult());
                }
            }
            // Sponge end


            // Sponge start - store result instead of returning
            if (stack == null) {
                result = EnumActionResult.PASS;
            } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
                result = EnumActionResult.PASS;
            } else if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockCommandBlock && !player
                    .canCommandSenderUseCommand(2, "")) {
                result = EnumActionResult.FAIL;
            // Sponge start - nest isCreative check instead of calling the method twice.
            } else {
                // Run if useItemResult is true, or if useItemResult is undefined and the block interaction failed
                if (stack != null && (event.getUseItemResult() == Tristate.TRUE || (event.getUseItemResult() == Tristate.UNDEFINED && result == EnumActionResult.FAIL))) {
                    int meta = stack.getMetadata();
                    int size = stack.stackSize;
                    result = stack.onItemUse(player, worldIn, pos, hand, facing, offsetX, offsetY, offsetZ);
                    if (isCreative()) {
                        stack.setItemDamage(meta);
                        stack.stackSize = size;
                    }
                }
            }

            // Since we cancel the second packet received while looking at a block with
            // item in hand, we need to make sure to make an attempt to run the 'tryUseItem'
            // method during the first packet.

            // TODO - should this even be a thing? Do we really want to manually trigger right click air, when it didn't happen?
            if (stack != null && result != EnumActionResult.FAIL && !event.isCancelled() && event.getUseItemResult() != Tristate.FALSE) {
                this.processRightClick(player, worldIn, stack, hand);
            }

            // if cancelled, force client itemstack update
            if (!ItemStack.areItemStacksEqual(player.getHeldItem(hand), oldStack) || result != EnumActionResult.SUCCESS) {
                // TODO - maybe send just main/off hand?
                player.openContainer.detectAndSendChanges();
                /*((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new SPacketSetSlot(player.openContainer.windowId, player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem),
                        player.inventory.getCurrentItem());*/
            }

            return result;
        }
    }
}
