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
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.CraftingOutputAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.item.inventory.custom.CustomContainer;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.ContainerChestInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.ContainerPlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.CraftingOutputSlotLensImpl;
import org.spongepowered.common.mixin.core.inventory.MixinInventoryHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.Random;

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
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) worldServer;
        final PhaseData currentPhase = mixinWorld.getCauseTracker().getCurrentPhaseData();
        final IPhaseState currentState = currentPhase.state;
        if (CauseTracker.ENABLED && currentState.tracksBlockSpecificDrops()) {
            final PhaseContext context = currentPhase.context;
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

                            EntityItem entityitem = new EntityItem(worldServer, x + (double)f, y + (double)f1, z + (double)f2, itemStack.splitStack(i));

                            float f3 = 0.05F;
                            entityitem.motionX = RANDOM.nextGaussian() * 0.05000000074505806D;
                            entityitem.motionY = RANDOM.nextGaussian() * 0.05000000074505806D + 0.20000000298023224D;
                            entityitem.motionZ = RANDOM.nextGaussian() * 0.05000000074505806D;
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

    // TODO Inventory - Container lens
    // TODO Inventory - Add a fallback Container lens
    @SuppressWarnings("unchecked")
    @Nullable
    public static MinecraftLens getLens(net.minecraft.inventory.Container container, SlotCollection collection) {
        if (container instanceof ContainerChest) {
            return new ContainerChestInventoryLens((InventoryAdapter<IInventory, ItemStack>) container, collection, ((ContainerChest) container).numRows);
        } else if (container instanceof ContainerPlayer) {
            return new ContainerPlayerInventoryLens((InventoryAdapter<IInventory, ItemStack>) container, collection);
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
            return new SlotCollection.Builder().add(1, CraftingOutputAdapter.class, (i) -> new CraftingOutputSlotLensImpl(i, (t) -> false, (t) -> false)).add(4).add(4, EquipmentSlotAdapter.class).add(36).add(1).build();
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
            AbstractHorse horse = ((ContainerHorseInventory) container).theHorse;
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
                return null;
                // TODO: Decide what the carrier should be (wrapper of 2 Block-based carriers including info which block is the upper inventory)
            } else {
                return inventory instanceof Carrier ? ((Carrier) inventory) : null;
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
            return (Carrier) ((ContainerHorseInventory) container).theHorse;
        } else if (container instanceof ContainerMerchant && ((ContainerMerchant) container).theMerchant instanceof Carrier) {
            return (Carrier) ((ContainerMerchant) container).theMerchant;
        } else if (container instanceof ContainerPlayer) {
            EntityPlayer player = ((ContainerPlayer) container).player;
            if (player instanceof EntityPlayerMP) {
                return (Carrier) player;
            }
        }
        return null;
    }

    private static Carrier carrierOrNull(IInventory inventory) {
        if (inventory instanceof Carrier) {
            return (Carrier) inventory;
        }
        return null;
    }
}
