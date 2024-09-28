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
package org.spongepowered.common.mixin.api.minecraft.world.item;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.item.SpongeItemStack;
import org.spongepowered.common.item.SpongeItemStackSnapshot;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.item.ItemStack.class)
@Implements(@Interface(iface = ItemStack.class, prefix = "itemStack$", remap = Remap.NONE)) // We need to soft implement this interface due to a synthetic bridge method
public abstract class ItemStackMixin_API implements SerializableDataHolder.Mutable, ComponentLike, HoverEventSource<HoverEvent.ShowItem> {       // conflict from overriding ValueContainer#copy() from DataHolder

    // @formatter:off
    @Shadow public abstract int shadow$getCount();
    @Shadow public abstract void shadow$setCount(int size); // Do not use field directly as Minecraft tracks the empty state
    @Shadow public abstract int shadow$getMaxStackSize();
    @Shadow public abstract boolean shadow$isEmpty();
    @Shadow public abstract net.minecraft.world.item.ItemStack shadow$copy();
    @Shadow public abstract Item shadow$getItem();
    @Shadow public abstract net.minecraft.network.chat.Component shadow$getDisplayName();
    @Shadow public abstract void shadow$applyComponents(final DataComponentPatch $$0);
    @Shadow public abstract DataComponentPatch shadow$getComponentsPatch();
    @Shadow @Nullable public abstract <T> T shadow$update(final DataComponentType<T> $$0, final T $$1, final UnaryOperator<T> $$2);

    // @formatter:on

    public int itemStack$quantity() {
        return this.shadow$getCount();
    }

    public ItemType itemStack$type() {
        return (ItemType) this.shadow$getItem();
    }

    public void itemStack$setQuantity(final int quantity) throws IllegalArgumentException {
        this.shadow$setCount(quantity);
    }

    @Override
    public boolean validateRawData(final DataView container) {
        Objects.requireNonNull(container);
        return false;
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        Objects.requireNonNull(container);

        if (this.shadow$isEmpty()) {
            throw new IllegalArgumentException("Cannot set data on empty item stacks!");
        }

        try {
            this.shadow$applyComponents(SpongeItemStack.patchFromData(container));
        } catch (final Exception e) {
            throw new InvalidDataException("Unable to set raw data or translate raw data for ItemStack setting", e);
        }
    }

    public ItemStack itemStack$asMutable() {
        return (ItemStack) this;
    }

    public ItemStack itemStack$asMutableCopy() {
        return this.itemStack$copy();
    }

    public ItemStackSnapshot itemStack$asImmutable() {
        return new SpongeItemStackSnapshot((ItemStack) this);
    }

    @Override
    public SerializableDataHolder.Mutable copy() {
        return this.itemStack$copy();
    }

    public ItemStack itemStack$copy() {
        return (ItemStack) (Object) this.shadow$copy();
    }

    public Collection<AttributeModifier> itemStack$attributeModifiers(final AttributeType attributeType, final EquipmentType equipmentType) {
        Objects.requireNonNull(attributeType, "Attribute type cannot be null");
        Objects.requireNonNull(equipmentType, "Equipment type cannot be null");

        final ImmutableList.Builder<AttributeModifier> builder = ImmutableList.builder();

        final var modifiers = ((net.minecraft.world.item.ItemStack) (Object) this).getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (final ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().value().equals(attributeType) && entry.slot().test(((EquipmentSlot) (Object) equipmentType))) {
                builder.add((AttributeModifier) (Object) entry.modifier());
            }
        }

        return builder.build();
    }

    public void itemStack$addAttributeModifier(final AttributeType attributeType, final AttributeModifier modifier, final EquipmentType equipmentType) {
        Objects.requireNonNull(attributeType, "Attribute type cannot be null");
        Objects.requireNonNull(modifier, "Attribute modifier cannot be null");
        Objects.requireNonNull(equipmentType, "Equipment type cannot be null");

        this.shadow$update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, component ->
                component.withModifierAdded(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) attributeType),
                                            (net.minecraft.world.entity.ai.attributes.AttributeModifier) (Object) modifier,
                                            SpongeItemStack.asEquipmentSlotGroup(equipmentType)));
    }

    @Override
    public int contentVersion() {
        return 3;
    }

    @Override
    public DataContainer toContainer() {
        return SpongeItemStack.getDataContainer((net.minecraft.world.item.ItemStack) (Object) this);
    }

    public boolean itemStack$equalTo(final ItemStack that) {
        return net.minecraft.world.item.ItemStack.matches(
                (net.minecraft.world.item.ItemStack) (Object) this,
                (net.minecraft.world.item.ItemStack) (Object) that
        );
    }

    @Intrinsic
    public boolean itemStack$isEmpty() {
        return this.shadow$isEmpty();
    }

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.shadow$getDisplayName());
    }

    @Override
    public @NonNull HoverEvent<HoverEvent.ShowItem> asHoverEvent(@NonNull final UnaryOperator<HoverEvent.ShowItem> op) {
        final HoverEvent.ShowItem event = HoverEvent.ShowItem.showItem(
            SpongeAdventure.asAdventure(SpongeCommon.vanillaRegistry(Registries.ITEM).getKey(this.shadow$getItem())),
            this.shadow$getCount(),
                Map.of()
            //SpongeAdventure.asBinaryTagHolder(this.shadow$getComponentsPatch())
        );
        return HoverEvent.showItem(Objects.requireNonNull(op, "op").apply(event));
    }

}
