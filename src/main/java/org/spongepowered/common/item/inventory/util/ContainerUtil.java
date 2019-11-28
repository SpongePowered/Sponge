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
package org.spongepowered.common.item.inventory.util;

import com.google.common.collect.Multimap;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.BeaconContainer;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.DispenserContainer;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.slot.InputSlot;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.inventory.DefaultSingleBlockCarrier;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.VanillaContainerAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.CraftingOutputAdapter;
import org.spongepowered.common.item.inventory.custom.CustomContainer;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.CraftingInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.Inventory2DLens;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.DelegatingLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.Inventory2DLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.MainPlayerInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.LargeChestInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.CraftingOutputSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.mixin.core.inventory.ContainerAccessor;
import org.spongepowered.common.mixin.core.inventory.ContainerBrewingStandAccessor;
import org.spongepowered.common.mixin.core.inventory.ContainerDispenserAccessor;
import org.spongepowered.common.mixin.core.inventory.ContainerFurnaceAccessor;
import org.spongepowered.common.mixin.core.inventory.ContainerHopperAccessor;
import org.spongepowered.common.mixin.core.inventory.ContainerHorseInventoryAccessor;
import org.spongepowered.common.mixin.core.inventory.ContainerMerchantAccessor;
import org.spongepowered.common.mixin.core.inventory.ContainerRepairAccessor;
import org.spongepowered.common.mixin.core.inventory.SlotCraftingAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class ContainerUtil {

    private static final Random RANDOM = new Random();

    private ContainerUtil() {
    }

    // Note this is likely not doable throughout the implementation, only in certain cases

    public static Container fromNative(final net.minecraft.inventory.container.Container container) {
        return (Container) container;
    }

    // Note, this really cannot be guaranteed to work
    public static net.minecraft.inventory.container.Container toNative(final Container container) {
        return (net.minecraft.inventory.container.Container) container;
    }

    public static ContainerBridge toMixin(final net.minecraft.inventory.container.Container container) {
        return (ContainerBridge) container;
    }

    public static net.minecraft.inventory.container.Container fromMixin(final ContainerBridge container) {
        return (net.minecraft.inventory.container.Container) container;
    }

    /**
     * Replacement helper method for {@code InventoryHelperMixin#spongeDropInventoryItems(World, double, double, double, IInventory)}
     * to perform cause tracking related drops. This is specific for blocks, not for any other cases.
     *
     * @param worldServer The world server
     * @param x the x position
     * @param y the y position
     * @param z the z position
     * @param inventory The inventory to drop items from
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void performBlockInventoryDrops(final ServerWorld worldServer, final double x, final double y, final double z, final IInventory inventory) {
        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = context.state;
        if (((IPhaseState) currentState).tracksBlockSpecificDrops(context)) {
            // this is where we could perform item stack pre-merging.
            // For development reasons, not performing any pre-merging except after the entity item spawns.

            // Don't do pre-merging - directly spawn in item
            final Multimap<BlockPos, ItemEntity> multimap = context.getBlockItemDropSupplier().get();
            final BlockPos pos = new BlockPos(x, y, z);
            final Collection<ItemEntity> itemStacks = multimap.get(pos);
            for (int j = 0; j < inventory.getSizeInventory(); j++) {
                final net.minecraft.item.ItemStack itemStack = inventory.getStackInSlot(j);
                if (!itemStack.isEmpty()) {
                    final float f = RANDOM.nextFloat() * 0.8F + 0.1F;
                    final float f1 = RANDOM.nextFloat() * 0.8F + 0.1F;
                    final float f2 = RANDOM.nextFloat() * 0.8F + 0.1F;

                    while (!itemStack.isEmpty())
                    {
                        final int i = RANDOM.nextInt(21) + 10;

                        final ItemEntity entityitem = new ItemEntity(worldServer, x + f, y + f1, z + f2, itemStack.split(i));

                        entityitem.motionX = RANDOM.nextGaussian() * 0.05;
                        entityitem.motionY = RANDOM.nextGaussian() * 0.05 + 0.2;
                        entityitem.motionZ = RANDOM.nextGaussian() * 0.05;
                        itemStacks.add(entityitem);
                    }
                }
            }
            return;
        }
        // Finally, just default to spawning the entities normally, regardless of the case.
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            final net.minecraft.item.ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                InventoryHelper.spawnItemStack(worldServer, x, y, z, itemStack);
            }
        }
    }

    private static class CraftingInventoryData {
        @Nullable private Integer out;
        @Nullable private Integer base;
        @Nullable private CraftingInventory grid;
    }

    /**
     * Generates a fallback lens for given Container
     *
     * @param container The Container to generate a lens for
     * @param slots The slots of the Container
     * @return The generated fallback lens
     */
    @SuppressWarnings("unchecked") public static Lens generateLens(final net.minecraft.inventory.container.Container container, final SlotProvider slots) {
        // Get all inventories viewed in the Container & count slots & retain order
        final Map<Optional<IInventory>, List<Slot>> viewed = container.inventorySlots.stream()
                .collect(Collectors.groupingBy(i -> Optional.<IInventory>ofNullable(i.inventory), LinkedHashMap::new, Collectors.toList()));
        int index = 0; // Count the index
        final CraftingInventoryData crafting = new CraftingInventoryData();
        final List<Lens> lenses = new ArrayList<>();
        for (final Map.Entry<Optional<IInventory>, List<Slot>> entry : viewed.entrySet()) {
            final List<Slot> slotList = entry.getValue();
            final int slotCount = slotList.size();
            final IInventory subInventory = entry.getKey().orElse(null);
            // Generate Lens based on existing InventoryAdapter
            Lens lens = generateAdapterLens(slots, index, crafting, slotList, subInventory);
            // Check if sub-inventory is LensProviderBridge
            if (lens == null && subInventory instanceof LensProviderBridge) {
                final Fabric keyFabric = ((Fabric) subInventory);
                lens = ((LensProviderBridge) subInventory).bridge$rootLens(keyFabric, new VanillaContainerAdapter(keyFabric, container));
            }
            // Unknown Inventory or Inventory size <> Lens size
            if (lens == null || lens.slotCount() != slotCount) {
                if (subInventory instanceof CraftResultInventory) { // InventoryCraftResult is a Slot
                    final Slot slot = slotList.get(0);
                    lens = new CraftingOutputSlotLensImpl(index,
                            item -> slot.isItemValid(((ItemStack) item)),
                            itemType -> (slot.isItemValid((ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1))));
                } else if (subInventory instanceof CraftingInventory) { // InventoryCrafting has width and height and is Input
                    final CraftingInventory craftGrid = (CraftingInventory) subInventory;
                    lens = new GridInventoryLensImpl(index, craftGrid.getWidth(), craftGrid.getHeight(), craftGrid.getWidth(), InputSlot.class, slots);
                } else if (slotCount == 1) { // Unknown - A single Slot
                    lens = new SlotLensImpl(index);
                }
                else if (subInventory instanceof net.minecraft.inventory.Inventory && subInventory.getClass().isAnonymousClass()) {
                    // Anonymous InventoryBasic -> Check for Vanilla Containers:
                    switch (subInventory.getName()) {
                        case "Enchant": // Container InputSlots
                        case "Repair": // Container InputSlots
                            lens = new OrderedInventoryLensImpl(index, slotCount, 1, InputSlot.class, slots);
                            break;
                        default: // Unknown
                            lens = new OrderedInventoryLensImpl(index, slotCount, 1, slots);
                    }
                }
                else {
                    // Unknown - fallback to OrderedInventory
                    lens = new OrderedInventoryLensImpl(index, slotCount, 1, slots);
                }
            }
            lenses.add(lens);
            index += slotCount;
        }


        final List<Lens> additional = new ArrayList<>();
        try {
            if (crafting.out != null && crafting.base != null && crafting.grid != null) {
                additional.add(new CraftingInventoryLensImpl(crafting.out, crafting.base, crafting.grid.getWidth(), crafting.grid.getHeight(), slots));
            } else if (crafting.base != null && crafting.grid != null) {
                additional.add(new GridInventoryLensImpl(crafting.base, crafting.grid.getWidth(), crafting.grid.getHeight(), crafting.grid.getWidth(), slots));
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Error while creating CraftingInventoryLensImpl or GridInventoryLensImpl for " + container.getClass().getName(), e);
        }


        // Lens containing/delegating to other lenses
        return new ContainerLens(container.inventorySlots.size(), (Class<? extends Inventory>) container.getClass(), slots, lenses, additional);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @Nullable Lens generateAdapterLens(final SlotProvider slots, final int index,
            final CraftingInventoryData crafting, final List<Slot> slotList, @Nullable final IInventory subInventory) {
        if (!(subInventory instanceof InventoryAdapter)) {
            return null;
        }
        Lens adapterLens = ((InventoryAdapter) subInventory).bridge$getRootLens();
        if (adapterLens == null) {
            return null;
        }
        if (subInventory.getSizeInventory() == 0) {
            return new DefaultEmptyLens(((InventoryAdapter) subInventory));
        }

        if (adapterLens instanceof PlayerInventoryLens) {
            if (slotList.size() == 36) {
                return new DelegatingLens(index, new MainPlayerInventoryLensImpl(index, slots, true), slots);
            }
            return null;
        }
        // For Crafting Result we need the Slot to get Filter logic
        if (subInventory instanceof CraftResultInventory) {
            final Slot slot = slotList.get(0);
            adapterLens = new CraftingOutputSlotLensImpl(index, item -> slot.isItemValid(((ItemStack) item)),
                    itemType -> (slot.isItemValid((ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1))));
            if (slot instanceof SlotCraftingAccessor) {
                crafting.out = index;
                if (crafting.base == null) {
                    // In case we do not find the InventoryCrafting later assume it is directly after the SlotCrafting
                    // e.g. for IC2 ContainerIndustrialWorkbench
                    crafting.base = index + 1;
                    crafting.grid = ((SlotCraftingAccessor) slot).accessor$getCraftingMatrix();
                }
            }
        }
        if (subInventory instanceof CraftingInventory) {
            crafting.base = index;
            crafting.grid = ((CraftingInventory) subInventory);
        }
        return new DelegatingLens(index, slotList, adapterLens, slots);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Lens copyLens(final int base, final InventoryAdapter adapter, final Lens lens,
            final SlotCollection slots) {
        if (lens instanceof LargeChestInventoryLens) {
            return new LargeChestInventoryLens(base, adapter, slots);
        }
        if (lens instanceof FurnaceInventoryLens) {
            return new FurnaceInventoryLens(base, adapter, slots);
        }
        if (lens instanceof BrewingStandInventoryLens) {
            return new BrewingStandInventoryLens(base, adapter, slots);
        }
        if (lens instanceof CraftingInventoryLens) {
            return new CraftingInventoryLensImpl(0, base,
                    ((GridInventoryLens) lens).getWidth(),
                    ((GridInventoryLens) lens).getHeight(),
                    slots);
        }
        if (lens instanceof GridInventoryLens) {
            return new GridInventoryLensImpl(base,
                    ((GridInventoryLens) lens).getWidth(),
                    ((GridInventoryLens) lens).getHeight(),
                    ((GridInventoryLens) lens).getStride(),
                    slots);
        }
        if (lens instanceof Inventory2DLens) {
            return new Inventory2DLensImpl(base,
                    ((Inventory2DLens) lens).getWidth(),
                    ((Inventory2DLens) lens).getHeight(),
                    slots);
        }
        return null;
    }

    /**
     * Calculates the slot count for the passed {@link Container}
     *
     * @return The {@link SlotCollection} with the amount of slots for this container.
     */
    public static SlotProvider countSlots(final net.minecraft.inventory.container.Container container, final Fabric fabric) {
        if (container instanceof LensProviderBridge) {
            return ((LensProviderBridge) container).bridge$slotProvider(fabric, ((InventoryAdapter) container));
        }

        final SlotCollection.Builder builder = new SlotCollection.Builder();
        for (final Slot slot : container.inventorySlots) {
            if (slot instanceof CraftingResultSlot) {
                builder.add(1, CraftingOutputAdapter.class, (i) -> new CraftingOutputSlotLensImpl(i,
                        item -> slot.isItemValid(((ItemStack) item)),
                        itemType -> (slot.isItemValid((ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1)))));
            } else {
                builder.add(1);
            }
        }
        return builder.build();
    }

    public static InventoryArchetype getArchetype(final net.minecraft.inventory.container.Container container) {
        if (container instanceof ChestContainer) {
            final IInventory inventory = ((ChestContainer) container).getLowerChestInventory();
            if (inventory instanceof ChestTileEntity) {
                return InventoryArchetypes.CHEST;
            } else if (inventory instanceof DoubleSidedInventory) {
                return InventoryArchetypes.DOUBLE_CHEST;
            } else {
                return InventoryArchetypes.UNKNOWN;
            }
        } else if (container instanceof HopperContainer) {
            return InventoryArchetypes.HOPPER;
        } else if (container instanceof DispenserContainer) {
            return InventoryArchetypes.DISPENSER;
        } else if (container instanceof WorkbenchContainer) {
            return InventoryArchetypes.WORKBENCH;
        } else if (container instanceof FurnaceContainer) {
            return InventoryArchetypes.FURNACE;
        } else if (container instanceof EnchantmentContainer) {
            return InventoryArchetypes.ENCHANTING_TABLE;
        } else if (container instanceof RepairContainer) {
            return InventoryArchetypes.ANVIL;
        } else if (container instanceof BrewingStandContainer) {
            return InventoryArchetypes.BREWING_STAND;
        } else if (container instanceof BeaconContainer) {
            return InventoryArchetypes.BEACON;
        } else if (container instanceof ContainerHorseInventoryAccessor) {
            final AbstractHorseEntity horse = ((ContainerHorseInventoryAccessor) container).accessor$getHorseCarrier();
            if (horse instanceof AbstractChestedHorseEntity && ((AbstractChestedHorseEntity) horse).hasChest()) {
                return InventoryArchetypes.HORSE_WITH_CHEST;
            }
            return InventoryArchetypes.HORSE;
        } else if (container instanceof MerchantContainer) {
            return InventoryArchetypes.VILLAGER;
        } else if (container instanceof PlayerContainer) {
            return InventoryArchetypes.PLAYER;
        }
        return InventoryArchetypes.UNKNOWN;
    }

    @Nullable
    public static Carrier getCarrier(final Container container) {
        if (container instanceof BlockCarrier) {
            return ((BlockCarrier) container);
        }
        if (container instanceof CustomContainer) {
            return ((CustomContainer) container).inv.getCarrier();
        } else if (container instanceof ChestContainer) {
            final IInventory inventory = ((ChestContainer) container).getLowerChestInventory();
            if (inventory instanceof Carrier) {
                if (inventory instanceof ChestTileEntity) {
                    return (Carrier) inventory;
                } else if (inventory instanceof DoubleSidedInventory) {
                    return ((BlockCarrier) inventory);
                }
            }
            return carrierOrNull(inventory);
        } else if (container instanceof ContainerHopperAccessor) {
            return carrierOrNull(((ContainerHopperAccessor) container).accessor$getHopperInventory());
        } else if (container instanceof ContainerDispenserAccessor) {
            return carrierOrNull(((ContainerDispenserAccessor) container).accessor$getDispenserInventory());
        } else if (container instanceof ContainerFurnaceAccessor) {
            return carrierOrNull(((ContainerFurnaceAccessor) container).accessor$getFurnaceInventory());
        } else if (container instanceof ContainerBrewingStandAccessor) {
            return carrierOrNull(((ContainerBrewingStandAccessor) container).accessor$getBrewingStandInventory());
        } else if (container instanceof BeaconContainer) {
            return carrierOrNull(((BeaconContainer) container).getTileEntity());
        } else if (container instanceof ContainerHorseInventoryAccessor) {
            return (Carrier) ((ContainerHorseInventoryAccessor) container).accessor$getHorseCarrier();
        } else if (container instanceof ContainerMerchantAccessor && ((ContainerMerchantAccessor) container).accessor$getMerchantCarrier() instanceof Carrier) {
            return (Carrier) ((ContainerMerchantAccessor) container).accessor$getMerchantCarrier();
        } else if (container instanceof ContainerRepairAccessor) {
            final PlayerEntity player = ((ContainerRepairAccessor) container).accessor$getPlayerCarrier();
            if (player instanceof ServerPlayerEntity) {
                return (Carrier) player;
            }
        }

        // Fallback: Try to find a Carrier owning the first Slot of the Container
        if (container instanceof ContainerAccessor) {
            for (final Slot slot : ((ContainerAccessor) container).accessor$getSlots()) {
                // Slot Inventory is a Carrier?
                if (slot.inventory instanceof Carrier) {
                    return ((Carrier) slot.inventory);
                }
                // Slot Inventory is a TileEntity
                if (slot.inventory instanceof TileEntity) {
                    return new DefaultSingleBlockCarrier() {
                        @Override
                        public Location<org.spongepowered.api.world.World> getLocation() {
                            final BlockPos pos = ((TileEntity) slot.inventory).getPos();
                            return new Location<>(((org.spongepowered.api.world.World) ((TileEntity) slot.inventory).getWorld()), pos.getX(), pos.getY(), pos.getZ());
                        }

                        @SuppressWarnings("rawtypes")
                        @Override
                        public CarriedInventory<?> getInventory() {
                            return ((CarriedInventory) container);
                        }
                    };
                }
            }
        }
        final Location<org.spongepowered.api.world.World> loc = ((ContainerBridge) container).bridge$getOpenLocation();
        if (loc != null) {
            return new DefaultSingleBlockCarrier() {
                @Override
                public Location<org.spongepowered.api.world.World> getLocation() {
                    return loc;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public CarriedInventory<?> getInventory() {
                    return ((CarriedInventory) container);
                }
            };
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Carrier carrierOrNull(final IInventory inventory) {
        if (inventory instanceof Carrier) {
            return (Carrier) inventory;
        }
        if (inventory instanceof CarriedInventory) {
            final Optional<Carrier> carrier = ((CarriedInventory) inventory).getCarrier();
            return carrier.orElse(null);
        }
        return null;
    }

    public static org.spongepowered.api.item.inventory.Slot getSlot(final net.minecraft.inventory.container.Container container, final int slot) {
        return ((ContainerBridge) container).bridge$getContainerSlot(slot);
    }
}
