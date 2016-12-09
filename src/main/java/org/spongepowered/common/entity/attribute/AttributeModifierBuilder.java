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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.operation.AttributeOperation;
import org.spongepowered.common.entity.attribute.operation.SpongeAttributeOperation;

import java.util.UUID;

import javax.annotation.Nullable;

public class AttributeModifierBuilder implements AttributeModifier.Builder {

    @Nullable private UUID id;
    @Nullable private String name;
    @Nullable private AttributeOperation operation;
    @Nullable private Double amount = null;

    @Override
    public AttributeModifier.Builder id(UUID id) {
        this.id = checkNotNull(id, "id");
        return this;
    }

    @Override
    public AttributeModifier.Builder name(String name) {
        this.name = checkNotNull(name, "name");
        return this;
    }

    @Override
    public AttributeModifier.Builder operation(AttributeOperation operation) {
        this.operation = checkNotNull(operation, "operation");
        return this;
    }

    @Override
    public AttributeModifier.Builder amount(double amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public AttributeModifier build() {
        checkState(this.id != null, "id must be set");
        checkState(this.name != null, "name must be set");
        checkState(this.operation != null, "operation must be set");
        checkState(this.operation instanceof SpongeAttributeOperation, "operation must be an instance of SpongeAttributeOperation");
        checkState(this.amount != null, "operation must be set");
        return (AttributeModifier) new net.minecraft.entity.ai.attributes.AttributeModifier(this.id, this.name, this.amount, ((SpongeAttributeOperation) this.operation).getOpcode());
    }

    @Override
    public AttributeModifier.Builder from(AttributeModifier value) {
        this.id = value.getUniqueId();
        this.name = value.getName();
        this.operation = value.getOperation();
        this.amount = value.getAmount();
        return this;
    }

    @Override
    public AttributeModifier.Builder reset() {
        this.id = null;
        this.name = null;
        this.operation = null;
        this.amount = null;
        return this;
    }

}
