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
package org.spongepowered.common.item.inventory.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.property.GuiId;
import org.spongepowered.api.item.inventory.property.GuiIdProperty;
import org.spongepowered.api.item.inventory.property.GuiIds;
import org.spongepowered.api.item.inventory.property.InventoryCapacity;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.type.SpongeGuiId;
import org.spongepowered.common.item.inventory.archetype.CompositeInventoryArchetype;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class CustomInventory implements IInventory, IInteractionObject {

    public static final String INVENTORY_CAPACITY = InventoryCapacity.class.getSimpleName().toLowerCase(Locale.ENGLISH);
    public static final String INVENTORY_DIMENSION = InventoryDimension.class.getSimpleName().toLowerCase(Locale.ENGLISH);
    public static final String TITLE = InventoryTitle.class.getSimpleName().toLowerCase(Locale.ENGLISH);
    private final Map<Class<? extends InteractContainerEvent>, List<Consumer<? extends InteractContainerEvent>>> listeners;
    private final PluginContainer plugin;

    private net.minecraft.inventory.Inventory inv;
    protected InventoryArchetype archetype;
    private Map<String, InventoryProperty<?, ?>> properties;
    private Carrier carrier;

    private Set<PlayerEntity> viewers = new HashSet<>();
    private boolean registered;

    @SuppressWarnings("deprecation")
    public CustomInventory(final InventoryArchetype archetype, final Map<String, InventoryProperty<?, ?>> properties, final Carrier carrier,
            final Map<Class<? extends InteractContainerEvent>, List<Consumer<? extends InteractContainerEvent>>> listeners, final PluginContainer plugin) {
        this.archetype = archetype;
        this.properties = properties;
        this.carrier = carrier;
        this.listeners = listeners;
        this.plugin = plugin;

        final int count;
        final InventoryDimension size = (InventoryDimension)properties.get(INVENTORY_DIMENSION); // TODO INVENTORY_CAPACITY
        if (size != null) {
            count = size.getColumns() * size.getRows();
        } else {
            count = getSlotCount(archetype);
        }

        final InventoryTitle titleProperty = (InventoryTitle) properties.getOrDefault(TITLE, archetype.getProperty(TITLE).orElse(null));
        final boolean isCustom = !(titleProperty != null && titleProperty.getValue() instanceof TranslatableText);

        final String title = titleProperty == null ? "" :
                isCustom ? TextSerializers.LEGACY_FORMATTING_CODE.serialize(titleProperty.getValue())
                        : ((TranslatableText) titleProperty.getValue()).getTranslation().getId();
        this.inv = new net.minecraft.inventory.Inventory(title, isCustom, count);

        // Updates the Inventory for all viewers on any change
        this.inv.addListener(i -> this.viewers.forEach(v -> {
            v.openContainer.detectAndSendChanges();
        }));


    }

    private void doRegistration() {
        for (final Map.Entry<Class<? extends InteractContainerEvent>, List<Consumer<? extends InteractContainerEvent>>> entry: this.listeners.entrySet()) {
            Sponge.getEventManager().registerListener(this.plugin, entry.getKey(), new CustomInventoryListener((Inventory) this, entry.getValue()));
        }
        this.registered = true;
    }

    private static int getSlotCount(final InventoryArchetype archetype) {
        final Optional<InventoryDimension> dimension = archetype.getProperty(InventoryDimension.class, INVENTORY_DIMENSION);
        if (dimension.isPresent()) {
            return dimension.get().getColumns() * dimension.get().getRows();
        }
        final Optional<InventoryCapacity> capacity = archetype.getProperty(InventoryCapacity.class, INVENTORY_CAPACITY);
        if (capacity.isPresent()) {
            return capacity.get().getValue();
        }

        int count = 0;
        final List<InventoryArchetype> childs = archetype.getChildArchetypes();
        if (childs.isEmpty()) {
            throw new IllegalArgumentException("Missing dimensions!");
        }
        for (final InventoryArchetype childArchetype : childs) {
            count += getSlotCount(childArchetype);
        }
        return count;
    }

    public InventoryArchetype getArchetype() {
        return this.archetype;
    }

    @Override
    public Container createContainer(final PlayerInventory playerInventory, final PlayerEntity playerIn) {

        // To be viewable the Inventory has to implement IInteractionObject and thus provide a Container
        // displayChest falls back to Chest for IInventory instance

        // if the ArcheType given is CHEST or DOUBLECHEST
        // TODO how can this be checked? ContainerProperty? (Class<? extends Container>)
        // vanilla ContainerChest takes the Size of the InventoryBasic / 9 as rows


        // Display property?
        // TODO custom container including filters and other slot stuff
        this.viewers.add(playerIn);

        if (this.archetype instanceof CompositeInventoryArchetype) {
            final CompositeInventoryArchetype.ContainerProvider provider = ((CompositeInventoryArchetype) this.archetype).getContainerProvider();
            if (provider != null) {
                return provider.provide(this, playerIn);
            }
        }
        return new CustomContainer(playerIn, this);
    }

    @Override
    public String getGuiID() {
        final String key = AbstractInventoryProperty.getDefaultKey(GuiIdProperty.class).toString();
        final InventoryProperty<?, ?> property = this.properties.get(key);
        if (property instanceof GuiIdProperty) {
            if (property.getValue() instanceof SpongeGuiId) {
                return ((SpongeGuiId) property.getValue()).getInternalId(); // Handle Vanilla EntityHorse GuiId
            }
            return ((GuiIdProperty) property).getValue().getId();
        }
        final GuiId guiId = this.archetype.getProperty(GuiIdProperty.class, key).map(GuiIdProperty::getValue).orElse(GuiIds.CHEST);
        if (guiId instanceof SpongeGuiId) {
            return ((SpongeGuiId) guiId).getInternalId(); // Handle Vanilla EntityHorse GuiId
        }
        return guiId.getId();
    }

    // IInventory delegation

    @Override
    public ITextComponent getDisplayName() {
        return this.inv.getDisplayName();
    }

    @Override
    public String getName() {
        return this.inv.getName();
    }

    @Override
    public boolean hasCustomName() {
        return this.inv.hasCustomName();
    }

    @Override
    public int getSizeInventory() {
        return this.inv.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return this.inv.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(final int index) {
        return this.inv.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        return this.inv.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(final int index) {
        return this.inv.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        this.inv.setInventorySlotContents(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return this.inv.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        this.inv.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player) {
        return this.inv.isUsableByPlayer(player);
    }

    @Override
    public void openInventory(final PlayerEntity player) {
        this.viewers.add(player);
        this.inv.openInventory(player);
        this.ensureListenersRegistered();
    }

    @Override
    public void closeInventory(final PlayerEntity player) {
        this.viewers.remove(player);
        this.inv.closeInventory(player);
        if (this.viewers.isEmpty()) {
            Task.builder().execute(() -> {
                if (this.viewers.isEmpty()) {
                    Sponge.getEventManager().unregisterListeners(this);
                    this.registered = false;
                }
            }).delayTicks(1).submit(SpongeImpl.getPlugin());
        }
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return this.inv.isItemValidForSlot(index, stack);
    }

    @Override
    public int getField(final int id) {
        return this.inv.getField(id);
    }

    @Override
    public void setField(final int id, final int value) {
        this.inv.setField(id, value);
    }

    @Override
    public int getFieldCount() {
        return this.inv.getFieldCount();
    }

    @Override
    public void clear() {
        this.inv.clear();
    }

    public Map<String, InventoryProperty<?, ?>> getProperties() {
        return this.properties;
    }

    public Carrier getCarrier() {
        return this.carrier;
    }

    public void ensureListenersRegistered() {
        if (!this.registered) {
            this.doRegistration();
        }
    }
}
