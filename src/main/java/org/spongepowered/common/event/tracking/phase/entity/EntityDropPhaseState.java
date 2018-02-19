package org.spongepowered.common.event.tracking.phase.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            Sponge.getCauseStackManager().pushCause(damageSource);
            Sponge.getCauseStackManager().pushCause(dyingEntity);
            final boolean isPlayer = dyingEntity instanceof EntityPlayer;
            final EntityPlayer entityPlayer = isPlayer ? (EntityPlayer) dyingEntity : null;
            final Optional<User> notifier = context.getNotifier();
            final Optional<User> owner = context.getOwner();
            final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
            context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(entities -> {
                    // Separate experience orbs from other entity drops
                    final List<Entity> experience = entities.stream()
                        .filter(entity -> entity instanceof ExperienceOrb)
                        .collect(Collectors.toList());
                    if (!experience.isEmpty()) {
                        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.EXPERIENCE);

                        final SpawnEntityEvent
                            spawnEntityEvent =
                            SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), experience);
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            for (Entity entity : spawnEntityEvent.getEntities()) {
                                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                            }
                        }
                    }

                    // Now process other entities, this is separate from item drops specifically
                    final List<Entity> other = entities.stream()
                        .filter(entity -> !(entity instanceof ExperienceOrb))
                        .collect(Collectors.toList());
                    if (!other.isEmpty()) {
                        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.ENTITY_DEATH);
                        final SpawnEntityEvent
                            spawnEntityEvent =
                            SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), experience);
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            for (Entity entity : spawnEntityEvent.getEntities()) {
                                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                            }
                        }
                    }
                });

            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
            // Forge always fires a living drop event even if nothing was captured
            // This allows mods such as Draconic Evolution to add items to the drop list
            if (context.getCapturedEntityItemDropSupplier().isEmpty() && context.getCapturedEntityDropSupplier().isEmpty()) {
                final ArrayList<Entity> entities = new ArrayList<>();
                final DropItemEvent.Destruct destruct = SpongeEventFactory.createDropItemEventDestruct(frame.getCurrentCause(), entities);
                SpongeImpl.postEvent(destruct);
                if (!destruct.isCancelled()) {
                    for (Entity entity : destruct.getEntities()) {
                        EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                    }
                }
                return;
            }
            context.getCapturedEntityItemDropSupplier().acceptAndRemoveIfPresent(dyingEntity.getUniqueId(), items -> {
                final ArrayList<Entity> entities = new ArrayList<>();
                for (EntityItem item : items) {
                    entities.add(EntityUtil.fromNative(item));
                }

                if (isPlayer) {
                    // Forge and Vanilla always clear items on player death BEFORE drops occur
                    // This will also provide the highest compatibility with mods such as Tinkers Construct
                    entityPlayer.inventory.clear();
                }

                final DropItemEvent.Destruct
                    destruct =
                    SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), entities);
                SpongeImpl.postEvent(destruct);
                if (!destruct.isCancelled()) {
                    for (Entity entity : destruct.getEntities()) {
                        EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                    }
                }

                // Note: If cancelled, the items do not spawn in the world and are NOT copied back to player inventory.
                // This avoids many issues with mods such as Tinkers Construct's soulbound items.
            });
            // Note that this is only used if and when item pre-merging is enabled. Which is never enabled in forge.
            context.getCapturedEntityDropSupplier().acceptAndRemoveIfPresent(dyingEntity.getUniqueId(), itemStacks -> {
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

                    final DropItemEvent.Destruct
                        destruct =
                        SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), itemEntities);
                    SpongeImpl.postEvent(destruct);
                    if (!destruct.isCancelled()) {
                        for (Entity entity : destruct.getEntities()) {
                            if (entityCreator != null) {
                                EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                            }
                            EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                        }
                    }

                    // Note: If cancelled, the items do not spawn in the world and are NOT copied back to player inventory.
                    // This avoids many issues with mods such as Tinkers Construct's soulbound items.
                }

            });
        }
    }

}
