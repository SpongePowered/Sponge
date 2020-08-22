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

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class EntityDropPhaseState extends EntityPhaseState<BasicEntityContext> {

    private final BiConsumer<CauseStackManager.StackFrame, BasicEntityContext> DEATH_STATE_MODIFIER = super.getFrameModifier()
        .andThen((frame, ctx) -> {
            final Entity dyingEntity =
                ctx.getSource(Entity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", ctx));
            final DamageSource damageSource = ctx.getDamageSource();
            frame.pushCause(dyingEntity);
            if (damageSource != null) {
                frame.pushCause(damageSource);
            }
        });

    @Override
    public boolean tracksEntityDeaths() {
        return true;
    }

    @Override
    public BasicEntityContext createNewContext(final PhaseTracker tracker) {
        return new BasicEntityContext(this, tracker).addCaptures()
            .addEntityDropCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BasicEntityContext> getFrameModifier() {
        return this.DEATH_STATE_MODIFIER;
    }

    @Override
    public void unwind(final BasicEntityContext context) {
        final Entity dyingEntity =
            context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", context));

        final boolean isPlayer = dyingEntity instanceof PlayerEntity;
        final PlayerEntity entityPlayer = isPlayer ? (PlayerEntity) dyingEntity : null;
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> this.standardSpawnCapturedEntities(context, entities));

        // Forge always fires a living drop event even if nothing was captured
        // This allows mods such as Draconic Evolution to add items to the drop list
        if (context.getPerEntityItemEntityDropSupplier().isEmpty()) {
            PhaseTracker.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            final ArrayList<Entity> entities = new ArrayList<>();
            SpongeCommonEventFactory.callDropItemDestruct(entities, context);
            return;
        }
        context.getPerEntityItemEntityDropSupplier().acceptAndRemoveIfPresent(dyingEntity.getUniqueId(), items -> {
            final ArrayList<Entity> entities = new ArrayList<>();
            for (final ItemEntity item : items) {
                entities.add((Entity) item);
            }

            if (isPlayer) {
                // Forge and Vanilla always clear items on player death BEFORE drops occur
                // This will also provide the highest compatibility with mods such as Tinkers Construct
                entityPlayer.inventory.clear();
            }
            PhaseTracker.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            SpongeCommonEventFactory.callDropItemDestruct(entities, context);

            // Note: If cancelled, the items do not spawn in the world and are NOT copied back to player inventory.
            // This avoids many issues with mods such as Tinkers Construct's soulbound items.
        });
        // Note that this is only used if and when item pre-merging is enabled. Which is never enabled in forge.
    }


}
