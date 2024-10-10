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
package org.spongepowered.common.mixin.api.minecraft.world.entity.ai.attributes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.AttributeOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.world.entity.ai.attributes.AttributeModifier.class)
public abstract class AttributeModifierMixin_API implements AttributeModifier {

    // @formatter:off
    @Shadow @Final private double amount;
    @Shadow @Final private Operation operation;
    @Shadow @Final private ResourceLocation id;

    // @formatter:on



    @Override
    public ResourceKey key() {
        return (ResourceKey) (Object) this.id;
    }

    @Override
    public AttributeOperation operation() {

        return (AttributeOperation) (Object) this.operation;
    }

    @Override
    public double amount() {
        return this.amount;
    }

}
