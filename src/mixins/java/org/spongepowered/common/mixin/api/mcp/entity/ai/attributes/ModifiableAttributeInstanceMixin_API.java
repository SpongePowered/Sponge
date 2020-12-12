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

import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
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

@Mixin(ModifiableAttributeInstance.class)
@Implements(@Interface(iface = Attribute.class, prefix = "attribute$"))
public abstract class ModifiableAttributeInstanceMixin_API implements Attribute {

    // @formatter:off
    @Shadow public abstract net.minecraft.entity.ai.attributes.Attribute shadow$getAttribute();
    @Shadow public abstract double shadow$getBaseValue();
    @Shadow public abstract void shadow$setBaseValue(double baseValue);
    @Shadow public abstract double shadow$getValue();
    @Shadow public abstract net.minecraft.entity.ai.attributes.AttributeModifier shadow$getModifier(UUID uuid);
    @Shadow public abstract boolean shadow$hasModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow protected abstract void shadow$addModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow public abstract void shadow$removeModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow public abstract void shadow$removeModifier(UUID uuid);
    @Shadow public abstract Set<net.minecraft.entity.ai.attributes.AttributeModifier> shadow$getModifiers(net.minecraft.entity.ai.attributes.AttributeModifier.Operation p_225504_1_);
    @Shadow public abstract Set<net.minecraft.entity.ai.attributes.AttributeModifier> shadow$getModifiers();
    // @formatter:on

    @Override
    public AttributeType getType() {
        return (AttributeType) this.shadow$getAttribute();
    }

    @Intrinsic
    public double attribute$getBaseValue() {
        return this.shadow$getBaseValue();
    }

    @Intrinsic
    public double attribute$getValue() {
        return this.shadow$getValue();
    }

    @Intrinsic
    public void attribute$setBaseValue(final double baseValue) {
        this.shadow$setBaseValue(baseValue);
    }

    @Override
    public Collection<AttributeModifier> getModifiers() {
        return (Collection) this.shadow$getModifiers();
    }

    @Override
    public Collection<AttributeModifier> getModifiers(final AttributeOperation operation) {
        return (Collection) this.shadow$getModifiers((net.minecraft.entity.ai.attributes.AttributeModifier.Operation) (Object) operation);
    }

    @Override
    public boolean hasModifier(final AttributeModifier modifier) {
        return this.shadow$hasModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    public Optional<AttributeModifier> getModifier(final UUID uniqueId) {
        return Optional.ofNullable((AttributeModifier) this.shadow$getModifier(uniqueId));
    }

    @Override
    public void addModifier(final AttributeModifier modifier) {
        this.shadow$addModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    public void removeModifier(final AttributeModifier modifier) {
        this.shadow$removeModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    public void removeModifier(final UUID uniqueId) {
        this.shadow$removeModifier(uniqueId);
    }

}
