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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;
import java.util.function.BiConsumer;

final class EntityDeathState extends EntityPhaseState<EntityDeathContext> {

    private final BiConsumer<CauseStackManager.StackFrame, EntityDeathContext> DEATH_STATE_MODIFIER = super.getFrameModifier()
        .andThen((frame, ctx) -> {
            final Entity dyingEntity =
                ctx.getSource(Entity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", ctx));
            final DamageSource damageSource = ctx.getDamageSource();
            frame.pushCause(dyingEntity);
            frame.pushCause(damageSource);
        });

    @Override
    public boolean tracksBlockSpecificDrops(final EntityDeathContext context) {
        return true;
    }

    @Override
    public boolean tracksEntityDeaths() {
        return true;
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, EntityDeathContext> getFrameModifier() {
        return this.DEATH_STATE_MODIFIER;
    }

    @Override
    public EntityDeathContext createNewContext(final PhaseTracker tracker) {
        return new EntityDeathContext(this, tracker)
            .addCaptures()
            .addEntityDropCaptures();
    }

    @Override
    public void unwind(final EntityDeathContext context) {
        final Entity dyingEntity =
            context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", context));
        final boolean isPlayer = dyingEntity instanceof PlayerEntity;
        final PlayerEntity entityPlayer = isPlayer ? (PlayerEntity) dyingEntity : null;
        // WE have to handle per-item entity drops and entity item drops before we handle other entity spawns
        // the reason we have to do it this way is because forge allows for item drops to potentially spawn
        // other entities at the same time.
        final boolean hasCaptures = true;
        // Forge always fires a living drop event even if nothing was captured
        // This allows mods such as Draconic Evolution to add items to the drop list
        if (!hasCaptures) {
            final ArrayList<Entity> entities = new ArrayList<>();
            PhaseTracker.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            boolean keepInventoryRule = false;

            if (entityPlayer != null) {
                if (((PlayerEntityBridge) entityPlayer).bridge$keepInventory()) {
                    keepInventoryRule = entityPlayer.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
                    // Set global keep-inventory gamerule so mods do not drop items
                     entityPlayer.world.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, entityPlayer.getServer());
                }
            }
            SpongeCommonEventFactory.callDropItemDestruct(entities, context);

            if (entityPlayer != null) {
                if (((PlayerEntityBridge) entityPlayer).bridge$keepInventory()) {
                    // Restore global keep-inventory gamerule
                    entityPlayer.world.getGameRules().get(GameRules.KEEP_INVENTORY).set(keepInventoryRule, entityPlayer.getServer());
                }
            }
        }

        TrackingUtil.processBlockCaptures(context);
    }
}
