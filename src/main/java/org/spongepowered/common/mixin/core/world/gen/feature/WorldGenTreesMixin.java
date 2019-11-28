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
package org.spongepowered.common.mixin.core.world.gen.feature;

import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.TreeFeature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.WorldGenTreesBridge;

@Mixin(TreeFeature.class)
public abstract class WorldGenTreesMixin extends AbstractTreeFeature implements WorldGenTreesBridge {

    @Shadow @Final @Mutable private int minTreeHeight;

    private VariableAmount impl$minHeight = VariableAmount.fixed(4);
    private String impl$id = "minecraft:oak";
    private String impl$name = "Oak tree";

    public WorldGenTreesMixin(final boolean notify) {
        super(notify);
    }

    @Override
    public String bridge$getId() {
        return this.impl$id;
    }

    @Override
    public void bridge$setId(final String id) {
        this.impl$id = id;
    }

    @Override
    public String bridge$getName() {
        return this.impl$name;
    }

    @Override
    public void bridge$setName(final String name) {
        this.impl$name = name;
    }

    @Override
    public void bridge$setMinHeight(final VariableAmount height) {
        this.impl$minHeight = height;
    }

    @Override
    public VariableAmount bridge$getMinimumHeight() {
        return this.impl$minHeight;
    }
}
