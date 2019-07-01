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
package org.spongepowered.common.mixin.api.item.inventory;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.InventoryIterator;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.AdapterLogic;
import org.spongepowered.common.item.inventory.adapter.impl.SlotCollectionIterator;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.CompoundSlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.CompoundFabric;
import org.spongepowered.common.item.inventory.query.Query;
import org.spongepowered.common.item.inventory.query.operation.LensQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.SlotLensQueryOperation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = {
    TileEntityLockable.class,
    SpongeUserInventory.class,
    InventoryBasic.class,
    InventoryCrafting.class,
    InventoryCraftResult.class,
    InventoryLargeChest.class,
    InventoryPlayer.class,
    EntityVillager.class,
    EntityMinecartContainer.class,
    CustomInventory.class,
    Container.class,
    Slot.class
}, priority = 899)
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class AdapterBasedInventoryMixin_API implements Inventory {

    @Nullable private SlotCollectionIterator api$slotIterator;

    @Override
    public PluginContainer getPlugin() {
        return ((InventoryAdapterBridge) this).bridge$getPlugin();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Translation getName() {
        if (((InventoryAdapter) this).bridge$getRootLens() == null) {
            return ((InventoryAdapter) this).bridge$getFabric().getDisplayName();
        }
        return ((InventoryAdapter) this).bridge$getRootLens().getName(((InventoryAdapter) this).bridge$getFabric());
    }

    @Override
    public Inventory root() {
        return this.parent() == this ? this : this.parent().root();
    }

    @Override
    public Inventory parent() {
        return this;
    }

    @Override
    public Optional<ItemStack> poll() {
        return AdapterLogic.pollSequential((InventoryAdapter) this);
    }

    @Override
    public Optional<ItemStack> poll(final int limit) {
        return AdapterLogic.pollSequential((InventoryAdapter) this, limit);
    }

    @Override
    public Optional<ItemStack> peek() {
        return AdapterLogic.peekSequential((InventoryAdapter) this);
    }

    @Override
    public Optional<ItemStack> peek(final int limit) {
        return AdapterLogic.peekSequential((InventoryAdapter) this, limit);
    }

    @Override
    public InventoryTransactionResult offer(final ItemStack stack) {
        //        try {
        return AdapterLogic.appendSequential((InventoryAdapter) this, stack);
        //        } catch (Exception ex) {
        //            return false;
        //        }
    }

    @Override
    public boolean canFit(final ItemStack stack) {
        return AdapterLogic.canFit((InventoryAdapter) this, stack);
    }

    @Override
    public InventoryTransactionResult set(final ItemStack stack) {
        return AdapterLogic.insertSequential((InventoryAdapter) this, stack);
    }

    @Override
    public int size() {
        return AdapterLogic.countStacks((InventoryAdapter) this);
    }

    @Override
    public int totalItems() {
        return AdapterLogic.countItems((InventoryAdapter) this);
    }

    @Override
    public int capacity() {
        return AdapterLogic.getCapacity((InventoryAdapter) this);
    }

    @Override
    public boolean hasChildren() {
        return ((InventoryAdapter) this).bridge$getRootLens().getChildren().size() != 0;
    }

    @Override
    public boolean contains(final ItemStack stack) {
        return AdapterLogic.contains((InventoryAdapter) this, stack);
    }

    @Override
    public boolean containsAny(final ItemStack stack) {
        return AdapterLogic.contains((InventoryAdapter) this, stack, 1);
    }

    @Override
    public boolean contains(final ItemType type) {
        return AdapterLogic.contains((InventoryAdapter) this, type);
    }

    @Override
    public int getMaxStackSize() {
        return ((InventoryAdapter) this).bridge$getRootLens().getMaxStackSize(((InventoryAdapter) this).bridge$getFabric());
    }

    @Override
    public void setMaxStackSize(final int size) {
        throw new UnsupportedOperationException("This inventory does not support stack limit adjustment");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends InventoryProperty<?, ?>> Collection<T> getProperties(final Inventory child, final Class<T> property) {
        return (Collection<T>) AdapterLogic.getProperties((InventoryAdapter) this, child, property);
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Collection<T> getProperties(final Class<T> property) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperties((InventoryAdapter) this, property);
        }
        return this.parent().getProperties(this, property);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getProperty(final Inventory child, final Class<T> property, final Object key) {
        for (final InventoryProperty<?, ?> prop : AdapterLogic.getProperties((InventoryAdapter) this, child, property)) {
            if (key.equals(prop.getKey())) {
                return Optional.of((T)prop);
            }
        }
        return Optional.empty();
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getProperty(final Class<T> property, final Object key) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperty((InventoryAdapter) this, property, key);
        }
        return this.parent().getProperty(this, property, key);
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(final Inventory child, final Class<T> property) {
        final Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(child, property, key);
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(final Class<T> property) {
        final Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(property, key);
    }

    @Override
    public Iterator<Inventory> iterator() {
        return new InventoryIterator(((InventoryAdapter) this).bridge$getRootLens(), ((InventoryAdapter) this).bridge$getFabric(), this);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public <T extends Inventory> T first() {
        return (T) this.iterator().next();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public <T extends Inventory> T next() {
        return (T) new EmptyInventoryImpl(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T query(final QueryOperation<?>... queries) {
        return (T) Query.compile((InventoryAdapter) this, queries).execute();
    }

    @Override
    public Inventory intersect(final Inventory inventory) {
        return Query.compile((InventoryAdapter) this, new SlotLensQueryOperation(ImmutableSet.of(inventory))).execute();
    }

    @Override
    public Inventory union(final Inventory inventory) {
        final CompoundLens.Builder lensBuilder = CompoundLens.builder().add(((InventoryAdapter) this).bridge$getRootLens());
        final CompoundFabric fabric = new CompoundFabric((MinecraftFabric) ((InventoryAdapter) this).bridge$getFabric(), (MinecraftFabric) ((InventoryAdapter) inventory).bridge$getFabric());
        final CompoundSlotProvider provider = new CompoundSlotProvider().add((InventoryAdapter) this);
        for (final Object inv : inventory) {
            lensBuilder.add(((InventoryAdapter) inv).bridge$getRootLens());
            provider.add((InventoryAdapter) inv);
        }
        final CompoundLens lens = lensBuilder.build(provider);
        final InventoryAdapter compoundAdapter = lens.getAdapter(fabric, this);

        return Query.compile(compoundAdapter, new SlotLensQueryOperation(ImmutableSet.of((Inventory) compoundAdapter))).execute();
    }

    @Override
    public boolean containsInventory(final Inventory inventory) {
        final Inventory result = Query.compile((InventoryAdapter) this, new LensQueryOperation(((InventoryAdapter) inventory).bridge$getRootLens())).execute();
        return result.capacity() == inventory.capacity() && ((InventoryAdapter) result).bridge$getRootLens() == ((InventoryAdapter) inventory).bridge$getRootLens();
    }

    @Override
    public InventoryArchetype getArchetype() {
        return InventoryArchetypes.UNKNOWN;
    }
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        if (this.api$slotIterator == null) {
            this.api$slotIterator = new SlotCollectionIterator(this, ((InventoryAdapter) this).bridge$getFabric(), ((InventoryAdapter) this).bridge$getRootLens(), ((InventoryAdapter) this).bridge$getSlotProvider());
        }
        return (Iterable<T>) this.api$slotIterator;
    }

    // Soft implemented in development since the targets have the same method from IInventory
    @Intrinsic
    public void inventory$clear() {
        ((InventoryAdapter) this).bridge$getFabric().clear();
    }

}
