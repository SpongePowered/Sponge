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
package org.spongepowered.common.item;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Preconditions;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeItemStackBuilder extends AbstractDataBuilder<ItemStack> implements ItemStack.Builder {
    @Nullable private Set<Mutable<?, ?>> itemDataSet;
    private ItemType type;
    private int quantity;
    @Nullable private LinkedHashMap<Key<?>, Object> keyValues;
    @Nullable private CompoundNBT compound;

    public SpongeItemStackBuilder() {
        super(ItemStack.class, 1);
        this.reset();
    }

    @Override
    public ItemStack.Builder itemType(final ItemType itemType) {
        checkNotNull(itemType, "Item type cannot be null");
        this.type = itemType;
        return this;
    }

    @Override
    public ItemType getCurrentItem() {
        return this.type == null ? BlockTypes.AIR.get().getItem().get() : this.type;
    }

    @Override
    public ItemStack.Builder quantity(final int quantity) throws IllegalArgumentException {
        checkArgument(quantity >= 0, "Quantity must not be smaller than 0");
        this.quantity = quantity;
        return this;
    }

    @Override
    public <V> ItemStack.Builder add(final Key<? extends Value<V>> key, final V value) throws IllegalArgumentException {
        if (this.keyValues == null) {
            this.keyValues = new LinkedHashMap<>();
        }
        this.keyValues.put(checkNotNull(key, "Key cannot be null!"), checkNotNull(value, "Value cannot be null!"));
        return this;
    }

    @Override
    public ItemStack.Builder fromItemStack(final ItemStack itemStack) {
        checkNotNull(itemStack, "Item stack cannot be null");
        this.itemDataSet = new HashSet<>();
        // Assumes the item stack's values don't need to be validated
        this.type = itemStack.getType();
        this.quantity = itemStack.getQuantity();
        if ((Object) itemStack instanceof net.minecraft.item.ItemStack) {
            final CompoundNBT itemCompound = ((net.minecraft.item.ItemStack) (Object) itemStack).getTag();
            if (itemCompound != null && !itemCompound.isEmpty()) {
                this.compound = itemCompound.copy();
            } else {
                this.compound = null;
            }
//            this.itemDataSet.addAll(((CustomDataHolderBridge) itemStack).bridge$getCustomManipulators());

        } else {
//            this.itemDataSet.addAll(itemStack.getValues());
        }
        return this;
    }

    @Override
    public ItemStack.Builder attributeModifier(final AttributeType attributeType, final AttributeModifier modifier, final EquipmentType equipmentType) {
        Preconditions.checkNotNull(attributeType, "AttributeType cannot be null");
        Preconditions.checkNotNull(modifier, "AttributeModifier cannot be null");
        Preconditions.checkNotNull(equipmentType, "EquipmentType cannot be null");

        // Create the compound if needed
        if (this.compound == null) {
            this.compound = new CompoundNBT();
        }

        final CompoundNBT compound = this.compound;

        if (!compound.contains(Constants.ItemStack.ATTRIBUTE_MODIFIERS, Constants.NBT.TAG_LIST)) {
            compound.put(Constants.ItemStack.ATTRIBUTE_MODIFIERS, new ListNBT());
        }

        final ListNBT attributeModifiers = compound.getList(Constants.ItemStack.ATTRIBUTE_MODIFIERS, Constants.NBT.TAG_COMPOUND);

        // The modifier will apply in any slot, equipable or not. Pass null for the slot
//        if (equipmentType.equals(EquipmentTypes.ANY.get()) || equipmentType.equals(EquipmentTypes.EQUIPPED.get())) {
//            this.writeAttributeModifier(attributeModifiers, (net.minecraft.entity.ai.attributes.AttributeModifier) modifier, null);
//        } else {
//            // Write modifier to every applicable slot.
//            for (EquipmentSlotType slot : ((SpongeEquipmentType) equipmentType).getSlots()) {
//                this.writeAttributeModifier(attributeModifiers, (net.minecraft.entity.ai.attributes.AttributeModifier) modifier, slot);
//            }
//        }

        return this;
    }

    private void writeAttributeModifier(final ListNBT attributeModifiers, final net.minecraft.entity.ai.attributes.AttributeModifier attributeModifier, final EquipmentSlotType slot) {
        final CompoundNBT modifierNbt = SharedMonsterAttributes.writeAttributeModifier(attributeModifier);
        modifierNbt.putString(Constants.ItemStack.ATTRIBUTE_NAME, attributeModifier.getName());

        if (slot != null) {
            modifierNbt.putString(Constants.ItemStack.ATTRIBUTE_SLOT, slot.getName());
        }

        attributeModifiers.add(modifierNbt);
    }

    @Override
    public ItemStack.Builder fromContainer(final DataView container) {
        checkNotNull(container);
        if (!container.contains(Constants.ItemStack.TYPE, Constants.ItemStack.COUNT)) {
            return this;
        }
        this.reset();

        final int count = container.getInt(Constants.ItemStack.COUNT).get();
        this.quantity(count);

        final ItemType itemType = container.getCatalogType(Constants.ItemStack.TYPE, ItemType.class).get();
        this.itemType(itemType);

        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            final CompoundNBT compound = NbtTranslator.getInstance().translate(container.getView(Constants.Sponge.UNSAFE_NBT).get());
            if (compound.contains(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
                compound.remove(Constants.Sponge.SPONGE_DATA);
            }
            if (!compound.isEmpty()) {
                this.compound = compound;
            } else {
                this.compound = null;
            }
        }
        if (container.contains(Constants.Sponge.DATA_MANIPULATORS)) {
            final List<DataView> views = container.getViewList(Constants.Sponge.DATA_MANIPULATORS).get();
            // TODO -
//            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
//            final List<Mutable<?, ?>> manipulators = transaction.deserializedManipulators;
//            this.itemDataSet = new HashSet<>();
//            manipulators.forEach(this.itemDataSet::add);
        }
        return this;
    }

    @Override
    public ItemStack.Builder fromSnapshot(final ItemStackSnapshot snapshot) {
        checkNotNull(snapshot, "The snapshot was null!");
        this.itemType(snapshot.getType());
        this.quantity(snapshot.getQuantity());

        for (Value.Immutable<?> value : snapshot.getValues()) {
            this.add(value);
        }

        if (snapshot instanceof SpongeItemStackSnapshot) {
            this.compound = ((SpongeItemStackSnapshot) snapshot).getCompound().orElse(null);
        }

        return this;
    }

    @Override
    public ItemStack.Builder fromBlockSnapshot(final BlockSnapshot blockSnapshot) {
        checkNotNull(blockSnapshot, "The snapshot was null!");
        this.reset();
        final BlockType blockType = blockSnapshot.getState().getType();
        final Optional<ItemType> itemType = blockType.getItem();
        this.itemType(itemType.orElseThrow(() -> new IllegalArgumentException("ItemType not found for block type: " + blockType.getKey())));
        this.quantity(1);
        if (blockSnapshot instanceof SpongeBlockSnapshot) {
            final Optional<CompoundNBT> compound = ((SpongeBlockSnapshot) blockSnapshot).getCompound();
            if (compound.isPresent()) {
                this.compound = new CompoundNBT();
                this.compound.put(Constants.Item.BLOCK_ENTITY_TAG, compound.get());
            }
            // todo probably needs more testing, but this'll do donkey...
        } else { // TODO handle through the API specifically handling the rest of the data stuff
//            blockSnapshot.getContainers().forEach(this::itemData);
        }
        return this;
    }

    @Override
    public ItemStack.Builder fromBlockState(final BlockState blockState) {
        final net.minecraft.block.BlockState minecraftState = (net.minecraft.block.BlockState) blockState;
        final Optional<ItemType> item = blockState.getType().getItem();
        if (!item.isPresent()) {
            new PrettyPrinter(60).add("Invalid BlockState").centre().hr()
                .add("Someone attempted to create an ItemStack from a BlockState that does not have a valid item!")
                .add("%s : %s", "BlockState", blockState)
                .add("%s : %s", "BlockType", blockState.getType())
                .add(new Exception("Stacktrace"))
                .trace();
            return this;
        }
        this.itemType(item.get());
        return this;
    }


    @Override
    public ItemStack.Builder from(final ItemStack value) {
        return this.fromItemStack(value);
    }

    @Override
    protected Optional<ItemStack> buildContent(final DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(Constants.ItemStack.TYPE, Constants.ItemStack.COUNT)) {
            return Optional.empty();
        }
        final int count = container.getInt(Constants.ItemStack.COUNT).get();
        final ItemType itemType = container.getCatalogType(Constants.ItemStack.TYPE, ItemType.class).orElseThrow(() -> new IllegalStateException("Unable to find item with id: "));
        final net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack((Item) itemType, count);
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            final CompoundNBT compound = NbtTranslator.getInstance().translate(container.getView(Constants.Sponge.UNSAFE_NBT).get());
            if (!compound.isEmpty()) {
                fixEnchantmentData(itemType, compound);
                itemStack.setTag(compound);
            }
        }
        if (container.contains(Constants.Sponge.DATA_MANIPULATORS)) {
            final List<DataView> views = container.getViewList(Constants.Sponge.DATA_MANIPULATORS).get();
//            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
//            final List<Mutable<?, ?>> manipulators = transaction.deserializedManipulators;
//            for (final Mutable<?, ?> manipulator : manipulators) {
//                ((CustomDataHolderBridge) itemStack).bridge$offerCustom(manipulator, MergeFunction.IGNORE_ALL);
//            }
//            if (!transaction.failedData.isEmpty()) {
//                ((CustomDataHolderBridge) itemStack).bridge$addFailedData(transaction.failedData);
//            }
        }
        return Optional.of((ItemStack) (Object) itemStack);
    }

    @Override
    public ItemStack.Builder reset() {
        this.type = null;
        this.quantity = 1;
        this.itemDataSet = new HashSet<>();
        this.compound = null;
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "ConstantConditions"})
    @Override
    public ItemStack build() throws IllegalStateException {
        checkState(this.type != null, "Item type has not been set");

        if (this.type == null || this.quantity <= 0) {
            // If either type is none(air) or quantity is 0 return the vanilla EMPTY item
            return ((ItemStack) (Object) net.minecraft.item.ItemStack.EMPTY);
        }

        final ItemStack stack = (ItemStack) (Object) new net.minecraft.item.ItemStack((Item) this.type, this.quantity);
        if (this.compound != null && !this.compound.isEmpty()) {
            ((net.minecraft.item.ItemStack) (Object) stack).setTag(this.compound.copy());
        }
//        if (this.itemDataSet != null) {
//            this.itemDataSet.forEach(stack::offer);
//        }

        if (this.keyValues != null) {
            this.keyValues.forEach((key, value) -> stack.offer((Key) key, value));
        }
        if (this.compound != null && this.compound.contains(Constants.Forge.FORGE_CAPS, Constants.NBT.TAG_COMPOUND)) {
            final CompoundNBT compoundTag = this.compound.getCompound(Constants.Forge.FORGE_CAPS);
            if (compoundTag != null) {
                SpongeImplHooks.setCapabilitiesFromSpongeBuilder((net.minecraft.item.ItemStack) (Object) stack, compoundTag);
            }
        }

        return stack;
    }

    /**
     * Fixes enchantment data by explicitly setting short values
     * See {@link EnchantmentHelper#setEnchantments}
     *
     * @param itemType the item type
     * @param compound the itemstacks NBTTagCompound
     */
    public static void fixEnchantmentData(final ItemType itemType, final CompoundNBT compound) {
        final ListNBT nbttaglist;
        if (itemType == Items.ENCHANTED_BOOK) {
            nbttaglist = compound.getList(Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST, Constants.NBT.TAG_COMPOUND);
        } else {
            nbttaglist = compound.getList(Constants.Item.ITEM_ENCHANTMENT_LIST, Constants.NBT.TAG_COMPOUND);
        }
        for (int i = 0; i < nbttaglist.size(); ++i)
        {
            final CompoundNBT nbttagcompound = nbttaglist.getCompound(i);
            final short id = nbttagcompound.getShort(Constants.Item.ITEM_ENCHANTMENT_ID);
            final short lvl = nbttagcompound.getShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL);

            nbttagcompound.putShort(Constants.Item.ITEM_ENCHANTMENT_ID, id);
            nbttagcompound.putShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL, lvl);
        }
    }
}
