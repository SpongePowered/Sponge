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
package org.spongepowered.common.registry.type.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.api.item.inventory.gui.ContainerTypes;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.SpongeInventoryBuilder;
import org.spongepowered.common.item.inventory.archetype.SlotArchetype;
import org.spongepowered.common.item.inventory.archetype.SpongeInventoryArchetypeBuilder;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;
import org.spongepowered.common.registry.type.text.TextStyleRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

@RegisterCatalog(InventoryArchetypes.class)
@RegistrationDependency({ContainerTypeRegistryModule.class, TextColorRegistryModule.class, TextStyleRegistryModule.class})
public class InventoryArchetypeRegistryModule extends AbstractCatalogRegistryModule<InventoryArchetype>
    implements AlternateCatalogRegistryModule<InventoryArchetype>, SpongeAdditionalCatalogRegistryModule<InventoryArchetype> {

    public static InventoryArchetypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(InventoryArchetype archetype) {
        checkNotNull(archetype, "archetype");
        register(archetype);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void registerDefaults() {
        InventoryArchetype SLOT = new SlotArchetype(ImmutableMap.of(InventoryProperties.DIMENSION, new Vector2i(1, 1)));
        register(SLOT);
        InventoryArchetype MENU_ROW;
        InventoryArchetype MENU_COLUMN;
        InventoryArchetype MENU_GRID;
        InventoryArchetype CHEST;
        InventoryArchetype DOUBLE_CHEST;
        InventoryArchetype FURNACE;
        InventoryArchetype DISPENSER;
        InventoryArchetype WORKBENCH;
        InventoryArchetype BREWING_STAND;
        InventoryArchetype HOPPER;
        InventoryArchetype BEACON;
        InventoryArchetype ANVIL;
        InventoryArchetype ENCHANTING_TABLE;
        InventoryArchetype VILLAGER;
        InventoryArchetype HORSE;
        InventoryArchetype HORSE_WITH_CHEST;
        InventoryArchetype PLAYER;
        InventoryArchetype CRAFTING;
        InventoryArchetype UNKNOWN;


        final SpongeInventoryArchetypeBuilder builder = new SpongeInventoryArchetypeBuilder();
        for (int i = 0; i < 9; i++) {
            builder.with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(InventoryProperties.SLOT_INDEX, SlotIndex.of(i))
                .build("minecraft:slot" + i, "Slot"));
        }
        MENU_ROW = builder.property(InventoryProperties.DIMENSION, new Vector2i(9, 1))
            .build("sponge:menu_row", "Menu Row");

        MENU_COLUMN = builder.property(InventoryProperties.DIMENSION, new Vector2i(9, 1))
            .build("sponge:menu_column", "Menu Column");

        MENU_GRID = builder.reset()
            .with(MENU_ROW)
            .with(MENU_ROW)
            .with(MENU_ROW)
            .property(InventoryProperties.DIMENSION, new Vector2i(9, 3))
            .build("sponge:menu_grid", "Menu Grid");

        CHEST = builder.reset()
            .with(MENU_GRID)
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.chest")))
            .property(InventoryProperties.GUI_ID, GuiIds.CHEST)
            .container((i, p) -> new ContainerChest(p.inventory, i, p))
            .build("minecraft:chest", "Chest");

        DOUBLE_CHEST = builder.reset()
            .with(CHEST)
            .property(InventoryProperties.DIMENSION, new Vector2i(9, 6))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.chestDouble")))
            .property(InventoryProperties.GUI_ID, GuiIds.CHEST)
            .container((i, p) -> new ContainerChest(p.inventory, i, p))
            .build("minecraft:double_chest", "DoubleChest");

        FURNACE = builder.reset()
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(InventoryProperties.SLOT_INDEX, SlotIndex.of(0))
                .build("minecraft:furnace_input", "FurnaceInput"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(InventoryProperties.SLOT_INDEX, SlotIndex.of(1))
                .build("minecraft:furnace_fuel", "FurnaceFuel"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(InventoryProperties.SLOT_INDEX, SlotIndex.of(2))
                .build("minecraft:furnace_output", "FurnaceOutput"))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.furnace")))
            .property(InventoryProperties.DIMENSION, new Vector2i(3, 1))
            .property(InventoryProperties.GUI_ID, GuiIds.FURNACE)
            .container((i, p) -> new ContainerFurnace(p.inventory, i))
            .build("minecraft:furnace", "Furnace");

        DISPENSER = builder.reset()
            .with(MENU_GRID)
            .property(InventoryProperties.DIMENSION, new Vector2i(3, 3))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.dispenser")))
            .property(InventoryProperties.GUI_ID, GuiIds.DISPENSER)
            .container((i, p) -> new ContainerDispenser(p.inventory, i))
            .build("minecraft:dispenser", "Dispenser");

        WORKBENCH = builder.reset()
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(InventoryProperties.DIMENSION, new Vector2i(3, 3))
                .build("minecraft:workbench_grid", "Workbench Grid"))
            .with(SLOT)
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.crafting")))
            .property(InventoryProperties.GUI_ID, GuiIds.CRAFTING_TABLE)
            .container((i, p) -> {
                ContainerWorkbench container = new ContainerWorkbench(p.inventory, p.getEntityWorld(), p.getPosition());
                // Pre-Fills the container input with the items from the inventory
                for (int index = 0; index < container.craftMatrix.getSizeInventory(); index++) {
                    container.craftMatrix.setInventorySlotContents(index, i.getStackInSlot(index));
                }
                return container;
            })
            // TODO link inventory with container? (craftMatrix;craftResult)
            .build("minecraft:workbench", "Workbench");

        BREWING_STAND = builder.reset()
            .with(MENU_ROW)
            .property(InventoryProperties.DIMENSION, new Vector2i(5, 1))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.brewing")))
            .property(InventoryProperties.GUI_ID, GuiIds.BREWING_STAND)
            .container((i, p) -> new ContainerBrewingStand(p.inventory, i))
            .build("minecraft:brewing_stand", "BrewingStand");

        HOPPER = builder.reset()
            .with(MENU_ROW)
            .property(InventoryProperties.DIMENSION, new Vector2i(5, 1))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.hopper")))
            .property(InventoryProperties.GUI_ID, GuiIds.HOPPER)
            .container((i, p) -> new ContainerHopper(p.inventory, i, p))
            .build("minecraft:hopper", "Hopper");

        BEACON = builder.reset()
            .with(SLOT)
            .property(InventoryProperties.DIMENSION, new Vector2i(1, 1))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.beacon")))
            .property(InventoryProperties.GUI_ID, GuiIds.BEACON)
            .container((i, p) -> new ContainerBeacon(p.inventory, i))
            .build("minecraft:beacon", "Beacon");

        ENCHANTING_TABLE = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .property(InventoryProperties.DIMENSION, new Vector2i(2, 1))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.enchant")))
            .property(InventoryProperties.GUI_ID, GuiIds.ENCHANTING_TABLE)
            .container((i, p) -> {
                ContainerEnchantment container = new ContainerEnchantment(p.inventory, p.getEntityWorld(), p.getPosition());
                // Pre-Fills the container with the items from the inventory
                for (int index = 0; index < container.tableInventory.getSizeInventory(); index++) {
                    container.tableInventory.setInventorySlotContents(index, i.getStackInSlot(index));
                }
                return container;
            })
            // TODO link inventory to container (tableInventory)
            .build("minecraft:enchanting_table", "EnchantingTable");

        ANVIL = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .with(SLOT)
            .property(InventoryProperties.DIMENSION, new Vector2i(3, 1))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.repair")))
            .property(InventoryProperties.GUI_ID, GuiIds.ANVIL)
            .container((i, p) -> {
                ContainerRepair container = new ContainerRepair(p.inventory, p.getEntityWorld(), p.getPosition(), p);
                // Pre-Fills the container input with the items from the inventory
                for (int index = 0; index < container.inputSlots.getSizeInventory(); index++) {
                    container.inputSlots.setInventorySlotContents(index, i.getStackInSlot(index));
                }
                return container;
            })
            // TODO link inventory to container (outputSlot;inputSlots)
            .build("minecraft:anvil", "Anvil");

        VILLAGER = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .with(SLOT)
            .property(InventoryProperties.DIMENSION, new Vector2i(3, 1))
            .property(InventoryProperties.GUI_ID, GuiIds.VILLAGER)
                .container((i, p) -> {
                    if (i instanceof CarriedInventory
                            && ((CarriedInventory) i).getCarrier().isPresent()
                            && ((CarriedInventory) i).getCarrier().get() instanceof IMerchant) {
                        IMerchant merchant = ((IMerchant) ((CarriedInventory) i).getCarrier().get());
                        // TODO Pre-Fill the Container?
                        return new ContainerMerchant(p.inventory, merchant, p.getEntityWorld());
                    }
                    throw new IllegalArgumentException("Cannot open merchant inventory without a merchant as Carrier");
                })
            .build("minecraft:villager", "Villager");

        HORSE = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .property(InventoryProperties.DIMENSION, new Vector2i(2, 1))
            .property(InventoryProperties.GUI_ID, GuiIds.HORSE) // hardcoded openGuiHorseInventory
                .container((i, p) -> {
                    if (i instanceof CarriedInventory
                            && ((CarriedInventory) i).getCarrier().isPresent()
                            && ((CarriedInventory) i).getCarrier().get() instanceof AbstractHorse) {
                        AbstractHorse horse = ((AbstractHorse) ((CarriedInventory) i).getCarrier().get());
                        return new ContainerHorseInventory(p.inventory, i, horse, p);
                    }
                    throw new IllegalArgumentException("Cannot open horse inventory without a horse as Carrier");
                })
            .build("minecraft:horse", "Horse");

        HORSE_WITH_CHEST = builder.reset()
            .with(HORSE)
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(InventoryProperties.DIMENSION, new Vector2i(5,3))
                .build("horse_grid", "HorseGrid"))
            // TODO Size
            .property(InventoryProperties.GUI_ID, GuiIds.HORSE) // hardcoded openGuiHorseInventory
            .container((i, p) -> {
                if (i instanceof CarriedInventory
                        && ((CarriedInventory) i).getCarrier().isPresent()
                        && ((CarriedInventory) i).getCarrier().get() instanceof AbstractHorse) {
                    AbstractHorse horse = ((AbstractHorse) ((CarriedInventory) i).getCarrier().get());
                    // TODO size
                    return new ContainerHorseInventory(p.inventory, i, horse, p);
                }
                throw new IllegalArgumentException("Cannot open horse inventory without a horse as Carrier");
            })
            .build("minecraft:horse_with_chest", "Horse with Chest");

        CRAFTING = builder.reset()
            .with(SLOT)
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(InventoryProperties.DIMENSION, new Vector2i(2, 2))
                .build("minecraft:crafting_grid", "Crafting Grid"))
            .property(InventoryProperties.TITLE, Text.of(new SpongeTranslation("container.crafting")))
            .build("minecraft:crafting", "Crafting");

        PLAYER = builder.reset()
            .with(CRAFTING)
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(InventoryProperties.DIMENSION, new Vector2i(1, 4))
                .build("minecraft:armor", "Armor"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(InventoryProperties.DIMENSION, new Vector2i(9, 3))
                .build("minecraft:player_main", "Player Main"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(InventoryProperties.DIMENSION, new Vector2i(9, 1))
                .build("minecraft:player_hotbar", "Player Hotbar"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(InventoryProperties.DIMENSION, new Vector2i(1, 1))
                .build("minecraft:player_offhand", "Player Offhand"))
            .build("minecraft:player", "Player");

        UNKNOWN = builder.reset()
            .build("minecraft:unknown", "UKNOWN");


        SpongeInventoryBuilder.registerInventory(TileEntityChest.class, CHEST);
        SpongeInventoryBuilder.registerContainer(ContainerChest.class, CHEST);
        SpongeInventoryBuilder.registerInventory(TileEntityFurnace.class, FURNACE);
        SpongeInventoryBuilder.registerContainer(ContainerFurnace.class, FURNACE);
        SpongeInventoryBuilder.registerInventory(TileEntityDispenser.class, DISPENSER);
        SpongeInventoryBuilder.registerInventory(TileEntityDropper.class, DISPENSER);
        SpongeInventoryBuilder.registerContainer(ContainerDispenser.class, DISPENSER);
        SpongeInventoryBuilder.registerContainer(ContainerWorkbench.class, WORKBENCH);
        SpongeInventoryBuilder.registerInventory(TileEntityBrewingStand.class, BREWING_STAND);
        SpongeInventoryBuilder.registerContainer(ContainerBrewingStand.class, BREWING_STAND);
        SpongeInventoryBuilder.registerInventory(TileEntityHopper.class, HOPPER);
        SpongeInventoryBuilder.registerContainer(ContainerHopper.class, HOPPER);
        SpongeInventoryBuilder.registerInventory(TileEntityBeacon.class, BEACON);
        SpongeInventoryBuilder.registerContainer(ContainerBeacon.class, BEACON);
        SpongeInventoryBuilder.registerContainer(ContainerEnchantment.class, ENCHANTING_TABLE);
        SpongeInventoryBuilder.registerContainer(ContainerRepair.class, ANVIL);
        // TODO internal Villager Inventory? make Villager Carrier?
        SpongeInventoryBuilder.registerContainer(ContainerMerchant.class, VILLAGER);
        // TODO Horse IInventory? SpongeInventoryBuilder.registerInventory(EntityHorse.class, HORSE);
        SpongeInventoryBuilder.registerContainer(ContainerHorseInventory.class, HORSE);

        // Helper Archetypes for Menu
        InventoryArchetype MENU_ICON;
        InventoryArchetype MENU_BUTTON;
        InventoryArchetype MENU_CHECKBOX;
        InventoryArchetype MENU_SPINNER;

        MENU_ICON = builder.reset()
            .with(SLOT)
            // TODO show item as icon - no interaction
            .build("sponge:menu_icon", "Menu Icon");
        MENU_BUTTON = builder.reset()
            .with(MENU_ICON)
            // TODO icon + run code on click
            .build("sponge:menu_button", "Menu Button");
        MENU_CHECKBOX = builder.reset()
            .with(MENU_ICON)
            // TODO 2 different icons
            .build("sponge:menu_checkbox", "Menu Checkbox");
        MENU_SPINNER = builder.reset()
            .with(MENU_ICON)
            // TODO icon + count up and down on click
            .build("sponge:menu_spinner", "Menu Spinner");

    }

    InventoryArchetypeRegistryModule() {}

    private static final class Holder {
        static final InventoryArchetypeRegistryModule INSTANCE = new InventoryArchetypeRegistryModule();
    }
}
