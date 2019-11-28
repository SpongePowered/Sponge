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
package org.spongepowered.common.world;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.teleport.PortalAgent;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// This is a giant hack to allow passing a Forge ITeleporter to the API.
// TODO: https://github.com/SpongePowered/SpongeForge/issues/2266
public class VirtualPortalAgent implements PortalAgent {
    private static final Map<Class<? extends ForgeITeleporterBridge>, PortalAgentType> TYPES = new HashMap<>();
    private final ForgeITeleporterBridge teleporter;
    private final PortalAgentType type;

    public static PortalAgent workaround(final ForgeITeleporterBridge teleporter) {
        if (teleporter instanceof PortalAgent) {
            return (PortalAgent) teleporter;
        }
        return new VirtualPortalAgent(teleporter);
    }

    public VirtualPortalAgent(final ForgeITeleporterBridge teleporter) {
        this.teleporter = teleporter;
        this.type = getType(teleporter.getClass());
    }

    private static PortalAgentType getType(final Class<? extends ForgeITeleporterBridge> teleporterClass) {
        return TYPES.computeIfAbsent(teleporterClass, klass -> {
            final String modId = SpongeImplHooks.getModIdFromClass(teleporterClass);
            final String name = teleporterClass.getSimpleName().toLowerCase(Locale.ENGLISH);
            return new SpongePortalAgentType(modId + ':' + name, name, teleporterClass);
        });
    }

    public ForgeITeleporterBridge getTeleporter() {
        return this.teleporter;
    }

    @Override
    public int getSearchRadius() {
        return 0;
    }

    @Override
    public PortalAgent setSearchRadius(final int radius) {
        return this;
    }

    @Override
    public int getCreationRadius() {
        return 0;
    }

    @Override
    public PortalAgent setCreationRadius(final int radius) {
        return this;
    }

    @Override
    public Optional<Location<World>> findOrCreatePortal(final Location<World> targetLocation) {
        return Optional.empty();
    }

    @Override
    public Optional<Location<World>> findPortal(final Location<World> targetLocation) {
        return Optional.empty();
    }

    @Override
    public Optional<Location<World>> createPortal(final Location<World> targetLocation) {
        return Optional.empty();
    }

    @Override
    public PortalAgentType getType() {
        return this.type;
    }
}
