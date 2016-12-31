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

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerPhase extends TrackingPhase {

    public static final class State {
        public static final IPhaseState PLAYER_LOGOUT = new PlayerPhaseState();
    }


    static final class PlayerPhaseState implements IPhaseState {

        PlayerPhaseState() {
        }

        @Override
        public TrackingPhase getPhase() {
            return TrackingPhases.PLAYER;
        }
    }

    public static PlayerPhase getInstance() {
        return Holder.INSTANCE;
    }

    private PlayerPhase() {
    }

    private static final class Holder {
        static final PlayerPhase INSTANCE = new PlayerPhase();
    }

    @SuppressWarnings("unchecked")
	@Override
    public void unwind(IPhaseState state, PhaseContext phaseContext) {
        // Since currently all we have is PLAYER_LOGOUT, don't care about states.
        final Player player = phaseContext.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing a player leaving, but we're not!", phaseContext));
        Object frame = Sponge.getCauseStackManager().pushCauseFrame();
        Sponge.getCauseStackManager().pushCause(player);
        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DISPENSE);
        phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(items -> {
            final ArrayList<Entity> entities = new ArrayList<>();
            for (EntityItem item : items) {
                entities.add(EntityUtil.fromNative(item));
            }
            final DropItemEvent.Dispense
                    dispense =
                    SpongeEventFactory.createDropItemEventDispense(Sponge.getCauseStackManager().getCurrentCause(), entities);
            SpongeImpl.postEvent(dispense);
            if (!dispense.isCancelled()) {
                for (Entity entity : dispense.getEntities()) {
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                }
            }
        });
        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
        phaseContext.getCapturedItemStackSupplier().ifPresentAndNotEmpty(items -> {
            final List<EntityItem> drops = items.stream()
                    .map(drop -> drop.create(EntityUtil.getMinecraftWorld(player)))
                    .collect(Collectors.toList());
            final List<Entity> entities = (List<Entity>) (List<?>) drops;
            if (!entities.isEmpty()) {
                DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity droppedItem : event.getEntities()) {
                        EntityUtil.getMixinWorld(droppedItem).forceSpawnEntity(droppedItem);
                    }
                }
            }
        });
        phaseContext.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, state, phaseContext));
        Sponge.getCauseStackManager().popCauseFrame(frame);

    }


}
