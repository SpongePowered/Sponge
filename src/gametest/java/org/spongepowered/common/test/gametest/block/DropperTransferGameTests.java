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
package org.spongepowered.common.test.gametest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A dropper that moves an item into a container fires the transfer events.
 *
 * <p>{@code sponge:notify/water_spread}: a bedrock floor ringed by obsidian, empty inside. Each test places an
 * upward-facing dropper at (3,1,3), a chest above it, and powers it with a redstone block at (3,1,2).</p>
 */
@GameTestGroup("dropper_transfer")
public final class DropperTransferGameTests {

    private static final String ARENA = "sponge:notify/water_spread";
    private static final BlockPos DISPENSER = new BlockPos(3, 1, 3);
    private static final BlockPos CHEST = new BlockPos(3, 2, 3);
    private static final BlockPos POWER = new BlockPos(3, 1, 2);

    @GameTest(structure = DropperTransferGameTests.ARENA, maxTicks = 40)
    void dropperTransferFiresPreAndPost(final GameTestHelper helper) {
        final DispenserBlockEntity dropper = DropperTransferGameTests.place(helper, Blocks.DROPPER, Items.DIAMOND);
        helper.setBlock(DropperTransferGameTests.CHEST, Blocks.CHEST);
        final ChestBlockEntity chest = helper.getBlockEntity(DropperTransferGameTests.CHEST, ChestBlockEntity.class);
        final AtomicBoolean pre = new AtomicBoolean();
        final AtomicBoolean post = new AtomicBoolean();
        final EventListener<TransferInventoryEvent.Pre> preListener = event -> {
            if (event.sourceInventory() == dropper && event.targetInventory() == chest) {
                pre.set(true);
            }
        };
        final EventListener<TransferInventoryEvent.Post> postListener = event -> {
            if (event.sourceInventory() == dropper && event.targetInventory() == chest) {
                post.set(true);
            }
        };
        SpongeGameTests.listen(TransferInventoryEvent.Pre.class, preListener);
        SpongeGameTests.listen(TransferInventoryEvent.Post.class, postListener);

        helper.setBlock(DropperTransferGameTests.POWER, Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertContainerContains(DropperTransferGameTests.CHEST, Items.DIAMOND);
            helper.assertContainerEmpty(DropperTransferGameTests.DISPENSER);
            helper.assertTrue(pre.get(), "No TransferInventoryEvent.Pre from the dropper to the chest");
            helper.assertTrue(post.get(), "No TransferInventoryEvent.Post from the dropper to the chest");
            Sponge.eventManager().unregisterListeners(preListener);
            Sponge.eventManager().unregisterListeners(postListener);
        });
    }

    // A cancelled pre event leaves the item in the dropper: it is neither inserted nor dropped
    @GameTest(structure = DropperTransferGameTests.ARENA, maxTicks = 40)
    void cancelledTransferKeepsItemInDropper(final GameTestHelper helper) {
        final DispenserBlockEntity dropper = DropperTransferGameTests.place(helper, Blocks.DROPPER, Items.DIAMOND);
        helper.setBlock(DropperTransferGameTests.CHEST, Blocks.CHEST);
        final AtomicBoolean cancelled = new AtomicBoolean();
        final AtomicBoolean post = new AtomicBoolean();
        final EventListener<TransferInventoryEvent.Pre> preListener = event -> {
            if (event.sourceInventory() == dropper) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        final EventListener<TransferInventoryEvent.Post> postListener = event -> {
            if (event.sourceInventory() == dropper) {
                post.set(true);
            }
        };
        SpongeGameTests.listen(TransferInventoryEvent.Pre.class, preListener);
        SpongeGameTests.listen(TransferInventoryEvent.Post.class, postListener);

        helper.setBlock(DropperTransferGameTests.POWER, Blocks.REDSTONE_BLOCK);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(cancelled.get(), "No TransferInventoryEvent.Pre from the dropper"))
            .thenExecuteFor(10, () -> {
                helper.assertContainerEmpty(DropperTransferGameTests.CHEST);
                helper.assertContainerContains(DropperTransferGameTests.DISPENSER, Items.DIAMOND);
                helper.assertEntityNotPresent(EntityTypes.ITEM);
                helper.assertFalse(post.get(), "TransferInventoryEvent.Post fired for a cancelled transfer");
            })
            .thenExecute(() -> {
                Sponge.eventManager().unregisterListeners(preListener);
                Sponge.eventManager().unregisterListeners(postListener);
            })
            .thenSucceed();
    }

    private static DispenserBlockEntity place(final GameTestHelper helper, final Block block, final net.minecraft.world.item.Item item) {
        helper.setBlock(DropperTransferGameTests.DISPENSER, block.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
        final DispenserBlockEntity blockEntity = helper.getBlockEntity(DropperTransferGameTests.DISPENSER, DispenserBlockEntity.class);
        blockEntity.setItem(0, new ItemStack(item));
        return blockEntity;
    }
}
