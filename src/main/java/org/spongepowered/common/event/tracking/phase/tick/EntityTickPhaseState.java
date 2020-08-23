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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.common.accessor.entity.item.ItemFrameEntityAccessor;
import org.spongepowered.common.accessor.util.CombatEntryAccessor;
import org.spongepowered.common.accessor.util.CombatTrackerAccessor;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

class EntityTickPhaseState extends TickPhaseState<EntityTickContext> {


    private final BiConsumer<CauseStackManager.StackFrame, EntityTickContext> ENTITY_TICK_MODIFIER =
        super.getFrameModifier().andThen((frame, context) -> {
            final Entity tickingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", context));
            if (tickingEntity instanceof FallingBlockEntity) {
                context.getCreator().ifPresent(frame::pushCause);
            }
            frame.pushCause(tickingEntity);
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
        if (EntityUtil.isEntityDead((net.minecraft.entity.Entity) tickingEntity)) {
            if (tickingEntity instanceof LivingEntity) {
                final CombatEntry entry = ((CombatTrackerAccessor) ((LivingEntity) tickingEntity).getCombatTracker()).accessor$getBestCombatEntry();
                if (entry != null) {
                    if (((CombatEntryAccessor) entry).accessor$getDamageSrc() != null) {
                        frame.addContext(EventContextKeys.LAST_DAMAGE_SOURCE,
                                (DamageSource) ((CombatEntryAccessor) entry).accessor$getDamageSrc());
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
    public void postBlockTransactionApplication(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> transaction,
        final EntityTickContext context) {
        if (blockChange == BlockChange.BREAK) {
            final Entity tickingEntity = context.getSource(Entity.class).get();
            final BlockPos blockPos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
            final List<HangingEntity> hangingEntities = ((ServerWorld) tickingEntity.getWorld())
                .getEntitiesWithinAABB(HangingEntity.class, new AxisAlignedBB(blockPos, blockPos).grow(1.1D, 1.1D, 1.1D),
                    entityIn -> {
                        if (entityIn == null) {
                            return false;
                        }

                        final BlockPos entityPos = entityIn.getPosition();
                        // Hanging Neighbor Entity
                        if (entityPos.equals(blockPos.add(0, 1, 0))) {
                            return true;
                        }

                        // Check around source block
                        final Direction entityFacing = entityIn.getHorizontalFacing();

                        if (entityFacing == Direction.NORTH) {
                            return entityPos.equals(blockPos.add(Constants.Entity.HANGING_OFFSET_NORTH));
                        } else if (entityFacing == Direction.SOUTH) {
                            return entityIn.getPosition().equals(blockPos.add(Constants.Entity.HANGING_OFFSET_SOUTH));
                        } else if (entityFacing == Direction.WEST) {
                            return entityIn.getPosition().equals(blockPos.add(Constants.Entity.HANGING_OFFSET_WEST));
                        } else if (entityFacing == Direction.EAST) {
                            return entityIn.getPosition().equals(blockPos.add(Constants.Entity.HANGING_OFFSET_EAST));
                        }
                        return false;
                    });
            for (final HangingEntity entityHanging : hangingEntities) {
                if (entityHanging instanceof ItemFrameEntity) {
                    final ItemFrameEntity itemFrame = (ItemFrameEntity) entityHanging;
                    if (!itemFrame.removed) {
                        ((ItemFrameEntityAccessor) itemFrame).accessor$dropItemOrSelf((net.minecraft.entity.Entity) tickingEntity, true);
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

    @Override
    public boolean spawnEntityOrCapture(final EntityTickContext context, final Entity entity) {
        // Always need our source
        final Entity tickingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", context));

        // Now to actually do something....
        // It kinda sucks we have to make the cause frame here, but if we're already here, we are
        // effectively already going to throw an event, and we're configured not to bulk capture.
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            context.addCreatorAndNotifierToCauseStack(frame);
            frame.pushCause(tickingEntity);
            if (entity instanceof ExperienceOrbEntity) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                this.appendContextOfPossibleEntityDeath(tickingEntity, frame);
                final List<Entity> experience = new ArrayList<>(1);
                experience.add(entity);

                return SpongeCommonEventFactory.callSpawnEntity(experience, context);
            } else if (tickingEntity instanceof Ageable && tickingEntity.getClass() == entity.getClass()) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BREEDING);
                if (tickingEntity instanceof AnimalEntity) {
                    final PlayerEntity playerInLove = ((AnimalEntity) tickingEntity).getLoveCause();
                    if (playerInLove != null) {
                        frame.addContext(EventContextKeys.PLAYER, (Player) playerInLove);
                    }
                }
                final List<Entity> breeding = new ArrayList<>(1);
                breeding.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(breeding, context);

            } else if (entity instanceof Projectile) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PROJECTILE);
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
    public boolean alreadyProcessingBlockItemDrops() {
        return true;
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if bulk block captures are usable for this entity type (default true)
     */
    @Override
    public boolean doesBulkBlockCapture(final EntityTickContext context) {
        return context.allowsBulkBlockCaptures();
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
