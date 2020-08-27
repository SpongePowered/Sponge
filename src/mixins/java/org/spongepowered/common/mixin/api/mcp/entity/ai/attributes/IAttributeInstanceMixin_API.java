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

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.spongepowered.api.entity.attribute.Attribute;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.AttributeOperation;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(IAttributeInstance.class)
@Implements(@Interface(iface = Attribute.class, prefix = "api$"))
public interface IAttributeInstanceMixin_API extends Attribute {

    @Shadow IAttribute shadow$getAttribute();
    @Shadow double shadow$getBaseValue();
    @Shadow void shadow$setBaseValue(double baseValue);
    @Shadow double shadow$getValue();
    @Shadow net.minecraft.entity.ai.attributes.AttributeModifier shadow$getModifier(UUID uuid);
    @Shadow boolean shadow$hasModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow void shadow$applyModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow void shadow$removeModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow void shadow$removeModifier(UUID uuid);

    @Shadow Set<net.minecraft.entity.ai.attributes.AttributeModifier> shadow$func_225504_a_(
            net.minecraft.entity.ai.attributes.AttributeModifier.Operation p_225504_1_);

    @Shadow Set<net.minecraft.entity.ai.attributes.AttributeModifier> shadow$func_225505_c_();

    @Override
    default AttributeType getType() {
        return (AttributeType) this.shadow$getAttribute();
    }

    @Intrinsic
    default double api$getBaseValue() {
        return this.shadow$getBaseValue();
    }

    @Intrinsic
    default double api$getValue() {
        return this.shadow$getValue();
    }

    @Intrinsic
    default void api$setBaseValue(final double baseValue) {
        this.shadow$setBaseValue(baseValue);
    }

    @Override
    default Collection<AttributeModifier> getModifiers() {
        return (Collection) this.shadow$func_225505_c_();
    }

    @Override
    default Collection<AttributeModifier> getModifiers(final AttributeOperation operation) {
        return (Collection) this.shadow$func_225504_a_((net.minecraft.entity.ai.attributes.AttributeModifier.Operation) (Object) operation);
    }

    @Override
    default boolean hasModifier(final AttributeModifier modifier) {
        return this.shadow$hasModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    default Optional<AttributeModifier> getModifier(final UUID uniqueId) {
        return Optional.ofNullable((AttributeModifier) this.shadow$getModifier(uniqueId));
    }

    @Override
    default void addModifier(final AttributeModifier modifier) {
        this.shadow$applyModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    default void removeModifier(final AttributeModifier modifier) {
        this.shadow$removeModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    default void removeModifier(final UUID uniqueId) {
        this.shadow$removeModifier(uniqueId);
    }

}
