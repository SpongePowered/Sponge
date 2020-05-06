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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.LocatableSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.block.DispenserBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DispensePhaseState extends BlockPhaseState {

    @Override
    public GeneralizedContext createNewContext() {
        return super.createNewContext()
            .addBlockCaptures()
            .addEntityCaptures()
            .addEntityDropCaptures();
    }

    @SuppressWarnings({"unchecked", "Duplicates", "RedundantCast"})
    @Override
    public void unwind(final GeneralizedContext phaseContext) {
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(phaseContext);
        phaseContext.getCapturedItemsSupplier()
            .acceptAndClearIfNotEmpty(items -> {
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DISPENSE);
                SpongeCommonEventFactory.callDropItemDispense(items, phaseContext);
            });
        phaseContext.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> {
                List<Entity> projectiles = new ArrayList<>();
                List<Entity> rest = new ArrayList<>();
                for (Entity entity : entities) {
                    if (entity instanceof Projectile) {
                        projectiles.add(entity);
                    } else {
                        rest.add(entity);
                    }
                }

                if (!rest.isEmpty()) {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DISPENSE);
                    SpongeCommonEventFactory.callSpawnEntity(rest, phaseContext);
                }

                if (!projectiles.isEmpty()) {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PROJECTILE);
                        TileEntity tileEntity = phaseContext.getSource(BlockSnapshot.class)
                                .flatMap(LocatableSnapshot::getLocation)
                                .flatMap(Location::getTileEntity)
                                .orElse(null);
                        if (tileEntity instanceof ProjectileSource) {
                            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, ((ProjectileSource) tileEntity));
                        }

                        int projectilesCount = projectiles.size();
                        projectiles.removeIf(projectile ->
                                SpongeImpl.postEvent(SpongeEventFactory.createLaunchProjectileEvent(frame.getCurrentCause(), (Projectile) projectile)));
                        if (!projectiles.isEmpty()) {
                            SpongeCommonEventFactory.callSpawnEntity(projectiles, phaseContext);
                        }

                        if (projectilesCount != projectiles.size() && tileEntity instanceof DispenserBridge) {
                            ((DispenserBridge) tileEntity).bridge$restoreDispensedItem(projectiles.size());
                        }
                    }
                }
            });
        phaseContext.getBlockItemDropSupplier()
            .acceptAndClearIfNotEmpty(drops -> {
                drops.asMap().forEach((key, value) -> {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                    SpongeCommonEventFactory.callDropItemDestruct(new ArrayList<>((Collection<? extends Entity>) (Collection<?>) value), phaseContext);
                });
            });
    }

}
