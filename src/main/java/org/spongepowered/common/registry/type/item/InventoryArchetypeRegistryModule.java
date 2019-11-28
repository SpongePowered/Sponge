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

import com.google.common.collect.ImmutableList;
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
import org.spongepowered.api.item.inventory.property.AcceptsItems;
import org.spongepowered.api.item.inventory.property.GuiIdProperty;
import org.spongepowered.api.item.inventory.property.GuiIds;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.SpongeInventoryBuilder;
import org.spongepowered.common.item.inventory.archetype.SlotArchetype;
import org.spongepowered.common.item.inventory.archetype.SpongeInventoryArchetypeBuilder;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.mixin.core.inventory.ContainerRepairAccessor;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(GuiIdRegistryModule.class)
public class InventoryArchetypeRegistryModule implements AlternateCatalogRegistryModule<InventoryArchetype>,
    SpongeAdditionalCatalogRegistryModule<InventoryArchetype> {

    public static InventoryArchetypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(InventoryArchetypes.class)
    private final Map<String, InventoryArchetype> mapping = new HashMap<>();

    @Override
    public Map<String, InventoryArchetype> provideCatalogMap() {

        Map<String, InventoryArchetype> map = new HashMap<>();
        map.putAll(this.mapping);
        for (Map.Entry<String, InventoryArchetype> entry : this.mapping.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", "").replace("sponge:", ""), entry.getValue());
        }
        return map;
    }

    @Override
    public Optional<InventoryArchetype> getById(String id) {
        return Optional.ofNullable(this.mapping.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<InventoryArchetype> getAll() {
        return ImmutableList.copyOf(this.mapping.values());
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(InventoryArchetype archetype) {
        checkNotNull(archetype, "archetype");
        String id = archetype.getId();
        this.mapping.put(id.toLowerCase(Locale.ENGLISH), archetype);
    }

    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    @Override
    public void registerDefaults() {
        InventoryArchetype SLOT = new SlotArchetype(ImmutableMap.of(CustomInventory.INVENTORY_DIMENSION, new InventoryDimension(1, 1)));
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
                .property(SlotIndex.of(i))
                .build("minecraft:slot" + i, "Slot"));
        }
        MENU_ROW = builder.property(new InventoryDimension(9, 1))
            .build("sponge:menu_row", "Menu Row");

        MENU_COLUMN = builder.property(new InventoryDimension(9, 1))
            .build("sponge:menu_column", "Menu Column");

        MENU_GRID = builder.reset()
            .with(MENU_ROW)
            .with(MENU_ROW)
            .with(MENU_ROW)
            .property(new InventoryDimension(9, 3))
            .build("sponge:menu_grid", "Menu Grid");

        CHEST = builder.reset()
            .with(MENU_GRID)
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.chest"))))
            .property(new GuiIdProperty(GuiIds.CHEST))
            .container((i, p) -> new ContainerChest(p.field_71071_by, i, p))
            .build("minecraft:chest", "Chest");

        DOUBLE_CHEST = builder.reset()
            .with(CHEST)
            .property(new InventoryDimension(9, 6))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.chestDouble"))))
            .property(new GuiIdProperty(GuiIds.CHEST))
            .container((i, p) -> new ContainerChest(p.field_71071_by, i, p))
            .build("minecraft:double_chest", "DoubleChest");

        FURNACE = builder.reset()
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(new SlotIndex(0))
                .build("minecraft:furnace_input", "FurnaceInput"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(new SlotIndex(1))
                .property(AcceptsItems.of(/*fuelsPredicate?*/))
                .build("minecraft:furnace_fuel", "FurnaceFuel"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(new SlotIndex(2))
                .property(AcceptsItems.of())
                .build("minecraft:furnace_output", "FurnaceOutput"))
            .property(new InventoryTitle(Text.of(new SpongeTranslation("container.furnace"))))
            .property(new InventoryDimension(3, 1))
            .property(new GuiIdProperty(GuiIds.FURNACE))
            .container((i, p) -> new ContainerFurnace(p.field_71071_by, i))
            .build("minecraft:furnace", "Furnace");

        DISPENSER = builder.reset()
            .with(MENU_GRID)
            .property(new InventoryDimension(3, 3))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.dispenser"))))
            .property(new GuiIdProperty(GuiIds.DISPENSER))
            .container((i, p) -> new ContainerDispenser(p.field_71071_by, i))
            .build("minecraft:dispenser", "Dispenser");

        WORKBENCH = builder.reset()
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(new InventoryDimension(3, 3))
                .build("minecraft:workbench_grid", "Workbench Grid"))
            .with(SLOT)
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.crafting"))))
            .property(new GuiIdProperty(GuiIds.CRAFTING_TABLE))
            .container((i, p) -> {
                ContainerWorkbench container = new ContainerWorkbench(p.field_71071_by, p.func_130014_f_(), p.func_180425_c());
                // Pre-Fills the container input with the items from the inventory
                for (int index = 0; index < container.field_75162_e.func_70302_i_(); index++) {
                    container.field_75162_e.func_70299_a(index, i.func_70301_a(index));
                }
                return container;
            })
            // TODO link inventory with container? (craftMatrix;craftResult)
            .build("minecraft:workbench", "Workbench");

        BREWING_STAND = builder.reset()
            .with(MENU_ROW)
            .property(new InventoryDimension(5, 1))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.brewing"))))
            .property(new GuiIdProperty(GuiIds.BREWING_STAND))
            .container((i, p) -> new ContainerBrewingStand(p.field_71071_by, i))
            .build("minecraft:brewing_stand", "BrewingStand");

        HOPPER = builder.reset()
            .with(MENU_ROW)
            .property(new InventoryDimension(5, 1))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.hopper"))))
            .property(new GuiIdProperty(GuiIds.HOPPER))
            .container((i, p) -> new ContainerHopper(p.field_71071_by, i, p))
            .build("minecraft:hopper", "Hopper");

        BEACON = builder.reset()
            .with(SLOT)
            .property(new InventoryDimension(1, 1))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.beacon"))))
            .property(new GuiIdProperty(GuiIds.BEACON))
            .container((i, p) -> new ContainerBeacon(p.field_71071_by, i))
            .build("minecraft:beacon", "Beacon");

        ENCHANTING_TABLE = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .property(new InventoryDimension(2, 1))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.enchant"))))
            .property(new GuiIdProperty(GuiIds.ENCHANTING_TABLE))
            .container((i, p) -> {
                ContainerEnchantment container = new ContainerEnchantment(p.field_71071_by, p.func_130014_f_(), p.func_180425_c());
                // Pre-Fills the container with the items from the inventory
                for (int index = 0; index < container.field_75168_e.func_70302_i_(); index++) {
                    container.field_75168_e.func_70299_a(index, i.func_70301_a(index));
                }
                return container;
            })
            // TODO link inventory to container (tableInventory)
            .build("minecraft:enchanting_table", "EnchantingTable");

        ANVIL = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .with(SLOT)
            .property(new InventoryDimension(3, 1))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.repair"))))
            .property(new GuiIdProperty(GuiIds.ANVIL))
            .container((i, p) -> {
                ContainerRepair container = new ContainerRepair(p.field_71071_by, p.func_130014_f_(), p.func_180425_c(), p);
                // Pre-Fills the container input with the items from the inventory
                for (int index = 0; index < ((ContainerRepairAccessor) container).accessor$getInputSlots().func_70302_i_(); index++) {
                    ((ContainerRepairAccessor) container).accessor$getInputSlots().func_70299_a(index, i.func_70301_a(index));
                }
                return container;
            })
            // TODO link inventory to container (outputSlot;inputSlots)
            .build("minecraft:anvil", "Anvil");

        VILLAGER = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .with(SLOT)
            .property(new InventoryDimension(3, 1))
            .property(new GuiIdProperty(GuiIds.VILLAGER))
                .container((i, p) -> {
                    if (i instanceof CarriedInventory
                            && ((CarriedInventory) i).getCarrier().isPresent()
                            && ((CarriedInventory) i).getCarrier().get() instanceof IMerchant) {
                        IMerchant merchant = ((IMerchant) ((CarriedInventory) i).getCarrier().get());
                        ContainerMerchant container = new ContainerMerchant(p.field_71071_by, merchant, p.func_130014_f_());
                        // TODO Pre-Fill the Container?
                        return container;
                    }
                    throw new IllegalArgumentException("Cannot open merchant inventory without a merchant as Carrier");
                })
            .build("minecraft:villager", "Villager");

        HORSE = builder.reset()
            .with(SLOT)
            .with(SLOT)
            .property(new InventoryDimension(2, 1))
            .property(new GuiIdProperty(GuiIds.HORSE)) // hardcoded openGuiHorseInventory
                .container((i, p) -> {
                    if (i instanceof CarriedInventory
                            && ((CarriedInventory) i).getCarrier().isPresent()
                            && ((CarriedInventory) i).getCarrier().get() instanceof AbstractHorse) {
                        AbstractHorse horse = ((AbstractHorse) ((CarriedInventory) i).getCarrier().get());
                        return new ContainerHorseInventory(p.field_71071_by, i, horse, p);
                    }
                    throw new IllegalArgumentException("Cannot open horse inventory without a horse as Carrier");
                })
            .build("minecraft:horse", "Horse");

        HORSE_WITH_CHEST = builder.reset()
            .with(HORSE)
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(new InventoryDimension(5,3))
                .build("horse_grid", "HorseGrid"))
            // TODO Size
            .property(new GuiIdProperty(GuiIds.HORSE)) // hardcoded openGuiHorseInventory
            .container((i, p) -> {
                if (i instanceof CarriedInventory
                        && ((CarriedInventory) i).getCarrier().isPresent()
                        && ((CarriedInventory) i).getCarrier().get() instanceof AbstractHorse) {
                    AbstractHorse horse = ((AbstractHorse) ((CarriedInventory) i).getCarrier().get());
                    // TODO size
                    return new ContainerHorseInventory(p.field_71071_by, i, horse, p);
                }
                throw new IllegalArgumentException("Cannot open horse inventory without a horse as Carrier");
            })
            .build("minecraft:horse_with_chest", "Horse with Chest");

        CRAFTING = builder.reset()
            .with(SLOT)
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(new InventoryDimension(2, 2))
                .build("minecraft:crafting_grid", "Crafting Grid"))
            .property(InventoryTitle.of(Text.of(new SpongeTranslation("container.crafting"))))
            .build("minecraft:crafting", "Crafting");

        PLAYER = builder.reset()
            .with(CRAFTING)
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(new InventoryDimension(1, 4))
                .build("minecraft:armor", "Armor"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(new InventoryDimension(9, 3))
                .build("minecraft:player_main", "Player Main"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(MENU_GRID)
                .property(new InventoryDimension(9, 1))
                .build("minecraft:player_hotbar", "Player Hotbar"))
            .with(new SpongeInventoryArchetypeBuilder()
                .from(SLOT)
                .property(new InventoryDimension(1, 1))
                .build("minecraft:player_offhand", "Player Offhand"))
            .build("minecraft:player", "Player");

        UNKNOWN = builder.reset()
            .build("minecraft:unknown", "UNKNOWN");

        registerAdditionalCatalog(SLOT);
        registerAdditionalCatalog(MENU_ROW);
        registerAdditionalCatalog(MENU_COLUMN);
        registerAdditionalCatalog(MENU_GRID);
        registerAdditionalCatalog(CHEST);
        SpongeInventoryBuilder.registerInventory(TileEntityChest.class, CHEST);
        SpongeInventoryBuilder.registerContainer(ContainerChest.class, CHEST);
        registerAdditionalCatalog(DOUBLE_CHEST);
        registerAdditionalCatalog(FURNACE);
        SpongeInventoryBuilder.registerInventory(TileEntityFurnace.class, FURNACE);
        SpongeInventoryBuilder.registerContainer(ContainerFurnace.class, FURNACE);
        registerAdditionalCatalog(DISPENSER);
        SpongeInventoryBuilder.registerInventory(TileEntityDispenser.class, DISPENSER);
        SpongeInventoryBuilder.registerInventory(TileEntityDropper.class, DISPENSER);
        SpongeInventoryBuilder.registerContainer(ContainerDispenser.class, DISPENSER);
        registerAdditionalCatalog(WORKBENCH);
        SpongeInventoryBuilder.registerContainer(ContainerWorkbench.class, WORKBENCH);
        registerAdditionalCatalog(BREWING_STAND);
        SpongeInventoryBuilder.registerInventory(TileEntityBrewingStand.class, BREWING_STAND);
        SpongeInventoryBuilder.registerContainer(ContainerBrewingStand.class, BREWING_STAND);
        registerAdditionalCatalog(HOPPER);
        SpongeInventoryBuilder.registerInventory(TileEntityHopper.class, HOPPER);
        SpongeInventoryBuilder.registerContainer(ContainerHopper.class, HOPPER);
        registerAdditionalCatalog(BEACON);
        SpongeInventoryBuilder.registerInventory(TileEntityBeacon.class, BEACON);
        SpongeInventoryBuilder.registerContainer(ContainerBeacon.class, BEACON);
        registerAdditionalCatalog(ENCHANTING_TABLE);
        SpongeInventoryBuilder.registerContainer(ContainerEnchantment.class, ENCHANTING_TABLE);
        registerAdditionalCatalog(ANVIL);
        SpongeInventoryBuilder.registerContainer(ContainerRepair.class, ANVIL);
        registerAdditionalCatalog(VILLAGER);
        // TODO internal Villager Inventory? make Villager Carrier?
        SpongeInventoryBuilder.registerContainer(ContainerMerchant.class, VILLAGER);
        registerAdditionalCatalog(HORSE);
        // TODO Horse IInventory? SpongeInventoryBuilder.registerInventory(EntityHorse.class, HORSE);
        SpongeInventoryBuilder.registerContainer(ContainerHorseInventory.class, HORSE);
        registerAdditionalCatalog(HORSE_WITH_CHEST);
        registerAdditionalCatalog(CRAFTING);
        registerAdditionalCatalog(PLAYER);
        registerAdditionalCatalog(UNKNOWN);

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

        registerAdditionalCatalog(MENU_ICON);
        registerAdditionalCatalog(MENU_BUTTON);
        registerAdditionalCatalog(MENU_CHECKBOX);
        registerAdditionalCatalog(MENU_SPINNER);
    }

    private InventoryArchetypeRegistryModule() {}

    private static final class Holder {
        static final InventoryArchetypeRegistryModule INSTANCE = new InventoryArchetypeRegistryModule();
    }
}