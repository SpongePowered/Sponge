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

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.ItemAttribute;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.api.item.inventory.equipment.EquipmentCondition;
import org.spongepowered.common.SpongeCommon;

import java.util.Objects;

public final class SpongeItemAttributeFactory implements ItemAttribute.Factory {

    @Override
    public ItemAttribute of(final AttributeType type, final AttributeModifier modifier, final EquipmentCondition condition) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(modifier, "modifier");
        Objects.requireNonNull(condition, "condition");
        return (ItemAttribute) (Object) new ItemAttributeModifiers.Entry(
            SpongeCommon.vanillaRegistry(Registries.ATTRIBUTE).wrapAsHolder((Attribute) type),
            (net.minecraft.world.entity.ai.attributes.AttributeModifier) (Object) modifier,
            (EquipmentSlotGroup) (Object) condition
        );
    }
}
