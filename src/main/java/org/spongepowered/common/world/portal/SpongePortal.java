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
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public class SpongePortal implements Portal {

    private final ServerLocation position;
    @Nullable private final PortalLogic portalLogic;
    @Nullable private final AABB aabb;

    public SpongePortal(final ServerLocation position, @Nullable final PortalLogic portalLogic, @Nullable final AABB aabb) {
        this.position = position;
        this.portalLogic = portalLogic;
        this.aabb = aabb;
    }

    public SpongePortal(final ServerLocation position, final PortalLogic portalLogic) {
        this(position, portalLogic, null);
    }


    public SpongePortal(final ServerLocation position) {
        this(position, null, null);
    }

    @Override
    public Optional<PortalLogic> logic() {
        return Optional.ofNullable(this.portalLogic);
    }

    @Override
    public ServerLocation position() {
        return this.position;
    }

    @Override
    public Optional<AABB> boundingBox() {
        return Optional.ofNullable(this.aabb);
    }
}
