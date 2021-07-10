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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

@Mixin(AttributeInstance.class)
@Implements(@Interface(iface = Attribute.class, prefix = "attribute$"))
public abstract class AttributeInstanceMixin_API implements Attribute {

    // @formatter:off
    @Shadow public abstract net.minecraft.world.entity.ai.attributes.Attribute shadow$getAttribute();
    @Shadow public abstract double shadow$getBaseValue();
    @Shadow public abstract void shadow$setBaseValue(double baseValue);
    @Shadow public abstract double shadow$getValue();
    @Shadow public abstract net.minecraft.world.entity.ai.attributes.AttributeModifier shadow$getModifier(UUID uuid);
    @Shadow public abstract boolean shadow$hasModifier(net.minecraft.world.entity.ai.attributes.AttributeModifier modifier);
    @Shadow protected abstract void shadow$addModifier(net.minecraft.world.entity.ai.attributes.AttributeModifier modifier);
    @Shadow public abstract void shadow$removeModifier(net.minecraft.world.entity.ai.attributes.AttributeModifier modifier);
    @Shadow public abstract void shadow$removeModifier(UUID uuid);
    @Shadow public abstract Set<net.minecraft.world.entity.ai.attributes.AttributeModifier> shadow$getModifiers(net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation p_225504_1_);
    @Shadow public abstract Set<net.minecraft.world.entity.ai.attributes.AttributeModifier> shadow$getModifiers();
    // @formatter:on

    @Override
    public AttributeType type() {
        return (AttributeType) this.shadow$getAttribute();
    }

    @Override
    public double baseValue() {
        return this.shadow$getBaseValue();
    }

    @Override
    public double value() {
        return this.shadow$getValue();
    }

    @Intrinsic
    public void attribute$setBaseValue(final double baseValue) {
        this.shadow$setBaseValue(baseValue);
    }

    @Override
    public Collection<AttributeModifier> modifiers() {
        return (Collection) this.shadow$getModifiers();
    }

    @Override
    public Collection<AttributeModifier> modifiers(final AttributeOperation operation) {
        return (Collection) this.shadow$getModifiers((net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation) (Object) Objects.requireNonNull(operation, "operation"));
    }

    @Override
    public boolean hasModifier(final AttributeModifier modifier) {
        return this.shadow$hasModifier((net.minecraft.world.entity.ai.attributes.AttributeModifier) Objects.requireNonNull(modifier, "modifier"));
    }

    @Override
    public Optional<AttributeModifier> modifier(final UUID uniqueId) {
        return Optional.ofNullable((AttributeModifier) this.shadow$getModifier(Objects.requireNonNull(uniqueId, "uniqueId")));
    }

    @Override
    public void addModifier(final AttributeModifier modifier) {
        this.shadow$addModifier((net.minecraft.world.entity.ai.attributes.AttributeModifier) Objects.requireNonNull(modifier, "modifier"));
    }

    @Override
    public void removeModifier(final AttributeModifier modifier) {
        this.shadow$removeModifier((net.minecraft.world.entity.ai.attributes.AttributeModifier) Objects.requireNonNull(modifier, "modifier"));
    }

    @Intrinsic
    public void attribute$removeModifier(final UUID uniqueId) {
        this.shadow$removeModifier(Objects.requireNonNull(uniqueId, "uniqueId"));
    }

}
