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
package org.spongepowered.common.event;

import static org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil.handleCustomCursor;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase.State;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.InventoryUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeLocatableBlock;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.WorldUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class SpongeCommonEventFactory {

    public static boolean convertingMapFormat = false;
    // Set if the player's held item changes during InteractBlockEvent.Secondary
    public static boolean playerInteractItemChanged = false;
    // Set if any of the events fired during interaction with a block (open
    public static boolean interactBlockEventCancelled = false;

    public static int lastAnimationPacketTick = 0;
    // For animation packet
    public static int lastSecondaryPacketTick = 0;
    public static int lastPrimaryPacketTick = 0;
    public static long lastTryBlockPacketTimeStamp = 0;
    public static boolean lastInteractItemOnBlockCancelled = false;
    public static WeakReference<EntityPlayerMP> lastAnimationPlayer;

    public static void callDropItemDispense(List<EntityItem> items, PhaseContext<?> context) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DISPENSE);
            final ArrayList<Entity> entities = new ArrayList<>();
            for (EntityItem item : items) {
                entities.add(EntityUtil.fromNative(item));
            }
            final DropItemEvent.Dispense dispense =
                SpongeEventFactory.createDropItemEventDispense(frame.getCurrentCause(), entities);
            SpongeImpl.postEvent(dispense);
            if (!dispense.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(context, dispense);
            }
        }
    }

    public static void callDropItemDrop(List<EntityItem> items, PhaseContext<?> context) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            final ArrayList<Entity> entities = new ArrayList<>();
            for (EntityItem item : items) {
                entities.add(EntityUtil.fromNative(item));
            }
            final DropItemEvent.Dispense dispense =
                SpongeEventFactory.createDropItemEventDispense(frame.getCurrentCause(), entities);
            SpongeImpl.postEvent(dispense);
            if (!dispense.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(context, dispense);
            }
        }
    }

    public static void callDropItemCustom(List<Entity> items, PhaseContext<?> context) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            final DropItemEvent.Custom event =
                SpongeEventFactory.createDropItemEventCustom(frame.getCurrentCause(), items);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(context, event);
            }
        }
    }

    public static void callDropItemCustom(List<Entity> items, PhaseContext<?> context, Supplier<Optional<UUID>> supplier) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.getCurrentContext().require(EventContextKeys.SPAWN_TYPE);
            final DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(frame.getCurrentCause(), items);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(event, supplier);
            }
        }
    }

    public static boolean callSpawnEntitySpawner(List<Entity> entities, PhaseContext<?> context) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.WORLD_SPAWNER);

            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEventSpawner(frame.getCurrentCause(), entities);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled() && event.getEntities().size() > 0) {
                return EntityUtil.processEntitySpawnsFromEvent(context, event);
            }
            return false;
        }
    }

    public static void callDropItemDestruct(List<Entity> entities, PhaseContext<?> context) {
        final DropItemEvent.Destruct destruct = SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), entities);
        SpongeImpl.postEvent(destruct);
        if (!destruct.isCancelled()) {
            EntityUtil.processEntitySpawnsFromEvent(context, destruct);
        }
    }

    public static boolean callSpawnEntity(List<Entity> entities, PhaseContext<?> context) {
        Sponge.getCauseStackManager().getCurrentContext().require(EventContextKeys.SPAWN_TYPE);
        try {
            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
            SpongeImpl.postEvent(event);
            return !event.isCancelled() && EntityUtil.processEntitySpawnsFromEvent(context, event);
        } catch (Exception e) {
            final PrettyPrinter printer = new PrettyPrinter(60).add("Exception trying to create a Spawn Event").centre().hr()
                .addWrapped(
                    "Something did not go well trying to create an event or while trying to throw a SpawnEntityEvent. My bet is it's gremlins")
                .add()
                .add("At the very least here's some information about what's going to be directly spawned without an event:");
            printer.add("Entities:");
            for (Entity entity : entities) {
                printer.add(" - " + entity);
            }
            printer.add("PhaseContext:");
            context.printCustom(printer, 4);
            printer.add();
            printer.add("Exception:");
            printer.add(e);
            printer.log(SpongeImpl.getLogger(), Level.ERROR);
            for (Entity entity : entities) {
                EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
            }
            return true;
        }
    }

    public static boolean callSpawnEntityCustom(List<Entity> entities, PhaseContext<?> context) {
        SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
        SpongeImpl.postEvent(event);
        return event.isCancelled() && EntityUtil.processEntitySpawnsFromEvent(context, event);
    }



    public static boolean callPlayerChangeInventoryPickupPreEvent(EntityPlayer player, EntityItem itemToPickup, int pickupDelay, UUID creator) {
        ItemStack stack = itemToPickup.getItem();
        Sponge.getCauseStackManager().pushCause(player);
        ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
        ChangeInventoryEvent.Pickup.Pre event =
                SpongeEventFactory.createChangeInventoryEventPickupPre(Sponge.getCauseStackManager().getCurrentCause(),
                        Optional.empty(), Collections.singletonList(snapshot), snapshot, ((Item) itemToPickup),
                        ((Inventory) player.inventory));
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        if (event.isCancelled()) {
            return false;
        }
        if (event.getCustom().isPresent()) {
            List<ItemStackSnapshot> list = event.getCustom().get();
            if (list.isEmpty()) {
                itemToPickup.getItem().setCount(0);
                return false;
            }

            boolean fullTransfer = true;
            IMixinInventoryPlayer capture = (IMixinInventoryPlayer) player.inventory;
            capture.setCapture(true);
            for (ItemStackSnapshot item : list) {
                org.spongepowered.api.item.inventory.ItemStack itemStack = item.createStack();
                player.inventory.addItemStackToInventory(ItemStackUtil.toNative(itemStack));
                if (!itemStack.isEmpty()) {
                    fullTransfer = false;
                    break;
                }

            }
            capture.setCapture(false);
            if (!fullTransfer) {
                for (SlotTransaction trans : capture.getCapturedTransactions()) {
                    trans.getSlot().set(trans.getOriginal().createStack());
                }
                return false;
            }
            if (!callPlayerChangeInventoryPickupEvent(player, capture)) {
                return false;
            }
            itemToPickup.getItem().setCount(0);
        }
        return true;
    }

    public static boolean callPlayerChangeInventoryPickupEvent(EntityPlayer player, IMixinInventoryPlayer inventory) {
        if (inventory.getCapturedTransactions().isEmpty()) {
            return true;
        }
        Sponge.getCauseStackManager().pushCause(player);
        ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(Sponge.getCauseStackManager().getCurrentCause(), (Inventory) player.inventoryContainer,
                inventory.getCapturedTransactions());
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        applyTransactions(event);
        inventory.getCapturedTransactions().clear();
        return !event.isCancelled();
    }

    public static ItemStack callInventoryPickupEvent(IInventory inventory, EntityItem item, ItemStack stack) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(inventory);

            ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            ChangeInventoryEvent.Pickup.Pre event =
                    SpongeEventFactory.createChangeInventoryEventPickupPre(frame.getCurrentCause(),
                            Optional.empty(), Collections.singletonList(snapshot), snapshot, ((Item) item),
                            ((Inventory) inventory));
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                return stack;
            }

            int size = inventory.getSizeInventory();
            ItemStack[] prevInventory = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                prevInventory[i] = inventory.getStackInSlot(i);
            }

            if (event.getCustom().isPresent()) {
                if (event.getCustom().get().isEmpty()) {
                    return ItemStack.EMPTY;
                }

                boolean fullTransfer = true;
                for (ItemStackSnapshot snap : event.getCustom().get()) {
                    ItemStack stackToAdd = ItemStackUtil.fromSnapshotToNative(snap);
                    ItemStack remaining = TileEntityHopper.putStackInInventoryAllSlots(null, inventory, stackToAdd, null);
                    if (!remaining.isEmpty()) {
                        fullTransfer = false;
                        break;
                    }
                }
                if (!fullTransfer) {
                    for (int i = 0; i < prevInventory.length; i++) {
                        inventory.setInventorySlotContents(i, prevInventory[i]);
                    }
                    return stack;
                }

                if (callInventoryPickupEvent(inventory, prevInventory)) {
                    return ItemStack.EMPTY;
                }
                return stack;
            } else {
                ItemStack remainder = TileEntityHopper.putStackInInventoryAllSlots(null, inventory, stack, null);
                if (callInventoryPickupEvent(inventory, prevInventory)) {
                    return remainder;
                }
                return stack;
            }
        }
    }

    private static List<SlotTransaction> generateTransactions(@Nullable Inventory inv, IInventory inventory, ItemStack[] previous) {
        if (inv == null) {
            return Collections.emptyList();
        }
        List<SlotTransaction> trans = new ArrayList<>();
        Iterator<Inventory> it = inv.slots().iterator();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            org.spongepowered.api.item.inventory.Slot slot = (org.spongepowered.api.item.inventory.Slot) it.next();
            ItemStack newStack = inventory.getStackInSlot(i);
            ItemStack prevStack = previous[i];
            if (!ItemStack.areItemStacksEqual(newStack, prevStack)) {
                trans.add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(prevStack), ItemStackUtil.snapshotOf(newStack)));
            }
        }
        return trans;
    }

    public static boolean callInventoryPickupEvent(IInventory inventory, ItemStack[] prevInventory) {
        Inventory spongeInventory = InventoryUtil.toInventory(inventory, null);
        List<SlotTransaction> trans = generateTransactions(spongeInventory, inventory, prevInventory);
        if (trans.isEmpty()) {
            return true;
        }
        ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(Sponge.getCauseStackManager().getCurrentCause(), spongeInventory, trans);
        SpongeImpl.postEvent(event);
        applyTransactions(event);
        return !event.isCancelled();
    }

    private static void applyTransactions(ChangeInventoryEvent.Pickup event) {
        if (event.isCancelled()) {
            for (SlotTransaction trans : event.getTransactions()) {
                trans.getSlot().set(trans.getOriginal().createStack());
            }
            return;
        }
        for (SlotTransaction trans : event.getTransactions()) {
            if (!trans.isValid()) {
                trans.getSlot().set(trans.getOriginal().createStack());
            } else if (trans.getCustom().isPresent()) {
                trans.getSlot().set(trans.getFinal().createStack());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static CollideEntityEvent callCollideEntityEvent(net.minecraft.world.World world, @Nullable net.minecraft.entity.Entity sourceEntity,
            List<net.minecraft.entity.Entity> entities) {

        PhaseTracker phaseTracker = PhaseTracker.getInstance();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (sourceEntity != null) {
                frame.pushCause(sourceEntity);
            } else {
                PhaseContext<?> context = phaseTracker.getCurrentContext();

                final Optional<LocatableBlock> currentTickingBlock = context.getSource(LocatableBlock.class);
                if (currentTickingBlock.isPresent()) {
                    frame.pushCause(currentTickingBlock.get());
                } else {
                    final Optional<TileEntity> currentTickingTileEntity = context.getSource(TileEntity.class);
                    if (currentTickingTileEntity.isPresent()) {
                        frame.pushCause(currentTickingTileEntity.get());
                    } else {
                        final Optional<Entity> currentTickingEntity = context.getSource(Entity.class);
                        if (currentTickingEntity.isPresent()) {
                            frame.pushCause(currentTickingEntity.get());
                        } else {
                            return null;
                        }
                    }
                }
            }
            phaseTracker.getCurrentPhaseData().context.addNotifierAndOwnerToCauseStack(frame);

            List<Entity> spEntities = (List<Entity>) (List<?>) entities;
            CollideEntityEvent event =
                    SpongeEventFactory.createCollideEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), spEntities);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, BlockPos pos) {
        return callChangeBlockEventPre(worldIn, ImmutableList.of(new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ())), null);
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, BlockPos pos, Object source) {
        return callChangeBlockEventPre(worldIn, ImmutableList.of(new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ())), source);
    }

    /**
     * Processes pre block event data then fires event.
     *
     * @param worldIn The world
     * @param locations The locations affected
     * @param source The source of event
     * @return The event
     */
    private static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, ImmutableList<Location<World>> locations, @Nullable Object source) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final PhaseData data = phaseTracker.getCurrentPhaseData();
            if (source == null) {
                source = data.context.getSource() == null ? worldIn : data.context.getSource();
            }

            EntityPlayer player = null;
            User owner = data.context.getOwner().orElse(null);
            User notifier = data.context.getNotifier().orElse(null);

            frame.pushCause(source);
            if (source instanceof Player) {
                player = (EntityPlayer) source;
                if (SpongeImplHooks.isFakePlayer(player)) {
                    frame.addContext(EventContextKeys.FAKE_PLAYER, EntityUtil.toPlayer(player));
                }
            }

            if (owner != null) {
                frame.addContext(EventContextKeys.OWNER, owner);
            } else if (player != null) {
                frame.addContext(EventContextKeys.OWNER, (User) player);
            }

            if (notifier != null) {
                frame.addContext(EventContextKeys.NOTIFIER, notifier);
            }

            ChangeBlockEvent.Pre event =
                SpongeEventFactory.createChangeBlockEventPre(frame.getCurrentCause(), locations);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Modify callChangeBlockEventModifyLiquidMix(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, @Nullable Object source) {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData data = phaseTracker.getCurrentPhaseData();

        BlockState fromState = BlockUtil.fromNative(worldIn.getBlockState(pos));
        BlockState toState = BlockUtil.fromNative(state);
        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);

        if (source == null) {
            // If source is null the source is the block itself
            source = new SpongeLocatableBlockBuilder().state(fromState).world((World) worldIn).position(pos.getX(), pos.getY(), pos.getZ()).build();
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            frame.addContext(EventContextKeys.LIQUID_MIX, (World) worldIn);
            if (owner != null) {
                frame.addContext(EventContextKeys.OWNER, owner);
            }
            if (notifier != null) {
                frame.addContext(EventContextKeys.NOTIFIER, notifier);
            }

            WorldProperties world = ((World) worldIn).getProperties();
            Vector3i position = new Vector3i(pos.getX(), pos.getY(), pos.getZ());

            Transaction<BlockSnapshot> transaction = new Transaction<>(BlockSnapshot.builder().blockState(fromState).world(world).position(position).build(),
                                                                       BlockSnapshot.builder().blockState(toState).world(world).position(position).build());
            ChangeBlockEvent.Modify event = SpongeEventFactory.createChangeBlockEventModify(frame.getCurrentCause(),
                    Collections.singletonList(transaction));

            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Break callChangeBlockEventModifyLiquidBreak(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int flags) {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData data = phaseTracker.getCurrentPhaseData();

        BlockState fromState = BlockUtil.fromNative(worldIn.getBlockState(pos));
        BlockState toState = BlockUtil.fromNative(state);
        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);
        Object source = data.context.getSource(LocatableBlock.class).orElse(null);
        if (source == null) {
            source = worldIn; // Fallback
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            frame.addContext(EventContextKeys.LIQUID_BREAK, (World) worldIn);
            if (owner != null) {
                frame.addContext(EventContextKeys.OWNER, owner);
            }
            if (notifier != null) {
                frame.addContext(EventContextKeys.NOTIFIER, notifier);
            }
            WorldProperties world = ((World) worldIn).getProperties();
            Vector3i position = new Vector3i(pos.getX(), pos.getY(), pos.getZ());

            Transaction<BlockSnapshot> transaction = new Transaction<>(BlockSnapshot.builder().blockState(fromState).world(world).position(position).build(),
                    BlockSnapshot.builder().blockState(toState).world(world).position(position).build());
            ChangeBlockEvent.Break event = SpongeEventFactory.createChangeBlockEventBreak(frame.getCurrentCause(),
                    Collections.singletonList(transaction));

            SpongeImpl.postEvent(event);
            return event;
        }
    }


    /**
     * This simulates the blocks a piston moves and calls the event for saner
     * debugging.
     *
     * @return if the event was cancelled
     */
    public static boolean handlePistonEvent(IMixinWorldServer world, WorldServer.ServerBlockEventList list, Object obj, BlockPos pos, Block blockIn,
            int eventId, int eventParam) {
        boolean extending = (eventId == 0);
        final IBlockState blockstate = ((net.minecraft.world.World) world).getBlockState(pos);
        EnumFacing direction = blockstate.getValue(BlockDirectional.FACING);
        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world((World) world).state((BlockState) blockstate).position(pos.getX(), pos.getY(), pos.getZ()).build();

        // Sets toss out duplicate values (even though there shouldn't be any)
        HashSet<Location<org.spongepowered.api.world.World>> locations = new HashSet<>();
        locations.add(new Location<>((World) world, pos.getX(), pos.getY(), pos.getZ()));

        BlockPistonStructureHelper movedBlocks = new BlockPistonStructureHelper((WorldServer) world, pos, direction, extending);
        movedBlocks.canMove(); // calculates blocks to be moved

        Stream.concat(movedBlocks.getBlocksToMove().stream(), movedBlocks.getBlocksToDestroy().stream())
                .map(block -> new Location<>((World) world, block.getX(), block.getY(), block.getZ()))
                .collect(Collectors.toCollection(() -> locations)); // SUPER
                                                                    // efficient
                                                                    // code!

        // If the piston is extending and there are no blocks to destroy, add the offset location for protection purposes
        if (extending && movedBlocks.getBlocksToDestroy().isEmpty()) {
            final List<BlockPos> movedPositions = movedBlocks.getBlocksToMove();
            BlockPos offsetPos;
            // If there are no blocks to move, add the offset of piston
            if (movedPositions.isEmpty()) {
                offsetPos = pos.offset(direction);
            } else {
                // Add the offset of last block set to move
                offsetPos = movedPositions.get(movedPositions.size() - 1).offset(direction);
            }
            locations.add(new Location<>((World) world, offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()));
        }

        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (extending) {
                frame.addContext(EventContextKeys.PISTON_EXTEND, WorldUtil.fromNative(world));
            } else {
                frame.addContext(EventContextKeys.PISTON_RETRACT, WorldUtil.fromNative(world));
            }
            return SpongeCommonEventFactory.callChangeBlockEventPre(world, ImmutableList.copyOf(locations), locatable)
                .isCancelled();
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(World world, BlockPos sourcePos, EnumSet notifiedSides) {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData peek = phaseTracker.getCurrentPhaseData();
        final PhaseContext<?> context = peek.context;
        // Don't fire notify events during world gen or while restoring
        if (peek.state.isWorldGeneration() || peek.state == State.RESTORING_BLOCKS) {
            return null;
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final BlockState blockstate = (BlockState) ((net.minecraft.world.World) world).getBlockState(sourcePos);
            final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(world).position(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ())
                    .location(new Location<>(world, sourcePos.getX(), sourcePos.getY(), sourcePos.getZ()))
                    .state(blockstate)
                    .build();
            if (context.getNotifier().isPresent()) {
                context.addNotifierAndOwnerToCauseStack(frame);
            } else {

                final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) world).getChunk(sourcePos);
                mixinChunk.getBlockNotifier(sourcePos).ifPresent(user -> frame.addContext(EventContextKeys.NOTIFIER, user));
                mixinChunk.getBlockOwner(sourcePos).ifPresent(owner -> frame.addContext(EventContextKeys.OWNER, owner));
            }
            Sponge.getCauseStackManager().pushCause(locatable);

            final Map<Direction, BlockState> neighbors = new HashMap<>();
            for (Object obj : notifiedSides) {
                EnumFacing notifiedSide = (EnumFacing) obj;
                BlockPos offset = sourcePos.offset(notifiedSide);

                Direction direction = DirectionFacingProvider.getInstance().getKey(notifiedSide).get();
                Location<World> location = new Location<>(world, VecHelper.toVector3i(offset));
                if (location.getBlockY() >= 0 && location.getBlockY() <= 255) {
                    neighbors.put(direction, location.getBlock());
                }
            }

            NotifyNeighborBlockEvent event =
                    SpongeEventFactory.createNotifyNeighborBlockEvent(Sponge.getCauseStackManager().getCurrentCause(), neighbors, neighbors);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static InteractEntityEvent.Primary callInteractEntityEventPrimary(EntityPlayerMP player, net.minecraft.entity.Entity entity, EnumHand
            hand, @Nullable Vector3d hitVec) {
        Sponge.getCauseStackManager().pushCause(player);
        InteractEntityEvent.Primary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractEntityEventPrimaryMainHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND, Optional.ofNullable(hitVec), EntityUtil.fromNative(entity));
        } else {
            event = SpongeEventFactory.createInteractEntityEventPrimaryOffHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND, Optional.ofNullable(hitVec), EntityUtil.fromNative(entity));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractEntityEvent.Secondary callInteractEntityEventSecondary(EntityPlayerMP player, net.minecraft.entity.Entity entity,
            EnumHand hand, @Nullable Vector3d hitVec) {
        InteractEntityEvent.Secondary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractEntityEventSecondaryMainHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND, Optional.ofNullable(hitVec), EntityUtil.fromNative(entity));
        } else {
            event = SpongeEventFactory.createInteractEntityEventSecondaryOffHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND, Optional.ofNullable(hitVec), EntityUtil.fromNative(entity));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractItemEvent.Primary callInteractItemEventPrimary(EntityPlayer player, ItemStack stack, EnumHand hand,
        @Nullable Vector3d hitVec, Object hitTarget) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (hitTarget instanceof Entity) {
                frame.addContext(EventContextKeys.ENTITY_HIT, ((Entity) hitTarget));
            } else if (hitTarget instanceof BlockSnapshot) {
                frame.addContext(EventContextKeys.BLOCK_HIT, (BlockSnapshot) hitTarget);
            }
            InteractItemEvent.Primary event;
            if (hand == EnumHand.MAIN_HAND) {
                event = SpongeEventFactory.createInteractItemEventPrimaryMainHand(frame.getCurrentCause(),
                        HandTypes.MAIN_HAND, Optional.ofNullable(hitVec), ItemStackUtil.snapshotOf(stack));
            } else {
                event = SpongeEventFactory.createInteractItemEventPrimaryOffHand(frame.getCurrentCause(),
                        HandTypes.OFF_HAND, Optional.ofNullable(hitVec), ItemStackUtil.snapshotOf(stack));
            }
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static InteractItemEvent.Secondary callInteractItemEventSecondary(EntityPlayer player, ItemStack stack, EnumHand hand,
            @Nullable Vector3d hitVec, Object hitTarget) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (hitTarget instanceof Entity) {
                frame.addContext(EventContextKeys.ENTITY_HIT, ((Entity) hitTarget));
            } else if (hitTarget instanceof BlockSnapshot) {
                frame.addContext(EventContextKeys.BLOCK_HIT, (BlockSnapshot) hitTarget);
            }
            InteractItemEvent.Secondary event;
            if (hand == EnumHand.MAIN_HAND) {
                event = SpongeEventFactory.createInteractItemEventSecondaryMainHand(frame.getCurrentCause(),
                        HandTypes.MAIN_HAND, Optional.ofNullable(hitVec), ItemStackUtil.snapshotOf(stack));
            } else {
                event = SpongeEventFactory.createInteractItemEventSecondaryOffHand(frame.getCurrentCause(),
                        HandTypes.OFF_HAND, Optional.ofNullable(hitVec), ItemStackUtil.snapshotOf(stack));
            }
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(EntityPlayer player, EnumHand hand, @Nullable Vector3d hitVec) {
        InteractBlockEvent.Primary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND,
                    Optional.ofNullable(hitVec), BlockSnapshot.NONE, Direction.NONE);
        } else {
            event = SpongeEventFactory.createInteractBlockEventPrimaryOffHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND,
                    Optional.ofNullable(hitVec), BlockSnapshot.NONE, Direction.NONE);
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(EntityPlayer player, BlockSnapshot blockSnapshot, EnumHand hand,
            EnumFacing side, @Nullable Vector3d hitVec) {
        InteractBlockEvent.Primary event;
        Direction direction = DirectionFacingProvider.getInstance().getKey(side).get();
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND,
                    Optional.ofNullable(hitVec), blockSnapshot, direction);
        } else {
            event = SpongeEventFactory.createInteractBlockEventPrimaryOffHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND,
                    Optional.ofNullable(hitVec), blockSnapshot, direction);
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Secondary createInteractBlockEventSecondary(EntityPlayer player, ItemStack heldItem, @Nullable Vector3d hitVec,
            BlockSnapshot targetBlock, Direction targetSide, EnumHand hand) {
        return createInteractBlockEventSecondary(player, heldItem, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED,
                hitVec, targetBlock, targetSide, hand);
    }

    public static InteractBlockEvent.Secondary createInteractBlockEventSecondary(EntityPlayer player, ItemStack heldItem, Tristate originalUseBlockResult, Tristate useBlockResult,
            Tristate originalUseItemResult, Tristate useItemResult, @Nullable Vector3d hitVec, BlockSnapshot targetBlock,
            Direction targetSide, EnumHand hand) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            InteractBlockEvent.Secondary event;
            if (!heldItem.isEmpty()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            }
            if (hand == EnumHand.MAIN_HAND) {
                event = SpongeEventFactory.createInteractBlockEventSecondaryMainHand(frame.getCurrentCause(),
                        originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, HandTypes.MAIN_HAND, Optional.ofNullable
                                (hitVec), targetBlock, targetSide);
            } else {
                event = SpongeEventFactory.createInteractBlockEventSecondaryOffHand(frame.getCurrentCause(),
                        originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, HandTypes.OFF_HAND, Optional.ofNullable
                                (hitVec), targetBlock, targetSide);
            }
            return event;
        }
    }

    public static MoveEntityEvent callMoveEntityEvent(net.minecraft.entity.Entity entity) {
        // Ignore movement event if entity is dead, a projectile, or item.
        // Note: Projectiles are handled with CollideBlockEvent.Impact
        if (entity.isDead || entity instanceof IProjectile || entity instanceof EntityItem) {
            return null;
        }

        Entity spongeEntity = (Entity) entity;

        if (entity.lastTickPosX != entity.posX
            || entity.lastTickPosY != entity.posY
            || entity.lastTickPosZ != entity.posZ
            || entity.rotationPitch != entity.prevRotationPitch
            || entity.rotationYaw != entity.prevRotationYaw) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(entity);
                // yes we have a move event.
                final double currentPosX = entity.posX;
                final double currentPosY = entity.posY;
                final double currentPosZ = entity.posZ;
    
                final Vector3d oldPositionVector = new Vector3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
                final Vector3d currentPositionVector = new Vector3d(currentPosX, currentPosY, currentPosZ);
    
                Vector3d oldRotationVector = new Vector3d(entity.prevRotationPitch, entity.prevRotationYaw, 0);
                Vector3d currentRotationVector = new Vector3d(entity.rotationPitch, entity.rotationYaw, 0);
                final Transform<World> oldTransform = new Transform<>(spongeEntity.getWorld(), oldPositionVector, oldRotationVector,
                        spongeEntity.getScale());
                final Transform<World> newTransform = new Transform<>(spongeEntity.getWorld(), currentPositionVector, currentRotationVector,
                        spongeEntity.getScale());
                final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), oldTransform, newTransform, spongeEntity);
    
                if (SpongeImpl.postEvent(event)) {
                    entity.posX = entity.lastTickPosX;
                    entity.posY = entity.lastTickPosY;
                    entity.posZ = entity.lastTickPosZ;
                    entity.rotationPitch = entity.prevRotationPitch;
                    entity.rotationYaw = entity.prevRotationYaw;
                } else {
                    Vector3d newPosition = event.getToTransform().getPosition();
                    if (!newPosition.equals(currentPositionVector)) {
                        entity.posX = newPosition.getX();
                        entity.posY = newPosition.getY();
                        entity.posZ = newPosition.getZ();
                    }
                    if (!event.getToTransform().getRotation().equals(currentRotationVector)) {
                        entity.rotationPitch = (float) currentRotationVector.getX();
                        entity.rotationYaw = (float) currentRotationVector.getY();
                    }
                    //entity.setPositionWithRotation(position.getX(), position.getY(), position.getZ(), rotation.getFloorX(), rotation.getFloorY());
                        /*
                        Some thoughts from gabizou: The interesting thing here is that while this is only called
                        in World.updateEntityWithOptionalForce, by default, it supposedly handles updating the rider entity
                        of the entity being handled here. The interesting issue is that since we are setting the transform,
                        the rider entity (and the rest of the rider entities) are being updated as well with the new position
                        and potentially world, which results in a dirty world usage (since the world transfer is handled by
                        us). Now, the thing is, the previous position is not updated either, and likewise, the current position
                        is being set by us as well. So, there's some issue I'm sure that is bound to happen with this
                        logic.
                         */
                    //((Entity) entity).setTransform(event.getToTransform());
                }
                return event;
            }
        }

        return null;
    }
    public static Optional<DestructEntityEvent.Death> callDestructEntityEventDeath(EntityLivingBase entity, @Nullable DamageSource source, boolean isMainThread) {
        final MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter();
        MessageChannel originalChannel;
        MessageChannel channel;
        Text originalMessage;
        Optional<User> sourceCreator = Optional.empty();
        boolean messageCancelled = false;

        if (entity instanceof EntityPlayerMP) {
            originalChannel = channel = ((IMixinEntityPlayerMP) entity).getDeathMessageChannel();
        } else {
            originalChannel = MessageChannel.TO_NONE;
            channel = MessageChannel.TO_NONE;
        }
        if (source instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) source;
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getImmediateSource();
            if (spongeEntity != null) {
                sourceCreator = spongeEntity.getCreatorUser();
            }
        }

        originalMessage = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
        formatter.getBody().add(new MessageEvent.DefaultBodyApplier(originalMessage));
        // Try-with-resources will not produce an NPE when trying to autoclose the frame if it is null. Client sided
        // checks need to be made here since entities can die on the client world.
        try (final StackFrame frame = isMainThread ? Sponge.getCauseStackManager().pushCauseFrame() : null) {
            if (isMainThread) {
                if (source != null) {
                    frame.pushCause(source);
                }
                if (sourceCreator.isPresent()) {
                    frame.addContext(EventContextKeys.OWNER, sourceCreator.get());
                }
            }

            final Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), source == null ? entity : source);
            DestructEntityEvent.Death event = SpongeEventFactory.createDestructEntityEventDeath(cause,
                originalChannel, Optional.of(channel), formatter,
                (Living) entity, entity.world.getGameRules().getBoolean("keepInventory"), messageCancelled);
            SpongeImpl.postEvent(event, true); // Client code should be able to cancel the death event if server cancels it.
            Text message = event.getMessage();
            // Check the event isn't cancelled either. If it is, then don't spawn the message.
            if (!event.isCancelled() && !event.isMessageCancelled() && !message.isEmpty()) {
                event.getChannel().ifPresent(eventChannel -> eventChannel.send(entity, event.getMessage()));
            }
            return Optional.of(event);
        }
    }

    public static boolean handleCollideBlockEvent(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entity, Direction direction) {
        if (pos.getY() <= 0) {
            return false;
        }

        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause( entity);

            if (!(entity instanceof EntityPlayer)) {
                IMixinEntity spongeEntity = (IMixinEntity) entity;
                User user = spongeEntity.getCreatorUser().orElse(null);
                if (user != null) {
                    frame.addContext(EventContextKeys.OWNER, user);
                }
            }

            // TODO: Add target side support
            CollideBlockEvent event = SpongeEventFactory.createCollideBlockEvent(frame.getCurrentCause(), (BlockState) state,
                    new Location<>((World) world, VecHelper.toVector3d(pos)), direction);
            boolean cancelled = SpongeImpl.postEvent(event);
            if (!cancelled) {
                IMixinEntity spongeEntity = (IMixinEntity) entity;
                if (!pos.equals(spongeEntity.getLastCollidedBlockPos())) {
                    final PhaseData peek = phaseTracker.getCurrentPhaseData();
                    final User notifier = peek.context.getNotifier().orElse(null);
                    if (notifier != null) {
                        IMixinChunk spongeChunk = spongeEntity.getActiveChunk();
                        if (spongeChunk == null) {
                            spongeChunk = (IMixinChunk) world.getChunk(pos);
                        }
                        spongeChunk.addTrackedBlockPosition(block, pos, notifier, PlayerTracker.Type.NOTIFIER);
                    }
                }
            }
            return cancelled;
        }
    }

    public static boolean handleCollideImpactEvent(net.minecraft.entity.Entity projectile, @Nullable ProjectileSource projectileSource,
            RayTraceResult movingObjectPosition) {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        RayTraceResult.Type movingObjectType = movingObjectPosition.typeOfHit;
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(projectile);
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, projectileSource == null
                    ? ProjectileSource.UNKNOWN
                    : projectileSource);
            final Optional<User> owner = phaseTracker.getCurrentPhaseData().context.getOwner();
            owner.ifPresent(user -> frame.addContext(EventContextKeys.OWNER, user));

            Location<World> impactPoint = new Location<>((World) projectile.world, VecHelper.toVector3d(movingObjectPosition.hitVec));
            boolean cancelled = false;

            if (movingObjectType == RayTraceResult.Type.BLOCK) {
                final BlockPos blockPos = movingObjectPosition.getBlockPos();
                if (blockPos.getY() <= 0) {
                    return false;
                }

                BlockSnapshot targetBlock = ((World) projectile.world).createSnapshot(VecHelper.toVector3i(movingObjectPosition.getBlockPos()));
                Direction side = Direction.NONE;
                if (movingObjectPosition.sideHit != null) {
                    side = DirectionFacingProvider.getInstance().getKey(movingObjectPosition.sideHit).get();
                }

                CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(frame.getCurrentCause(),
                        impactPoint, targetBlock.getState(),
                        targetBlock.getLocation().get(), side);
                cancelled = SpongeImpl.postEvent(event);
                // Track impact block if event is not cancelled
                if (!cancelled && owner.isPresent()) {
                    BlockPos targetPos = VecHelper.toBlockPos(impactPoint.getBlockPosition());
                    IMixinChunk spongeChunk = (IMixinChunk) projectile.world.getChunk(targetPos);
                    spongeChunk.addTrackedBlockPosition((Block) targetBlock.getState().getType(), targetPos, owner.get(), PlayerTracker.Type.NOTIFIER);
                }
            } else if (movingObjectPosition.entityHit != null) { // entity
                ArrayList<Entity> entityList = new ArrayList<>();
                entityList.add((Entity) movingObjectPosition.entityHit);
                CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(frame.getCurrentCause(), entityList, impactPoint);
                        cancelled = SpongeImpl.postEvent(event);
            }

            if (cancelled) {
                // Entities such as EnderPearls call setDead during onImpact. However, if the event is cancelled
                // setDead will never be called resulting in a bad state such as falling through world.
                projectile.setDead();
            }
            return cancelled;
        }
    }

    public static ClickInventoryEvent.Creative callCreativeClickInventoryEvent(EntityPlayerMP player, CPacketCreativeInventoryAction packetIn) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            // Creative doesn't inform server of cursor status so there is no way of knowing what the final stack is
            // Due to this, we can only send the original item that was clicked in slot
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
            if (((IMixinContainer) player.openContainer).getCapturedTransactions().isEmpty() && packetIn.getSlotId() >= 0
                    && packetIn.getSlotId() < player.openContainer.inventorySlots.size()) {
                org.spongepowered.api.item.inventory.Slot slot = ((IMixinContainer)player.openContainer).getContainerSlot(packetIn.getSlotId());
                if (slot != null) {
                    ItemStackSnapshot clickedItem = ItemStackUtil.snapshotOf(slot.peek().orElse(org.spongepowered.api.item.inventory.ItemStack.empty()));
                    ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(packetIn.getStack());
                    SlotTransaction slotTransaction = new SlotTransaction(slot, clickedItem, replacement);
                    ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
                }
            }
            ClickInventoryEvent.Creative event =
                SpongeEventFactory.createClickInventoryEventCreative(frame.getCurrentCause(), cursorTransaction,
                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                    new ArrayList<>(((IMixinContainer) player.openContainer).getCapturedTransactions()));
            ((IMixinContainer) player.openContainer).getCapturedTransactions().clear();
            ((IMixinContainer) player.openContainer).setCaptureInventory(false);
            SpongeImpl.postEvent(event);
            frame.popCause();
            return event;
        }
    }

    public static boolean callInteractInventoryOpenEvent(EntityPlayerMP player) {
        ItemStackSnapshot newCursor =
                player.inventory.getItemStack().isEmpty() ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, newCursor);
        InteractInventoryEvent.Open event =
                SpongeEventFactory.createInteractInventoryEventOpen(Sponge.getCauseStackManager().getCurrentCause(), cursorTransaction,
                        (org.spongepowered.api.item.inventory.Container) player.openContainer);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            player.closeScreen();
            return false;
        }
        // TODO - determine if/how we want to fire inventory events outside of click packet handlers
        //((IMixinContainer) player.openContainer).setCaptureInventory(true);
        // Custom cursor
        if (event.getCursorTransaction().getCustom().isPresent()) {
            handleCustomCursor(player, event.getCursorTransaction().getFinal());
        }
        return true;
    }

    public static InteractInventoryEvent.Close callInteractInventoryCloseEvent(Container container, EntityPlayerMP player,
            ItemStackSnapshot lastCursor, ItemStackSnapshot newCursor, boolean clientSource) {
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final InteractInventoryEvent.Close event =
                SpongeEventFactory.createInteractInventoryEventClose(Sponge.getCauseStackManager().getCurrentCause(), cursorTransaction, ContainerUtil.fromNative(container));
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            if (clientSource && container.getSlot(0) != null) {
                if (!(container instanceof ContainerPlayer)) {
                    // Inventory closed by client, reopen window and send
                    // container
                    player.openContainer = container;
                    final String guiId;
                    final Slot slot = container.getSlot(0);
                    final IInventory slotInventory = slot.inventory;
                    if (slotInventory instanceof IInteractionObject) {
                        guiId = ((IInteractionObject) slotInventory).getGuiID();
                    } else {
                        // expected fallback for unknown types
                        guiId = "minecraft:container";
                    }
                    slotInventory.openInventory(player);
                    player.connection.sendPacket(new SPacketOpenWindow(container.windowId, guiId, slotInventory
                            .getDisplayName(), slotInventory.getSizeInventory()));
                    // resync data to client
                    player.sendContainerToPlayer(container);
                } else {
                    // TODO: Maybe print a warning or throw an exception here?
                    // The player gui cannot be opened from the
                    // server so allowing this event to be cancellable when the
                    // GUI has been closed already would result
                    // in opening the wrong GUI window.
                }
            }
            // Handle cursor
            if (!event.getCursorTransaction().isValid()) {
                handleCustomCursor(player, event.getCursorTransaction().getOriginal());
            }
        } else {
            IMixinContainer mixinContainer = (IMixinContainer) player.openContainer;
            mixinContainer.getCapturedTransactions().clear();
            mixinContainer.setCaptureInventory(false);
            // Handle cursor
            if (!event.getCursorTransaction().isValid()) {
                handleCustomCursor(player, event.getCursorTransaction().getOriginal());
            } else if (event.getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
            if (!clientSource && player.openContainer != null && player.connection != null) {
                player.closeScreen();
            }
        }

        return event;
    }

    @Nullable
    public static Container displayContainer(EntityPlayerMP player, Inventory inventory, Text displayName) {
        net.minecraft.inventory.Container previousContainer = player.openContainer;
        net.minecraft.inventory.Container container;

        if (inventory instanceof CustomInventory) {
            if (!checkValidVanillaCustomInventory(((CustomInventory) inventory))) {
                return null; // Invalid size for vanilla inventory ; This is to
                             // prevent crashing the client with invalid data
            }
        }



        try {
            if (displayName != null) {
                ((IMixinEntityPlayerMP) player).setContainerDisplay(displayName);
            }
            if (inventory instanceof IInteractionObject) {
                final String guiId = ((IInteractionObject) inventory).getGuiID();

                switch (guiId) {
                    case "EntityHorse":
                        if (inventory instanceof CarriedInventory) {
                            CarriedInventory<?> cinventory = (CarriedInventory<?>) inventory;
                            if (cinventory.getCarrier().isPresent() && cinventory.getCarrier().get() instanceof AbstractHorse) {
                                player.openGuiHorseInventory(((AbstractHorse) cinventory.getCarrier().get()), (IInventory) inventory);
                            }
                        }
                        break;
                    case "minecraft:chest":
                        player.displayGUIChest((IInventory) inventory);
                        break;
                    case "minecraft:crafting_table":
                    case "minecraft:anvil":
                    case "minecraft:enchanting_table":
                        player.displayGui((IInteractionObject) inventory);
                        break;
                    default:
                        player.displayGUIChest((IInventory) inventory);
                        break;
                }
            } else if (inventory instanceof IInventory) {
                player.displayGUIChest(((IInventory) inventory));
            } else {
                return null;
            }
        } finally {
            if (displayName != null) {
                ((IMixinEntityPlayerMP) player).setContainerDisplay(null);
            }
        }

        container = player.openContainer;

        if (previousContainer == container) {
            return null;
        }

        if (!callInteractInventoryOpenEvent(player)) {
            return null;
        }

        if (container instanceof IMixinContainer) {
            // This overwrites the normal container behaviour and allows viewing
            // inventories that are more than 8 blocks away
            // This currently actually only works for the Containers mixed into
            // by MixinContainerCanInteract ; but throws no errors for other
            // containers

            // Allow viewing inventory; except when dead
            ((IMixinContainer) container).setCanInteractWith(p -> !p.isDead);
        }
        return container;
    }

    private static boolean checkValidVanillaCustomInventory(CustomInventory inventory) {
        InventoryArchetype archetype = inventory.getArchetype();
        if (InventoryArchetypes.CHEST.equals(archetype) || InventoryArchetypes.DOUBLE_CHEST.equals(archetype)) {
            int size = inventory.getSizeInventory(); // Divisible by
            // 9 AND less than 6 rows of 9 slots
            return size % 9 == 0 && size / 9 <= 6 && size != 0;
        }
        if (InventoryArchetypes.HOPPER.equals(archetype)) {
            return inventory.getSizeInventory() == 5 * 1;
        }
        if (InventoryArchetypes.DISPENSER.equals(archetype)) {
            return inventory.getSizeInventory() == 3 * 3;
        }
        if (InventoryArchetypes.WORKBENCH.equals(archetype)) {
            return inventory.getSizeInventory() == 3 * 3 + 1;
        }
        if (InventoryArchetypes.FURNACE.equals(archetype)) {
            return inventory.getSizeInventory() == 3;
        }
        if (InventoryArchetypes.ENCHANTING_TABLE.equals(archetype)) {
            return inventory.getSizeInventory() == 2;
        }
        if (InventoryArchetypes.ANVIL.equals(archetype)) {
            return inventory.getSizeInventory() == 3;
        }
        if (InventoryArchetypes.BREWING_STAND.equals(archetype)) {
            return inventory.getSizeInventory() == 5;
        }
        if (InventoryArchetypes.BEACON.equals(archetype)) {
            return inventory.getSizeInventory() == 1;
        }
        // TODO horse container are actually dependent on an horse entity
        if (InventoryArchetypes.HORSE.equals(archetype)) {
            return inventory.getSizeInventory() == 2;
        }
        if (InventoryArchetypes.HORSE_WITH_CHEST.equals(archetype)) {
            return inventory.getSizeInventory() == 2 + 5 * 3;
        }
        if (InventoryArchetypes.VILLAGER.equals(archetype)) {
            return inventory.getSizeInventory() == 3;
        }
        // else any other Archetype we cannot be sure which size is correct
        return true;
    }

    public static ChangeInventoryEvent.Transfer.Pre callTransferPre(Inventory source, Inventory destination) {
        Sponge.getCauseStackManager().pushCause(source);
        ChangeInventoryEvent.Transfer.Pre event = SpongeEventFactory.createChangeInventoryEventTransferPre(
                Sponge.getCauseStackManager().getCurrentCause(), source, destination);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        return event;
    }

    public static boolean callTransferPost(IMixinInventory captureSource, Inventory source, Inventory destination) {
        // TODO make sure we never got null
        if (captureSource == null || source == null || destination == null) {
            return true;
        }
        Sponge.getCauseStackManager().pushCause(source);
        ChangeInventoryEvent.Transfer.Post event =
                SpongeEventFactory.createChangeInventoryEventTransferPost(Sponge.getCauseStackManager().getCurrentCause(),
                        source, destination, captureSource.getCapturedTransactions());
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            // restore inventories
            setSlots(event.getTransactions(), SlotTransaction::getOriginal);
        } else {
            // handle custom inventory transaction result
            setSlots(event.getTransactions(), SlotTransaction::getFinal);
        }

        captureSource.getCapturedTransactions().clear();
        Sponge.getCauseStackManager().popCause();

        return event.isCancelled();
    }

    public static void setSlots(List<SlotTransaction> transactions, Function<SlotTransaction, ItemStackSnapshot> func) {
        transactions.forEach(t -> t.getSlot().set(func.apply(t).createStack()));
    }

    /**
     * Captures a transaction
     *
     * @param captureIn the {@link IMixinInventory} to capture the transaction in
     * @param inv the Inventory
     * @param index the affected SlotIndex
     * @param originalStack the original Stack
     */
    @SuppressWarnings("deprecation")
    public static void captureTransaction(IMixinInventory captureIn, Inventory inv, int index, ItemStack originalStack) {
        // TODO make sure we never got null
        if (captureIn == null || inv == null) {
            return;
        }
        Optional<org.spongepowered.api.item.inventory.Slot> slot = ((InventoryAdapter) inv).getSlot(index);
        if (slot.isPresent()) {
            SlotTransaction trans = new SlotTransaction(slot.get(),
                    ItemStackUtil.snapshotOf(originalStack),
                    ItemStackUtil.snapshotOf(slot.get().peek().orElse(org.spongepowered.api.item.inventory.ItemStack.empty())));
            captureIn.getCapturedTransactions().add(trans);
        } else {
            SpongeImpl.getLogger().warn("Unable to capture transaction from " + inv.getClass() + " at index " + index);
        }
    }

    /**
     * Captures a transaction
     *
     * @param captureIn the {@link IMixinInventory} to capture the transaction in
     * @param inv the Inventory
     * @param index the affected SlotIndex
     * @param transaction the transaction to execute
     * @return the result if the transaction
     */
    @SuppressWarnings("deprecation")
    public static ItemStack captureTransaction(IMixinInventory captureIn, Inventory inv, int index, Supplier<ItemStack> transaction) {
        // TODO make sure we never got null
        if (captureIn == null || inv == null) {
            return transaction.get();
        }

        Optional<org.spongepowered.api.item.inventory.Slot> slot = ((InventoryAdapter) inv).getSlot(index);
        if (!slot.isPresent()) {
            SpongeImpl.getLogger().warn("Unable to capture transaction from " + inv.getClass() + " at index " + index);
            return transaction.get();
        }
        ItemStackSnapshot original = slot.get().peek().map(ItemStackUtil::snapshotOf).orElse(ItemStackSnapshot.NONE);
        ItemStack remaining = transaction.get();
        if (remaining.isEmpty()) {
            ItemStackSnapshot replacement = slot.get().peek().map(ItemStackUtil::snapshotOf).orElse(ItemStackSnapshot.NONE);
            captureIn.getCapturedTransactions().add(new SlotTransaction(slot.get(), original, replacement));
        }
        return remaining;
    }

    public static SetAITargetEvent callSetAttackTargetEvent(@Nullable Entity target, Agent agent) {
        SetAITargetEvent event = SpongeEventFactory.createSetAITargetEvent(Sponge.getCauseStackManager().getCurrentCause(), Optional.ofNullable(target), agent);
        SpongeImpl.postEvent(event);
        return event;
    }

    public static Inventory toInventory(IInventory iinventory) {
        return ((Inventory) iinventory);
    }

    public static CraftItemEvent.Preview callCraftEventPre(EntityPlayer player, CraftingInventory inventory,
            SlotTransaction previewTransaction, @Nullable CraftingRecipe recipe, Container container, List<SlotTransaction> transactions) {
        CraftItemEvent.Preview event = SpongeEventFactory
                .createCraftItemEventPreview(Sponge.getCauseStackManager().getCurrentCause(), inventory, previewTransaction, Optional.ofNullable(recipe), ((Inventory) container), transactions);
        SpongeImpl.postEvent(event);
        PacketPhaseUtil.handleSlotRestore(player, container, new ArrayList<>(transactions), event.isCancelled());
        if (player instanceof EntityPlayerMP) {
            if (event.getPreview().getCustom().isPresent() || event.isCancelled() || !event.getPreview().isValid()) {
                ItemStackSnapshot stack = event.getPreview().getFinal();
                if (event.isCancelled() || !event.getPreview().isValid()) {
                    stack = event.getPreview().getOriginal();
                }
                // Resend modified output
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(0, 0, ItemStackUtil.fromSnapshotToNative(stack)));
            }

        }
        return event;
    }

    public static CraftItemEvent.Craft callCraftEventPost(EntityPlayer player, CraftingInventory inventory, ItemStackSnapshot result,
           @Nullable CraftingRecipe recipe, Container container, List<SlotTransaction> transactions) {
        // Get previous cursor if captured
        ItemStack previousCursor = ((IMixinContainer) container).getPreviousCursor();
        if (previousCursor == null) {
            previousCursor = player.inventory.getItemStack(); // or get the current one
        }
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackUtil.snapshotOf(previousCursor), ItemStackUtil.snapshotOf(player.inventory.getItemStack()));
        CraftItemEvent.Craft event = SpongeEventFactory
                .createCraftItemEventCraft(Sponge.getCauseStackManager().getCurrentCause(), result, inventory,
                        cursorTransaction, Optional.ofNullable(recipe), ((org.spongepowered.api.item.inventory.Container) container), transactions);
        SpongeImpl.postEvent(event);

        ((IMixinContainer) container).setCaptureInventory(false);
        // handle slot-transactions
        PacketPhaseUtil.handleSlotRestore(player, container, new ArrayList<>(transactions), event.isCancelled());
        if (event.isCancelled() || !event.getCursorTransaction().isValid() || event.getCursorTransaction().getCustom().isPresent()) {
            // handle cursor-transaction
            ItemStackSnapshot newCursor = event.isCancelled() || event.getCursorTransaction().isValid() ? event.getCursorTransaction().getOriginal() : event.getCursorTransaction().getFinal();
            player.inventory.setItemStack(ItemStackUtil.fromSnapshotToNative(newCursor));
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
            }
        }

        transactions.clear();
        ((IMixinContainer) container).setCaptureInventory(true);
        return event;
    }

    public static void callPostPlayerRespawnEvent(EntityPlayerMP playerMP, boolean conqueredEnd) {
        // We overwrite this method in SpongeForge, in order to fire
        // Forge's PlayerRespawnEvent
    }
}
