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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
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
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.util.Constants;

import java.util.Collection;

@Mixin(net.minecraft.world.item.ItemStack.class)
@Implements(@Interface(iface = ItemStack.class, prefix = "itemStack$", remap = Remap.NONE)) // We need to soft implement this interface due to a synthetic bridge method
public abstract class ItemStackMixin_API implements SerializableDataHolder.Mutable {       // conflict from overriding ValueContainer#copy() from DataHolder

    // @formatter:off
    @Shadow public abstract int shadow$getCount();
    @Shadow public abstract void shadow$setCount(int size); // Do not use field directly as Minecraft tracks the empty state
    @Shadow public abstract void shadow$setDamageValue(int meta);
    @Shadow public abstract void shadow$setTag(@Nullable CompoundTag compound);
    @Shadow public abstract int shadow$getDamageValue();
    @Shadow public abstract int shadow$getMaxStackSize();
    @Shadow public abstract boolean shadow$hasTag();
    @Shadow public abstract boolean shadow$isEmpty();
    @Shadow public abstract CompoundTag shadow$getTag();
    @Shadow public abstract net.minecraft.world.item.ItemStack shadow$copy();
    @Shadow public abstract Item shadow$getItem();
    @Shadow public abstract Multimap<Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> shadow$getAttributeModifiers(EquipmentSlot equipmentSlot);
    @Shadow public abstract void shadow$addAttributeModifier(Attribute attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier modifier, @Nullable EquipmentSlot equipmentSlot);
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
        Preconditions.checkNotNull(container);
        return false;
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        Preconditions.checkNotNull(container);

        if (this.shadow$isEmpty()) {
            throw new IllegalArgumentException("Cannot set data on empty item stacks!");
        }
        if (!container.contains(Constants.Sponge.UNSAFE_NBT)) {
            throw new InvalidDataException("There's no NBT Data set in the provided container");
        }
        final DataView nbtData = container.getView(Constants.Sponge.UNSAFE_NBT).get();
        try {
            final int integer = container.getInt(Constants.ItemStack.DAMAGE_VALUE).orElse(this.shadow$getDamageValue());
            this.shadow$setDamageValue(integer);
            final CompoundTag stackCompound = NBTTranslator.INSTANCE.translate(nbtData);
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
        Preconditions.checkNotNull(attributeType, "Attribute type cannot be null");
        Preconditions.checkNotNull(equipmentType, "Equipment type cannot be null");

        final ImmutableList.Builder<AttributeModifier> builder = ImmutableList.builder();

        final Multimap<Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> modifierMultimap = this.shadow$getAttributeModifiers(((EquipmentSlot) (Object) equipmentType));
        builder.addAll((Iterable) modifierMultimap.get(((Attribute) attributeType)));

        return builder.build();
    }

    public void itemStack$addAttributeModifier(final AttributeType attributeType, final AttributeModifier modifier, final EquipmentType equipmentType) {
        Preconditions.checkNotNull(attributeType, "Attribute type cannot be null");
        Preconditions.checkNotNull(modifier, "Attribute modifier cannot be null");
        Preconditions.checkNotNull(equipmentType, "Equipment type cannot be null");

        this.shadow$addAttributeModifier((Attribute) attributeType, (net.minecraft.world.entity.ai.attributes.AttributeModifier) modifier, ((EquipmentSlot) (Object) equipmentType));
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final ResourceKey key = (ResourceKey) (Object) Registry.ITEM.getKey((Item) this.itemStack$type());
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.ItemStack.TYPE, key)
                .set(Constants.ItemStack.COUNT, this.itemStack$quantity())
                .set(Constants.ItemStack.DAMAGE_VALUE, this.shadow$getDamageValue());
        if (this.shadow$hasTag()) { // no tag? no data, simple as that.
            final CompoundTag compound = this.shadow$getTag().copy();
            if (compound.contains(Constants.Sponge.Data.V2.SPONGE_DATA)) {
                final CompoundTag spongeCompound = compound.getCompound(Constants.Sponge.Data.V2.SPONGE_DATA);
                if (spongeCompound.contains(Constants.Sponge.Data.V2.CUSTOM_MANIPULATOR_TAG_LIST)) {
                    spongeCompound.remove(Constants.Sponge.Data.V2.CUSTOM_MANIPULATOR_TAG_LIST);
                }
            }
            Constants.NBT.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
            if (!compound.isEmpty()) {
                final DataContainer unsafeNbt = NBTTranslator.INSTANCE.translateFrom(compound);
                container.set(Constants.Sponge.UNSAFE_NBT, unsafeNbt);
            }
        }
        // We only need to include the custom data, not vanilla manipulators supported by sponge implementation
//        final Collection<Mutable<?, ?>> manipulators = ((CustomDataHolderBridge) this).bridge$getCustomManipulators();
//        if (!manipulators.isEmpty()) {
//            container.set(Constants.Sponge.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
//        }
        try {
            PlatformHooks.INSTANCE.getItemHooks().writeItemStackCapabilitiesToDataView(container, (net.minecraft.world.item.ItemStack) (Object) this);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return container;
    }

    public ItemStackSnapshot itemStack$createSnapshot() {
        return new SpongeItemStackSnapshot((ItemStack) this);
    }

    public boolean itemStack$equalTo(final ItemStack that) {
        return net.minecraft.world.item.ItemStack.tagMatches(
                (net.minecraft.world.item.ItemStack) (Object) this,
                (net.minecraft.world.item.ItemStack) (Object) that
        );
    }

    @Intrinsic
    public boolean itemStack$isEmpty() {
        return this.shadow$isEmpty();
    }

}
