package org.spongepowered.common.registry.builtin.supplier;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.SpongeEventContextKey;

import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class EventContextKeySupplier {

    public static Stream<EventContextKey<?>> stream() {
        return Stream.of(
                new SpongeEventContextKey<>(CatalogKey.sponge("block_event_queue"), new TypeToken<LocatableBlock>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("block_event_process"), new TypeToken<LocatableBlock>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("creator"), new TypeToken<User>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("damage_type"), new TypeToken<DamageType>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("dismount_type"), new TypeToken<DismountType>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("igniter"), new TypeToken<User>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("last_damage_source"), new TypeToken<DamageSource>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("liquid_break"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("liquid_flow"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("liquid_mix"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("neighbor_notify_source"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("notifier"), new TypeToken<User>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("owner"), new TypeToken<User>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("player"), new TypeToken<Player>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("player_simulated"), new TypeToken<GameProfile>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("projectile_source"), new TypeToken<ProjectileSource>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("service_manager"), new TypeToken<ServiceManager>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("spawn_type"), new TypeToken<SpawnType>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("teleport_type"), new TypeToken<TeleportType>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("thrower"), new TypeToken<User>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("weapon"), new TypeToken<ItemStackSnapshot>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("fake_player"), new TypeToken<Player>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("player_break"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("player_place"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("fire_spread"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("leaves_decay"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("piston_retract"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("piston_extend"), new TypeToken<World<?>>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("block_hit"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("entity_hit"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("used_item"), new TypeToken<ItemStackSnapshot>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("used_hand"), new TypeToken<HandType>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("plugin"), new TypeToken<PluginContainer>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("break_event"), new TypeToken<ChangeBlockEvent.Break>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("place_event"), new TypeToken<ChangeBlockEvent.Place>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("modify_event"), new TypeToken<ChangeBlockEvent.Modify>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("decay_event"), new TypeToken<ChangeBlockEvent.Decay>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("grow_event"), new TypeToken<ChangeBlockEvent.Grow>() { private static final long serialVersionUID = 1L; }),
        new SpongeEventContextKey<>(CatalogKey.sponge("growth_origin"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; })
        );
    }
}
