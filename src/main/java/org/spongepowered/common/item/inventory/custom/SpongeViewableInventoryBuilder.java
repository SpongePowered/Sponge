package org.spongepowered.common.item.inventory.custom;

import org.apache.commons.lang3.Validate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.custom.ContainerType;
import org.spongepowered.api.item.inventory.custom.ContainerTypes;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

// TODO move to IMPL
public class SpongeViewableInventoryBuilder implements ViewableInventory.Builder,
                                                       ViewableInventory.Builder.SingleStep,
                                                       ViewableInventory.Builder.EndStep

{

    // Helper classes
    private class SlotDefinition {

        @Nullable ItemStackSnapshot item;

        @Nullable Inventory source;
        @Nullable Integer index;

        public SlotDefinition(ItemStackSnapshot item) {
            this.item = item;
        }

        public SlotDefinition(Inventory source) {
            this.source = source;
        }
    }

    private class Buffer {

        List<SlotDefinition> slots = new ArrayList<>();

        boolean grid = false;
        int sizeX;
        int sizeY;

        @Nullable private Integer atSlot;
        int atGridX;
        int atGridY;

        public void clear() {
            this.slots.clear();
            this.atSlot = null;
            this.atGridX = 0;
            this.atGridY = 0;
        }
    }

    private ContainerType type;
    private int size;
    private int sizeX;
    private int sizeY;
    private int currentSlot;

    private Inventory source; // not null when calling on StepSource
    private int sourceSize;
    private int sourceSizeX;
    private int sourceSizeY;
    private int currentSourceSlot;

    private Map<Integer, SlotDefinition> slotDefinitions;
    private Buffer buffer = new Buffer();

    private void writeSlots() {
        if (!buffer.slots.isEmpty()) {

            if (buffer.grid) {

                int index = 0;
                for (int yy = 0; yy < this.buffer.sizeY; yy++) {
                    for (int xx = 0; xx < this.buffer.sizeX; xx++) {

                        SlotDefinition def = this.buffer.slots.get(index++);

                        int slotX = buffer.atGridX + xx;
                        int slotY = buffer.atGridY + yy;

                        int slotIndex = slotY * this.sizeX + slotX;
                        this.slotDefinitions.put(slotIndex, def);
                    }
                }
            } else {

                if (this.buffer.atSlot == null) {
                    this.buffer.atSlot = currentSlot;
                }

                for (SlotDefinition def : buffer.slots) {
                    this.slotDefinitions.put(this.buffer.atSlot++, def);
                    if (def.source != null && def.index == null) {
                        def.index = currentSourceSlot++;
                    }
                }
            }

        }
        this.buffer.clear();

    }

    @Override
    public BuildingStep type(ContainerType type) {
        this.type = type;
        this.slotDefinitions = new HashMap<>();
        this.currentSlot = 0;
        // TODO ContainerType sizes
        this.sizeX = type.getSizeX();
        this.sizeY = type.getSizeY();
        this.size = type.getSize();
        return this;
    }

    @Override
    public SourceStep source(Inventory inventory) {
        this.source = inventory;
        this.currentSourceSlot = 0;
        return this;
    }

    // Slots and dummies


    @Override
    public SourceStep dummySource() {
        return this.dummySource(ItemStackSnapshot.NONE);
    }

    @Override
    public SourceStep dummySource(ItemStackSnapshot item) {
        this.source = null;
        this.item = item;
        return this;
    }

    @Override
    public ViewableInventory.Builder.StepDummy dummy(ItemStackSnapshot item) {
        return this.dummy(1, item);
    }

    @Override
    public ViewableInventory.Builder.StepDummy dummy() {
        return this.dummy(ItemStackSnapshot.NONE);
    }

    @Override
    public ViewableInventory.Builder.StepDummy dummy(int amount) {
        return this.dummy(amount, ItemStackSnapshot.NONE);
    }

    @Override
    public ViewableInventory.Builder.StepDummy dummy(int amount, ItemStackSnapshot item) {
        writeSlots();
        Validate.isTrue(this.sourceSize >= amount, "Source Inventory is too small");
        Validate.isTrue(this.size >= amount, "Target Inventory is too small");
        for (int i = 0; i < amount; i++) {
            this.buffer.slots.add(new SlotDefinition(item));
        }
        return (ViewableInventory.Builder.StepDummy) from(0);
    }

    @Override
    public ViewableInventory.Builder.StepDummy fillDummy(ItemStackSnapshot item) {
        for (int i = 0; i < this.size; i++) {
            if (!this.slotDefinitions.containsKey(i)) {
                this.dummy(item).at(i);
            }
        }
        return this;
    }

    @Override
    public ViewableInventory.Builder.StepDummy fillDummy() {
        return this.fillDummy(ItemStackSnapshot.NONE);
    }

    @Override
    public ViewableInventory.Builder.StepSlot slot() {
        return this.slots(1);
    }

    @Override
    public ViewableInventory.Builder.StepSlot slots(int amount) {
        writeSlots();
        this.sourceSize = this.source.capacity();
        Validate.isTrue(this.sourceSize >= amount, "Source Inventory is too small");
        Validate.isTrue(this.size >= amount, "Target Inventory is too small");
        for (int i = 0; i < amount; i++) {
            SlotDefinition def = new SlotDefinition(this.source);
            this.buffer.slots.add(def);
        }
        this.buffer.grid = false;
        return (ViewableInventory.Builder.StepSlot) from(0);
    }

    @Override
    public ViewableInventory.Builder.StepSlot from(int index) {
        Validate.isTrue(this.sourceSize >= this.buffer.slots.size() + index, "Source Inventory is too small");
        int curIndex = index;
        for (SlotDefinition def : this.buffer.slots) {
            def.index = curIndex++;
        }
        return this;
    }

    @Override
    public ViewableInventory.Builder.StepSlot at(int index) {
        Validate.isTrue(this.size >= this.buffer.slots.size() + index, "Target Inventory is too small");
        this.buffer.atSlot = index;
        return this;
    }

    // Grid

    @Override
    public ViewableInventory.Builder.StepGrid grid(int sizeX, int sizeY) {
        writeSlots();
        Validate.isTrue(this.source instanceof GridInventory, "Source Inventory is not a grid");
        // TODO type supports grids Validate.isTrue(type is a grid, "Target Inventory is not a grid");
        this.sourceSizeX = ((GridInventory) this.source).getColumns();
        this.sourceSizeY = ((GridInventory) this.source).getRows();
        Validate.isTrue(this.sizeX >= sizeX, "Target Inventory is too small");
        Validate.isTrue(this.sizeY >= sizeY, "Target Inventory is too small");
        this.slots(sizeX * sizeY);
        this.buffer.sizeX = sizeX;
        this.buffer.sizeY = sizeY;
        this.buffer.grid = true;
        this.from(0, 0);
        return this;
    }

    @Override
    public ViewableInventory.Builder.StepGrid from(int x, int y) {

        Validate.isTrue(this.sourceSizeX >= this.buffer.sizeX + x, "Source Inventory is too small");
        Validate.isTrue(this.sourceSizeY >= this.buffer.sizeY + y, "Source Inventory is too small");

        int index = 0;
        for (int yy = 0; yy < this.buffer.sizeY; yy++) {
            for (int xx = 0; xx < this.buffer.sizeX; xx++) {

                SlotDefinition def = this.buffer.slots.get(index++);

                int slotY = y + yy;
                int slotX = x + xx;

                def.index = slotY * this.sourceSizeX + slotX;
            }
        }
        return this;
    }

    @Override
    public ViewableInventory.Builder.StepGrid at(int x, int y) {
        Validate.isTrue(this.sizeX >= this.buffer.sizeX + x, "Target Inventory is too small");
        Validate.isTrue(this.sizeY >= this.buffer.sizeY + y, "Target Inventory is too small");
        this.buffer.atGridX = x;
        this.buffer.atGridY = y;
        return this;
    }

    @Override
    public EndStep carrier(Carrier carrier) {
        for (SlotDefinition def : this.buffer.slots) {
            // TODO
        }
        return this;
    }

    // Build

    @Override
    public EndStep completeStructure() {
        // TODO build lens
        return this;
    }

    @Override
    public EndStep ofViewable(Inventory inventory) {
        // TODO copy type from inventory
        return null;
    }

    @Override
    public EndStep title(Text title) {
        // TODO
        return null;
    }

    @Override public EndStep identity(UUID uuid) {
        // TODO
        return null;
    }

    @Override
    public ViewableInventory build() {
        // TODO
        return null;
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // EXAMPLE USAGE:
    // ---------------------------------------------------------------------------------------------------------------------------

    static void foobar()
    {
Player player = null;
Player player2 = null;

Inventory inv1 = Inventory.builder().grid(3, 3).completeStructure().build();
Inventory inv2 = Inventory.builder().grid(3, 3).completeStructure().build();
Inventory inv3 = Inventory.builder().grid(9, 3).completeStructure().build();

ViewableInventory inv = ViewableInventory.builder()
        .type(ContainerTypes.CHEST)
        .source(inv1).grid(3, 3)
        .source(inv2).grid(3, 3).at(3, 1)
        .source(inv3).grid(3, 3).from(3, 0).at(6, 3)
                     .slot().from(0).at(37)
        .dummySource().slot().at(16)
        .fillDummy()
        .completeStructure()
        .identity(UUID.randomUUID())
        .build();

ViewableInventory basicChest = ViewableInventory.builder()
        .type(ContainerTypes.CHEST)
        .completeStructure()
        .build();

ItemStackSnapshot disabled = ItemStack.of(ItemTypes.LIGHT_GRAY_STAINED_GLASS_PANE, 1).createSnapshot();
ItemStackSnapshot emerald = ItemStack.of(ItemTypes.EMERALD, 1).createSnapshot();

ViewableInventory display = ViewableInventory.builder().type(ContainerTypes.DISPENSER)
        .dummySource(disabled).fillDummy()
        .source(inv2).grid(1,1).from(1,1).at(1,1)
        .completeStructure().build();
display.query(GridInventory.class).get().set(1,1, ItemStack.of(ItemTypes.DIAMOND, 1));

ViewableInventory display2 = ViewableInventory.builder().type(ContainerTypes.DISPENSER)
        .dummySource().fillDummy()
        .dummySource(emerald).slot().at(1, 1)
        .completeStructure().build();
display.query(GridInventory.class).get().set(1,1, ItemStack.of(ItemTypes.DIAMOND, 1));

InventoryMenu menu = InventoryMenu.of(display);
menu.open(player);
menu.open(player2);

menu.setCurrentInventory(display2); // matching ContainerType so the inventory is silently swapped
menu.setTitle(Text.of("This reopens containers"));
menu.registerClick((container, slot, slotIndex, clickType) -> checkClick(), SlotIndex.of(4));

menu.setReadOnly(false);
menu.registerChange((container, slot, slotIndex) -> checkAllChange());

menu.setReadOnly(true);
menu.setCurrentInventory(basicChest);
menu.unregisterAll(); // already done as changing the ContainerType clears all callbacks

EquipmentInventory armor = null;
GridInventory mainGrid = null;
Slot offhand = null;

ViewableInventory.builder().type(ContainerTypes.CHEST)
.source(armor).slots(4)
.source(mainGrid).slots(5)
.source(offhand).slot()
.completeStructure();

    }

    static boolean checkClick() { return true; }
    static boolean checkAllChange() { return false; }


}
