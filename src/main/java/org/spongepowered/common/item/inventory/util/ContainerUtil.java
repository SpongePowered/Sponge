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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.slot.InputSlot;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinSingleBlockCarrier;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.VanillaAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.CraftingOutputAdapter;
import org.spongepowered.common.item.inventory.custom.CustomContainer;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.LensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.DelegatingLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.PrimaryPlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.fabric.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.CraftingOutputSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.mixin.core.inventory.MixinInventoryHelper;

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

    public static Container fromNative(net.minecraft.inventory.Container container) {
        return (Container) container;
    }

    // Note, this really cannot be guaranteed to work
    public static net.minecraft.inventory.Container toNative(Container container) {
        return (net.minecraft.inventory.Container) container;
    }

    public static IMixinContainer toMixin(net.minecraft.inventory.Container container) {
        return (IMixinContainer) container;
    }

    public static net.minecraft.inventory.Container fromMixin(IMixinContainer container) {
        return (net.minecraft.inventory.Container) container;
    }

    /**
     * Replacement helper method for {@link MixinInventoryHelper#spongeDropInventoryItems(World, double, double, double, IInventory)}
     * to perform cause tracking related drops. This is specific for blocks, not for any other cases.
     *
     * @param worldServer The world server
     * @param x the x position
     * @param y the y position
     * @param z the z position
     * @param inventory The inventory to drop items from
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void performBlockInventoryDrops(WorldServer worldServer, double x, double y, double z, IInventory inventory) {
        final PhaseData currentPhase = PhaseTracker.getInstance().getCurrentPhaseData();
        final IPhaseState<?> currentState = currentPhase.state;
        final PhaseContext<?> context = currentPhase.context;
        if (((IPhaseState) currentState).tracksBlockSpecificDrops(context)) {
            // this is where we could perform item stack pre-merging.
            // For development reasons, not performing any pre-merging except after the entity item spawns.

            // Don't do pre-merging - directly spawn in item
            final Multimap<BlockPos, EntityItem> multimap = context.getBlockItemDropSupplier().get();
            final BlockPos pos = new BlockPos(x, y, z);
            final Collection<EntityItem> itemStacks = multimap.get(pos);
            for (int j = 0; j < inventory.getSizeInventory(); j++) {
                final net.minecraft.item.ItemStack itemStack = inventory.getStackInSlot(j);
                if (!itemStack.isEmpty()) {
                    float f = RANDOM.nextFloat() * 0.8F + 0.1F;
                    float f1 = RANDOM.nextFloat() * 0.8F + 0.1F;
                    float f2 = RANDOM.nextFloat() * 0.8F + 0.1F;

                    while (!itemStack.isEmpty())
                    {
                        int i = RANDOM.nextInt(21) + 10;

                        EntityItem entityitem = new EntityItem(worldServer, x + f, y + f1, z + f2, itemStack.splitStack(i));

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static Lens getLens(Fabric fabric, net.minecraft.inventory.Container container, SlotProvider slots) {
        // Container is Adapter?
        if (container instanceof InventoryAdapter) {
            Lens lens = ((InventoryAdapter) container).getRootLens();
            if (lens != null) {
                return lens;
            }
            // else lens is null: Adapter is probably getting initialized
        }

        // Container provides Lens?
        if (container instanceof LensProvider) {
            // TODO LensProviders for all Vanilla Containers
            InventoryAdapter adapter = ((InventoryAdapter) container);
            return ((LensProvider) container).rootLens(fabric, adapter);
        }

        // For those Sheep-Crafting inventories
        if (container.getInventory().size() == 0) {
            return new DefaultEmptyLens(((InventoryAdapter) container)); // Empty Container
        }

        // Unknown Container - try to get Lenses for Sub-Inventories and wrap them in a ContainerLens
        return generateLens(container, slots);
    }


    private static class CraftingInventoryData {
        @Nullable private Integer out;
        @Nullable private Integer base;
        @Nullable private InventoryCrafting grid;
    }

    /**
     * Generates a fallback lens for given Container
     *
     * @param container The Container to generate a lens for
     * @param slots The slots of the Container
     * @return The generated fallback lens
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Lens generateLens(net.minecraft.inventory.Container container, SlotProvider slots) {
        // Get all inventories viewed in the Container & count slots & retain order
        Map<Optional<IInventory>, List<Slot>> viewed = container.inventorySlots.stream()
                .collect(Collectors.groupingBy(i -> Optional.<IInventory>ofNullable(i.inventory), LinkedHashMap::new, Collectors.toList()));
        int index = 0; // Count the index
        CraftingInventoryData crafting = new CraftingInventoryData();
        List<Lens> lenses = new ArrayList<>();
        for (Map.Entry<Optional<IInventory>, List<Slot>> entry : viewed.entrySet()) {
            List<Slot> slotList = entry.getValue();
            int slotCount = slotList.size();
            IInventory subInventory = entry.getKey().orElse(null);
            // Generate Lens based on existing InventoryAdapter
            Lens lens = generateAdapterLens(slots, index, crafting, slotList, subInventory);
            // Check if sub-inventory is LensProvider
            if (lens == null && subInventory instanceof LensProvider) {
                Fabric keyFabric = MinecraftFabric.of(subInventory);
                lens = ((LensProvider) subInventory).rootLens(keyFabric, new VanillaAdapter(keyFabric, container));
            }
            // Unknown Inventory or Inventory size <> Lens size
            if (lens == null || lens.slotCount() != slotCount) {
                if (subInventory instanceof InventoryCraftResult) { // InventoryCraftResult is a Slot
                    Slot slot = slotList.get(0);
                    lens = new CraftingOutputSlotLensImpl(index,
                            item -> slot.isItemValid(((ItemStack) item)),
                            itemType -> (slot.isItemValid((ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1))));
                } else if (subInventory instanceof InventoryCrafting) { // InventoryCrafting has width and height and is Input
                    InventoryCrafting craftGrid = (InventoryCrafting) subInventory;
                    lens = new GridInventoryLensImpl(index, craftGrid.getWidth(), craftGrid.getHeight(), InputSlot.class, slots);
                } else if (slotCount == 1) { // Unknown - A single Slot
                    lens = new SlotLensImpl(index);
                }
                else if (subInventory instanceof InventoryBasic && subInventory.getClass().isAnonymousClass()) {
                    // Anonymous InventoryBasic -> Check for Vanilla Containers:
                    switch (subInventory.getName()) {
                        case "Enchant": // Container InputSlots
                        case "Repair": // Container InputSlots
                            lens = new DefaultIndexedLens(index, slotCount, slots);
                            break;
                        default: // Unknown
                            lens = new DefaultIndexedLens(index, slotCount, slots);
                    }
                }
                else {
                    // Unknown - fallback to OrderedInventory
                    lens = new DefaultIndexedLens(index, slotCount, slots);
                }
            }
            lenses.add(lens);
            index += slotCount;
        }


        List<Lens> additional = new ArrayList<>();
        try {
            if (crafting.out != null && crafting.base != null && crafting.grid != null) {
                additional.add(new CraftingInventoryLensImpl(crafting.out, crafting.base, crafting.grid.getWidth(), crafting.grid.getHeight(), slots));
            } else if (crafting.base != null && crafting.grid != null) {
                additional.add(new GridInventoryLensImpl(crafting.base, crafting.grid.getWidth(), crafting.grid.getHeight(), slots));
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Error while creating CraftingInventoryLensImpl or GridInventoryLensImpl for " + container.getClass().getName(), e);
        }

        // Lens containing/delegating to other lenses
        return new ContainerLens((InventoryAdapter) container, slots, lenses, additional);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @Nullable Lens generateAdapterLens(SlotProvider slots, int index,
            CraftingInventoryData crafting, List<Slot> slotList, @Nullable IInventory subInventory) {
        if (!(subInventory instanceof InventoryAdapter)) {
            return null;
        }
        Lens adapterLens = ((InventoryAdapter) subInventory).getRootLens();
        if (adapterLens == null) {
            return null;
        }
        if (subInventory.getSizeInventory() == 0) {
            return new DefaultEmptyLens(((InventoryAdapter) subInventory));
        }

        if (adapterLens instanceof PlayerInventoryLens) {
            if (slotList.size() == 36) {
                return new DelegatingLens(index, new PrimaryPlayerInventoryLens(index, slots, true), slots);
            }
            return null;
        }
        // For Crafting Result we need the Slot to get Filter logic
        if (subInventory instanceof InventoryCraftResult) {
            Slot slot = slotList.get(0);
            adapterLens = new CraftingOutputSlotLensImpl(index, item -> slot.isItemValid(((ItemStack) item)),
                    itemType -> (slot.isItemValid((ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1))));
            if (slot instanceof SlotCrafting) {
                crafting.out = index;
                if (crafting.base == null) {
                    // In case we do not find the InventoryCrafting later assume it is directly after the SlotCrafting
                    // e.g. for IC2 ContainerIndustrialWorkbench
                    crafting.base = index + 1;
                    crafting.grid = ((SlotCrafting) slot).craftMatrix;
                }
            }
        }
        if (subInventory instanceof InventoryCrafting) {
            crafting.base = index;
            crafting.grid = ((InventoryCrafting) subInventory);
        }
        return new DelegatingLens(index, adapterLens, slots);
    }

    /**
     * Calculates the slot count for the passed {@link Container}
     *
     * @return The {@link SlotLensCollection} with the amount of slots for this container.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static SlotProvider countSlots(net.minecraft.inventory.Container container, Fabric fabric) {
        if (container instanceof LensProvider) {
            return ((LensProvider) container).slotProvider(fabric, ((InventoryAdapter) container));
        }

        SlotLensCollection.Builder builder = new SlotLensCollection.Builder();
        for (Slot slot : container.inventorySlots) {
            if (slot instanceof SlotCrafting) {
                builder.add(1, CraftingOutputAdapter.class, (i) -> new CraftingOutputSlotLensImpl(i,
                        item -> slot.isItemValid(((ItemStack) item)),
                        itemType -> (slot.isItemValid((ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1)))));
            } else {
                builder.add(1);
            }
        }
        return builder.build();
    }

    public static InventoryArchetype getArchetype(net.minecraft.inventory.Container container) {
        if (container instanceof ContainerChest) {
            IInventory inventory = ((ContainerChest) container).getLowerChestInventory();
            if (inventory instanceof TileEntityChest) {
                return InventoryArchetypes.CHEST;
            } else if (inventory instanceof InventoryLargeChest) {
                return InventoryArchetypes.DOUBLE_CHEST;
            } else {
                return InventoryArchetypes.UNKNOWN;
            }
        } else if (container instanceof ContainerHopper) {
            return InventoryArchetypes.HOPPER;
        } else if (container instanceof ContainerDispenser) {
            return InventoryArchetypes.DISPENSER;
        } else if (container instanceof ContainerWorkbench) {
            return InventoryArchetypes.WORKBENCH;
        } else if (container instanceof ContainerFurnace) {
            return InventoryArchetypes.FURNACE;
        } else if (container instanceof ContainerEnchantment) {
            return InventoryArchetypes.ENCHANTING_TABLE;
        } else if (container instanceof ContainerRepair) {
            return InventoryArchetypes.ANVIL;
        } else if (container instanceof ContainerBrewingStand) {
            return InventoryArchetypes.BREWING_STAND;
        } else if (container instanceof ContainerBeacon) {
            return InventoryArchetypes.BEACON;
        } else if (container instanceof ContainerHorseInventory) {
            AbstractHorse horse = ((ContainerHorseInventory) container).horse;
            if (horse instanceof AbstractChestHorse && ((AbstractChestHorse) horse).hasChest()) {
                return InventoryArchetypes.HORSE_WITH_CHEST;
            }
            return InventoryArchetypes.HORSE;
        } else if (container instanceof ContainerMerchant) {
            return InventoryArchetypes.VILLAGER;
        } else if (container instanceof ContainerPlayer) {
            return InventoryArchetypes.PLAYER;
        }
        return InventoryArchetypes.UNKNOWN;
    }

    public static Carrier getCarrier(Container container) {
        if (container instanceof BlockCarrier) {
            return ((BlockCarrier) container);
        }
        if (container instanceof CustomContainer) {
            return ((CustomContainer) container).inv.getCarrier();
        } else if (container instanceof ContainerChest) {
            IInventory inventory = ((ContainerChest) container).getLowerChestInventory();
            if (inventory instanceof Carrier) {
                if (inventory instanceof TileEntityChest) {
                    return (Carrier) inventory;
                } else if (inventory instanceof InventoryLargeChest) {
                    return ((BlockCarrier) inventory);
                }
            }
            return carrierOrNull(inventory);
        } else if (container instanceof ContainerHopper) {
            return carrierOrNull(((ContainerHopper) container).hopperInventory);
        } else if (container instanceof ContainerDispenser) {
            return carrierOrNull(((ContainerDispenser) container).dispenserInventory);
        } else if (container instanceof ContainerFurnace) {
            return carrierOrNull(((ContainerFurnace) container).tileFurnace);
        } else if (container instanceof ContainerBrewingStand) {
            return carrierOrNull(((ContainerBrewingStand) container).tileBrewingStand);
        } else if (container instanceof ContainerBeacon) {
            return carrierOrNull(((ContainerBeacon) container).getTileEntity());
        } else if (container instanceof ContainerHorseInventory) {
            return (Carrier) ((ContainerHorseInventory) container).horse;
        } else if (container instanceof ContainerMerchant && ((ContainerMerchant) container).merchant instanceof Carrier) {
            return (Carrier) ((ContainerMerchant) container).merchant;
        } else if (container instanceof ContainerPlayer) {
            EntityPlayer player = ((ContainerPlayer) container).player;
            if (player instanceof EntityPlayerMP) {
                return (Carrier) player;
            }
        }

        // Fallback: Try to find a Carrier owning the first Slot of the Container
        if (container instanceof net.minecraft.inventory.Container) {
            for (Slot slot : ((net.minecraft.inventory.Container) container).inventorySlots) {
                // Slot Inventory is a Carrier?
                if (slot.inventory instanceof Carrier) {
                    return ((Carrier) slot.inventory);
                }
                // Slot Inventory is a TileEntity
                if (slot.inventory instanceof TileEntity) {
                    return new IMixinSingleBlockCarrier() {
                        @Override
                        public Location<org.spongepowered.api.world.World> getLocation() {
                            BlockPos pos = ((TileEntity) slot.inventory).getPos();
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
        Location<org.spongepowered.api.world.World> loc = ((IMixinContainer) container).getOpenLocation();
        if (loc != null) {
            return new IMixinSingleBlockCarrier() {
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
    private static Carrier carrierOrNull(IInventory inventory) {
        if (inventory instanceof Carrier) {
            return (Carrier) inventory;
        }
        if (inventory instanceof CarriedInventory) {
            Optional<Carrier> carrier = ((CarriedInventory) inventory).getCarrier();
            return carrier.orElse(null);
        }
        return null;
    }

    public static org.spongepowered.api.item.inventory.Slot getSlot(net.minecraft.inventory.Container container, int slot) {
        return ((IMixinContainer) container).getContainerSlot(slot);
    }
}
