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
package org.spongepowered.common.mixin.core.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Implements(@Interface(iface = AttributeType.class, prefix = "api$"))
@Mixin(IAttribute.class)
public interface MixinIAttribute extends AttributeType {

    @Shadow String shadow$getName();
    @Shadow IAttribute shadow$getParent();
    @Shadow double shadow$getDefaultValue();
    @Shadow double shadow$clampValue(double value);

    @Override
    default String getId() {
        return this.shadow$getName();
    }

    @Intrinsic
    default String api$getName() {
        return this.shadow$getName();
    }

    @Override
    default Optional<AttributeType> getParent() {
        return Optional.ofNullable((AttributeType) this.shadow$getParent());
    }

    @Intrinsic
    default double api$getDefaultValue() {
        return this.shadow$getDefaultValue();
    }

    @Intrinsic
    default double api$clampValue(double value) {
        return this.shadow$clampValue(value);
    }

}
