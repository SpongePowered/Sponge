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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.portal.Portal;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;

public final class EndPortalType extends VanillaPortalType {

    @Override
    public boolean generatePortal(final ServerLocation location, final Axis axis) {
        Objects.requireNonNull(location);
        PortalHelper.generateEndPortal((ServerLevel) location.world(), location.blockX(), location.blockY(), location.blockZ(), true);
        return true;
    }

    @Override
    public Optional<Portal> findPortal(final ServerLocation location) {
        Objects.requireNonNull(location);
        return Optional.empty();
    }

    @Override
    public boolean teleport(final Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(destination);

        return false;
    }
}
