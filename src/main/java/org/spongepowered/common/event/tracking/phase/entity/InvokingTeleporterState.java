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
package org.spongepowered.common.event.tracking.phase.entity;

import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.SpongeHooks;

public final class InvokingTeleporterState extends EntityPhaseState<InvokingTeleporterContext> {

    InvokingTeleporterState() {
    }

    @Override
    public InvokingTeleporterContext createNewContext(final PhaseTracker tracker) {
        return new InvokingTeleporterContext(this, tracker)
            .addBlockCaptures()
            .addEntityCaptures();
    }

    @Override
    public void unwind(final InvokingTeleporterContext context) {
    }

    @Override
    public boolean tracksBlockSpecificDrops(final InvokingTeleporterContext context) {
        return true;
    }

    @Override
    public boolean spawnEntityOrCapture(final InvokingTeleporterContext context, final Entity entity) {
        final ServerWorld worldServer = context.getTargetWorld();
        if (!((WorldBridge)worldServer).bridge$isFake() && SpongeImplHooks.onServerThread()) {
            SpongeHooks.logEntitySpawn((net.minecraft.entity.Entity) entity);
        }
        if (entity instanceof ServerPlayerEntity) {
            worldServer.addNewPlayer((ServerPlayerEntity) entity);
            return true;
        }

        if (entity instanceof LightningBoltEntity) {
            worldServer.addLightningBolt((LightningBoltEntity) entity);
            return true;
        }

        return worldServer.addEntity((net.minecraft.entity.Entity) entity);
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public boolean doesDenyChunkRequests() {
        return false;
    }

    @Override
    public void markTeleported(final InvokingTeleporterContext phaseContext) {
        phaseContext.setDidPort(true);
    }
}
