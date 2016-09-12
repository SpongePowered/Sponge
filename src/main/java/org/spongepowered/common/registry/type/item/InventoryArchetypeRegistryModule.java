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
import com.google.common.collect.Maps;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
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
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.property.AcceptsItems;
import org.spongepowered.api.item.inventory.property.InventorySize;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.TitleProperty;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.SpongeInventoryBuilder;
import org.spongepowered.common.item.inventory.archetype.SlotArchetype;
import org.spongepowered.common.item.inventory.archetype.SpongeInventoryArchetypeBuilder;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
        map.putAll(mapping);
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

    @Override
    public void registerDefaults() {
        InventoryArchetype SLOT = new SlotArchetype(ImmutableMap.of("InventorySize", new InventorySize(1, 1)));
        InventoryArchetype MENU_ROW;
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

        //--------------------------------------------------------------
        InventoryArchetype.Builder builder = new SpongeInventoryArchetypeBuilder();
        for (int i = 0; i < 9; i++) {
            builder.with(new SpongeInventoryArchetypeBuilder().from(SLOT).property(SlotIndex.of(i)).build("minecraft:slot" + i, "Slot"));
        }
        MENU_ROW = builder.build("sponge:menu_row", "MenuRow");
        //--------------------------------------------------------------
        builder = new SpongeInventoryArchetypeBuilder();
        for (int i = 0; i < 3; i++) {
            //builder.with(new SpongeInventoryArchetypeBuilder().from(MENU_ROW).property(RowProperty.of(i)).build("minecraft:menurow" + i,
            // "MenuRow"));
        }
        builder.property(new InventorySize(9, 3));
        MENU_GRID = builder.build("sponge:menu_grid", "MenuGrid");
        //--------------------------------------------------------------
        builder = new SpongeInventoryArchetypeBuilder();
        builder.with(MENU_GRID)
                .property(new InventorySize(9, 3))
                .property(TitleProperty.of(Text.of(new SpongeTranslation("minecraft.tile.chest.name"))));
        CHEST = builder.build("minecraft:chest", "Chest");
        //--------------------------------------------------------------
        builder = new SpongeInventoryArchetypeBuilder();
        builder.with(CHEST)
                .with(CHEST)
                .property(new InventorySize(9, 6))
                .property(TitleProperty.of(Text.of(new SpongeTranslation("minecraft.tile.chest.name"))));
        DOUBLE_CHEST = builder.build("minecraft:double_chest", "DoubleChest");
        //--------------------------------------------------------------
        FURNACE = new SpongeInventoryArchetypeBuilder()
                .with(new SpongeInventoryArchetypeBuilder().from(SLOT).property(new SlotIndex(0)).build("minecraft:furnace_input", "FurnaceInput"))
                .with(new SpongeInventoryArchetypeBuilder().from(SLOT).property(new SlotIndex(1)).property(AcceptsItems.of(/*fuelsPredicate?*/))
                        .build("minecraft:furnace_fuel", "FurnaceFuel"))
                .with(new SpongeInventoryArchetypeBuilder().from(SLOT).property(new SlotIndex(2)).property(AcceptsItems.of()).build("minecraft:furnace_output", "FurnaceOutput"))
                .property(new TitleProperty(Text.of(new SpongeTranslation("minecraft.tile.furnace.name"))))
                .property(new InventorySize(3, 1))
                .build("minecraft:furnace", "Furnace");
        DISPENSER = new SpongeInventoryArchetypeBuilder()
                .with(MENU_GRID)
                .property(new InventorySize(3, 3))
                .build("minecraft:dispenser", "Dispenser");

        WORKBENCH = new SpongeInventoryArchetypeBuilder()
                .with(new SpongeInventoryArchetypeBuilder().from(MENU_GRID).property(new InventorySize(3, 3)).build("minecraft:workbench_grid", "Workbench Grid"))
                .with(SLOT)
                .build("minecraft:workbench", "Workbench");

        BREWING_STAND = new SpongeInventoryArchetypeBuilder()
                .with(MENU_ROW)
                .property(new InventorySize(4, 1))
                .build("minecraft:brewing_stand", "BrewingStand");

        HOPPER = new SpongeInventoryArchetypeBuilder()
                .with(MENU_ROW)
                .property(new InventorySize(5, 1))
                .build("minecraft:hopper", "Hopper");

        BEACON = new SpongeInventoryArchetypeBuilder()
                .with(SLOT)
                .property(new InventorySize(1, 1))
                .build("minecraft:beacon", "Beacon");

        ENCHANTING_TABLE = new SpongeInventoryArchetypeBuilder()
                .with(SLOT).with(SLOT)
                .property(new InventorySize(2, 1))
                .build("minecraft:enchanting_table", "EnchantingTable");

        ANVIL = new SpongeInventoryArchetypeBuilder()
                .with(SLOT).with(SLOT).with(SLOT)
                .property(new InventorySize(3, 1))
                .build("minecraft:anvil", "Anvil");

        VILLAGER = new SpongeInventoryArchetypeBuilder()
                .with(SLOT).with(SLOT).with(SLOT)
                .property(new InventorySize(3, 1))
                .build("minecraft:villager", "Villager");

        HORSE = new SpongeInventoryArchetypeBuilder()
                .with(SLOT).with(SLOT)
                .property(new InventorySize(2, 1))
                .build("minecraft:horse", "Horse");
        HORSE_WITH_CHEST = new SpongeInventoryArchetypeBuilder()
                .with(HORSE)
                .with(new SpongeInventoryArchetypeBuilder().from(MENU_GRID).property(new InventorySize(5,3)).build("horse_grid", "HorseGrid"))
                // TODO Size
                .build("minecraft:horse_with_chest", "Horse with Chest");

        CRAFTING = new SpongeInventoryArchetypeBuilder()
                .with(SLOT)
                .with(new SpongeInventoryArchetypeBuilder().from(MENU_GRID).property(new InventorySize(2, 2)).build("minecraft:crafting_grid",
                        "Crafting Grid"))
                .build("minecraft:crafting", "Crafting");

        PLAYER = new SpongeInventoryArchetypeBuilder()
                .with(CRAFTING)
                .with(new SpongeInventoryArchetypeBuilder().from(MENU_GRID).property(new InventorySize(1, 4)).build("minecraft:armor", "Armor"))
                .with(new SpongeInventoryArchetypeBuilder().from(MENU_GRID).property(new InventorySize(9, 3)).build("minecraft:player_main",
                        "Player Main"))
                .with(new SpongeInventoryArchetypeBuilder().from(MENU_GRID).property(new InventorySize(9, 1)).build("minecraft:player_hotbar",
                        "Player Hotbar"))
                .build("minecraft:player", "Player");

        registerAdditionalCatalog(SLOT);
        registerAdditionalCatalog(MENU_ROW);
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
    }

    InventoryArchetypeRegistryModule() {}

    private static final class Holder {
        static final InventoryArchetypeRegistryModule INSTANCE = new InventoryArchetypeRegistryModule();
    }
}
