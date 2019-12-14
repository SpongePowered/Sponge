package org.spongepowered.common.inventory.custom;

import net.minecraft.inventory.IInventory;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.data.type.SpongeContainerType;
import org.spongepowered.common.data.type.SpongeContainerTypeEntity;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.BasicSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.math.vector.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpongeViewableInventoryBuilder implements ViewableInventory.Builder,
                                                       ViewableInventory.Builder.DummyStep,
                                                       ViewableInventory.Builder.EndStep {

    private ContainerType type;
    private int size;
    private int sizeX;
    private int sizeY;

    private Map<Integer, Slot> slotDefinitions;
    private Slot lastSlot;

    private Carrier carrier;
    private UUID identity;

    private List<Inventory> finalInventories;
    private Lens finalLens;
    private SlotLensProvider finalProvider;

    @Override
    public BuildingStep type(ContainerType type) {
        Validate.isTrue(!(this.type instanceof SpongeContainerTypeEntity), "Inventory needs to be constructed by entity");
        this.type = type;
        this.slotDefinitions = new HashMap<>();
        if (type instanceof SpongeContainerType) {
            this.sizeX = ((SpongeContainerType) type).getWidth();
            this.sizeY = ((SpongeContainerType) type).getHeight();
            this.size = ((SpongeContainerType) type).getSize();
        }
        return this;
    }

    @Override
    public BuildingStep typeFrom(ViewableInventory inventory) {
        return this.type(inventory.getType());
    }

    // Helpers

    private int posToIndex(Vector2i pos) {
        return this.posToIndex(pos.getX(), pos.getY());
    }

    private int posToIndex(int x, int y) {
        Validate.isTrue(x <= this.sizeX, "Target inventory is too small: " + this.sizeX + " < " + x);
        Validate.isTrue(y <= this.sizeY, "Target inventory is too small: " + this.sizeY + " < " + y);
        return y * this.sizeX + x;
    }

    private Vector2i indexToPos(int offset) {
        Validate.isTrue(offset <= this.sizeX * this.sizeY, "Target inventory is too small: " + this.sizeX * this.sizeY + " < " + offset);
        int x = offset / this.sizeY;
        int y = offset % this.sizeX;
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
        List<Integer> indizes = IntStream.range(0, this.size).boxed().filter(idx -> !this.slotDefinitions.containsKey(idx)).collect(Collectors.toList());
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
            Inventory inventory = Inventory.builder().slots(this.size).completeStructure().build();
            this.finalInventories = Arrays.asList(inventory);
            this.finalProvider = ((InventoryBridge) inventory).bridge$getAdapter().inventoryAdapter$getSlotLensProvider();
        } else {
            this.fillDummy();
            this.finalInventories = this.slotDefinitions.values().stream().map(Inventory::parent).distinct().collect(Collectors.toList());
            CustomSlotProvider slotProvider = new CustomSlotProvider();
            for (Map.Entry<Integer, Slot> entry : this.slotDefinitions.entrySet()) {
                Slot slot = entry.getValue();
                int idx = slot.getProperty(InventoryProperties.SLOT_INDEX).get();

                int offset = 0;
                for (int i = 0; i < this.finalInventories.indexOf(slot.parent()); i++) {
                    offset += this.finalInventories.get(i).freeCapacity();
                }
                slotProvider.add(new BasicSlotLens(idx + offset));
            }
            this.finalProvider = slotProvider;
        }
        this.finalLens = ((SpongeContainerType) this.type).getLensCreator().createLens(this.finalProvider);
        return this;
    }

    @Override
    public ViewableInventory build() {
        ViewableCustomInventory inventory = new ViewableCustomInventory(this.type, this.size, this.finalLens, this.finalProvider, this.finalInventories, this.identity, this.carrier);
        if (this.slotDefinitions.isEmpty()) {
            inventory.vanilla();
        }
        return ((ViewableInventory) inventory);
    }
    public ViewableInventory.Builder reset() {
        this.type = null;
        this.size = 0;
        this.sizeX = 0;
        this.sizeY = 0;

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

}
