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
package org.spongepowered.common.world;

import com.google.common.collect.MapMaker;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A marker to state that this is a fake player.
 */
public interface FakePlayer {

    Controller controller = new Controller(SpongeImpl.getGame().createFakePlayerFactory());

    public class Factory {

        private final Map<UUID, EntityPlayerMP> players = new MapMaker().weakValues().makeMap();

        public EntityPlayerMP getPlayer(WorldServer world, com.mojang.authlib.GameProfile profile) {
            EntityPlayerMP player = players.get(profile.getId());
            if (player == null) {
                players.put(profile.getId(), player = new SimulatedPlayer(world, profile));
            }
            return player;
        }

    }

    public static class Controller {

        private final Factory factory;

        Controller(Factory factory) {
            this.factory = factory;
        }

        private EntityPlayerMP setUpPlayer(GameProfile profile, World world, int x, int y, int z, ItemStack itemInHand) {
            EntityPlayerMP player = factory.getPlayer((WorldServer) world, (com.mojang.authlib.GameProfile) profile);
            player.world = (WorldServer) world;
            player.interactionManager.setWorld((WorldServer) world);
            player.posX = x;
            player.posY = y;
            player.posZ = z;
            player.onGround = true;
            net.minecraft.item.ItemStack stack = (net.minecraft.item.ItemStack) itemInHand;
            if (stack == null) {
                stack = net.minecraft.item.ItemStack.EMPTY;
            }
            player.inventory.setItemStack(stack);
            player.inventory.mainInventory.set(player.inventory.currentItem, stack);

            // Set up cause capture
            PluginPhase.State.FAKE_PLAYER.createPhaseContext()
                    .addCaptures()
                    .buildAndSwitch();
            Sponge.getCauseStackManager().addContext(EventContextKeys.PLAYER_SIMULATED, profile);
            return player;
        }

        private static void tearDownPlayer(EntityPlayerMP player) {
            // Tear down cause capture
            CauseTracker.getInstance().completePhase(CauseTracker.getInstance().getCurrentState());

            player.inventory.clear();
            player.world = null;
            player.interactionManager.setWorld(null);
        }

        public boolean hit(World world, int x, int y, int z, Direction side, GameProfile profile) {
            EntityPlayerMP player = setUpPlayer(profile, world, x, y, z, null);
            EnumFacing facing = DirectionFacingProvider.directionMap.get(side);
            boolean result = onBlockClicked(player, new BlockPos(x, y, z), facing);
            tearDownPlayer(player);
            return result;
        }

        // Partial copy of PlayerInteractionManager#onBlockClicked
        private boolean onBlockClicked(EntityPlayerMP player, BlockPos pos, EnumFacing side) {
            InteractBlockEvent.Primary event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND, Optional.empty(),
                    ((World) player.world).createSnapshot(VecHelper.toVector3i(pos)), DirectionFacingProvider.directionMap.inverse().get(side));
            if (SpongeImpl.postEvent(event)) {
                return false;
            }
            net.minecraft.world.World world = player.interactionManager.world;
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            world.extinguishFire(null, pos, side);
            if (blockState.getMaterial() == Material.AIR) {
                return true;
            }
            block.onBlockClicked(world, pos, player);
            float f = blockState.getPlayerRelativeBlockHardness(player, player.world, pos);
            return f >= 1.0F ? player.interactionManager.tryHarvestBlock(pos) : true;
        }

        public boolean interact(World world, int x, int y, int z, ItemStack itemStack, Direction side, GameProfile profile) {
            EntityPlayerMP player = setUpPlayer(profile, world, x, y, z, itemStack);
            if (itemStack == null) {
                itemStack = (ItemStack) net.minecraft.item.ItemStack.EMPTY;
            }
            EnumFacing facing = DirectionFacingProvider.directionMap.get(side);
            EnumActionResult result =
                    player.interactionManager.processRightClickBlock(player, player.world, (net.minecraft.item.ItemStack) itemStack,
                            EnumHand.MAIN_HAND, new BlockPos(x, y, z), facing, 0, 0, 0);
            tearDownPlayer(player);
            return result != EnumActionResult.FAIL;
        }

        public boolean place(World world, int x, int y, int z, BlockState block, Direction side, GameProfile profile) {
            Item item = Item.getItemFromBlock((Block) block.getType());
            if (item == null) {
                return false;
            }
            net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(item, 1, ((Block) block.getType())
                    .getMetaFromState((IBlockState) block));
            EntityPlayerMP player = setUpPlayer(profile, world, x, y, z, (ItemStack) stack);
            EnumFacing facing = DirectionFacingProvider.directionMap.get(side);
            EnumActionResult result = stack.onItemUse(player, player.world, new BlockPos(x, y, z), EnumHand.MAIN_HAND, facing, 0, 0, 0);
            tearDownPlayer(player);
            return result != EnumActionResult.FAIL;
        }

        public boolean dig(World world, int x, int y, int z, org.spongepowered.api.item.inventory.ItemStack itemStack, GameProfile profile) {
            EntityPlayerMP player = setUpPlayer(profile, world, x, y, z, itemStack);
            boolean result = player.interactionManager.tryHarvestBlock(new BlockPos(x, y, z));
            tearDownPlayer(player);
            return result;
        }

        public int digTime(World world, int x, int y, int z, org.spongepowered.api.item.inventory.ItemStack itemStack, GameProfile profile) {
            EntityPlayerMP player = setUpPlayer(profile, world, x, y, z, itemStack);
            BlockPos pos = new BlockPos(x, y, z);
            net.minecraft.world.World w = player.world;
            // A value from 0.0 to 1.0 representing the percentage of the block
            // broken in one tick. We return the inverse.
            float percentagePerTick = w.getBlockState(pos).getPlayerRelativeBlockHardness(player, w, pos);
            tearDownPlayer(player);
            return MathHelper.ceil(1 / percentagePerTick);
        }

    }

}
