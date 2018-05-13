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
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.registry.type.event.SpawnTypeRegistryModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EntityDropPhaseState extends EntityPhaseState<BasicEntityContext> {

    @Override
    public boolean tracksEntityDeaths() {
        return true;
    }

    @Override
    public BasicEntityContext createPhaseContext() {
        return new BasicEntityContext(this).addCaptures()
            .addEntityDropCaptures();
    }

    @Override
    public void unwind(BasicEntityContext context) {
        final Entity dyingEntity =
            context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", context));
        final DamageSource damageSource = context.getDamageSource();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(damageSource);
            frame.pushCause(dyingEntity);
            final boolean isPlayer = dyingEntity instanceof EntityPlayer;
            final EntityPlayer entityPlayer = isPlayer ? (EntityPlayer) dyingEntity : null;
            context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(entities -> {
                    // Separate experience orbs from other entity drops
                    final List<Entity> experience = entities.stream()
                        .filter(entity -> entity instanceof ExperienceOrb)
                        .collect(Collectors.toList());
                    if (!experience.isEmpty()) {
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                        SpongeCommonEventFactory.callSpawnEntity(experience, context);

                    }

                    // Now process other entities, this is separate from item drops specifically
                    final List<Entity> other = entities.stream()
                        .filter(entity -> !(entity instanceof ExperienceOrb))
                        .collect(Collectors.toList());
                    if (!other.isEmpty()) {
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypeRegistryModule.ENTITY_DEATH);
                        SpongeCommonEventFactory.callSpawnEntity(experience, context);
                    }
                });

            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            // Forge always fires a living drop event even if nothing was captured
            // This allows mods such as Draconic Evolution to add items to the drop list
            if (context.getPerEntityItemEntityDropSupplier().isEmpty() && context.getPerEntityItemDropSupplier().isEmpty()) {
                final ArrayList<Entity> entities = new ArrayList<>();
                SpongeCommonEventFactory.callDropItemDestruct(entities, context);
                return;
            }
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

                SpongeCommonEventFactory.callDropItemDestruct(entities, context);


                // Note: If cancelled, the items do not spawn in the world and are NOT copied back to player inventory.
                // This avoids many issues with mods such as Tinkers Construct's soulbound items.
            });
            // Note that this is only used if and when item pre-merging is enabled. Which is never enabled in forge.
            context.getPerEntityItemDropSupplier().acceptAndRemoveIfPresent(dyingEntity.getUniqueId(), itemStacks -> {
                final List<ItemDropData> items = new ArrayList<>();
                items.addAll(itemStacks);

                if (!items.isEmpty()) {
                    final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(dyingEntity);
                    final List<Entity> itemEntities = items.stream()
                        .map(data -> data.create((WorldServer) minecraftEntity.world))
                        .map(EntityUtil::fromNative)
                        .collect(Collectors.toList());

                    if (isPlayer) {
                        // Forge and Vanilla always clear items on player death BEFORE drops occur
                        // This will also provide the highest compatibility with mods such as Tinkers Construct
                        entityPlayer.inventory.clear();
                    }

                    SpongeCommonEventFactory.callDropItemDestruct(itemEntities, context);

                    // Note: If cancelled, the items do not spawn in the world and are NOT copied back to player inventory.
                    // This avoids many issues with mods such as Tinkers Construct's soulbound items.
                }

            });
        }
    }

}
