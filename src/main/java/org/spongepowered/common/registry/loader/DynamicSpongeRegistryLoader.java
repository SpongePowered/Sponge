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
