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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;

import java.util.ArrayList;
import java.util.function.BiConsumer;

final class EntityDeathState extends EntityPhaseState<EntityDeathContext> {

    final BiConsumer<StackFrame, EntityDeathContext> DEATH_STATE_MODIFIER = super.getFrameModifier()
        .andThen((frame, ctx) -> {
            final Entity dyingEntity =
                ctx.getSource(Entity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", ctx));
            final DamageSource damageSource = ctx.getDamageSource();
            frame.pushCause(dyingEntity);
            frame.pushCause(damageSource);
        });

    @Override
    public boolean tracksBlockSpecificDrops(EntityDeathContext context) {
        return true;
    }

    @Override
    public boolean tracksEntityDeaths() {
        return true;
    }

    @Override
    public BiConsumer<StackFrame, EntityDeathContext> getFrameModifier() {
        return this.DEATH_STATE_MODIFIER;
    }

    @Override
    public EntityDeathContext createPhaseContext() {
        return new EntityDeathContext(this)
            .addCaptures()
            .addEntityDropCaptures();
    }

    @Override
    public void unwind(EntityDeathContext context) {
        final Entity dyingEntity =
            context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", context));
        final boolean isPlayer = dyingEntity instanceof EntityPlayer;
        final EntityPlayer entityPlayer = isPlayer ? (EntityPlayer) dyingEntity : null;
        // WE have to handle per-item entity drops and entity item drops before we handle other entity spawns
        // the reason we have to do it this way is because forge allows for item drops to potentially spawn
        // other entities at the same time.
        boolean hasCaptures = !context.getPerEntityItemEntityDropSupplier().isEmpty();
        context.getPerEntityItemEntityDropSupplier().acceptAndRemoveIfPresent(dyingEntity.getUniqueId(), items -> {
            final ArrayList<Entity> entities = new ArrayList<>();
            for (EntityItem item : items) {
                entities.add(EntityUtil.fromNative(item));
            }

            if (isPlayer) {
                // Forge and Vanilla always clear items on player death BEFORE drops occur
                // This will also provide the highest compatibility with mods such as Tinkers Construct
                entityPlayer.inventory.clear();
            }

            try (StackFrame internal = Sponge.getCauseStackManager().pushCauseFrame()) {
                internal.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                SpongeCommonEventFactory.callDropItemDestruct(entities, context);
            }

            // Note: If cancelled, the items do not spawn in the world and are NOT copied back to player inventory.
            // This avoids many issues with mods such as Tinkers Construct's soulbound items.
        });
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> this.standardSpawnCapturedEntities(context, entities));

        // Forge always fires a living drop event even if nothing was captured
        // This allows mods such as Draconic Evolution to add items to the drop list
        if (!hasCaptures) {
            final ArrayList<Entity> entities = new ArrayList<>();
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            boolean keepInventoryRule = false;

            if (entityPlayer != null) {
                if (((IMixinEntityPlayerMP) entityPlayer).keepInventory()) {
                    keepInventoryRule = entityPlayer.world.getGameRules().getBoolean(DefaultGameRules.KEEP_INVENTORY);
                    // Set global keep-inventory gamerule so mods do not drop items
                    entityPlayer.world.getGameRules().setOrCreateGameRule(DefaultGameRules.KEEP_INVENTORY, "true");
                }
            }
            SpongeCommonEventFactory.callDropItemDestruct(entities, context);

            if (entityPlayer != null) {
                if (((IMixinEntityPlayerMP) entityPlayer).keepInventory()) {
                    // Restore global keep-inventory gamerule
                    entityPlayer.world.getGameRules().setOrCreateGameRule(DefaultGameRules.KEEP_INVENTORY, String.valueOf(keepInventoryRule));
                }
            }
        }

        // Note that this is only used if and when item pre-merging is enabled. Which is never enabled in forge.
        EntityDropPhaseState.processPerItemDrop(context, dyingEntity, isPlayer, entityPlayer);

        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context);

    }


}
