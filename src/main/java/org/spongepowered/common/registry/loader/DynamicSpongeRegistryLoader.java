package org.spongepowered.common.registry.loader;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.placeholder.PlaceholderParsers;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;
import org.spongepowered.common.placeholder.SpongePlaceholderParserBuilder;
import org.spongepowered.common.registry.RegistryLoader;
import org.spongepowered.common.world.teleport.ConfigTeleportHelperFilter;
import org.spongepowered.common.world.teleport.DefaultTeleportHelperFilter;
import org.spongepowered.common.world.teleport.FlyingTeleportHelperFilter;
import org.spongepowered.common.world.teleport.NoPortalTeleportHelperFilter;
import org.spongepowered.common.world.teleport.SurfaceOnlyTeleportHelperFilter;

public class DynamicSpongeRegistryLoader {

    public static RegistryLoader<PlaceholderParser> placeholderParser() {
        return RegistryLoader.of(l -> {
            l.add(PlaceholderParsers.CURRENT_WORLD, k -> new SpongePlaceholderParserBuilder()
                    .parser(placeholderText -> Component.text(placeholderText.associatedObject().filter(x -> x instanceof Locatable)
                            .map(x -> ((Locatable) x).serverLocation().worldKey())
                            .orElseGet(() -> DefaultWorldKeys.DEFAULT).toString()))
                    .build());
            l.add(PlaceholderParsers.NAME, k -> new SpongePlaceholderParserBuilder()
                    .parser(placeholderText -> placeholderText.associatedObject()
                            .filter(x -> x instanceof Nameable)
                            .map(x -> Component.text(((Nameable) x).name()))
                            .orElse(Component.empty()))
                    .build());
        });
    }

    public static RegistryLoader<TeleportHelperFilter> teleportHelperFilter() {
        return RegistryLoader.of(l -> {
            l.add(TeleportHelperFilters.CONFIG, k -> new ConfigTeleportHelperFilter());
            l.add(TeleportHelperFilters.DEFAULT, k -> new DefaultTeleportHelperFilter());
            l.add(TeleportHelperFilters.FLYING, k -> new FlyingTeleportHelperFilter());
            l.add(TeleportHelperFilters.NO_PORTAL, k -> new NoPortalTeleportHelperFilter());
            l.add(TeleportHelperFilters.SURFACE_ONLY, k -> new SurfaceOnlyTeleportHelperFilter());
        });
    }

}
