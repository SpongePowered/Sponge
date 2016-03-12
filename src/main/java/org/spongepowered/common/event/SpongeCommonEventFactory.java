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

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.event.tracking.phase.util.PacketPhaseUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommonEventFactory {


    public static ChangeInventoryEvent.Held callChangeInventoryHeldEvent(EntityPlayerMP player,
            C09PacketHeldItemChange packetIn) {
        final Container inventoryContainer = player.inventoryContainer;
        final InventoryPlayer inventory = player.inventory;
        Slot sourceSlot = inventoryContainer.getSlot(inventory.currentItem + inventory.mainInventory.length);
        Slot targetSlot = inventoryContainer.getSlot(packetIn.getSlotId() + inventory.mainInventory.length);
        if (sourceSlot == null || targetSlot == null) {
            return null; // should never happen but just in case it does
        }

        ItemStackSnapshot sourceSnapshot =
                sourceSlot.getStack() != null ? ((org.spongepowered.api.item.inventory.ItemStack) sourceSlot.getStack()).createSnapshot()
                                              : ItemStackSnapshot.NONE;
        ItemStackSnapshot targetSnapshot = targetSlot.getStack() != null
                                           ? ((org.spongepowered.api.item.inventory.ItemStack) targetSlot.getStack()).createSnapshot() : ItemStackSnapshot.NONE;
        SlotTransaction sourceTransaction = new SlotTransaction(new SlotAdapter(sourceSlot), sourceSnapshot, sourceSnapshot);
        SlotTransaction targetTransaction = new SlotTransaction(new SlotAdapter(targetSlot), targetSnapshot, targetSnapshot);
        ImmutableList<SlotTransaction> transactions = new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();
        ChangeInventoryEvent.Held event = SpongeEventFactory.createChangeInventoryEventHeld(Cause.of(NamedCause.source(player)), (Inventory) inventoryContainer, transactions);
        SpongeImpl.postEvent(event);

        if (event.isCancelled()) {
            player.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(inventory.currentItem));
        } else {
            PacketPhaseUtil.handleCustomSlot(player, event.getTransactions());
            inventory.currentItem = packetIn.getSlotId();
            player.markPlayerActive();
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static CollideEntityEvent callCollideEntityEvent(net.minecraft.world.World world, @Nullable net.minecraft.entity.Entity sourceEntity,
                                                            List<net.minecraft.entity.Entity> entities) {
        Cause cause = null;
        if (sourceEntity != null) {
            cause = Cause.of(NamedCause.source(sourceEntity));
        } else {
            IMixinWorld spongeWorld = (IMixinWorld) world;
            CauseTracker causeTracker = spongeWorld.getCauseTracker();
            PhaseContext context = causeTracker.getPhases().peekContext();

            final Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(TrackingHelper.CURRENT_TICK_BLOCK, BlockSnapshot.class);
            final Optional<TileEntity> currentTickingTileEntity = context.firstNamed(NamedCause.SOURCE, TileEntity.class);
            final Optional<Entity> currentTickingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class);
            if (currentTickingBlock.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingBlock.get()));
            } else if (currentTickingTileEntity.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingTileEntity.get()));
            } else if (currentTickingEntity.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingEntity.get()));
            }

            if (cause == null) {
                return null;
            }
        }

        ImmutableList<Entity> originalEntities = ImmutableList.copyOf((List<Entity>) (List<?>) entities);
        CollideEntityEvent event = SpongeEventFactory.createCollideEntityEvent(cause, originalEntities, (List<Entity>) (List<?>) entities,
                (World) world);
        SpongeImpl.postEvent(event);
        return event;
    }

    @SuppressWarnings("rawtypes")
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(World world, BlockPos pos, EnumSet notifiedSides) {
        final CauseTracker causeTracker = ((IMixinWorld) world).getCauseTracker();
        final PhaseData currentPhase = causeTracker.getPhases().peek();
        Optional<User> playerNotifier = currentPhase.getContext().firstNamed(TrackingHelper.PACKET_PLAYER, User.class);
        BlockSnapshot snapshot = world.createSnapshot(VecHelper.toVector(pos));
        Map<Direction, BlockState> neighbors = new HashMap<>();

        if (notifiedSides != null) {
            for (Object obj : notifiedSides) {
                EnumFacing notifiedSide = (EnumFacing) obj;
                BlockPos offset = pos.offset(notifiedSide);
                Direction direction = DirectionFacingProvider.getInstance().getKey(notifiedSide).get();
                Location<World> location = new Location<World>(world, VecHelper.toVector(offset));
                if (location.getBlockY() >= 0 && location.getBlockY() <= 255) {
                    neighbors.put(direction, location.getBlock());
                }
            }
        }

        ImmutableMap<Direction, BlockState> originalNeighbors = ImmutableMap.copyOf(neighbors);
        // Determine cause
        Cause cause = Cause.of(NamedCause.source(snapshot));
        net.minecraft.world.World nmsWorld = (net.minecraft.world.World) world;
        IMixinChunk spongeChunk = (IMixinChunk) nmsWorld.getChunkFromBlockCoords(pos);
        if (spongeChunk != null) {
            if (playerNotifier.isPresent()) {
                cause = Cause.of(NamedCause.source(snapshot)).with(NamedCause.notifier(playerNotifier));
            } else {
                Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
                if (notifier.isPresent()) {
                    cause = Cause.of(NamedCause.source(snapshot)).with(NamedCause.notifier(notifier.get()));
                }
            }
            Optional<User> owner = spongeChunk.getBlockOwner(pos);
            if (owner.isPresent()) {
                cause = cause.with(NamedCause.owner(owner.get()));
            }
        }

        NotifyNeighborBlockEvent event = SpongeEventFactory.createNotifyNeighborBlockEvent(cause, originalNeighbors, neighbors);
        StaticMixinHelper.processingInternalForgeEvent = true;
        SpongeImpl.postEvent(event);
        StaticMixinHelper.processingInternalForgeEvent = false;
        return event;
    }

    public static boolean handleImpactEvent(net.minecraft.entity.Entity projectile, @Nullable ProjectileSource projectileSource,
            MovingObjectPosition movingObjectPosition) {
        MovingObjectType movingObjectType = movingObjectPosition.typeOfHit;
        Cause cause = Cause.source(projectile).named("ProjectileSource", projectileSource == null ? ProjectileSource.UNKNOWN : projectileSource).build();
        IMixinEntity spongeEntity = (IMixinEntity) projectile;
        Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
            cause = cause.with(NamedCause.of(NamedCause.OWNER, owner.get()));
        }

        Location<World> impactPoint = new Location<>((World) projectile.worldObj, VecHelper.toVector(movingObjectPosition.hitVec));

        if (movingObjectType == MovingObjectType.BLOCK) {
            BlockSnapshot targetBlock = ((World) projectile.worldObj).createSnapshot(VecHelper.toVector(movingObjectPosition.getBlockPos()));
            Direction side = Direction.NONE;
            if (movingObjectPosition.sideHit != null) {
                side = DirectionFacingProvider.getInstance().getKey(movingObjectPosition.sideHit).get();
            }

            CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(cause, impactPoint, targetBlock.getState(),
                    targetBlock.getLocation().get(), side);
            return SpongeImpl.postEvent(event);
        } else if (movingObjectPosition.entityHit != null) { // entity
            ImmutableList.Builder<Entity> entityBuilder = new ImmutableList.Builder<>();
            ArrayList<Entity> entityList = new ArrayList<>();
            entityList.add((Entity) movingObjectPosition.entityHit);
            CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(cause,
                    entityBuilder.add((Entity) movingObjectPosition.entityHit).build(), entityList, impactPoint, (World) projectile.worldObj);
            return SpongeImpl.postEvent(event);
        }

        return false;
    }


    public static void handleEntityMovement(net.minecraft.entity.Entity entity) {
        if (entity instanceof Player) {
            return; // this is handled elsewhere
        }
        if (entity.lastTickPosX != entity.posX || entity.lastTickPosY != entity.posY || entity.lastTickPosZ != entity.posZ
            || entity.rotationPitch != entity.prevRotationPitch || entity.rotationYaw != entity.prevRotationYaw) {
            // yes we have a move event.
            final double currentPosX = entity.posX;
            final double currentPosY = entity.posY;
            final double currentPosZ = entity.posZ;
            final Vector3d currentPositionVector = new Vector3d(currentPosX, currentPosY, currentPosZ);
            final double currentRotPitch = entity.rotationPitch;
            final double currentRotYaw = entity.rotationYaw;
            Vector3d currentRotationVector = new Vector3d(currentRotPitch, currentRotYaw, 0);
            DisplaceEntityEvent.Move event;
            Transform<World> previous = new Transform<>(((World) entity.worldObj),
                    new Vector3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ), new Vector3d(entity.prevRotationPitch, entity.prevRotationYaw,
                    0));
            Location<World> currentLocation = new Location<>(((World) entity.worldObj), currentPosX, currentPosY, currentPosZ);
            Transform<World> current = new Transform<>(currentLocation, currentRotationVector, ((Entity) entity).getScale());

            if (entity instanceof Humanoid) {
                event = SpongeEventFactory.createDisplaceEntityEventMoveTargetHumanoid(Cause.of(NamedCause.source(entity)), previous, current,
                        (Humanoid) entity);
            } else if (entity instanceof Living) {
                event = SpongeEventFactory.createDisplaceEntityEventMoveTargetLiving(Cause.of(NamedCause.source(entity)), previous, current,
                        (Living) entity);
            } else {
                event = SpongeEventFactory.createDisplaceEntityEventMove(Cause.of(NamedCause.source(entity)), previous, current,
                        (Entity) entity);
            }
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                entity.posX = entity.lastTickPosX;
                entity.posY = entity.lastTickPosY;
                entity.posZ = entity.lastTickPosZ;
                entity.rotationPitch = entity.prevRotationPitch;
                entity.rotationYaw = entity.prevRotationYaw;
            } else {
                Transform<World> worldTransform = event.getToTransform();
                Vector3d eventPosition = worldTransform.getPosition();
                Vector3d eventRotation = worldTransform.getRotation();
                if (!eventPosition.equals(currentPositionVector)) {
                    entity.posX = eventPosition.getX();
                    entity.posY = eventPosition.getY();
                    entity.posZ = eventPosition.getZ();
                }
                if (!eventRotation.equals(currentRotationVector)) {
                    entity.rotationPitch = (float) currentRotationVector.getX();
                    entity.rotationYaw = (float) currentRotationVector.getY();
                }
                //entity.setPositionAndRotation(position.getX(), position.getY(), position.getZ(), rotation.getFloorX(), rotation.getFloorY());
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
        }
    }

    public static void checkSpawnEvent(Entity entity, Cause cause) {
        final Optional<SpawnCause> spawnCause = cause.first(SpawnCause.class);
        checkArgument(cause.root() instanceof SpawnCause, "The cause does not have a SpawnCause! It has instead: {}", cause.root().toString());
        checkArgument(cause.containsNamed(NamedCause.SOURCE), "The cause does not have a \"Source\" named object!");
        checkArgument(cause.get(NamedCause.SOURCE, SpawnCause.class).isPresent(), "The SpawnCause is not the \"Source\" of the cause!");

    }

    public static Cause withExtra(Cause cause, String name, Object extra) {
        return cause.with(NamedCause.of(name, extra));
    }

    public static Event throwItemDropEvent(Cause cause, List<Entity> entities, ImmutableList<EntitySnapshot> snapshots, World world) {
        return SpongeEventFactory.createDropItemEventCustom(cause, entities, snapshots, world);
    }

    public static Event throwEntitySpawnCustom(Cause cause, List<Entity> entities, ImmutableList<EntitySnapshot> snapshots, World world) {
        return SpongeEventFactory.createSpawnEntityEventCustom(cause, entities, snapshots, world);
    }

    public static Cause handleEntityCreatedByPlayerCause(Cause cause) {
        final Object root = cause.root();
        Cause newCause;
        if (!(root instanceof SpawnCause)) {
            SpawnCause spawnCause;
            if (StaticMixinHelper.packetPlayer == null) {
                spawnCause = SpawnCause.builder().type(InternalSpawnTypes.PLACEMENT).build();
            } else {
                spawnCause = EntitySpawnCause.builder()
                        .entity((Entity) StaticMixinHelper.packetPlayer)
                        .type(InternalSpawnTypes.PLACEMENT)
                        .build();
            }
            List<NamedCause> causes = new ArrayList<>();
            causes.add(NamedCause.source(spawnCause));
            newCause = moveNewCausesUp(cause, causes);
        } else {
            newCause = cause;
        }
        return newCause;
    }

    public static Cause handleDropCause(Cause cause) {
        final Object root = cause.root();
        Cause newCause;
        if (!(root instanceof SpawnCause)) {
            SpawnCause spawnCause;
            if (StaticMixinHelper.packetPlayer == null) {
                spawnCause = SpawnCause.builder().type(InternalSpawnTypes.DROPPED_ITEM).build();
            } else {
                spawnCause = EntitySpawnCause.builder()
                        .entity((Entity) StaticMixinHelper.packetPlayer)
                        .type(InternalSpawnTypes.DROPPED_ITEM)
                        .build();
            }
            List<NamedCause> causes = new ArrayList<>();
            causes.add(NamedCause.source(spawnCause));
            newCause = moveNewCausesUp(cause, causes);
        } else {
            newCause = cause;
        }
        return newCause;
    }

    private static Cause moveNewCausesUp(Cause currentCause, List<NamedCause> newCauses) {
        int index = 0;
        for (Map.Entry<String, Object> entry : currentCause.getNamedCauses().entrySet()) {
            String entryName = entry.getKey();
            if ("Source".equals(entryName)) {
                newCauses.add(NamedCause.of("AdditionalSource", entry.getValue()));
            } else if ("AdditionalSource".equals(entryName)) {
                newCauses.add(NamedCause.of("PreviousSource", entry.getValue()));
            } else if (entryName.startsWith("PreviousSource")) {
                newCauses.add(NamedCause.of(entryName + index++, entry.getValue()));
            } else {
                newCauses.add(NamedCause.of(entryName, entry.getValue()));
            }
        }
        return Cause.of(newCauses);
    }
}
