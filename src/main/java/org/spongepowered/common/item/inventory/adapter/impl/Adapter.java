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
package org.spongepowered.common.item.inventory.adapter.impl;

import static com.google.common.base.Preconditions.*;

import net.minecraft.inventory.IInventory;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.InventoryIterator;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public class Adapter implements MinecraftInventoryAdapter {
    
    public static abstract class Logic {
        
        private Logic() {}
        
        public static Optional<ItemStack> pollSequential(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter) {
            return Logic.pollSequential(adapter.getInventory(), adapter.getRootLens());
        }
        
        public static Optional<ItemStack> pollSequential(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens) {
            if (lens == null) {
                return Optional.empty();
            }
            return Logic.findStack(inv, lens, true);
        }

        public static Optional<ItemStack> pollSequential(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter, int limit) {
            return Logic.pollSequential(adapter.getInventory(), adapter.getRootLens(), limit);
        }
        
        public static Optional<ItemStack> pollSequential(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, int limit) {
            if (lens == null) {
                return Optional.empty();
            }
            return Logic.findStacks(inv, lens, limit, true);
        }

        public static Optional<ItemStack> peekSequential(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter) {
            return Logic.peekSequential(adapter.getInventory(), adapter.getRootLens());
        }
        
        public static Optional<ItemStack> peekSequential(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens) {
            if (lens == null) {
                return Optional.empty();
            }
            return Logic.findStack(inv, lens, false);
        }
        
        public static Optional<ItemStack> peekSequential(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter, int limit) {
            return Logic.peekSequential(adapter.getInventory(), adapter.getRootLens(), limit);
        }
        
        public static Optional<ItemStack> peekSequential(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, int limit) {
            if (lens == null) {
                return Optional.empty();
            }
            return Logic.findStacks(inv, lens, limit, false);
        }
        
        private static Optional<ItemStack> findStack(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, boolean remove) {
            for (int ord = 0; ord < lens.slotCount(); ord++) {
                net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
                if (stack != null) {
                    System.err.println("Found stack!");
                }
                if (stack == null || (remove && !lens.setStack(inv, ord, null))) {
                    continue;
                }
                return ItemStackUtil.cloneDefensiveOptional(stack);
            }
            
            return Optional.empty();
        }

        private static Optional<ItemStack> findStacks(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, int limit, boolean remove) {
            ItemStack result = null; 
            
            for (int ord = 0; ord < lens.slotCount(); ord++) {
                net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
                if (stack == null || stack.stackSize < 1 || (result != null && !result.getItem().equals(stack.getItem()))) {
                    continue;
                }
                
                if (result == null) {
                    result = ItemStackUtil.cloneDefensive(stack, 0);
                }
                
                int pull = Math.min(stack.stackSize, limit);
                result.setQuantity(result.getQuantity() + pull);
                limit -= pull;
                
                if (!remove) {
                    continue;
                }
                
                if (pull >= stack.stackSize) {
                    lens.setStack(inv, ord, null);
                } else {
                    stack.stackSize -= pull;
                }
            }
            
            return Optional.ofNullable(result);
        }

        public static InventoryTransactionResult insertSequential(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter, ItemStack stack) {
            return Logic.insertSequential(adapter.getInventory(), adapter.getRootLens(), stack);
        }
        
        public static InventoryTransactionResult insertSequential(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, ItemStack stack) {
            if (lens == null) {
                return InventoryTransactionResult.builder().type(Type.FAILURE).reject(ItemStackUtil.cloneDefensive(stack)).build();
            }
            try {
                return Logic.insertStack(inv, lens, stack);
            } catch (Exception ex) {
               return InventoryTransactionResult.builder().type(Type.ERROR).reject(ItemStackUtil.cloneDefensive(stack)).build();
            }
        }

        private static InventoryTransactionResult insertStack(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, ItemStack stack) {
            InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(Type.SUCCESS);
            net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);
            
            int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.getMaxStackSize());
            int remaining = stack.getQuantity();
            
            for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
                net.minecraft.item.ItemStack old = lens.getStack(inv, ord);
                int push = Math.min(remaining, maxStackSize);
                if (lens.setStack(inv, ord, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
                    result.replace(ItemStackUtil.fromNative(old));
                    remaining -= push;
                }
            }
            
            if (remaining > 0) {
                result.reject(ItemStackUtil.cloneDefensive(nativeStack, remaining));
            }
            
            return result.build();
        }
        
        public static InventoryTransactionResult appendSequential(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter, ItemStack stack) {
            return Logic.appendSequential(adapter.getInventory(), adapter.getRootLens(), stack);
        }
        
        public static InventoryTransactionResult appendSequential(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, ItemStack stack) {
            InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(Type.SUCCESS);
            net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);
            
            int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.getMaxStackSize());
            int remaining = stack.getQuantity();
            
            for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
                net.minecraft.item.ItemStack old = lens.getStack(inv, ord);
                int push = Math.min(remaining, maxStackSize);
                if (old == null && lens.setStack(inv, ord, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
                    remaining -= push;
                } else if (old != null && ItemStackUtil.compare(old, stack)) {
                    push = Math.max(Math.min(maxStackSize - old.stackSize, remaining), 0); // max() accounts for oversized stacks
                    old.stackSize += push;
                    remaining -= push;
                }
            }
            
            if (remaining == stack.getQuantity()) {
                // No items were consumed
                result.type(Type.FAILURE).reject(ItemStackUtil.cloneDefensive(nativeStack));
            } else {
                stack.setQuantity(remaining);
            }
            
            return result.build();
        }

        public static int countStacks(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter) {
            return Logic.countStacks(adapter.getInventory(), adapter.getRootLens());
        }
        
        public static int countStacks(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens) {
            int stacks = 0;
            
            for (int ord = 0; ord < lens.slotCount(); ord++) {
                stacks += lens.getStack(inv, ord) != null ? 1 : 0;
            }
            
            return stacks;
        }

        public static int countItems(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter) {
            return Logic.countItems(adapter.getInventory(), adapter.getRootLens());
        }
        
        public static int countItems(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens) {
            int items = 0;
            
            for (int ord = 0; ord < lens.slotCount(); ord++) {
                net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
                items += stack != null ? stack.stackSize : 0;
            }
            
            return items;
        }

        public static int getCapacity(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter) {
            return Logic.getCapacity(adapter.getInventory(), adapter.getRootLens());
        }
        
        public static int getCapacity(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens) {
            return lens.getSlots().size();
        }

        public static Collection<InventoryProperty<?, ?>> getProperties(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter,
                Inventory child, Class<? extends InventoryProperty<?, ?>> property) {
            return Logic.getProperties(adapter.getInventory(), adapter.getRootLens(), child, property);
        }
            
        public static Collection<InventoryProperty<?, ?>> getProperties(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens,
                Inventory child, Class<? extends InventoryProperty<?, ?>> property) {
            
            if (child instanceof InventoryAdapter) {
                checkNotNull(property, "property");
                int index = lens.getChildren().indexOf(((InventoryAdapter<?, ?>) child).getRootLens());
                if (index > -1) {
                    return lens.getProperties(index).stream().filter(prop -> property.equals(prop.getClass()))
                            .collect(Collectors.toCollection(ArrayList::new));
                }
            }

            return Collections.emptyList();
        }

        public static boolean contains(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter, ItemStack stack) {
            return Logic.contains(adapter.getInventory(), adapter.getRootLens(), stack);
        }
        
        public static boolean contains(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, ItemStack stack) {
            // TODO contains
            return false;
        }

        public static boolean contains(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter, ItemType type) {
            return Logic.contains(adapter.getInventory(), adapter.getRootLens(), type);
        }

        public static boolean contains(Fabric<IInventory> inv, Lens<IInventory, net.minecraft.item.ItemStack> lens, ItemType type) {
            // TODO contains
            return false;
        }
    }
    
    public static class Iter extends InventoryIterator<IInventory, net.minecraft.item.ItemStack> {
        
        private final InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter;

        public Iter(InventoryAdapter<IInventory, net.minecraft.item.ItemStack> adapter) {
            super(adapter.getRootLens(), adapter.getInventory());
            this.adapter = adapter;
        }

        @Override
        public Inventory next() {
            try {
                return this.adapter.getChild(this.next++);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }
        
    }

    public static final Translation DEFAULT_NAME = new SpongeTranslation("inventory.default.title");

    /**
     * All inventories have their own empty inventory with themselves as the
     * parent. This empty inventory is initialised on-demand but returned for
     * every query which fails. This saves us from creating a new empty
     * inventory with this inventory as the parent for every failed query.
     */
    private EmptyInventory empty;  

    protected Inventory parent;
    protected Inventory next;
    
    protected final Fabric<IInventory> inventory;
    protected final SlotCollection slots;
    protected final Lens<IInventory, net.minecraft.item.ItemStack> lens;
    protected final List<Inventory> children = new ArrayList<Inventory>();
    protected Iterable<Slot> slotIterator; 
    
    public Adapter(Fabric<IInventory> inventory) {
        this(inventory, null, null);
    }
    
    public Adapter(Fabric<IInventory> inventory, Lens<IInventory, net.minecraft.item.ItemStack> root) {
        this(inventory, root, null);
    }
    
    public Adapter(Fabric<IInventory> inventory, Lens<IInventory, net.minecraft.item.ItemStack> root, Inventory parent) {
        this.inventory = inventory;
        this.parent = parent != null ? parent : this;
        this.slots = this.initSlots(inventory, root, parent);
        this.lens = root != null ? root : checkNotNull(this.initRootLens(), "root lens");
    }

    protected SlotCollection initSlots(Fabric<IInventory> inventory, Lens<IInventory, net.minecraft.item.ItemStack> root, Inventory parent) {
        if (parent instanceof InventoryAdapter) {
            @SuppressWarnings("unchecked")
            SlotProvider<IInventory, net.minecraft.item.ItemStack> slotProvider = ((InventoryAdapter<IInventory, net.minecraft.item.ItemStack>)parent).getSlotProvider();
            if (slotProvider instanceof SlotCollection) {
                return (SlotCollection) slotProvider;
            }
        }
        return new SlotCollection(inventory.getSize());
    }

    @Override
    public Inventory parent() {
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T first() {
        return (T) this.iterator().next();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T next() {
        return (T) this.emptyInventory();
    }

//    protected Inventory generateParent(Lens<IInventory, net.minecraft.item.ItemStack> root) {
//        Lens<IInventory, net.minecraft.item.ItemStack> parentLens = root.getParent();
//        if (parentLens == null) {
//            return this;
//        }
//        return parentLens.getAdapter(this.inventory);
//    }

    protected Lens<IInventory, net.minecraft.item.ItemStack> initRootLens() {
        int size = this.inventory.getSize();
        if (size == 0) {
            return new DefaultEmptyLens(this);
        }
        return new DefaultIndexedLens(0, size, this, this.slots);
    }
    
    @Override
    public SlotProvider<IInventory, net.minecraft.item.ItemStack> getSlotProvider() {
        return this.slots;
    }
    
    @Override
    public Lens<IInventory, net.minecraft.item.ItemStack> getRootLens() {
        return this.lens;
    }
    
    @Override
    public Fabric<IInventory> getInventory() {
        return this.inventory;
    }
    
    @Override
    public Inventory getChild(int index) {
        if (index < 0 || index >= this.lens.getChildren().size()) {
            throw new IndexOutOfBoundsException("No child at index: " + index);
        }
        while (index >= this.children.size()) {
            this.children.add(null);
        }
        Inventory child = this.children.get(index);
        if (child == null) {
            child = this.lens.getChildren().get(index).getAdapter(this.inventory, this);
            this.children.set(index, child);
        }
        return child;
    }
    
    @Override
    public Inventory getChild(Lens<IInventory, net.minecraft.item.ItemStack> lens) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void notify(Object source, InventoryEventArgs eventArgs) {
    }
    
    protected final EmptyInventory emptyInventory() {
        if (this.empty == null) {
            this.empty = new EmptyInventoryImpl(this);
        }
        return this.empty;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        if (this.slotIterator == null) {
            this.slotIterator = this.slots.getIterator(this);
        }
        return (Iterable<T>) this.slotIterator;
    }
    
    @Override
    public void clear() {
        this.inventory.clear();
    }

    public static Optional<Slot> forSlot(Fabric<IInventory> inv, SlotLens<IInventory, net.minecraft.item.ItemStack> slotLens, Inventory parent) {
        return slotLens == null ? Optional.<Slot>empty() : Optional.<Slot>ofNullable((Slot) slotLens.getAdapter(inv, parent));
    }

    @Override
    public PluginContainer getPlugin() {
        return null; // TODO
    }
}
