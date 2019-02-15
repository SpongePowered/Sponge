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
import static org.spongepowered.common.data.util.ItemsHelper.validateData;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
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
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeItemStackBuilder extends AbstractDataBuilder<ItemStack> implements ItemStack.Builder {
    @Nullable private Set<DataManipulator<?, ?>> itemDataSet;
    private ItemType type;
    private int quantity;
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
        return this.type == null ? ItemTypes.AIR : this.type;
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
    public <V> ItemStack.Builder add(Key<? extends Value<V>> key, V value) throws IllegalArgumentException {
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
        // Validation is required, we can't let devs set block data on a non-block item!
        DataTransactionResult result = validateData(this.type, itemData);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            throw new IllegalArgumentException("The item data is not compatible with the current item type!");
        }
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
        if ((Object) itemStack instanceof net.minecraft.item.ItemStack) {
            final NBTTagCompound itemCompound = ((net.minecraft.item.ItemStack) (Object) itemStack).getTag();
            if (itemCompound != null) {
                this.compound = itemCompound.copy();
            }
            this.itemDataSet.addAll(((IMixinCustomDataHolder) itemStack).getCustomManipulators());

        } else {
            this.itemDataSet.addAll(itemStack.getContainers());
        }
        return this;
    }

    @Override
    public ItemStack.Builder fromContainer(DataView container) {
        checkNotNull(container);
        if (!container.contains(DataQueries.ITEM_TYPE) || !container.contains(DataQueries.ITEM_COUNT)) {
            return this;
        }
        reset();

        final int count = getData(container, DataQueries.ITEM_COUNT, Integer.class);
        quantity(count);

        final String itemTypeId = getData(container, DataQueries.ITEM_TYPE, String.class);
        final ItemType itemType = SpongeImpl.getRegistry().getType(ItemType.class, itemTypeId).get();
        itemType(itemType);

        if (container.contains(DataQueries.UNSAFE_NBT)) {
            final NBTTagCompound compound = NbtTranslator.getInstance().translateData(container.getView(DataQueries.UNSAFE_NBT).get());
            if (compound.contains(NbtDataUtil.SPONGE_DATA)) {
                compound.remove(NbtDataUtil.SPONGE_DATA);
            }
            this.compound = compound;
        }
        if (container.contains(DataQueries.DATA_MANIPULATORS)) {
            final List<DataView> views = container.getViewList(DataQueries.DATA_MANIPULATORS).get();
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
        itemType(itemType.orElseThrow(() -> new IllegalArgumentException("ItemType not found for block type: " + blockType.getKey())));
        quantity(1);
        if (blockSnapshot instanceof SpongeBlockSnapshot) {
            final Block block = (Block) blockType;
            final Optional<NBTTagCompound> compound = ((SpongeBlockSnapshot) blockSnapshot).getCompound();
            if (compound.isPresent()) {
                this.compound = new NBTTagCompound();
                this.compound.put(NbtDataUtil.BLOCK_ENTITY_TAG, compound.get());
            }
            // todo probably needs more testing, but this'll do donkey...
        } else { // TODO handle through the API specifically handling the rest of the data stuff
            blockSnapshot.getContainers().forEach(this::itemData);
        }
        return this;
    }

    @Override
    public ItemStack.Builder fromBlockState(BlockState blockState) {
        final IBlockState minecraftState = BlockUtil.toNative(blockState);
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
        if (!container.contains(DataQueries.ITEM_TYPE) || !container.contains(DataQueries.ITEM_COUNT)) {
            return Optional.empty();
        }
        final String itemTypeId = container.getString(DataQueries.ITEM_TYPE).get();
        final int count = getData(container, DataQueries.ITEM_COUNT, Integer.class);
        final ItemType itemType = SpongeImpl.getRegistry().getType(ItemType.class, itemTypeId).orElseThrow(() -> new IllegalStateException("Unable to find item with id: " + itemTypeId));
        final net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack((Item) itemType, count);
        if (container.contains(DataQueries.UNSAFE_NBT)) {
            final NBTTagCompound compound = NbtTranslator.getInstance().translateData(container.getView(DataQueries.UNSAFE_NBT).get());
            itemStack.setTag(compound);
        }
        if (container.contains(DataQueries.DATA_MANIPULATORS)) {
            final List<DataView> views = container.getViewList(DataQueries.DATA_MANIPULATORS).get();
            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
            final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
            for (DataManipulator<?, ?> manipulator : manipulators) {
                ((IMixinCustomDataHolder) (Object) itemStack).offerCustom(manipulator, MergeFunction.IGNORE_ALL);
            }
            if (!transaction.failedData.isEmpty()) {
                ((IMixinCustomDataHolder) (Object) itemStack).addFailedData(transaction.failedData);
            }
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ItemStack build() throws IllegalStateException {
        checkState(this.type != null, "Item type has not been set");

        if (this.type == ItemTypes.AIR || this.quantity <= 0) {
            // If either type is none(air) or quantity is 0 return the vanilla EMPTY item
            return ((ItemStack) (Object) net.minecraft.item.ItemStack.EMPTY);
        }

        final ItemStack stack = (ItemStack) (Object) new net.minecraft.item.ItemStack((Item) this.type, this.quantity);
        if (this.compound != null) {
            ((net.minecraft.item.ItemStack) (Object) stack).setTag(this.compound.copy());
        }
        if (this.itemDataSet != null) {
            this.itemDataSet.forEach(stack::offer);
        }

        if (this.keyValues != null) {
            this.keyValues.entrySet().forEach(entry -> stack.offer((Key) entry.getKey(), entry.getValue()));
        }

        return stack;
    }
}
