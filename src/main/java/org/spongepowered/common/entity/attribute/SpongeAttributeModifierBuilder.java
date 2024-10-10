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
package org.spongepowered.common.entity.attribute;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.AttributeOperation;

import java.util.Objects;

public final class SpongeAttributeModifierBuilder implements AttributeModifier.Builder {
    // Use a random id
    private AttributeOperation operation;
    private double amount;
    private ResourceLocation key;

    public SpongeAttributeModifierBuilder() {
    }

    @Override
    public AttributeModifier.Builder key(final ResourceKey key) {
        this.key = (ResourceLocation) (Object) key;
        return this;
    }


    @Override
    public AttributeModifier.Builder operation(final AttributeOperation operation) {
        this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
        return this;
    }

    @Override
    public AttributeModifier.Builder amount(final double amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public AttributeModifier build() {
        Objects.requireNonNull(this.key, "ResourceKey must be set");
        Objects.requireNonNull(this.operation, "Operation must be set");
        return (AttributeModifier) (Object) new net.minecraft.world.entity.ai.attributes.AttributeModifier(this.key, this.amount, (net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation) (Object) this.operation);
    }

    @Override
    public AttributeModifier.Builder reset() {
        // Randomize id when reset
        this.key = null;
        this.amount = 0.0D;
        this.operation = null;
        return this;
    }
}
