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

import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.common.accessor.world.damagesource.CombatEntryAccessor;
import org.spongepowered.common.accessor.world.damagesource.CombatTrackerAccessor;
import org.spongepowered.common.accessor.world.entity.decoration.ItemFrameAccessor;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.EntityTrackedBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.AABB;

class EntityTickPhaseState extends TickPhaseState<EntityTickContext> {


    private final BiConsumer<CauseStackManager.StackFrame, EntityTickContext> ENTITY_TICK_MODIFIER =
        super.getFrameModifier().andThen((frame, context) -> {
            final Entity tickingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", context));
            if (tickingEntity instanceof FallingBlockEntity) {
                context.getCreator().ifPresent(frame::pushCause);
            }
            frame.pushCause(tickingEntity);
            ((EntityTrackedBridge) tickingEntity).populateFrameModifier(frame, context);
        });

    EntityTickPhaseState() {
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, EntityTickContext> getFrameModifier() {
        return this.ENTITY_TICK_MODIFIER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(final EntityTickContext phaseContext) {
        final Entity tickingEntity = phaseContext.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", phaseContext));
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            this.processCaptures(tickingEntity, phaseContext, frame);
        }
    }

    protected void processCaptures(final Entity tickingEntity, final EntityTickContext phaseContext, final CauseStackManager.StackFrame frame) {
        phaseContext.addCreatorAndNotifierToCauseStack(frame);
        // If we're doing bulk captures for blocks, go ahead and do them. otherwise continue with entity checks
        if (phaseContext.allowsBulkBlockCaptures()) {
            if (!TrackingUtil.processBlockCaptures(phaseContext)) {
                ((EntityBridge) tickingEntity).bridge$onCancelledBlockChange(phaseContext);
            }
        }
        // And finally, if we're not capturing entities, there's nothing left for us to do.
        if (!phaseContext.allowsBulkEntityCaptures()) {
            return; // The rest of this method is all about entity captures
        }
//        phaseContext.getCapturedEntitySupplier()
//                .acceptAndClearIfNotEmpty(entities -> {
//                    final List<Entity> experience = new ArrayList<>(entities.size());
//                    final List<Entity> nonExp = new ArrayList<>(entities.size());
//                    final List<Entity> breeding = new ArrayList<>(entities.size());
//                    final List<Entity> projectile = new ArrayList<>(entities.size());
//                    for (final Entity entity : entities) {
//                        if (entity instanceof ExperienceOrbEntity) {
//                            experience.add(entity);
//                        } else if (tickingEntity instanceof Ageable && tickingEntity.getClass() == entity.getClass()) {
//                            breeding.add(entity);
//                        } else if (entity instanceof Projectile) {
//                            projectile.add(entity);
//                        } else {
//                            nonExp.add(entity);
//                        }
//                    }
//
//                    if (!experience.isEmpty()) {
//                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
//                        this.appendContextOfPossibleEntityDeath(tickingEntity, frame);
//                        SpongeCommonEventFactory.callSpawnEntity(experience, phaseContext);
//                        frame.removeContext(EventContextKeys.LAST_DAMAGE_SOURCE);
//                    }
//                    if (!breeding.isEmpty()) {
//                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BREEDING);
//                        if (tickingEntity instanceof AnimalEntity) {
//                            final PlayerEntity playerInLove = ((AnimalEntity) tickingEntity).getLoveCause();
//                            if (playerInLove != null) {
//                                frame.addContext(EventContextKeys.PLAYER, (Player) playerInLove);
//                            }
//                        }
//                        SpongeCommonEventFactory.callSpawnEntity(breeding, phaseContext);
//
//                        frame.removeContext(EventContextKeys.PLAYER);
//                    }
//                    if (!projectile.isEmpty()) {
//                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PROJECTILE);
//                        SpongeCommonEventFactory.callSpawnEntity(projectile, phaseContext);
//                        frame.removeContext(EventContextKeys.SPAWN_TYPE);
//
//                    }
//                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
//                    SpongeCommonEventFactory.callSpawnEntity(nonExp, phaseContext);
//                    frame.removeContext(EventContextKeys.SPAWN_TYPE);
//
//                });
    }

    private void appendContextOfPossibleEntityDeath(final Entity tickingEntity, final CauseStackManager.StackFrame frame) {
        if (EntityUtil.isEntityDead((net.minecraft.world.entity.Entity) tickingEntity)) {
            if (tickingEntity instanceof LivingEntity) {
                final CombatEntry entry = ((CombatTrackerAccessor) ((LivingEntity) tickingEntity).getCombatTracker()).invoker$getMostSignificantFall();
                if (entry != null) {
                    if (((CombatEntryAccessor) entry).accessor$source() != null) {
                        frame.addContext(EventContextKeys.LAST_DAMAGE_SOURCE,
                                (DamageSource) ((CombatEntryAccessor) entry).accessor$source());
                    }
                }
            }
        }
    }

    @Override
    protected EntityTickContext createNewContext(final PhaseTracker tracker) {
        return new EntityTickContext(this, tracker).addCaptures();
    }

    @Override
    public void postBlockTransactionApplication(
        final EntityTickContext context, final BlockChange blockChange,
        final BlockTransactionReceipt transaction
    ) {
        if (blockChange == BlockChange.BREAK) {
            final Entity tickingEntity = context.getSource(Entity.class).get();
            final BlockPos blockPos = VecHelper.toBlockPos(transaction.originalBlock().position());
            final List<HangingEntity> hangingEntities = ((ServerLevel) tickingEntity.world())
                .getEntitiesOfClass(HangingEntity.class, new AABB(blockPos, blockPos).inflate(1.1D, 1.1D, 1.1D),
                    entityIn -> {
                        if (entityIn == null) {
                            return false;
                        }

                        final BlockPos entityPos = entityIn.getPos();
                        // Hanging Neighbor Entity
                        if (entityPos.equals(blockPos.offset(0, 1, 0))) {
                            return true;
                        }

                        // Check around source block
                        final Direction entityFacing = entityIn.getDirection();

                        if (entityFacing == Direction.NORTH) {
                            return entityPos.equals(blockPos.offset(Constants.Entity.HANGING_OFFSET_NORTH));
                        } else if (entityFacing == Direction.SOUTH) {
                            return entityIn.getPos().equals(blockPos.offset(Constants.Entity.HANGING_OFFSET_SOUTH));
                        } else if (entityFacing == Direction.WEST) {
                            return entityIn.getPos().equals(blockPos.offset(Constants.Entity.HANGING_OFFSET_WEST));
                        } else if (entityFacing == Direction.EAST) {
                            return entityIn.getPos().equals(blockPos.offset(Constants.Entity.HANGING_OFFSET_EAST));
                        }
                        return false;
                    });
            for (final HangingEntity entityHanging : hangingEntities) {
                if (entityHanging instanceof ItemFrame) {
                    final ItemFrame itemFrame = (ItemFrame) entityHanging;
                    if (!itemFrame.removed) {
                        ((ItemFrameAccessor) itemFrame).invoker$dropItem((net.minecraft.world.entity.Entity) tickingEntity, true);
                    }
                    itemFrame.remove();
                }
            }
        }
    }

    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final EntityTickContext context) {
        if (!context.applyNotifierIfAvailable(explosionContext::creator)) {
            context.applyOwnerIfAvailable(explosionContext::creator);
        }
        explosionContext.source(context.getSource(Entity.class).orElseThrow(() -> new IllegalStateException("Ticking a non Entity")));
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if block events are to be tracked by the specific type of entity (default is true)
     */
    @Override
    public boolean doesBlockEventTracking(final EntityTickContext context) {
        return context.allowsBlockEvents();
    }
}
