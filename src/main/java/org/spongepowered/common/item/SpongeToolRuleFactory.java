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
package org.spongepowered.common.item;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.type.ToolRule;
import org.spongepowered.api.tag.Tag;

import java.util.List;
import java.util.Optional;

public class SpongeToolRuleFactory implements ToolRule.Factory {

    @Override
    public ToolRule minesAndDrops(final List<BlockType> blocks, final double speed) {
        return (ToolRule) (Object) Tool.Rule.minesAndDrops(blocks.stream().map(Block.class::cast).toList(), (float) speed);
    }

    @Override
    public ToolRule minesAndDrops(final Tag<BlockType> blockTypeTag, final double speed) {
        return (ToolRule) (Object) Tool.Rule.minesAndDrops((TagKey<Block>) (Object) blockTypeTag, (float) speed);
    }

    @Override
    public ToolRule deniesDrops(final List<BlockType> blocks) {
        // See Tool#forBlocks
        final var holderSet = HolderSet.direct(blocks.stream().map(Block.class::cast).map(Block::builtInRegistryHolder).toList());
        return (ToolRule) (Object) new Tool.Rule(holderSet, Optional.empty(), Optional.empty());
    }

    @Override
    public ToolRule deniesDrops(final Tag<BlockType> blockTypeTag) {
        return (ToolRule) (Object) Tool.Rule.deniesDrops((TagKey<Block>) (Object) blockTypeTag);
    }

    @Override
    public ToolRule overrideSpeed(final List<BlockType> blocks, final double speed) {
        return (ToolRule) (Object) Tool.Rule.overrideSpeed(blocks.stream().map(Block.class::cast).toList(), (float) speed);
    }

    @Override
    public ToolRule overrideSpeed(final Tag<BlockType> blockTypeTag, final double speed) {
        return (ToolRule) (Object) Tool.Rule.overrideSpeed((TagKey<Block>) (Object) blockTypeTag, (float) speed);
    }

    @Override
    public ToolRule forBlocks(final List<BlockType> blocks, @Nullable final Double speed, @Nullable final Boolean drops) {
        // See Tool#forBlocks
        final var holderSet = HolderSet.direct(blocks.stream().map(Block.class::cast).map(Block::builtInRegistryHolder).toList());
        return (ToolRule) (Object) new Tool.Rule(holderSet, Optional.ofNullable(speed).map(Double::floatValue), Optional.ofNullable(drops));
    }

    @Override
    public ToolRule forTag(final Tag<BlockType> blockTypeTag, @Nullable final Double speed, @Nullable final Boolean drops) {
        // See Tool#forTag
        final var holderSet = BuiltInRegistries.BLOCK.getOrCreateTag((TagKey<Block>) (Object) blockTypeTag);
        return (ToolRule) (Object) new Tool.Rule(holderSet, Optional.ofNullable(speed).map(Double::floatValue), Optional.ofNullable(drops));
    }
}
