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
package org.spongepowered.common.event.tracking.phase.block;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.interfaces.block.tile.IMixinBlockDispenser;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;

final class DispensePhaseState extends BlockPhaseState {

    DispensePhaseState() {
    }

    @Override
    public GeneralizedContext createPhaseContext() {
        return super.createPhaseContext()
                .addBlockCaptures()
                .addEntityCaptures()
                .addEntityDropCaptures();
    }

    @Override
    public void unwind(GeneralizedContext phaseContext) {
        final BlockSnapshot blockSnapshot = phaseContext.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Could not find a block dispensing items!", phaseContext));
        phaseContext.getCapturedBlockSupplier()
                .acceptAndClearIfNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, this, phaseContext));
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(blockSnapshot);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DISPENSE);
            phaseContext.addNotifierAndOwnerToCauseStack();
            phaseContext.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(items -> {
                        final ArrayList<Entity> entities = new ArrayList<>();
                        for (EntityItem item : items) {
                            entities.add(EntityUtil.fromNative(item));
                        }
                        final DropItemEvent.Dispense
                                event =
                                SpongeEventFactory.createDropItemEventDispense(Sponge.getCauseStackManager().getCurrentCause(), entities);
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            for (Entity entity : event.getEntities()) {
                                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                            }
                        }
                    });
            phaseContext.getCapturedEntitySupplier()
                    .acceptAndClearIfNotEmpty(entities -> {
                        List<Projectile> projectiles = new ArrayList<>();
                        List<Entity> rest = new ArrayList<>();
                        for (Entity entity : entities) {
                            if (entity instanceof Projectile) {
                                projectiles.add((Projectile) entity);
                            } else {
                                rest.add(entity);
                            }
                        }

                        if (!rest.isEmpty() && ShouldFire.SPAWN_ENTITY_EVENT) {
                            fireSpawnEntityEvent(phaseContext, frame, rest);
                        }

                        if (!projectiles.isEmpty()) {
                            frame.addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PROJECTILE);
                            Dispenser dispenser = (Dispenser) blockSnapshot.getLocation().get().getExtent().getTileEntity(blockSnapshot.getLocation().get().getPosition().toInt()).get();
                            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, dispenser);
                            frame.addContext(EventContextKeys.THROWER, dispenser);

                            LaunchProjectileEvent launchProjectileEvent =
                                    SpongeEventFactory.createLaunchProjectileEvent(Sponge.getCauseStackManager().getCurrentCause(), projectiles);
                            if (SpongeImpl.postEvent(launchProjectileEvent)) {
                                projectiles.clear();
                                Vector3d position = dispenser.getLocation().getPosition();
                                BlockSourceImpl blockSource = new BlockSourceImpl(((World) dispenser.getWorld()), new BlockPos(position.getX(), position.getY(), position.getZ()));
                                TileEntityDispenser tileDispenser = blockSource.getBlockTileEntity();
                                ((IMixinBlockDispenser) tileDispenser.getBlockType()).restoreDispensedItem();
                            } else {
                                fireSpawnEntityEvent(phaseContext, frame, (List<Entity>) (List<?>) projectiles);
                            }
                        }
                    });
        }
    }

    private void fireSpawnEntityEvent(GeneralizedContext phaseContext, CauseStackManager.StackFrame frame, List<Entity> projectiles) {
        if(ShouldFire.SPAWN_ENTITY_EVENT) {
            SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(frame.getCurrentCause(), projectiles);
            if(!SpongeImpl.postEvent(spawnEntityEvent)) {
                final User user = phaseContext.getNotifier().orElseGet(() -> phaseContext.getOwner().orElse(null));
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    if (user != null) {
                        EntityUtil.toMixin(entity).setCreator(user.getUniqueId());
                    }
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                }
            }
        } else {
            final User user = phaseContext.getNotifier().orElseGet(() -> phaseContext.getOwner().orElse(null));
            for (Entity entity : projectiles) {
                if (user != null) {
                    EntityUtil.toMixin(entity).setCreator(user.getUniqueId());
                }
                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
            }
        }
    }
}
