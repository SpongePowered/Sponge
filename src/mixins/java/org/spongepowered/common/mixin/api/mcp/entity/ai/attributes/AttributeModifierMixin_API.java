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
package org.spongepowered.common.mixin.api.mcp.entity.ai.attributes;

import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.AttributeOperation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(net.minecraft.entity.ai.attributes.AttributeModifier.class)
@Implements(@Interface(iface = AttributeModifier.class, prefix = "api$"))
public abstract class AttributeModifierMixin_API implements AttributeModifier {
    @Shadow public abstract String shadow$getName();
    @Shadow public abstract double shadow$getAmount();
    @Shadow public abstract net.minecraft.entity.ai.attributes.AttributeModifier.Operation shadow$getOperation();
    @Shadow public abstract UUID shadow$getID();

    @Intrinsic
    public String api$getName() {
        return this.shadow$getName();
    }

    @Override
    public AttributeOperation getOperation() {
        return (AttributeOperation) (Object) this.shadow$getOperation();
    }

    @Intrinsic
    public double api$getAmount() {
        return this.shadow$getAmount();
    }

    @Override
    public UUID getUniqueId() {
        return this.shadow$getID();
    }

}
