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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.GameType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.bridge.server.management.PlayerInteractionManagerBridge;
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
public class PlayerInteractionManagerMixin_Tracker {


    @Shadow private GameType gameType;
    @Shadow public ServerPlayerEntity player;

    /**
     * @author Aaron1011
     * @author gabizou - May 28th, 2016 - Rewritten for 1.9.4
     * @author Morph - Bring the interactions up to date for 1.12.2 and in sync with Forge
     * @author gabizou - April 23rd, 2019 - 1.12.2 - Re-merge the overwrite in common so we do not have to manually
     *    sync the changes between SpongeForge and Common
     * @author faithcaio December 13th, 2019 TODO I only fixed the first part (gametype==SPECTATOR)
     *
     * @reason Fire interact block event.
     */
    @Overwrite
    public ActionResultType func_219441_a( // processRightClickBlock
        final PlayerEntity player, final net.minecraft.world.World worldIn, final ItemStack stack,
        final Hand hand, final BlockRayTraceResult blockRayTraceResult) {

        BlockPos blockpos = blockRayTraceResult.getPos();
        BlockState blockstate = worldIn.getBlockState(blockpos);

        if (this.gameType == GameType.SPECTATOR) {
            INamedContainerProvider inamedcontainerprovider = blockstate.getContainer(worldIn, blockpos);
            if (inamedcontainerprovider != null) {
                player.openContainer(inamedcontainerprovider);
                // TODO - fire event
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        } // else { // Sponge - Remove unecessary else
        // Sponge Start - Create an interact block event before something happens.
        // Store reference of current player's itemstack in case it changes
        final ItemStack oldStack = stack.copy();
        final Vector3d hitVec = VecHelper.toVector3d(pos).add(hitX, hitY, hitZ);
        final BlockSnapshot currentSnapshot = ((org.spongepowered.api.world.World) worldIn).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final InteractBlockEvent.Secondary event = SpongeCommonEventFactory.createInteractBlockEventSecondary(player, oldStack,
                hitVec, currentSnapshot, DirectionFacingProvider.getInstance().getKey(facing).get(), hand);

        // Specifically this will be the SpongeToForgeEventData compatibility so we eliminate an extra overwrite.
        @Nullable final Object forgeEventObject = SpongeImplHooks.postForgeEventDataCompatForSponge(event);

        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        if (!SpongeImplHooks.isFakePlayer(this.player) && !ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
            if (currentContext instanceof PacketContext) {
                ((PacketContext<?>) currentContext).interactItemChanged(true);
            }
        }

        this.bridge$setLastInteractItemOnBlockCancelled(event.isCancelled() || event.getUseItemResult() == Tristate.FALSE);

        if (event.isCancelled()) {
            final BlockState state = (BlockState) currentSnapshot.getState();

            if (state.getBlock() == Blocks.COMMAND_BLOCK) {
                // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
                this.player.connection.sendPacket(new SCloseWindowPacket(0));

            } else if (state.getProperties().containsKey(DoorBlock.HALF)) {
                // Stopping a door from opening while interacting the top part will allow the door to open, we need to update the
                // client to resolve this
                if (state.get(DoorBlock.HALF) == DoorBlock.EnumDoorHalf.LOWER) {
                    this.player.connection.sendPacket(new SChangeBlockPacket(worldIn, pos.up()));
                } else {
                    this.player.connection.sendPacket(new SChangeBlockPacket(worldIn, pos.down()));
                }

            } else if (!oldStack.isEmpty()) {
                // Stopping the placement of a door or double plant causes artifacts (ghosts) on the top-side of the block. We need to remove it
                final Item item = oldStack.getItem();
                if (item instanceof ItemDoor || (item instanceof BlockItem && ((BlockItem) item).getBlock().equals(Blocks.DOUBLE_PLANT))) {
                    this.player.connection.sendPacket(new SChangeBlockPacket(worldIn, pos.up(2)));
                }
            }
            SpongeImplHooks.shouldCloseScreen(worldIn, pos, forgeEventObject, this.player);

            ((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$setInteractBlockRightClickCancelled(true);

            ((ServerPlayerEntity) player).sendContainerToPlayer(player.container);
            return SpongeImplHooks.getInteractionCancellationResult(forgeEventObject);
        }
        // Sponge End

        ActionResultType result = ActionResultType.PASS;

        if (event.getUseItemResult() != Tristate.FALSE) {
            result = SpongeImplHooks.onForgeItemUseFirst(player, stack, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (result != ActionResultType.PASS) {
                return result ;
            }
        }

        // Sponge Start - Replace main hand and offhand empty checks with bypass flag, Forge has extra hooks
        final boolean bypass = SpongeImplHooks.doesItemSneakBypass(worldIn, pos, player, player.getHeldItemMainhand(), player.getHeldItemOffhand());

        // if (!player.isSneaking() || (player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) || event.getUseBlockResult == Tristate.TRUE) {
        if (!player.isSneaking() || bypass || event.getUseBlockResult() == Tristate.TRUE) {
            // Sponge start - check event useBlockResult, and revert the client if it's FALSE.
            // also, store the result instead of returning immediately
            if (event.getUseBlockResult() != Tristate.FALSE) {
                final BlockState iblockstate = (BlockState) currentSnapshot.getState();
                final Container lastOpenContainer = player.openContainer;

                // Don't close client gui based on the result of Block#onBlockActivated
                // See https://github.com/SpongePowered/SpongeForge/commit/a684cccd0355d1387a30a7fee08d23fa308273c9
                if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
                    result = ActionResultType.SUCCESS;
                }

                // if itemstack changed, avoid restore
                if (!SpongeImplHooks.isFakePlayer(this.player) && !ItemStack.areItemStacksEqual(oldStack, this.player.getHeldItem(hand))) {
                    if (currentContext instanceof PacketContext) {
                        ((PacketContext<?>) currentContext).interactItemChanged(true);
                    }
                }

                if (lastOpenContainer != player.openContainer) {
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(player);
                        frame.addContext(EventContextKeys.BLOCK_HIT, currentSnapshot);
                        ((ContainerBridge) player.openContainer).bridge$setOpenLocation(currentSnapshot.getLocation().orElse(null));
                        if (!InventoryEventFactory.callInteractInventoryOpenEvent(this.player)) {
                            result = ActionResultType.FAIL;
                            this.impl$interactBlockRightClickEventCancelled = true;
                        }
                    }
                }
            } else {
                // Need to send a block change to the client, because otherwise, they are not
                // going to be told about the block change.
                this.player.connection.sendPacket(new SChangeBlockPacket(this.world, pos));
                // Since the event was explicitly set to fail, we need to respect it and treat it as if
                // it wasn't cancelled, but perform no further processing.
                @Nullable final ActionResultType modifiedResult = SpongeImplHooks.getEnumResultForProcessRightClickBlock(this.player, event, result, worldIn, pos, hand);
                if (modifiedResult != null) {
                    return modifiedResult;
                }
            }
            // Sponge End
        }

        if (stack.isEmpty()) {
            return ActionResultType.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return ActionResultType.PASS;
        } else if (stack.getItem() instanceof BlockItem && !player.canUseCommandBlock()) {
            final Block block = ((BlockItem)stack.getItem()).getBlock();

            if (block instanceof CommandBlockBlock || block instanceof StructureBlock) {
                return ActionResultType.FAIL;
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

        if ((result != ActionResultType.SUCCESS && event.getUseItemResult() != Tristate.FALSE || result == ActionResultType.SUCCESS && event.getUseItemResult() == Tristate.TRUE)) {
            final int meta = stack.getMetadata();
            final int size = stack.getCount();
            result = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (this.isCreative()) {
                stack.setItemDamage(meta);
                stack.setCount(size);
            }
        }

        if (!ItemStack.areItemStacksEqual(player.getHeldItem(hand), oldStack) || result != ActionResultType.SUCCESS) {
            player.openContainer.detectAndSendChanges();
        }

        return result;
        // Sponge end
        // } // Sponge - Remove unecessary else bracket
    }
}
