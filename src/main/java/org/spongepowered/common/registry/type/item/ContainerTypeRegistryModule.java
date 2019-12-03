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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.BeaconContainer;
import net.minecraft.inventory.container.BlastFurnaceContainer;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.inventory.container.CartographyContainer;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.DispenserContainer;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.inventory.container.GrindstoneContainer;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.inventory.container.LoomContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.inventory.container.SmokerContainer;
import net.minecraft.inventory.container.StonecutterContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeContainerType;
import org.spongepowered.common.data.type.SpongeContainerTypeEntity;
import org.spongepowered.common.inventory.lens.LensCreator;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.data.type.SpongeContainerTypeEntity;
import org.spongepowered.common.inventory.lens.LensCreator;
import org.spongepowered.common.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.FurnaceInventoryLens;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ContainerTypeRegistryModule implements CatalogRegistryModule<ContainerType>, AdditionalCatalogRegistryModule<ContainerType> {

    public static ContainerTypeRegistryModule getInstance() {
        return org.spongepowered.common.registry.type.item.ContainerTypeRegistryModule.Holder.INSTANCE;
    }

    @RegisterCatalog(ContainerTypes.class)
    private final Map<String, ContainerType> map = new HashMap<>();

    private Optional<ContainerType> get(String id) {
        return Optional.ofNullable(this.map.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Optional<ContainerType> get(CatalogKey key) {
        return this.get(key);
    }

    @Override
    public Collection<ContainerType> getAll() {
        return ImmutableList.copyOf(this.map.values());
    }

    @Override
    public void registerDefaults() {
        LensCreator lensCreator; // TODO

        // Containers backed by an inventory.

        this.register("blast_furnace",
                (id, i, p) -> new BlastFurnaceContainer(id, p.inventory, i, new IntArray(4)), FurnaceInventoryLens::new, 3);
        this.register("brewing_stand",
                (id, i, p) -> new BrewingStandContainer(id, p.inventory, i, new IntArray(2)), BrewingStandInventoryLens::new, 5);
        this.register("furnace",
                (id, i, p) -> new FurnaceContainer(id, p.inventory, i, new IntArray(4)), FurnaceInventoryLens::new, 3);
        this.register("generic_3x3",
                (id, i, p) -> new DispenserContainer(id, p.inventory, i), 3, 3);
        this.register("generic_9x1",
                genericChestProvider(net.minecraft.inventory.container.ContainerType.GENERIC_9X1, 1), 9, 1);
        this.register("generic_9x2",
                genericChestProvider(net.minecraft.inventory.container.ContainerType.GENERIC_9X2, 2), 9, 2);
        this.register("generic_9x3",
                genericChestProvider(net.minecraft.inventory.container.ContainerType.GENERIC_9X3, 3), 9, 3);
        this.register("generic_9x4",
                genericChestProvider(net.minecraft.inventory.container.ContainerType.GENERIC_9X4, 4), 9, 4);
        this.register("generic_9x5",
                genericChestProvider(net.minecraft.inventory.container.ContainerType.GENERIC_9X5, 5), 9, 5);
        this.register("generic_9x6",
                genericChestProvider(net.minecraft.inventory.container.ContainerType.GENERIC_9X6, 6), 9, 6);
        this.register("hopper",
                (id, i, p) -> new HopperContainer(id, p.inventory, i), 5, 1);
        this.register("lectern",
                (id, i, p) -> new LecternContainer(id, i, new IntArray(1)), lensCreator, 1); // TODO playerInv?
        this.register("shulker_box",
                (id, i, p) -> new ShulkerBoxContainer(id, p.inventory, i), 9, 3);
        this.register("smoker",
                (id, i, p) -> new SmokerContainer(id, p.inventory, i, new IntArray(4)), lensCreator, 3);
        // Containers with internal Inventory.
        this.register("anvil",
                (id, i, p) -> new RepairContainer(id, p.inventory, toPos(p)), lensCreator, 3);
        this.register("beacon",
                (id, i, p) -> new BeaconContainer(id, p.inventory, new IntArray(3), toPos(p)), lensCreator, 1);
        this.register("cartography",
                (id, i, p) -> new CartographyContainer(id, p.inventory, toPos(p)), lensCreator, 2);
        this.register("crafting", // TODO height & width?
                (id, i, p) -> new WorkbenchContainer(id, p.inventory, toPos(p)), lensCreator, 10);
        this.register("enchantment",
                (id, i, p) -> new EnchantmentContainer(id, p.inventory, toPos(p)), lensCreator, 2);
        this.register("grindstone",
                (id, i, p) -> new GrindstoneContainer(id, p.inventory, toPos(p)), lensCreator, 2);
        this.register("loom",
                (id, i, p) -> new LoomContainer(id, p.inventory, toPos(p)), lensCreator, 3);
        this.register("stonecutter",
                (id, i, p) -> new StonecutterContainer(id, p.inventory, toPos(p)), lensCreator, 1);

        // Containers that cannot be opened on their own. Create an Entity to open the container instead.
        this.registerEntity(CatalogKey.minecraft("horse"));
        //                (id, i, p) -> new HorseInventoryContainer(id, p.inventory, i, horse), lensCreator, size);
        this.registerEntity(CatalogKey.minecraft("merchant"));
        //                (id, i, p) -> new MerchantContainer(id, p.inventory, merchant), lensCreator, size);
    }

    private static IWorldPosCallable toPos(PlayerEntity p) {
        return IWorldPosCallable.of(p.world, p.getPosition());
    }

    private static ContainerProvider genericChestProvider(net.minecraft.inventory.container.ContainerType mcType, int rows) {
        return (id, i, p) -> new ChestContainer(mcType, id, p.inventory, i, rows);
    }


    private void registerEntity(CatalogKey key) {
        this.map.put(key, new SpongeContainerTypeEntity(key));
    }
    private void register(final CatalogKey key, ContainerProvider provider, LensCreator lensCreator, int size, int width, int height) {
        this.map.put(key, new SpongeContainerType(key, size, width, height, lensCreator, provider));
    }

    private void register(final String key, ContainerProvider provider, int width, int height) {
        this.register(CatalogKey.minecraft(key), provider, sp -> new GridInventoryLens(0, width, height, sp), width * height, width, height);
    }

    private void register(final String key, ContainerProvider provider, LensCreator lensCreator, int size) {
        this.register(CatalogKey.minecraft(key), provider, lensCreator, size, 0, 0);
    }


    @Override
    public void registerAdditionalCatalog(ContainerType guiId) {
        if (this.map.containsKey(guiId.getKey())) {
            throw new IllegalArgumentException("GuiId is already registered");
        }
        this.map.put(guiId.getKey(), guiId);
    }

    private ContainerTypeRegistryModule() {
    }

    private static final class Holder {

        static final ContainerTypeRegistryModule INSTANCE = new ContainerTypeRegistryModule();
    }

    /**
     * Provides a {@link Container} for a {@link PlayerEntity} viewing an {@link IInventory}
     */
    public interface ContainerProvider {

        Container provide(int id, IInventory viewed, PlayerEntity viewing);
    }
}
