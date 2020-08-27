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
package org.spongepowered.common.inventory.custom;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
import net.minecraft.inventory.container.HorseInventoryContainer;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.inventory.container.LoomContainer;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.inventory.container.SmokerContainer;
import net.minecraft.inventory.container.StonecutterContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.LensCreator;
import org.spongepowered.common.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.math.vector.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class SpongeViewableInventoryBuilder implements ViewableInventory.Builder,
                                                       ViewableInventory.Builder.DummyStep,
                                                       ViewableInventory.Builder.EndStep {

    private ContainerType type;

    private Map<Integer, Slot> slotDefinitions;
    private Slot lastSlot;

    private Carrier carrier;
    private UUID identity;

    private List<Inventory> finalInventories;
    private Lens finalLens;
    private SlotLensProvider finalProvider;
    private ContainerTypeInfo info;

    @Override
    public BuildingStep type(ContainerType type) {
        Validate.isTrue(containerTypeInfo.containsKey(type), "Container Type cannot be used for this: " + type);
        this.type = type;
        this.slotDefinitions = new HashMap<>();
        this.info = SpongeViewableInventoryBuilder.containerTypeInfo.get(type);
        return this;
    }

    // Helpers

    private int posToIndex(Vector2i pos) {
        return this.posToIndex(pos.getX(), pos.getY());
    }

    private int posToIndex(int x, int y) {
        Validate.isTrue(x <= this.info.width, "Target inventory is too small: " + this.info.width + " < " + x);
        Validate.isTrue(y <= this.info.height, "Target inventory is too small: " + this.info.height + " < " + y);
        return y * this.info.width + x;
    }

    private Vector2i indexToPos(int offset) {
        Validate.isTrue(offset <= this.info.width * this.info.height, "Target inventory is too small: " + this.info.width * this.info.height + " < " + offset);
        int x = offset / this.info.height;
        int y = offset % this.info.width;
        return new Vector2i(x, y);
    }

    private Slot newDummySlot() {
        IInventory dummyInv = new net.minecraft.inventory.Inventory(1);
        return ((Inventory) dummyInv).getSlot(0).get();
    }

    // Slot definition Impl:
    public BuildingStep slotsAtIndizes(List<Slot> source, List<Integer> at) {
        Validate.isTrue(source.size() == at.size(), "Source and index list sizes differ");
        for (int i = 0; i < at.size(); i++) {
            Slot slot = source.get(i);
            Integer index = at.get(i);
            this.slotDefinitions.put(index, slot);
            this.lastSlot = slot;
        }
        return this;
    }

    // complex redirects - (source/index list generation)
    public BuildingStep slotsAtPositions(List<Slot> source, List<Vector2i> at) {
        return this.slotsAtIndizes(source, at.stream().map(this::posToIndex).collect(Collectors.toList()));
    }

    public DummyStep fillDummy() {
        Slot slot = this.newDummySlot();
        List<Integer> indizes = IntStream.range(0, this.info.size).boxed().filter(idx -> !this.slotDefinitions.containsKey(idx)).collect(Collectors.toList());
        List<Slot> source = Stream.generate(() -> slot).limit(indizes.size()).collect(Collectors.toList());
        this.slotsAtIndizes(source, indizes);
        return this;
    }

    public DummyStep dummySlots(int count, int offset) {
        Slot slot = this.newDummySlot();
        List<Slot> source = Stream.generate(() -> slot).limit(count).collect(Collectors.toList());
        this.slots(source, offset);
        return this;
    }

    public BuildingStep slots(List<Slot> source, int offset) {
        List<Integer> indizes = IntStream.range(offset, offset + source.size()).boxed().collect(Collectors.toList());
        return this.slotsAtIndizes(source, indizes);
    }

    public DummyStep dummyGrid(Vector2i size, Vector2i offset) {
        Slot slot = this.newDummySlot();
        List<Slot> source = Stream.generate(() -> slot).limit(size.getX() * size.getY()).collect(Collectors.toList());
        this.grid(source, size, offset);
        return this;
    }

    public BuildingStep grid(List<Slot> source, Vector2i size, Vector2i offset) {
        int xMin = offset.getX();
        int yMin = offset.getY();
        int xMax = xMin + size.getX() - 1;
        int yMax = yMin + size.getY() - 1;

        List<Integer> indizes = new ArrayList<>();
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                indizes.add(this.posToIndex(x, y));
            }
        }
        return this.slotsAtIndizes(source, indizes);
    }

    // simple redirects

    public DummyStep dummySlots(int count, Vector2i offset) {
        return this.dummySlots(count, this.posToIndex(offset));
    }

    public BuildingStep slots(List<Slot> source, Vector2i offset) {
        return this.slots(source, this.posToIndex(offset));
    }

    public DummyStep dummyGrid(Vector2i size, int offset) {
        return this.dummyGrid(size, this.indexToPos(offset));
    }

    @Override
    public BuildingStep grid(List<Slot> source, Vector2i size, int offset) {
        return this.grid(source, size, this.indexToPos(offset));
    }
    // dummy
    @Override
    public BuildingStep item(ItemStackSnapshot item) {
        this.lastSlot.set(item.createStack());
        return this;
    }
    public EndStep identity(UUID uuid) {
        this.identity = uuid;
        return this;
    }
    @Override
    public EndStep carrier(Carrier carrier) {
        this.carrier = carrier;
        return this;
    }
    // Build

    @Override
    public EndStep completeStructure() {
        if (this.slotDefinitions.isEmpty()) {
            Inventory inventory = Inventory.builder().slots(this.info.size).completeStructure().build();
            this.finalInventories = Arrays.asList(inventory);
        } else {
            this.fillDummy();
            this.finalInventories = this.slotDefinitions.values().stream().map(Inventory::parent).distinct().collect(Collectors.toList());
// TODO custom slot provider with reduces inventories
//            CustomSlotProvider slotProvider = new CustomSlotProvider();
//            for (Map.Entry<Integer, Slot> entry : this.slotDefinitions.entrySet()) {
//                Slot slot = entry.getValue();
//                int idx = slot.get(Keys.SLOT_INDEX).get();
//                int offset = 0;
//                for (int i = 0; i < this.finalInventories.indexOf(slot.parent()); i++) {
//                    offset += this.finalInventories.get(i).freeCapacity();
//                }
//                slotProvider.add(new BasicSlotLens(idx + offset));
//            }
//            this.finalProvider = slotProvider;

            this.finalInventories = this.slotDefinitions.values().stream().map(Inventory.class::cast).collect(Collectors.toList());
        }

        this.finalProvider = new LensRegistrar.BasicSlotLensProvider(this.info.size);
        this.finalLens = containerTypeInfo.get(this.type).lensCreator.createLens(this.finalProvider);
        return this;
    }

    @Override
    public ViewableInventory build() {
        ViewableCustomInventory inventory = new ViewableCustomInventory(this.type, containerTypeInfo.get(this.type), this.info.size, this.finalLens, this.finalProvider, this.finalInventories, this.identity, this.carrier);
        if (this.slotDefinitions.isEmpty()) {
            inventory.vanilla();
        }
        return ((ViewableInventory) inventory);
    }
    public ViewableInventory.Builder reset() {
        this.type = null;
        this.info = null;

        this.slotDefinitions = null;
        this.lastSlot = null;

        this.carrier = null;
        this.identity = null;

        this.finalInventories = null;
        this.finalLens = null;
        this.finalProvider = null;
        return this;
    }

    public static class CustomSlotProvider implements SlotLensProvider {

        private List<SlotLens> lenses = new ArrayList<>();

        public void add(SlotLens toAdd) {
            this.lenses.add(toAdd);
        }

        @Override
        public SlotLens getSlotLens(int index) {
            return this.lenses.get(index);
        }
    }

    private static Map<ContainerType, ContainerTypeInfo> containerTypeInfo = new HashMap<>();

    static
    {
        containerTypeInfo.put(ContainerTypes.GENERIC_3x3.get(),
                ContainerTypeInfo.ofGrid(3, 3,
                        (id, i, p, vi) -> new DispenserContainer(id, i, vi)));
        containerTypeInfo.put(ContainerTypes.GENERIC_9x1.get(),
                ContainerTypeInfo.ofGrid(9, 1,
                        (id, i, p, vi) -> new ChestContainer(net.minecraft.inventory.container.ContainerType.GENERIC_9X1, id, i, vi, 1)));
        containerTypeInfo.put(ContainerTypes.GENERIC_9x2.get(),
                ContainerTypeInfo.ofGrid(9, 2,
                        (id, i, p, vi) -> new ChestContainer(net.minecraft.inventory.container.ContainerType.GENERIC_9X2, id, i, vi, 2)));
        containerTypeInfo.put(ContainerTypes.GENERIC_9x3.get(),
                ContainerTypeInfo.ofGrid(9, 3,
                        (id, i, p, vi) -> new ChestContainer(net.minecraft.inventory.container.ContainerType.GENERIC_9X3, id, i, vi, 3)));
        containerTypeInfo.put(ContainerTypes.GENERIC_9x4.get(),
                ContainerTypeInfo.ofGrid(9, 4,
                        (id, i, p, vi) -> new ChestContainer(net.minecraft.inventory.container.ContainerType.GENERIC_9X4, id, i, vi, 4)));
        containerTypeInfo.put(ContainerTypes.GENERIC_9x5.get(),
                ContainerTypeInfo.ofGrid(9, 5,
                        (id, i, p, vi) -> new ChestContainer(net.minecraft.inventory.container.ContainerType.GENERIC_9X5, id, i, vi, 5)));
        containerTypeInfo.put(ContainerTypes.GENERIC_9x6.get(),
                ContainerTypeInfo.ofGrid(9, 6,
                        (id, i, p, vi) -> new ChestContainer(net.minecraft.inventory.container.ContainerType.GENERIC_9X6, id, i, vi, 6)));
        containerTypeInfo.put(ContainerTypes.HOPPER.get(),
                ContainerTypeInfo.ofGrid(5, 1,
                        (id, i, p, vi) -> new HopperContainer(id, i, vi)));
        containerTypeInfo.put(ContainerTypes.SHULKER_BOX.get(), // Container prevents ShulkerBoxes in Shulkerboxes
                ContainerTypeInfo.ofGrid(9, 3,
                        (id, i, p, vi) -> new ShulkerBoxContainer(id, i, vi)));

        // With IntArray data - data is synced with Container - but not ticked as the TileEntity do that normally
        containerTypeInfo.put(ContainerTypes.BLAST_FURNACE.get(),
                ContainerTypeInfo.of(FurnaceInventoryLens::new, 3,4,
                        (id, i, p, vi) -> new BlastFurnaceContainer(id, i, vi, vi.getData())));
        containerTypeInfo.put(ContainerTypes.BREWING_STAND.get(),
                ContainerTypeInfo.of(BrewingStandInventoryLens::new, 5,2,
                        (id, i, p, vi) -> new BrewingStandContainer(id, i, vi, vi.getData())));
        containerTypeInfo.put(ContainerTypes.FURNACE.get(),
                ContainerTypeInfo.of(FurnaceInventoryLens::new, 3,4,
                        (id, i, p, vi) -> new FurnaceContainer(id, i, vi, vi.getData())));
        containerTypeInfo.put(ContainerTypes.LECTERN.get(),
                ContainerTypeInfo.of(1, 1,
                        (id, i, p, vi) -> new LecternContainer(id, vi, vi.getData())));
        containerTypeInfo.put(ContainerTypes.SMOKER.get(),
                ContainerTypeInfo.of(3, 4,
                        (id, i, p, vi) -> new SmokerContainer(id, i, vi, vi.getData())));

        // Containers with internal Inventory
        // TODO how to handle internal Container inventories?
        containerTypeInfo.put(ContainerTypes.ANVIL.get(), // 3 internal slots
                ContainerTypeInfo.of(0, 0,
                        (id, i, p, vi) -> new RepairContainer(id, i, toPos(p))));
        containerTypeInfo.put(ContainerTypes.BEACON.get(), // 1 internal slot
                ContainerTypeInfo.of(0, 3,
                        (id, i, p, vi) -> new BeaconContainer(id, i, vi.getData(), toPos(p))));
        containerTypeInfo.put(ContainerTypes.CARTOGRAPHY_TABLE.get(),  // 2 internal slots
                ContainerTypeInfo.of(0, 0,
                        (id, i, p, vi) -> new CartographyContainer(id, i, toPos(p))));
        containerTypeInfo.put(ContainerTypes.CRAFTING.get(), // 3x3+1 10 internal slots
                ContainerTypeInfo.of(0, 0,
                        (id, i, p, vi) -> new WorkbenchContainer(id, i, toPos(p))));
        containerTypeInfo.put(ContainerTypes.ENCHANTMENT.get(), // 3 internal slot
                ContainerTypeInfo.of(0, 0,
                        (id, i, p, vi) -> new EnchantmentContainer(id, i, toPos(p))));
        containerTypeInfo.put(ContainerTypes.GRINDSTONE.get(), // 2 internal slot
                ContainerTypeInfo.of(0, 0,
                        (id, i, p, vi) -> new GrindstoneContainer(id, i, toPos(p))));
        containerTypeInfo.put(ContainerTypes.LOOM.get(), // 3 internal slot
                ContainerTypeInfo.of(0, 0,
                        (id, i, p, vi) -> new LoomContainer(id, i, toPos(p))));
        containerTypeInfo.put(ContainerTypes.STONECUTTER.get(), // 1 internal slot
                ContainerTypeInfo.of(0, 0,
                        (id, i, p, vi) -> new StonecutterContainer(id, i, toPos(p))));

        // Containers that need additional Info to construct

        // TODO ContainerTypes.HORSE
        // horse is used for distance to player
        // checking HorseArmor Item in Slot
        // chested State and capacity (hasChest/getInventoryColumns) to add more Slots
        AbstractHorseEntity horse = null;
        ContainerTypeInfo.of(0, 0,
                (id, i, p, vi) -> new HorseInventoryContainer(id, i, vi, horse));

        // TODO ContainerTypes.MERCHANT
        // IMerchant is used to
        // create the internal MerchantInventory (3 slots)
        // create the MerchantResultSlot
        // used to check if player is customer
        // trigger sound (casted to Entity when !getWorld().isRemote) !!!
        // reset customer on close
        // when closing and !getWorld().isRemote drop items back into world !!!
        // getOffers
        IMerchant merchant = null;
        ContainerTypeInfo.of(0, 0,
                (id, i, p, vi) -> new MerchantContainer(id, i, merchant));
    }
    
    private static IWorldPosCallable toPos(PlayerEntity p) {
        return IWorldPosCallable.of(p.world, p.getPosition());
    }

    @FunctionalInterface
    public interface CustomInventoryContainerProvider {
        @Nullable
        Container createMenu(int id, PlayerInventory inv, PlayerEntity player, ViewableCustomInventory customInv);
    }

    public static class ContainerTypeInfo {
        public final LensCreator lensCreator;
        public final Supplier<IntArray> dataProvider;
        public final CustomInventoryContainerProvider containerProvider;

        public final int width;
        public final int height;
        public final int size;

        public ContainerTypeInfo(LensCreator lensCreator, Supplier<IntArray> dataProvider,
                CustomInventoryContainerProvider containerProvider, int width, int height, int size) {
            this.lensCreator = lensCreator;
            this.dataProvider = dataProvider;
            this.containerProvider = containerProvider;
            this.width = width;
            this.height = height;
            this.size = size;
        }

        public static ContainerTypeInfo of(LensCreator lensCreator, int size, int dataSize, CustomInventoryContainerProvider provider) {
            return new ContainerTypeInfo(lensCreator, () -> new IntArray(dataSize), provider, 0, 0, size);
        }

        public static ContainerTypeInfo of(int size, int dataSize, CustomInventoryContainerProvider provider) {
            return new ContainerTypeInfo(sp -> new DefaultIndexedLens(0, size, sp), () -> new IntArray(dataSize), provider, 0, 0, size);
        }

        public static ContainerTypeInfo ofGrid(int width, int height, CustomInventoryContainerProvider provider) {
            return new ContainerTypeInfo(sp -> new GridInventoryLens(0, width, height, sp), () -> null, provider, width, height, width * height);
        }

        public static ContainerTypeInfo ofGrid(int width, int height, int size, CustomInventoryContainerProvider provider) {
            return new ContainerTypeInfo(sp -> new GridInventoryLens(0, width, height, sp), () -> null, provider, width, height, size);
        }
    }

}
