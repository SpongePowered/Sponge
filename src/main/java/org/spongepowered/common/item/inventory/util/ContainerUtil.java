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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.slot.InputSlot;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.Adapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.CraftingOutputAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.custom.CustomContainer;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.LensProvider;
import org.spongepowered.common.item.inventory.lens.comp.CraftingInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.Inventory2DLens;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.HotbarLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.Inventory2DLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.CraftingOutputSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.mixin.core.inventory.MixinInventoryHelper;
import org.spongepowered.common.util.VecHelper;

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
    public static void performBlockInventoryDrops(WorldServer worldServer, double x, double y, double z, IInventory inventory) {
        final PhaseData currentPhase = CauseTracker.getInstance().getCurrentPhaseData();
        final IPhaseState currentState = currentPhase.state;
        if (CauseTracker.ENABLED && currentState.tracksBlockSpecificDrops()) {
            final PhaseContext<?> context = currentPhase.context;
            if (!currentState.getPhase().ignoresItemPreMerging(currentState) && SpongeImpl.getGlobalConfig().getConfig().getOptimizations().doDropsPreMergeItemDrops()) {
                // Add itemstack to pre merge list
                final Multimap<BlockPos, ItemDropData> multimap = context.getBlockDropSupplier().get();
                final BlockPos pos = new BlockPos(x, y, z);
                final Collection<ItemDropData> itemStacks = multimap.get(pos);
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    final net.minecraft.item.ItemStack itemStack = inventory.getStackInSlot(i);
                    if (!itemStack.isEmpty()) {
                        SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.item(itemStack)
                                .position(VecHelper.toVector3d(pos))
                                .build());
                    }
                }
            } else {
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
    public static Lens<IInventory, ItemStack> getLens(Fabric<IInventory> fabric, net.minecraft.inventory.Container container, SlotCollection slots) {
        // Container is Adapter?
        if (container instanceof InventoryAdapter) {
            Lens lens = ((InventoryAdapter<?, ?>) container).getRootLens();
            if (lens != null) {
                return lens;
            }
            // else lens is null: Adapter is probably getting initialized
        }

        // Container provides Lens?
        if (container instanceof LensProvider) {
            // TODO LensProviders for all Vanilla Containers
            InventoryAdapter<IInventory, ItemStack> adapter;
            if (container instanceof InventoryAdapter) {
                adapter = ((InventoryAdapter) container);
            } else {
                adapter = new Adapter(MinecraftFabric.of(container), container);
            }
            return ((LensProvider) container).getRootLens(fabric, adapter);
        }

        // For those Sheep-Crafting inventories
        if (container.getInventory().size() == 0) {
            return null; // Empty Container
        }

        // Unknown Container - try to get Lenses for Sub-Inventories and wrap them in a ContainerLens
        return generateLens(container, slots);
    }

    /**
     * Generates a fallback lens for given Container
     *
     * @param container The Container to generate a lens for
     * @param slots The slots of the Container
     * @return The generated fallback lens
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Lens<IInventory, ItemStack> generateLens(net.minecraft.inventory.Container container, SlotCollection slots) {
        // Get all inventories viewed in the Container & count slots & retain order
        Map<Optional<IInventory>, List<Slot>> viewed = container.inventorySlots.stream()
                .collect(Collectors.groupingBy(i -> Optional.<IInventory>ofNullable(i.inventory), LinkedHashMap::new, Collectors.toList()));
        int index = 0; // Count the index
        List<Lens<IInventory, ItemStack>> lenses = new ArrayList<>();
        for (Map.Entry<Optional<IInventory>, List<Slot>> entry : viewed.entrySet()) {
            IInventory subInventory = entry.getKey().orElse(null);
            int slotCount = entry.getValue().size();
            Lens<IInventory, ItemStack> lens = null;
            boolean playerLens = false;
            if (subInventory instanceof InventoryAdapter) { // Check if sub-inventory is Adapter
                // TODO the lenses in "slots" are not used in this lens and thus cannot be found later
                Lens<IInventory, ItemStack> adapterLens = ((InventoryAdapter) subInventory).getRootLens();
                if (adapterLens != null) {
                    lens = copyLens(index, ((InventoryAdapter) subInventory), adapterLens, slots);
                    if (adapterLens instanceof PlayerInventoryLens) {
                        playerLens = true;
                    }
                }
            }
            if (lens == null && subInventory instanceof LensProvider) // Check if sub-inventory is LensProvider
            {
                Fabric<IInventory> keyFabric = MinecraftFabric.of(subInventory);
                lens = ((LensProvider) subInventory).getRootLens(keyFabric, new Adapter(keyFabric, container));
            }
            if (lens == null // Unknown Inventory or
                    || lens.slotCount() != slotCount) { // Inventory size <> Lens size
                if (subInventory instanceof InventoryCraftResult) { // InventoryCraftResult is a Slot
                    Slot slot = entry.getValue().get(0);
                    lens = new CraftingOutputSlotLensImpl(index, item -> slot.isItemValid(((ItemStack) item)),
                            itemType -> (slot.isItemValid((ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1))));
                } else if (subInventory instanceof InventoryCrafting) { // InventoryCrafting has width and height and is Input
                    InventoryCrafting craftGrid = (InventoryCrafting) subInventory;
                    lens = new GridInventoryLensImpl(index, craftGrid.getWidth(), craftGrid.getHeight(), craftGrid.getWidth(), InputSlot.class, slots);
                } else if (slotCount == 1) { // Unknown - A single Slot
                    lens = new SlotLensImpl(index);
                } else if ((lens instanceof PlayerInventoryLens || playerLens) && slotCount == 36) { // Player
                    // Player Inventory + Hotbar
                    lenses.add(new GridInventoryLensImpl(index, 9, 3, 9, slots));
                    lenses.add(new HotbarLensImpl(index + 27, 9, slots));
                    lens = null;
                }
                else if (subInventory instanceof InventoryBasic && subInventory.getClass().isAnonymousClass()) {
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
            if (lens != null) {
                lenses.add(lens);
            }
            index += slotCount;
        }
        // Lens containing/delegating to other lenses
        return new ContainerLens((InventoryAdapter<IInventory, ItemStack>) container, slots, lenses);
    }

    private static Lens<IInventory, ItemStack> copyLens(int base, InventoryAdapter<IInventory, ItemStack> adapter, Lens<IInventory, ItemStack> lens, SlotCollection slots)
    {
        if (lens instanceof FurnaceInventoryLens) {
            return new FurnaceInventoryLens(base, adapter, slots);
        }
        if (lens instanceof BrewingStandInventoryLens) {
            return new BrewingStandInventoryLens(base, adapter, slots);
        }
        if (lens instanceof CraftingInventoryLens) {
            return new CraftingInventoryLensImpl(base,
                    ((GridInventoryLens<?,?>) lens).getWidth(),
                    ((GridInventoryLens<?,?>) lens).getHeight(),
                    ((GridInventoryLens<?,?>) lens).getStride(),
                    slots);
        }
        if (lens instanceof GridInventoryLens) {
            return new GridInventoryLensImpl(base,
                    ((GridInventoryLens<?,?>) lens).getWidth(),
                    ((GridInventoryLens<?,?>) lens).getHeight(),
                    ((GridInventoryLens<?,?>) lens).getStride(),
                    slots);
        }
        if (lens instanceof Inventory2DLens) {
            return new Inventory2DLensImpl(base,
                    ((Inventory2DLens<?,?>) lens).getWidth(),
                    ((Inventory2DLens<?,?>) lens).getHeight(),
                    slots);
        }
        return null;
    }

    /**
     * Calculates the slot count for the passed {@link Container}
     *
     * @return The {@link SlotCollection} with the amount of slots for this container.
     */
    public static SlotCollection countSlots(net.minecraft.inventory.Container container) {
        if (container instanceof ContainerPlayer) {
            SlotCollection.Builder builder = new SlotCollection.Builder()
                    .add(1, CraftingOutputAdapter.class, (i) -> new CraftingOutputSlotLensImpl(i, (t) -> false, (t) -> false))
                    .add(4)
                    .add(4, EquipmentSlotAdapter.class)
                    .add(36)
                    .add(1);
            builder.add(container.inventorySlots.size() - 46); // Add additional slots (e.g. from mods)
            return builder.build();
        }
        return new SlotCollection.Builder().add(((MinecraftInventoryAdapter) container).getInventory().getSize()).build();
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
        if (container instanceof CustomContainer) {
            return ((CustomContainer) container).inv.getCarrier();
        } else if (container instanceof ContainerChest) {
            IInventory inventory = ((ContainerChest) container).getLowerChestInventory();
            if (inventory instanceof TileEntityChest) {
                return (Carrier) inventory;
            } else if (inventory instanceof InventoryLargeChest) {
                return ((Carrier) ((InventoryLargeChest) inventory).lowerChest);
                // TODO: Decide what the carrier should be (wrapper of 2 Block-based carriers including info which block is the upper inventory)
            } else {
                return carrierOrNull(inventory);
            }
        } else if (container instanceof ContainerHopper) {
            return carrierOrNull(((ContainerHopper) container).hopperInventory);
        } else if (container instanceof ContainerDispenser) {
            return carrierOrNull(((ContainerDispenser) container).dispenserInventory);
        } else if (container instanceof ContainerWorkbench) {
            return null; // TODO: Return a block-based carrier
        } else if (container instanceof ContainerFurnace) {
            return carrierOrNull(((ContainerFurnace) container).tileFurnace);
        } else if (container instanceof ContainerEnchantment) {
            /*ContainerEnchantment enchantment = ((ContainerEnchantment) container);
            net.minecraft.tileentity.TileEntity tileEntity = enchantment.worldPointer.getTileEntity(enchantment.position);
            return tileEntity != null && tileEntity instanceof TileEntityEnchantmentTable ? (Carrier) tileEntity : null;*/
            return null; // TODO: Decide whether or not this should be a Carrier in the api
        } else if (container instanceof ContainerRepair) {
            return null; // TODO: Return a block-base
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
                if (slot.inventory instanceof Carrier) {
                    return ((Carrier) slot.inventory);
                }
                return null;
            }
        }

        return null;
    }

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

    public static SlotAdapter getSlotAdapter(net.minecraft.inventory.Container container, int slot) {
        return ((IMixinContainer) container).getSlotAdapter(slot);
    }
}
