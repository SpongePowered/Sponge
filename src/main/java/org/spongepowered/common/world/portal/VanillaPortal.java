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
package org.spongepowered.common.world.portal;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalType;

import java.util.Optional;


public final class VanillaPortal implements Portal {

    private final PortalType type;
    private final ServerLocation origin, destination;

    public VanillaPortal(final PortalType type, final ServerLocation origin, final @Nullable ServerLocation destination) {
        this.type = type;
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public PortalType type() {
        return this.type;
    }

    @Override
    public ServerLocation origin() {
        return this.origin;
    }

    // Vanilla has no knowledge of where portals go to until you try, best we can do...
    @Override
    public Optional<ServerLocation> destination() {
        return Optional.ofNullable(this.destination);
    }
}
