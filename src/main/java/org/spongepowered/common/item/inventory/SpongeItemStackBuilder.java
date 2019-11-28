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
package org.spongepowered.common.item.inventory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.common.data.util.DataUtil.getData;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.util.Constants;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeItemStackBuilder extends AbstractDataBuilder<ItemStack> implements ItemStack.Builder {
    @Nullable private Set<DataManipulator<?, ?>> itemDataSet;
    private ItemType type;
    private int quantity;
    private int damageValue = 0;
    @Nullable private LinkedHashMap<Key<?>, Object> keyValues;
    @Nullable private NBTTagCompound compound;

    public SpongeItemStackBuilder() {
        super(ItemStack.class, 1);
        reset();
    }

    @Override
    public ItemStack.Builder itemType(ItemType itemType) {
        checkNotNull(itemType, "Item type cannot be null");
        this.type = itemType;
        return this;
    }

    @Override
    public ItemType getCurrentItem() {
        return this.type == null ? ItemTypes.NONE : this.type;
    }

    @Override
    public ItemStack.Builder quantity(int quantity) throws IllegalArgumentException {
        checkArgument(quantity >= 0, "Quantity must not be smaller than 0");
        this.quantity = quantity;
        return this;
    }

    @Override
    public ItemStack.Builder itemData(ImmutableDataManipulator<?, ?> itemData) throws IllegalArgumentException {
        return itemData(itemData.asMutable());
    }

    @Override
    public <V> ItemStack.Builder add(Key<? extends BaseValue<V>> key, V value) throws IllegalArgumentException {
        if (this.keyValues == null) {
            this.keyValues = new LinkedHashMap<>();
        }
        this.keyValues.put(checkNotNull(key, "Key cannot be null!"), checkNotNull(value, "Value cannot be null!"));
        return this;
    }

    @Override
    public ItemStack.Builder itemData(final DataManipulator<?, ?> itemData) throws IllegalArgumentException {
        checkNotNull(itemData, "Must have a non-null item data!");
        checkNotNull(this.type, "Cannot set item data without having set a type first!");
        if (this.itemDataSet == null) {
            this.itemDataSet = new HashSet<>();
        }
        this.itemDataSet.add(itemData);
        return this;
    }

    @Override
    public ItemStack.Builder fromItemStack(ItemStack itemStack) {
        checkNotNull(itemStack, "Item stack cannot be null");
        this.itemDataSet = new HashSet<>();
        // Assumes the item stack's values don't need to be validated
        this.type = itemStack.getType();
        this.quantity = itemStack.getQuantity();
        if (itemStack instanceof net.minecraft.item.ItemStack) {
            this.damageValue = ((net.minecraft.item.ItemStack) itemStack).func_77952_i();
            final NBTTagCompound itemCompound = ((net.minecraft.item.ItemStack) itemStack).func_77978_p();
            if (itemCompound != null) {
                this.compound = itemCompound.func_74737_b();
            }
            this.itemDataSet.addAll(((CustomDataHolderBridge) itemStack).bridge$getCustomManipulators());

        } else {
            this.itemDataSet.addAll(itemStack.getContainers());
        }
        return this;
    }

    @Override
    public ItemStack.Builder fromContainer(DataView container) {
        checkNotNull(container);
        if (!container.contains(Constants.ItemStack.TYPE) || !container.contains(Constants.ItemStack.COUNT)
            || !container.contains(Constants.ItemStack.DAMAGE_VALUE)) {
            return this;
        }
        reset();

        final int count = getData(container, Constants.ItemStack.COUNT, Integer.class);
        quantity(count);

        final String itemTypeId = getData(container, Constants.ItemStack.TYPE, String.class);
        final ItemType itemType = SpongeImpl.getRegistry().getType(ItemType.class, itemTypeId).get();
        itemType(itemType);

        this.damageValue = getData(container, Constants.ItemStack.DAMAGE_VALUE, Integer.class);
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            final NBTTagCompound compound = NbtTranslator.getInstance().translateData(container.getView(Constants.Sponge.UNSAFE_NBT).get());
            if (compound.func_150297_b(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
                compound.func_82580_o(Constants.Sponge.SPONGE_DATA);
            }
            this.compound = compound;
        }
        if (container.contains(Constants.Sponge.DATA_MANIPULATORS)) {
            final List<DataView> views = container.getViewList(Constants.Sponge.DATA_MANIPULATORS).get();
            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
            final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
            this.itemDataSet = new HashSet<>();
            manipulators.forEach(this.itemDataSet::add);
        }
        return this;
    }

    @Override
    public ItemStack.Builder fromSnapshot(ItemStackSnapshot snapshot) {
        checkNotNull(snapshot, "The snapshot was null!");
        itemType(snapshot.getType());
        quantity(snapshot.getQuantity());
        for (ImmutableDataManipulator<?, ?> manipulator : snapshot.getContainers()) {
            itemData(manipulator);
        }
        if (snapshot instanceof SpongeItemStackSnapshot) {
            this.damageValue = ((SpongeItemStackSnapshot) snapshot).getDamageValue();
            final Optional<NBTTagCompound> compoundOptional = ((SpongeItemStackSnapshot) snapshot).getCompound();
            if (compoundOptional.isPresent()) {
                this.compound = compoundOptional.get();
            } else {
                this.compound = null;
            }

        }
        return this;
    }

    @Override
    public ItemStack.Builder fromBlockSnapshot(BlockSnapshot blockSnapshot) {
        checkNotNull(blockSnapshot, "The snapshot was null!");
        reset();
        final BlockType blockType = blockSnapshot.getState().getType();
        final Optional<ItemType> itemType = blockType.getItem();
        itemType(itemType.orElseThrow(() -> new IllegalArgumentException("ItemType not found for block type: " + blockType.getId())));
        quantity(1);
        if (blockSnapshot instanceof SpongeBlockSnapshot) {
            final Block block = (Block) blockType;
            this.damageValue = block.func_180651_a((IBlockState) blockSnapshot.getState());
            final Optional<NBTTagCompound> compound = ((SpongeBlockSnapshot) blockSnapshot).getCompound();
            if (compound.isPresent()) {
                this.compound = new NBTTagCompound();
                this.compound.func_74782_a(Constants.Item.BLOCK_ENTITY_TAG, compound.get());
            }
            // todo probably needs more testing, but this'll do donkey...
        } else { // TODO handle through the API specifically handling the rest of the data stuff
            blockSnapshot.getContainers().forEach(this::itemData);
        }
        return this;
    }

    @Override
    public ItemStack.Builder fromBlockState(BlockState blockState) {
        final IBlockState minecraftState = (IBlockState) blockState;
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
        itemType(item.get());
        this.damageValue = minecraftState.func_177230_c().func_180651_a(minecraftState);
        return this;
    }

    @Override
    public ItemStack.Builder remove(Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        if (this.itemDataSet != null) {
            for (final Iterator<DataManipulator<?, ?>> iterator = this.itemDataSet.iterator(); iterator.hasNext(); ) {
                final DataManipulator<?, ?> next = iterator.next();
                if (manipulatorClass.isInstance(next)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public ItemStack.Builder from(ItemStack value) {
        return fromItemStack(value);
    }

    @Override
    protected Optional<ItemStack> buildContent(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(Constants.ItemStack.TYPE) || !container.contains(Constants.ItemStack.COUNT) || !container.contains(
            Constants.ItemStack.DAMAGE_VALUE)) {
            return Optional.empty();
        }
        final String itemTypeId = getData(container, Constants.ItemStack.TYPE, String.class);
        final int count = getData(container, Constants.ItemStack.COUNT, Integer.class);
        final ItemType itemType = SpongeImpl.getRegistry().getType(ItemType.class, itemTypeId).orElseThrow(() -> new IllegalStateException("Unable to find item with id: " + itemTypeId));
        final int damage = getData(container, Constants.ItemStack.DAMAGE_VALUE, Integer.class);
        final net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack((Item) itemType, count, damage);
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            final NBTTagCompound compound = NbtTranslator.getInstance().translateData(container.getView(Constants.Sponge.UNSAFE_NBT).get());
            fixEnchantmentData(itemType, compound);
            itemStack.func_77982_d(compound);
        }
        if (container.contains(Constants.Sponge.DATA_MANIPULATORS)) {
            final List<DataView> views = container.getViewList(Constants.Sponge.DATA_MANIPULATORS).get();
            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
            final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
            for (DataManipulator<?, ?> manipulator : manipulators) {
                ((CustomDataHolderBridge) itemStack).bridge$offerCustom(manipulator, MergeFunction.IGNORE_ALL);
            }
            if (!transaction.failedData.isEmpty()) {
                ((CustomDataHolderBridge) itemStack).bridge$addFailedData(transaction.failedData);
            }
        }
        return Optional.of((ItemStack) itemStack);
    }

    @Override
    public ItemStack.Builder reset() {
        this.type = null;
        this.quantity = 1;
        this.itemDataSet = new HashSet<>();
        this.compound = null;
        this.damageValue = 0;
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "ConstantConditions"})
    @Override
    public ItemStack build() throws IllegalStateException {
        checkState(this.type != null, "Item type has not been set");

        if (this.type == ItemTypes.NONE || this.quantity <= 0) {
            // If either type is none(air) or quantity is 0 return the vanilla EMPTY item
            return ((ItemStack) net.minecraft.item.ItemStack.field_190927_a);
        }

        final ItemStack stack = (ItemStack) new net.minecraft.item.ItemStack((Item) this.type, this.quantity, this.damageValue);
        if (this.compound != null) {
            ((net.minecraft.item.ItemStack) stack).func_77982_d(this.compound.func_74737_b());
        }
        if (this.itemDataSet != null) {
            this.itemDataSet.forEach(stack::offer);
        }

        if (this.keyValues != null) {
            this.keyValues.forEach((key, value) -> stack.offer((Key) key, value));
        }
        if (this.compound != null && this.compound.func_150297_b(Constants.Forge.FORGE_CAPS, Constants.NBT.TAG_COMPOUND)) {
            final NBTTagCompound compoundTag = this.compound.func_74775_l(Constants.Forge.FORGE_CAPS);
            if (compoundTag != null) {
                SpongeImplHooks.setCapabilitiesFromSpongeBuilder(stack, compoundTag);
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
    public static void fixEnchantmentData(ItemType itemType, NBTTagCompound compound) {
        NBTTagList nbttaglist;
        if (itemType == Items.field_151134_bR) {
            nbttaglist = compound.func_150295_c(Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST, Constants.NBT.TAG_COMPOUND);
        } else {
            nbttaglist = compound.func_150295_c(Constants.Item.ITEM_ENCHANTMENT_LIST, Constants.NBT.TAG_COMPOUND);
        }
        for (int i = 0; i < nbttaglist.func_74745_c(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.func_150305_b(i);
            short id = nbttagcompound.func_74765_d(Constants.Item.ITEM_ENCHANTMENT_ID);
            short lvl = nbttagcompound.func_74765_d(Constants.Item.ITEM_ENCHANTMENT_LEVEL);

            nbttagcompound.func_74777_a(Constants.Item.ITEM_ENCHANTMENT_ID, id);
            nbttagcompound.func_74777_a(Constants.Item.ITEM_ENCHANTMENT_LEVEL, lvl);
        }
    }
}
