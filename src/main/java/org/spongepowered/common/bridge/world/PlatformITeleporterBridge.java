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
package org.spongepowered.common.bridge.world;

import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalForcer;

/**
 * Bridge methods designed as hooks for various methods called on a {@link PortalForcer}.
 *
 * <p>The Forge platform adds a mod implementable "ITeleporter" that is meant to be passed to
 * methods where an {@link Entity} is changing dimensions. This bridge is modeled after that
 * hook</p>
 */
public interface PlatformITeleporterBridge {

    default Entity bridge$placeEntity(final Entity entity, final ServerLevel fromWorld, final ServerLevel toWorld, final float yaw,
        final Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(true);
    }

    default boolean bridge$isVanilla() {
        return this.getClass().equals(PortalForcer.class);
    }
}
