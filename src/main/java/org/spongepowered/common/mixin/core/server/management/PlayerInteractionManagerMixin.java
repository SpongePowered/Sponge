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
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.server.management.PlayerInteractionManagerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;

import javax.annotation.Nullable;

@Mixin(value = PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin implements PlayerInteractionManagerBridge {

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
    public void onBlockClicked(final BlockPos pos, final EnumFacing side) {
        // Sponge start - Fire interact block event
        // This was an @inject in SpongeVanilla and Forge is also firing its event.
        // To achieve compatibility and standardize this method, we use an @Overwrite
        final BlockSnapshot blockSnapshot = new Location<>((World) this.player.field_70170_p, VecHelper.toVector3d(pos)).createSnapshot();
        final RayTraceResult result = SpongeImplHooks.rayTraceEyes(this.player, SpongeImplHooks.getBlockReachDistance(this.player));
        final Vector3d vec = result == null ? null : VecHelper.toVector3d(result.field_72307_f);
        final ItemStack stack = this.player.func_184614_ca();

        final InteractBlockEvent.Primary blockEvent =
                SpongeCommonEventFactory.callInteractBlockEventPrimary(this.player, stack, blockSnapshot, EnumHand.MAIN_HAND, side, vec);

        final boolean isCancelled = blockEvent.isCancelled();
        this.impl$interactBlockLeftClickEventCancelled = isCancelled;

        if (isCancelled) {

            final IBlockState state = this.player.field_70170_p.func_180495_p(pos);
            ((EntityPlayerMPBridge) this.player).bridge$sendBlockChange(pos, state);
            this.player.field_70170_p.func_184138_a(pos, this.player.field_70170_p.func_180495_p(pos), state, 3);
            return;
        }
        // Sponge End

        if (this.isCreative()) {
            if (!this.world.func_175719_a((EntityPlayer)null, pos, side)) {
                this.tryHarvestBlock(pos);
            }
        } else {
            final IBlockState iblockstate = this.world.func_180495_p(pos);
            final Block block = iblockstate.func_177230_c();

            if (this.gameType.func_82752_c()) {
                if (this.gameType == GameType.SPECTATOR) {
                    return;
                }

                if (!this.player.func_175142_cm()) {
                    final ItemStack itemstack = this.player.func_184614_ca();

                    if (itemstack.func_190926_b()) {
                        return;
                    }

                    if (!itemstack.func_179544_c(block)) {
                        return;
                    }
                }
            }

            this.world.func_175719_a((EntityPlayer)null, pos, side);
            this.initialDamage = this.curblockDamage;
            float f = 1.0F;

            if (iblockstate.func_185904_a() != Material.field_151579_a) {
                block.func_180649_a(this.world, pos, this.player);
                f = iblockstate.func_185903_a(this.player, this.player.field_70170_p, pos);
            }

            if (iblockstate.func_185904_a() != Material.field_151579_a && f >= 1.0F) {
                this.tryHarvestBlock(pos);
            } else {
                this.isDestroyingBlock = true;
                this.destroyPos = pos;
                final int i = (int)(f * 10.0F);
                this.world.func_175715_c(this.player.func_145782_y(), pos, i);
                this.durabilityRemainingOnBlock = i;
            }
        }
    }

    /**
     * @author Aaron1011
     * @author gabizou - May 28th, 2016 - Rewritten for 1.9.4
     * @author Morph - Bring the interactions up to date for 1.12.2 and in sync with Forge
     * @author gabizou - April 23rd, 2019 - 1.12.2 - Re-merge the overwrite in common so we do not have to manually
     *    sync the changes between SpongeForge and Common
     *
     * @reason Fire interact block event.
     */
    @Overwrite
    public EnumActionResult processRightClickBlock(
        final EntityPlayer player, final net.minecraft.world.World worldIn, final ItemStack stack, final EnumHand hand, final BlockPos
            pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (this.gameType == GameType.SPECTATOR) {
            final TileEntity tileentity = worldIn.func_175625_s(pos);

            if (tileentity instanceof ILockableContainer) {
                final Block block = worldIn.func_180495_p(pos).func_177230_c();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block).func_180676_d(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    // TODO - fire event
                    player.func_71007_a(ilockablecontainer);
                    return EnumActionResult.SUCCESS;
                }
            } else if (tileentity instanceof IInventory) {
                // TODO - fire event
                player.func_71007_a((IInventory) tileentity);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;

        } // else { // Sponge - Remove unecessary else
        // Sponge Start - Create an interact block event before something happens.
        // Store reference of current player's itemstack in case it changes
        final ItemStack oldStack = stack.func_77946_l();
        final Vector3d hitVec = VecHelper.toVector3d(pos).add(hitX, hitY, hitZ);
        final BlockSnapshot currentSnapshot = ((org.spongepowered.api.world.World) worldIn).createSnapshot(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
        final InteractBlockEvent.Secondary event = SpongeCommonEventFactory.createInteractBlockEventSecondary(player, oldStack,
                hitVec, currentSnapshot, DirectionFacingProvider.getInstance().getKey(facing).get(), hand);

        // Specifically this will be the SpongeToForgeEventData compatibility so we eliminate an extra overwrite.
        @Nullable final Object forgeEventObject = SpongeImplHooks.postForgeEventDataCompatForSponge(event);

        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        if (!SpongeImplHooks.isFakePlayer(this.player) && !ItemStack.func_77989_b(oldStack, this.player.func_184586_b(hand))) {
            if (currentContext instanceof PacketContext) {
                ((PacketContext<?>) currentContext).interactItemChanged(true);
            }
        }

        this.bridge$setLastInteractItemOnBlockCancelled(event.isCancelled() || event.getUseItemResult() == Tristate.FALSE);

        if (event.isCancelled()) {
            final IBlockState state = (IBlockState) currentSnapshot.getState();

            if (state.func_177230_c() == Blocks.field_150483_bI) {
                // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
                this.player.field_71135_a.func_147359_a(new SPacketCloseWindow(0));

            } else if (state.func_177228_b().containsKey(BlockDoor.field_176523_O)) {
                // Stopping a door from opening while interacting the top part will allow the door to open, we need to update the
                // client to resolve this
                if (state.func_177229_b(BlockDoor.field_176523_O) == BlockDoor.EnumDoorHalf.LOWER) {
                    this.player.field_71135_a.func_147359_a(new SPacketBlockChange(worldIn, pos.func_177984_a()));
                } else {
                    this.player.field_71135_a.func_147359_a(new SPacketBlockChange(worldIn, pos.func_177977_b()));
                }

            } else if (!oldStack.func_190926_b()) {
                // Stopping the placement of a door or double plant causes artifacts (ghosts) on the top-side of the block. We need to remove it
                final Item item = oldStack.func_77973_b();
                if (item instanceof ItemDoor || (item instanceof ItemBlock && ((ItemBlock) item).func_179223_d().equals(Blocks.field_150398_cm))) {
                    this.player.field_71135_a.func_147359_a(new SPacketBlockChange(worldIn, pos.func_177981_b(2)));
                }
            }
            SpongeImplHooks.shouldCloseScreen(worldIn, pos, forgeEventObject, this.player);

            ((PlayerInteractionManagerBridge) this.player.field_71134_c).bridge$setInteractBlockRightClickCancelled(true);

            ((EntityPlayerMP) player).func_71120_a(player.field_71069_bz);
            return SpongeImplHooks.getInteractionCancellationResult(forgeEventObject);
        }
        // Sponge End

        EnumActionResult result = EnumActionResult.PASS;

        if (event.getUseItemResult() != Tristate.FALSE) {
            result = SpongeImplHooks.onForgeItemUseFirst(player, stack, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (result != EnumActionResult.PASS) {
                return result ;
            }
        }

        // Sponge Start - Replace main hand and offhand empty checks with bypass flag, Forge has extra hooks
        final boolean bypass = SpongeImplHooks.doesItemSneakBypass(worldIn, pos, player, player.func_184614_ca(), player.func_184592_cb());

        // if (!player.isSneaking() || (player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) || event.getUseBlockResult == Tristate.TRUE) {
        if (!player.func_70093_af() || bypass || event.getUseBlockResult() == Tristate.TRUE) {
            // Sponge start - check event useBlockResult, and revert the client if it's FALSE.
            // also, store the result instead of returning immediately
            if (event.getUseBlockResult() != Tristate.FALSE) {
                final IBlockState iblockstate = (IBlockState) currentSnapshot.getState();
                final Container lastOpenContainer = player.field_71070_bA;

                // Don't close client gui based on the result of Block#onBlockActivated
                // See https://github.com/SpongePowered/SpongeForge/commit/a684cccd0355d1387a30a7fee08d23fa308273c9
                if (iblockstate.func_177230_c().func_180639_a(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
                    result = EnumActionResult.SUCCESS;
                }

                // if itemstack changed, avoid restore
                if (!SpongeImplHooks.isFakePlayer(this.player) && !ItemStack.func_77989_b(oldStack, this.player.func_184586_b(hand))) {
                    if (currentContext instanceof PacketContext) {
                        ((PacketContext<?>) currentContext).interactItemChanged(true);
                    }
                }

                if (lastOpenContainer != player.field_71070_bA) {
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(player);
                        frame.addContext(EventContextKeys.BLOCK_HIT, currentSnapshot);
                        ((ContainerBridge) player.field_71070_bA).bridge$setOpenLocation(currentSnapshot.getLocation().orElse(null));
                        if (!SpongeCommonEventFactory.callInteractInventoryOpenEvent(this.player)) {
                            result = EnumActionResult.FAIL;
                            this.impl$interactBlockRightClickEventCancelled = true;
                        }
                    }
                }
            } else {
                // Need to send a block change to the client, because otherwise, they are not
                // going to be told about the block change.
                this.player.field_71135_a.func_147359_a(new SPacketBlockChange(this.world, pos));
                // Since the event was explicitly set to fail, we need to respect it and treat it as if
                // it wasn't cancelled, but perform no further processing.
                @Nullable final EnumActionResult modifiedResult = SpongeImplHooks.getEnumResultForProcessRightClickBlock(this.player, event, result, worldIn, pos, hand);
                if (modifiedResult != null) {
                    return modifiedResult;
                }
            }
            // Sponge End
        }

        if (stack.func_190926_b()) {
            return EnumActionResult.PASS;
        } else if (player.func_184811_cZ().func_185141_a(stack.func_77973_b())) {
            return EnumActionResult.PASS;
        } else if (stack.func_77973_b() instanceof ItemBlock && !player.func_189808_dh()) {
            final Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();

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

        if ((result != EnumActionResult.SUCCESS && event.getUseItemResult() != Tristate.FALSE || result == EnumActionResult.SUCCESS && event.getUseItemResult() == Tristate.TRUE)) {
            final int meta = stack.func_77960_j();
            final int size = stack.func_190916_E();
            result = stack.func_179546_a(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (this.isCreative()) {
                stack.func_77964_b(meta);
                stack.func_190920_e(size);
            }
        }

        if (!ItemStack.func_77989_b(player.func_184586_b(hand), oldStack) || result != EnumActionResult.SUCCESS) {
            player.field_71070_bA.func_75142_b();
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
    public EnumActionResult processRightClick(
        final EntityPlayer player, final net.minecraft.world.World worldIn, final ItemStack stack, final EnumHand hand) {
        // Sponge start - Fire interact item event
        // This is modified by Forge to fire its own event.
        // To achieve compatibility and standardize this method, we use an @Overwrite
        if (this.gameType == GameType.SPECTATOR) {
            return EnumActionResult.PASS;
        }

        // Sponge - start
        final ItemStack oldStack = stack.func_77946_l();
        final BlockSnapshot currentSnapshot = BlockSnapshot.NONE;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final InteractItemEvent.Secondary event = SpongeCommonEventFactory.callInteractItemEventSecondary(frame, player, oldStack, hand, null, currentSnapshot);

            if (!ItemStack.func_77989_b(oldStack, this.player.func_184586_b(hand))) {
                ((PacketContext<?>) PhaseTracker.getInstance().getCurrentContext()).interactItemChanged(true);
            }

            this.bridge$setLastInteractItemOnBlockCancelled(event.isCancelled()); //|| event.getUseItemResult() == Tristate.FALSE;

            if (event.isCancelled()) {
                this.impl$interactBlockRightClickEventCancelled = true;

                ((EntityPlayerMP) player).func_71120_a(player.field_71069_bz);
                return EnumActionResult.FAIL;
            }
        }
        // Sponge End

        if (stack.func_190926_b()) {
            return EnumActionResult.PASS;
        } else if (player.func_184811_cZ().func_185141_a(stack.func_77973_b())) {
            return EnumActionResult.PASS;
        }


        final int i = stack.func_190916_E();
        final int j = stack.func_77960_j();
        final ActionResult<ItemStack> actionresult = stack.func_77957_a(worldIn, player, hand);
        final ItemStack itemstack = actionresult.func_188398_b();

        if (itemstack == stack && itemstack.func_190916_E() == i && itemstack.func_77988_m() <= 0 && itemstack.func_77960_j() == j) {

            // Sponge - start

            // Sanity checks on the world being used (hey, i don't know the rules about clients...
            // and if the world is in fact a responsible server world.
            final EnumActionResult result = actionresult.func_188397_a();
            if (!(worldIn instanceof WorldBridge) || ((WorldBridge) worldIn).bridge$isFake()) {
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
                    packetToSend = new SPacketSetSlot(-2, player.field_71071_by.field_70461_c, actionresult.func_188398_b());
                } else {
                    // This is the type of stupidity that comes from finding out that offhand slots
                    // are always the last remaining slot index remaining of the player's overall inventory.
                    // And this has to be done to avoid duplications by inadvertently setting the main hand
                    // item.
                    final int offhandSlotIndex = player.field_71071_by.func_70302_i_() - 1;
                    packetToSend = new SPacketSetSlot(-2, offhandSlotIndex, actionresult.func_188398_b());
                }
                // And finally, set the packet.
                playerMP.field_71135_a.func_147359_a(packetToSend);
                // this is a full stop re-sync to the client, code above might not actually matter.
                playerMP.func_71120_a(player.field_71069_bz);
            }
            // Sponge - end

            return result;

        } else if (actionresult.func_188397_a() == EnumActionResult.FAIL && itemstack.func_77988_m() > 0 && !player.func_184587_cr()) {
            return actionresult.func_188397_a();
        } else {
            player.func_184611_a(hand, itemstack);

            if (this.isCreative()) {
                itemstack.func_190920_e(i);

                if (itemstack.func_77984_f()) {
                    itemstack.func_77964_b(j);
                }
            }

            if (itemstack.func_190926_b()) {
                player.func_184611_a(hand, ItemStack.field_190927_a);
            }

            if (!player.func_184587_cr()) {
                ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
            }

            return actionresult.func_188397_a();
        }
    }

}
