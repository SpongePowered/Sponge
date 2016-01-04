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

import net.minecraft.block.properties.PropertyHelper;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.util.Functional;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.block.IMixinPropertyHolder;

import java.util.function.Predicate;

@Mixin(value = PropertyHelper.class)
@Implements(@Interface(iface = BlockTrait.class, prefix = "trait$"))
public abstract class MixinPropertyHelper<T extends Comparable<T>> implements BlockTrait<T>, IMixinPropertyHolder {

    protected String propertyName;
    protected String idString;

    @Shadow private String name;
    @Shadow public abstract Class<T> shadow$getValueClass();

    @Override
    public String getId() {
        return this.idString;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Intrinsic
    public Class<T> trait$getValueClass() {
        return this.shadow$getValueClass();
    }

    @Override
    public Predicate<T> getPredicate() {
        return Functional.predicateIn(getPossibleValues());
    }

    @Override
    public void setId(String id) {
        this.idString = id;
    }
}
