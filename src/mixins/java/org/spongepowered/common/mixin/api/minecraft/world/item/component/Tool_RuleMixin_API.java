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
package org.spongepowered.common.mixin.api.minecraft.world.item.component;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.type.ToolRule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Tool.Rule.class)
@Implements(@Interface(iface = ToolRule.class, prefix = "toolrule$"))
public abstract class Tool_RuleMixin_API implements ToolRule {

    // @formatter:off
    @Shadow @Final private HolderSet<Block> blocks;
    @Shadow @Final private Optional<Float> speed;
    @Shadow @Final private Optional<Boolean> correctForDrops;
    // @formatter:on


    @Override
    public Set<BlockType> blocks() {
        return this.blocks.stream().map(Holder::value).map(BlockType.class::cast).collect(Collectors.toSet());
    }

    @Intrinsic
    public Optional<Double> toolrule$speed() {
        return this.speed.map(Float::doubleValue);
    }

    @Override
    public Optional<Boolean> drops() {
        return this.correctForDrops;
    }
}
