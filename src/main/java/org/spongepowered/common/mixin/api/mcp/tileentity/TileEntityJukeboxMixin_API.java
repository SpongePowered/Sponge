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
package org.spongepowered.common.mixin.api.mcp.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import org.spongepowered.api.block.tileentity.Jukebox;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.block.BlockJukeboxBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import java.util.List;

@Mixin(JukeboxBlock.TileEntityJukebox.class)
public abstract class TileEntityJukeboxMixin_API extends TileEntityMixin_API implements Jukebox {

    @Shadow public abstract net.minecraft.item.ItemStack getRecord();
    @Shadow public abstract void setRecord(net.minecraft.item.ItemStack recordStack);

    @Override
    public void playRecord() {
        if (!getRecord().func_190926_b()) {
            this.world.func_180498_a(null, Constants.WorldEvents.PLAY_RECORD_EVENT, this.pos, Item.func_150891_b(getRecord().func_77973_b()));
        }
    }

    @Override
    public void stopRecord() {
        this.world.func_175718_b(Constants.WorldEvents.PLAY_RECORD_EVENT, this.pos, 0);
        this.world.func_184149_a(this.pos, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void ejectRecord() {
        final BlockState block = this.world.func_180495_p(this.pos);
        if (block.func_177230_c() == Blocks.field_150421_aI) {
            // TODO - Mixin 0.8 accessors
            ((BlockJukeboxBridge) block.func_177230_c()).accessor$dropRecordItem(this.world, this.pos, block);
            this.world.func_180501_a(this.pos, block.func_177226_a(JukeboxBlock.field_176432_a, false), Constants.BlockChangeFlags.NOTIFY_CLIENTS);
        }
    }

    @Override
    public void insertRecord(final ItemStack record) {
        final net.minecraft.item.ItemStack itemStack = ItemStackUtil.toNative(record);
        if (!(itemStack.func_77973_b() instanceof MusicDiscItem)) {
            return;
        }
        final BlockState block = this.world.func_180495_p(this.pos);
        if (block.func_177230_c() == Blocks.field_150421_aI) {
            // Don't use BlockJukebox#insertRecord - it looses item data
            this.setRecord(itemStack);
            this.world.func_180501_a(this.pos, block.func_177226_a(JukeboxBlock.field_176432_a, true), Constants.BlockChangeFlags.NOTIFY_CLIENTS);
        }
    }

    @Override
    public void supplyVanillaManipulators(final List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        get(RepresentedItemData.class).ifPresent(manipulators::add);
    }

}
