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

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import java.util.ArrayList;
import java.util.List;

class NoCaptureBlockTickPhaseState extends BlockTickPhaseState {

    NoCaptureBlockTickPhaseState(String name) {
        super(name);
    }

    @Override
    public boolean shouldCaptureBlockChangeOrSkip(BlockTickContext phaseContext, BlockPos pos) {
        return false;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return false;
    }

    @Override
    public boolean requiresBlockBulkCaptures() {
        return false;
    }

    @Override
    public boolean alreadyCapturingItemSpawns() {
        return true;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns() {
        return true;
    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return false;
    }

    @Override
    public boolean performOrCaptureItemDrop(BlockTickContext phaseContext, Entity entity, EntityItem entityitem) {
        return false;
    }

    @Override
    public boolean spawnEntityOrCapture(BlockTickContext context, org.spongepowered.api.entity.Entity entity, int chunkX, int chunkZ) {
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        if (!ShouldFire.SPAWN_ENTITY_EVENT) { // We don't want to throw an event if we don't need to.
            return EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(locatableBlock);
            associateAdditionalCauses(context, frame);
            if (entity instanceof EntityXPOrb) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                final ArrayList<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(1);
                entities.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(entities, context);
            }
            final List<org.spongepowered.api.entity.Entity> nonExpEntities = new ArrayList<>(1);
            nonExpEntities.add(entity);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            return SpongeCommonEventFactory.callSpawnEntity(nonExpEntities, context);
        }
    }

    @Override
    public boolean requiresPost() {
        return false;
    }

    @Override
    public void unwind(BlockTickContext context) {
        // We didn't perform any capturing, so there's nothing to do
    }
}
