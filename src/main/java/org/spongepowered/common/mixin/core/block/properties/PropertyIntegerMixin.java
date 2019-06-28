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
package org.spongepowered.common.mixin.core.block.properties;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(value = PropertyInteger.class)
public abstract class PropertyIntegerMixin extends PropertyHelperMixin<Integer> {

    @Shadow @Final private ImmutableSet<Integer> allowedValues;

    @Nullable private Integer impl$hashCode;


    /**
     * @author blood - February 28th, 2019
     * @reason Block properties are immutable so there is no reason
     * to recompute the hashCode repeatedly. To improve performance, we
     * compute the hashCode once then cache the result.
     *
     * @return The cached hashCode
     */
    @Overwrite
    public int hashCode() {
        if (this.impl$hashCode == null) {
            this.impl$hashCode = 31 * super.hashCode() + this.allowedValues.hashCode();
        }
        return this.impl$hashCode;
    }
}
