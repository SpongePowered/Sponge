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
package org.spongepowered.common.inventory.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.inventory.AbstractContainerMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.AbstractFurnaceMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.BeaconMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.BrewingStandMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.DispenserMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.HopperMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.HorseInventoryMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.ItemCombinerMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.MerchantMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.ResultSlotAccessor;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.bridge.world.inventory.container.ContainerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.inventory.SpongeLocationCarrier;
import org.spongepowered.common.inventory.SpongeBlockEntityCarrier;
import org.spongepowered.common.inventory.custom.CustomContainer;
import org.spongepowered.common.inventory.lens.CompoundSlotLensProvider;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.inventory.lens.impl.DelegatingLens;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.PrimaryPlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.SingleGridLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public final class ContainerUtil {

    private static final Random RANDOM = new Random();

    private ContainerUtil() {
    }

    // Note this is likely not doable throughout the implementation, only in certain cases

    public static Container fromNative(final net.minecraft.world.inventory.AbstractContainerMenu container) {
        return (Container) container;
    }

    // Note, this really cannot be guaranteed to work
    public static net.minecraft.world.inventory.AbstractContainerMenu toNative(final Container container) {
        return (net.minecraft.world.inventory.AbstractContainerMenu) container;
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
    public static void performBlockInventoryDrops(final ServerLevel worldServer, final double x, final double y, final double z, final net.minecraft.world.Container inventory) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
        if (context.doesBlockEventTracking()) {
            // this is where we could perform item stack pre-merging.
            // TODO - figure out how inventory drops will work?
            for (int j = 0; j < inventory.getContainerSize(); j++) {
                final net.minecraft.world.item.ItemStack itemStack = inventory.getItem(j);
                if (!itemStack.isEmpty()) {
                    final float f = ContainerUtil.RANDOM.nextFloat() * 0.8F + 0.1F;
                    final float f1 = ContainerUtil.RANDOM.nextFloat() * 0.8F + 0.1F;
                    final float f2 = ContainerUtil.RANDOM.nextFloat() * 0.8F + 0.1F;

                    while (!itemStack.isEmpty())
                    {
                        final int i = ContainerUtil.RANDOM.nextInt(21) + 10;

                        final ItemEntity entityitem = new ItemEntity(worldServer, x + f, y + f1, z + f2, itemStack.split(i));

                        entityitem.setDeltaMovement(
                            ContainerUtil.RANDOM.nextGaussian() * 0.05,
                            ContainerUtil.RANDOM.nextGaussian() * 0.05 + 0.2,
                            ContainerUtil.RANDOM.nextGaussian() * 0.05);
                        worldServer.addFreshEntity(entityitem);
                    }
                }
            }
            return;
        }
        // Finally, just default to spawning the entities normally, regardless of the case.
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            final net.minecraft.world.item.ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                Containers.dropItemStack(worldServer, x, y, z, itemStack);
            }
        }
    }

    private static class CraftingInventoryData {
        private @Nullable Integer out;
        private @Nullable Integer base;
        private @Nullable CraftingContainer grid;
    }

    /**
     * Generates a fallback lens for given Container
     *
     * @param container The Container to generate a lens for
     * @param slots The slots of the Container
     * @return The generated fallback lens
     */
    @SuppressWarnings("unchecked") public static Lens generateLens(final net.minecraft.world.inventory.AbstractContainerMenu container, final SlotLensProvider slots) {
        // Get all inventories viewed in the Container & count slots & retain order
        final Map<Optional<net.minecraft.world.Container>, List<Slot>> viewed = container.slots.stream()
                .collect(Collectors.groupingBy(i -> Optional.<net.minecraft.world.Container>ofNullable(i.container), LinkedHashMap::new, Collectors.toList()));
        int index = 0; // Count the index
        final CraftingInventoryData crafting = new CraftingInventoryData();
        int chestHeight = 0;
        final List<Lens> lenses = new ArrayList<>();
        for (final Map.Entry<Optional<net.minecraft.world.Container>, List<Slot>> entry : viewed.entrySet()) {
            final List<Slot> slotList = entry.getValue();
            final int slotCount = slotList.size();
            final net.minecraft.world.Container subInventory = entry.getKey().orElse(null);
            // Generate Lens based on existing InventoryAdapter
            Lens lens = ContainerUtil.generateAdapterLens(slots, index, crafting, slotList, subInventory);
            // Inventory size <> Lens size
            if (lens.slotCount() != slotCount) {
                CompoundSlotLensProvider slotProvider = new CompoundSlotLensProvider().add(((InventoryBridge) subInventory).bridge$getAdapter());
                CompoundLens.Builder lensBuilder = CompoundLens.builder();
                for (Slot slot : slotList) {
                    lensBuilder.add(((InventoryBridge) slot).bridge$getAdapter().inventoryAdapter$getRootLens());
                }
                lens = lensBuilder.build(slotProvider);
            }
            lenses.add(lens);
            index += slotCount;

            // Count height of 9 width grid
            if (chestHeight != -1) {
                if (lens instanceof DelegatingLens) {
                    Lens delegated = ((DelegatingLens) lens).getDelegate();
                    if (delegated instanceof PrimaryPlayerInventoryLens) {
                        delegated = ((PrimaryPlayerInventoryLens) delegated).getFullGrid();
                    }
                    if (delegated instanceof SingleGridLens) {
                        delegated = delegated.getSpanningChildren().get(0);
                    }
                    if (delegated instanceof GridInventoryLens) {
                        if (((GridInventoryLens) delegated).getWidth() == 9) {
                            chestHeight += ((GridInventoryLens) delegated).getHeight();
                        } else {
                            chestHeight = -1;
                        }
                    } else {
                        chestHeight = -1;
                    }
                } else {
                    chestHeight = -1;
                }
            }
        }

        final List<Lens> additional = new ArrayList<>();
        try {
            if (crafting.out != null && crafting.base != null && crafting.grid != null) {
                additional.add(new CraftingInventoryLens(crafting.out, crafting.base, crafting.grid.getWidth(), crafting.grid.getHeight(), slots));
            } else if (crafting.base != null && crafting.grid != null) {
                additional.add(new GridInventoryLens(crafting.base, crafting.grid.getWidth(), crafting.grid.getHeight(), slots));
            }
        } catch (Exception e) {
            SpongeCommon
                .logger().error("Error while creating CraftingInventoryLensImpl or GridInventoryLensImpl for " + container.getClass().getName(), e);
        }
        if (chestHeight > 0) { // Add container grid for chest/double chest
            additional.add(new GridInventoryLens(0, 9, chestHeight, slots));
        }


        // Lens containing/delegating to other lenses
        return new ContainerLens(container.slots.size(), (Class<? extends Inventory>) container.getClass(), slots, lenses, additional);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Lens generateAdapterLens(final SlotLensProvider slots, final int index,
            final org.spongepowered.common.inventory.util.ContainerUtil.CraftingInventoryData crafting, final List<Slot> slotList, final net.minecraft.world.@Nullable Container subInventory) {

        Lens lens = ((InventoryBridge) subInventory).bridge$getAdapter().inventoryAdapter$getRootLens();
        if (lens instanceof PlayerInventoryLens) {
            if (slotList.size() == 36) {
                return new DelegatingLens(index, new PrimaryPlayerInventoryLens(0, slots, true), slots);
            }
            return lens;
        }
        // For Crafting Result we need the Slot to get Filter logic
        if (subInventory instanceof ResultContainer) {
            final Slot slot = slotList.get(0);
            if (slot instanceof ResultSlotAccessor) {
                crafting.out = index;
                if (crafting.base == null) {
                    // In case we do not find the InventoryCrafting later assume it is directly after the SlotCrafting
                    // e.g. for IC2 ContainerIndustrialWorkbench
                    crafting.base = index + 1;
                    crafting.grid = ((ResultSlotAccessor) slot).accessor$craftSlots();
                }
            }
        }
        if (subInventory instanceof CraftingContainer) {
            crafting.base = index;
            crafting.grid = ((CraftingContainer) subInventory);
        }
        return new DelegatingLens(index, slotList, lens, slots);
    }

    public static @Nullable Carrier getCarrier(final Container container) {
        if (container instanceof BlockCarrier) {
            return ((BlockCarrier) container);
        }
        if (container instanceof CustomContainer) {
            return ((CustomContainer) container).inv.getCarrier();
        } else if (container instanceof ChestMenu) {
            final net.minecraft.world.Container inventory = ((ChestMenu) container).getContainer();
            if (inventory instanceof Carrier) {
                if (inventory instanceof ChestBlockEntity) {
                    return (Carrier) inventory;
                } else if (inventory instanceof CompoundContainer) {
                    return ((BlockCarrier) inventory);
                }
            }
            return ContainerUtil.carrierOrNull(inventory);
        } else if (container instanceof HopperMenuAccessor) {
            return ContainerUtil.carrierOrNull(((HopperMenuAccessor) container).accessor$hopper());
        } else if (container instanceof DispenserMenuAccessor) {
            return ContainerUtil.carrierOrNull(((DispenserMenuAccessor) container).accessor$dispenser());
        } else if (container instanceof AbstractFurnaceMenuAccessor) {
            return ContainerUtil.carrierOrNull(((AbstractFurnaceMenuAccessor) container).accessor$container());
        } else if (container instanceof BrewingStandMenuAccessor) {
            return ContainerUtil.carrierOrNull(((BrewingStandMenuAccessor) container).accessor$brewingStand());
        } else if (container instanceof BeaconMenu) {
            return new SpongeBlockEntityCarrier(((BeaconMenuAccessor) container).accessor$access().evaluate(Level::getBlockEntity).orElse(null), container);
        } else if (container instanceof HorseInventoryMenuAccessor) {
            return (Carrier) ((HorseInventoryMenuAccessor) container).accessor$horse();
        } else if (container instanceof MerchantMenuAccessor && ((MerchantMenuAccessor) container).accessor$trader() instanceof Carrier) {
            return (Carrier) ((MerchantMenuAccessor) container).accessor$trader();
        } else if (container instanceof ItemCombinerMenuAccessor) {
            final Player player = ((ItemCombinerMenuAccessor) container).accessor$player();
            if (player instanceof ServerPlayer) {
                return (Carrier) player;
            }
        }

        // Fallback: Try to find a Carrier owning the first Slot of the Container
        if (container instanceof AbstractContainerMenuAccessor) {
            for (final Slot slot : ((AbstractContainerMenuAccessor) container).accessor$slots()) {
                // Slot Inventory is a Carrier?
                if (slot.container instanceof Carrier) {
                    return ((Carrier) slot.container);
                }
                // Slot Inventory is a TileEntity
                if (slot.container instanceof BlockEntity) {
                    return new SpongeBlockEntityCarrier((BlockEntity) slot.container, container);
                }
            }
        }
        final ServerLocation loc = ((ContainerBridge) container).bridge$getOpenLocation();
        if (loc != null) {
            return new SpongeLocationCarrier(loc, container);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Carrier carrierOrNull(final net.minecraft.world.Container inventory) {
        if (inventory instanceof Carrier) {
            return (Carrier) inventory;
        }
        if (inventory instanceof CarriedInventory) {
            final Optional<Carrier> carrier = ((CarriedInventory) inventory).carrier();
            return carrier.orElse(null);
        }
        return null;
    }

}
