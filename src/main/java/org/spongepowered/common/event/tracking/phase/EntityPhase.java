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
package org.spongepowered.common.event.tracking.phase;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.function.EntityFunction;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class EntityPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        DEATH_DROPS_SPAWNING,
        DEATH_UPDATE,
        ;

        @Override
        public EntityPhase getPhase() {
            return TrackingPhases.ENTITY;
        }

        @Override
        public boolean tracksEntitySpecificDrops() {
            return true;
        }

    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.DEATH_DROPS_SPAWNING) {
            final Entity dyingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class).orElseThrow(PhaseUtil.throwWithContext("Dying entity not found!", phaseContext));
            phaseContext.getCapturedItemsSupplier()
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing entities but we're not capturing!", phaseContext))
                    .ifPresentAndNotEmpty(items -> EntityFunction.Drops.DEATH_DROPS.process(dyingEntity, causeTracker, phaseContext, items));
            phaseContext.getCapturedEntitySupplier()
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing entities but we're not capturing!", phaseContext))
                    .ifPresentAndNotEmpty(entities -> EntityFunction.Entities.DEATH_DROPS.process(dyingEntity, causeTracker, phaseContext, entities));
            phaseContext.getCapturedEntityDrops().ifPresent(map -> {
                final Collection<ItemStack> itemStacks = map.get(dyingEntity.getUniqueId());
                if (itemStacks.isEmpty()) {
                    return;
                }
                final List<ItemStack> items = new ArrayList<>();
                final List<ItemStackSnapshot> snapshots = itemStacks.stream().map(ItemStack::createSnapshot).collect(Collectors.toList());
                final ImmutableList<ItemStackSnapshot> originals = ImmutableList.copyOf(snapshots);
                final DamageSource damageSource = phaseContext.firstNamed(InternalNamedCauses.General.DAMAGE_SOURCE, DamageSource.class).get();

                final Cause preCause = Cause.source(dyingEntity).named("Attacker", damageSource).build();
                EventConsumer.event(SpongeEventFactory.createDropItemEventPre(preCause, originals, snapshots))
                        .nonCancelled(event -> event.getDroppedItems().stream().map(ItemStackSnapshot::createStack).forEach(items::add))
                        .process();
                if (!items.isEmpty()) {
                    final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(dyingEntity);
                    final List<Entity> itemEntities = items.stream()
                            .map(ItemStackUtil::toNative)
                            .map(item -> new EntityItem(causeTracker.getMinecraftWorld(), minecraftEntity.posX, minecraftEntity.posY,
                                    minecraftEntity.posZ, item))
                            .map(EntityUtil::fromNative)
                            .collect(Collectors.toList());
                    final Cause cause = Cause.source(EntitySpawnCause.builder().entity(dyingEntity)
                            .type(InternalSpawnTypes.ENTITY_DEATH)
                            .build())
                            .named(NamedCause.of("Attacker", damageSource))
                            .build();
                    EventConsumer.event(SpongeEventFactory.createDropItemEventDestruct(cause, itemEntities, causeTracker.getWorld()))
                            .nonCancelled(event -> event.getEntities().stream().forEach(entity -> {
                                TrackingUtil.associateEntityCreator(phaseContext, entity, causeTracker.getMinecraftWorld());
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }))
                            .process();

                }

            });
            // TODO Handle block changes
        } else if (state == State.DEATH_UPDATE) {
            final Entity dyingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Dying entity not found!", phaseContext));
            phaseContext.getCapturedItemsSupplier()
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing entities from a dying entity!", phaseContext))
                    .ifPresentAndNotEmpty(items -> EntityFunction.Drops.DEATH_UPDATES.process(dyingEntity, causeTracker, phaseContext, items));
            phaseContext.getCapturedEntitySupplier()
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing entities from a dying entity!", phaseContext))
                    .ifPresentAndNotEmpty(entities -> EntityFunction.Entities.DEATH_UPDATES.process(dyingEntity, causeTracker, phaseContext, entities));
            phaseContext.getCapturedEntityDrops().ifPresent(map -> {
                if (map.isEmpty()) {
                    return;
                }
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Entity Death Updates Spawning").centre().hr();
                printer.add("Entity Dying: " + dyingEntity);
                printer.add("The item stacks captured are: ");
                map.asMap().entrySet().forEach(entry -> {
                    printer.add("  - Entity with UUID: %s", entry.getKey());
                    for (ItemStack stack : entry.getValue()) {
                        printer.add("    - %s", stack);
                    }
                });
                printer.trace(System.err);
            });
        }

    }

    EntityPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        // For now, short circuit and ignore block changes (they're passing right through currently)
        return false;
    }

}
