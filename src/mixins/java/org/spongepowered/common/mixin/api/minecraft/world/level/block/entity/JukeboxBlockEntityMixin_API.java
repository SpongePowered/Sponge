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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.entity;

import org.spongepowered.api.block.entity.Jukebox;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.block.JukeboxBlockAccessor;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin_API extends BlockEntityMixin_API implements Jukebox {

    // @formatter:off
    @Shadow public abstract net.minecraft.world.item.ItemStack shadow$getRecord();
    @Shadow public abstract void shadow$setRecord(net.minecraft.world.item.ItemStack recordStack);
    // @formatter:on

    @Override
    public void play() {
        if (!this.shadow$getRecord().isEmpty()) {
            this.level.levelEvent(null, Constants.WorldEvents.PLAY_RECORD_EVENT, this.shadow$getBlockPos(), Item.getId(this.shadow$getRecord().getItem()));
        }
    }

    @Override
    public void stop() {
        this.level.levelEvent(Constants.WorldEvents.PLAY_RECORD_EVENT, this.shadow$getBlockPos(), 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void eject() {
        final BlockState block = this.level.getBlockState(this.shadow$getBlockPos());
        if (block.getBlock() == Blocks.JUKEBOX) {
            ((JukeboxBlockAccessor) block.getBlock()).invoker$dropRecording(this.level, this.shadow$getBlockPos());
            this.level.setBlock(this.shadow$getBlockPos(), block.setValue(JukeboxBlock.HAS_RECORD, false), Constants.BlockChangeFlags.NOTIFY_CLIENTS);
        }
    }

    @Override
    public void insert(final ItemStack record) {
        final net.minecraft.world.item.ItemStack itemStack = ItemStackUtil.toNative(record);
        if (!(itemStack.getItem() instanceof RecordItem)) {
            return;
        }
        final BlockState block = this.level.getBlockState(this.shadow$getBlockPos());
        if (block.getBlock() == Blocks.JUKEBOX) {
            // Don't use BlockJukebox#insertRecord - it looses item data
            this.shadow$setRecord(itemStack);
            this.level.setBlock(this.shadow$getBlockPos(), block.setValue(JukeboxBlock.HAS_RECORD, true), Constants.BlockChangeFlags.NOTIFY_CLIENTS);
        }
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.item().asImmutable());

        return values;
    }

}
