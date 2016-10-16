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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class CustomInventory implements IInventory, IInteractionObject {

    public static final String INVENTORY_DIMENSION = InventoryDimension.class.getSimpleName().toLowerCase(Locale.ENGLISH);
    public static final String TITLE = InventoryTitle.class.getSimpleName().toLowerCase(Locale.ENGLISH);

    private InventoryBasic inv;
    protected InventoryArchetype archetype;
    private Map<String, InventoryProperty> properties;
    private Carrier carrier;

    private Set<EntityPlayer> viewers = new HashSet<>();

    public CustomInventory(InventoryArchetype archetype, Map<String, InventoryProperty> properties, Carrier carrier,
            Map<Class<? extends InteractInventoryEvent>, List<Consumer<? extends InteractInventoryEvent>>> listeners, Object plugin) {
        this.archetype = archetype;
        this.properties = properties;
        this.carrier = carrier;

        int count = 0;
        InventoryDimension size = (InventoryDimension)properties.getOrDefault(INVENTORY_DIMENSION,
                archetype.getProperty(INVENTORY_DIMENSION).orElse(null));
        if (size != null) {
            count = size.getColumns() * size.getRows();
        }
        else {
            for (InventoryArchetype childArchetype : archetype.getChildArchetypes()) {
                Optional<InventoryDimension> property = childArchetype.getProperty(InventoryDimension.class, INVENTORY_DIMENSION);
                if (!property.isPresent()) {
                    throw new IllegalArgumentException("Missing dimensions!");
                }
                count += property.get().getColumns() * property.get().getRows();
            }
        }

        InventoryTitle titleProperty = (InventoryTitle) properties.getOrDefault(TITLE, archetype.getProperty(TITLE).orElse(null));
        boolean isCustom = !(titleProperty != null && titleProperty.getValue() instanceof TranslatableText);

        String title = titleProperty == null ? "" :
                isCustom ? TextSerializers.LEGACY_FORMATTING_CODE.serialize(titleProperty.getValue())
                        : ((TranslatableText) titleProperty.getValue()).getTranslation().getId();
        this.inv = new InventoryBasic(title, isCustom, count);

        // Updates the Inventory for all viewers on any change
        this.inv.addInventoryChangeListener(i -> viewers.forEach(v -> {
            v.openContainer.detectAndSendChanges();
        }));

        for (Map.Entry<Class<? extends InteractInventoryEvent>, List<Consumer<? extends InteractInventoryEvent>>> entry: listeners.entrySet()) {
            Sponge.getEventManager().registerListener(plugin, entry.getKey(), new CustomInventoryListener((Inventory) this, entry.getValue()));
        }
    }

    public InventoryArchetype getArchetype() {
        return archetype;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {

        // To be viewable the Inventory has to implement IInteractionObject and thus provide a Container
        // displayChest falls back to Chest for IInventory instance

        // if the ArcheType given is CHEST or DOUBLECHEST
        // TODO how can this be checked? ContainerProperty? (Class<? extends Container>)
        // vanilla ContainerChest takes the Size of the InventoryBasic / 9 as rows


        // Display property?
        // TODO custom container including filters and other slot stuff
        this.viewers.add(playerIn);

        return new CustomContainer(playerIn, this);
    }

    @Override
    public String getGuiID() {
        if (archetype == InventoryArchetypes.CHEST || archetype == InventoryArchetypes.DOUBLE_CHEST) {
            return "minecraft:chest";
        } else if (archetype == InventoryArchetypes.HOPPER) {
            return "minecraft:hopper";
        } else if (archetype == InventoryArchetypes.DISPENSER) {
            return "minecraft:dispenser";
        } else if (archetype == InventoryArchetypes.WORKBENCH) {
            return "minecraft:crafting_table";
        } else if (archetype == InventoryArchetypes.FURNACE) {
            return "minecraft:furnace";
        } else if (archetype == InventoryArchetypes.ENCHANTING_TABLE) {
            return "minecraft:enchanting_table";
        } else if (archetype == InventoryArchetypes.ANVIL) {
            return "minecraft:anvil";
        } else if (archetype == InventoryArchetypes.BREWING_STAND) {
            return "minecraft:brewing_stand";
        } else if (archetype == InventoryArchetypes.BEACON) {
            return "minecraft:beacon";
        } else if (archetype == InventoryArchetypes.HORSE || archetype == InventoryArchetypes.HORSE_WITH_CHEST) {
            return "EntityHorse";
        } else if (archetype == InventoryArchetypes.VILLAGER) {
            return "minecraft:villager";
        } else {
            return "minecraft:chest";
        }
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
    public ItemStack getStackInSlot(int index) {
        return this.inv.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return this.inv.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return this.inv.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
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
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.inv.isUseableByPlayer(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        this.viewers.add(player);
        this.inv.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        this.viewers.remove(player);
        this.inv.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return this.inv.isItemValidForSlot(index, stack);
    }

    @Override
    public int getField(int id) {
        return this.inv.getField(id);
    }

    @Override
    public void setField(int id, int value) {
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

    public Map<String, InventoryProperty> getProperties() {
        return properties;
    }
}
