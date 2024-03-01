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
import com.google.common.collect.Multimap;
import com.mojang.serialization.DataResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.item.SpongeItemStack;
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Objects;
import java.util.function.UnaryOperator;

@Mixin(net.minecraft.world.item.ItemStack.class)
@Implements(@Interface(iface = ItemStack.class, prefix = "itemStack$", remap = Remap.NONE)) // We need to soft implement this interface due to a synthetic bridge method
public abstract class ItemStackMixin_API implements SerializableDataHolder.Mutable, ComponentLike, HoverEventSource<HoverEvent.ShowItem> {       // conflict from overriding ValueContainer#copy() from DataHolder

    // @formatter:off
    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract int shadow$getCount();
    @Shadow public abstract void shadow$setCount(int size); // Do not use field directly as Minecraft tracks the empty state
    @Shadow public abstract void shadow$setDamageValue(int meta);
    @Shadow public abstract int shadow$getDamageValue();
    @Shadow public abstract int shadow$getMaxStackSize();
    @Shadow public abstract boolean shadow$isEmpty();
    @Shadow public abstract net.minecraft.world.item.ItemStack shadow$copy();
    @Shadow public abstract Item shadow$getItem();
    @Shadow public abstract Multimap<Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> shadow$getAttributeModifiers(EquipmentSlot equipmentSlot);
    @Shadow public abstract void shadow$addAttributeModifier(Attribute attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier modifier, @Nullable EquipmentSlot equipmentSlot);
    @Shadow public abstract String shadow$getDescriptionId();
    @Shadow public abstract net.minecraft.network.chat.Component shadow$getDisplayName();
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

    public int itemStack$maxStackQuantity() {
        return this.shadow$getMaxStackSize();
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
        if (!container.contains(Constants.Sponge.UNSAFE_NBT)) {
            throw new InvalidDataException("There's no NBT Data set in the provided container");
        }
        final DataView nbtData = container.getView(Constants.Sponge.UNSAFE_NBT).get();
        try {
            final int integer = container.getInt(Constants.ItemStack.V2.DAMAGE_VALUE).orElse(this.shadow$getDamageValue());
            this.shadow$setDamageValue(integer);
            final CompoundTag stackCompound = NBTTranslator.INSTANCE.translate(nbtData);
            final DataResult<DataComponentPatch> parse = DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, stackCompound);
            this.shadow$setTag(stackCompound);
        } catch (final Exception e) {
            throw new InvalidDataException("Unable to set raw data or translate raw data for ItemStack setting", e);
        }
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

        final Multimap<Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> modifierMultimap = this.shadow$getAttributeModifiers(((EquipmentSlot) (Object) equipmentType));
        builder.addAll((Iterable) modifierMultimap.get(((Attribute) attributeType)));

        return builder.build();
    }

    public void itemStack$addAttributeModifier(final AttributeType attributeType, final AttributeModifier modifier, final EquipmentType equipmentType) {
        Objects.requireNonNull(attributeType, "Attribute type cannot be null");
        Objects.requireNonNull(modifier, "Attribute modifier cannot be null");
        Objects.requireNonNull(equipmentType, "Equipment type cannot be null");

        this.shadow$addAttributeModifier((Attribute) attributeType, (net.minecraft.world.entity.ai.attributes.AttributeModifier) modifier, ((EquipmentSlot) (Object) equipmentType));
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return SpongeItemStack.getDataContainer((net.minecraft.world.item.ItemStack) (Object) this);
    }

    public ItemStackSnapshot itemStack$createSnapshot() {
        return new SpongeItemStackSnapshot((ItemStack) this);
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
        final HoverEvent.ShowItem event = HoverEvent.ShowItem.of(
            SpongeAdventure.asAdventure(SpongeCommon.vanillaRegistry(Registries.ITEM).getKey(this.shadow$getItem())),
            this.shadow$getCount(),
            SpongeAdventure.asBinaryTagHolder(this.shadow$getTag())
        );
        return HoverEvent.showItem(Objects.requireNonNull(op, "op").apply(event));
    }

}
