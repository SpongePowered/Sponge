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
package org.spongepowered.common.event.tracking.phase.tick;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

class EntityTickPhaseState extends TickPhaseState<EntityTickContext> {

    EntityTickPhaseState() {
    }
    @SuppressWarnings("unchecked")
    @Override
    public void unwind(EntityTickContext phaseContext) {
        final Entity tickingEntity = phaseContext.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", phaseContext));
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(tickingEntity);
            phaseContext.addNotifierAndOwnerToCauseStack(frame);
            phaseContext.getCapturedEntitySupplier()
                    .acceptAndClearIfNotEmpty(entities -> {
                        final List<Entity> experience = new ArrayList<>(entities.size());
                        final List<Entity> nonExp = new ArrayList<>(entities.size());
                        final List<Entity> breeding = new ArrayList<>(entities.size());
                        final List<Entity> projectile = new ArrayList<>(entities.size());
                        for (Entity entity : entities) {
                            if (entity instanceof EntityXPOrb) {
                                experience.add(entity);
                            } else if (tickingEntity instanceof Ageable && tickingEntity.getClass() == entity.getClass()) {
                                breeding.add(entity);
                            } else if (entity instanceof Projectile) {
                                projectile.add(entity);
                            } else {
                                nonExp.add(entity);
                            }
                        }

                        if (!experience.isEmpty()) {
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                            if (EntityUtil.isEntityDead(tickingEntity)) {
                                if (tickingEntity instanceof EntityLivingBase) {
                                    CombatEntry entry = ((EntityLivingBase) tickingEntity).getCombatTracker().getBestCombatEntry();
                                    if (entry != null) {
                                        if (entry.damageSrc != null) {
                                            frame.addContext(EventContextKeys.LAST_DAMAGE_SOURCE,
                                                    (DamageSource) entry.damageSrc);
                                        }
                                    }
                                }
                            }
                            SpongeCommonEventFactory.callSpawnEntity(experience, phaseContext);
                            frame.removeContext(EventContextKeys.LAST_DAMAGE_SOURCE);
                        }
                        if (!breeding.isEmpty()) {
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BREEDING);
                            if (tickingEntity instanceof EntityAnimal) {
                                final EntityPlayer playerInLove = ((EntityAnimal) tickingEntity).getLoveCause();
                                if (playerInLove != null) {
                                    frame.addContext(EventContextKeys.PLAYER, (Player) playerInLove);
                                }
                            }
                            SpongeCommonEventFactory.callSpawnEntity(breeding, phaseContext);

                            frame.removeContext(EventContextKeys.PLAYER);
                        }
                        if (!projectile.isEmpty()) {
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PROJECTILE);
                            SpongeCommonEventFactory.callSpawnEntity(projectile, phaseContext);
                            frame.removeContext(EventContextKeys.SPAWN_TYPE);

                        }
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
                        SpongeCommonEventFactory.callSpawnEntity(nonExp, phaseContext);
                        frame.removeContext(EventContextKeys.SPAWN_TYPE);

                    });
            phaseContext.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(entities -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : entities) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        SpongeCommonEventFactory.callDropItemCustom(capturedEntities, phaseContext);
                        frame.removeContext(EventContextKeys.SPAWN_TYPE);
                    });
            phaseContext.getCapturedBlockSupplier()
                    .acceptAndClearIfNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, this, phaseContext));
            phaseContext.getBlockItemDropSupplier()
                    .acceptIfNotEmpty(map -> {
                        final List<BlockSnapshot> capturedBlocks = phaseContext.getCapturedBlocks();
                        for (BlockSnapshot snapshot : capturedBlocks) {
                            final BlockPos blockPos = ((IMixinLocation) (Object) snapshot.getLocation().get()).getBlockPos();
                            final Collection<EntityItem> entityItems = map.get(blockPos);
                            if (!entityItems.isEmpty()) {
                                frame.pushCause(snapshot);
                                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                                final List<Entity> items = entityItems.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                                SpongeCommonEventFactory.callDropItemDestruct(items, phaseContext);

                                frame.popCause();
                            }
                        }

                    });
            phaseContext.getCapturedItemStackSupplier()
                    .acceptAndClearIfNotEmpty(drops -> {
                        final List<Entity> items = drops.stream()
                                .map(drop -> drop.create(EntityUtil.getMinecraftWorld(tickingEntity)))
                                .map(EntityUtil::fromNative)
                                .collect(Collectors.toList());
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        SpongeCommonEventFactory.callDropItemCustom(items, phaseContext);
                    });
            this.fireMovementEvents(EntityUtil.toNative(tickingEntity));
        }
    }

    private void fireMovementEvents(net.minecraft.entity.Entity entity) {
        // Ignore movement event if entity is dead, a projectile, or item.
        // Note: Projectiles are handled with CollideBlockEvent.Impact
        if (entity.isDead || entity instanceof IProjectile || entity instanceof EntityItem) {
            return;
        }
        Entity spongeEntity = (Entity) entity;

        if (entity.lastTickPosX != entity.posX
            || entity.lastTickPosY != entity.posY
            || entity.lastTickPosZ != entity.posZ
            || entity.rotationPitch != entity.prevRotationPitch
            || entity.rotationYaw != entity.prevRotationYaw) {
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
        }
    }

    @Override
    public EntityTickContext createPhaseContext() {
        return new EntityTickContext().addCaptures();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, Transaction<BlockSnapshot> transaction,
        EntityTickContext context) {
        if (blockChange == BlockChange.BREAK) {
            final Entity tickingEntity = context.getSource(Entity.class).get();
            final BlockPos blockPos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
            for (EntityHanging entityHanging : EntityUtil.findHangingEntities(EntityUtil.getMinecraftWorld(tickingEntity), blockPos)) {
                if (entityHanging instanceof EntityItemFrame) {
                    final EntityItemFrame frame = (EntityItemFrame) entityHanging;
                    if (tickingEntity != null && !frame.isDead) {
                        frame.dropItemOrSelf(EntityUtil.toNative(tickingEntity), true);
                    }
                    frame.setDead();
                }
            }
        }
    }

    @Override
    public void postProcessSpawns(EntityTickContext phaseContext, ArrayList<Entity> entities) {
        super.postProcessSpawns(phaseContext, entities);
    }

    @Override
    public void appendContextPreExplosion(ExplosionContext explosionContext, EntityTickContext context) {
        context.getOwner().ifPresent(explosionContext::owner);
        context.getNotifier().ifPresent(explosionContext::notifier);
        final Entity tickingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking entity!", context));
        Sponge.getCauseStackManager().pushCause(tickingEntity);
    }

    @Override
    public boolean spawnEntityOrCapture(EntityTickContext context, Entity entity, int chunkX, int chunkZ) {
        final Entity tickingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", context));
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            context.addNotifierAndOwnerToCauseStack(frame);
            frame.pushCause(tickingEntity);
            if (entity instanceof EntityXPOrb) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                if (EntityUtil.isEntityDead(tickingEntity)) {
                    if (tickingEntity instanceof EntityLivingBase) {
                        CombatEntry entry = ((EntityLivingBase) tickingEntity).getCombatTracker().getBestCombatEntry();
                        if (entry != null) {
                            if (entry.damageSrc != null) {
                                frame.addContext(EventContextKeys.LAST_DAMAGE_SOURCE, (DamageSource) entry.damageSrc);
                            }
                        }
                    }
                }
                final List<Entity> experience = new ArrayList<>(1);
                experience.add(entity);
    
                return SpongeCommonEventFactory.callSpawnEntity(experience, context);
            } else if (tickingEntity instanceof Ageable && tickingEntity.getClass() == entity.getClass()) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BREEDING);
                if (tickingEntity instanceof EntityAnimal) {
                    final EntityPlayer playerInLove = ((EntityAnimal) tickingEntity).getLoveCause();
                    if (playerInLove != null) {
                        frame.addContext(EventContextKeys.PLAYER, (Player) playerInLove);
                    }
                }
                final List<Entity> breeding = new ArrayList<>(1);
                breeding.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(breeding, context);

            } else if (entity instanceof Projectile) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PROJECTILE);
                final List<Entity> projectile = new ArrayList<>(1);
                projectile.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(projectile, context);

            }
            final List<Entity> nonExp = new ArrayList<>(1);
            nonExp.add(entity);

            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
            return SpongeCommonEventFactory.callSpawnEntity(nonExp, context);
        }
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public String toString() {
        return "EntityTickPhase";
    }
}
