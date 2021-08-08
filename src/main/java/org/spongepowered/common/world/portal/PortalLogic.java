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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.world.portal.PortalType;

import java.util.function.Function;

public interface PortalLogic {

    // Matches Forge ITeleporter
    @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel targetWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo);

    // Matches Forge ITeleporter
    // Implementor note: the final function Boolean is true if a portal exists
    @Nullable Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel targetWorld, float yRot, Function<Boolean, Entity> teleportLogic);

    // Matches Forge ITeleporter
    // This isn't if it's a vanilla portal - it's if it's vanilla(ish) logic.
    boolean isVanilla();

    MovementType getMovementType();

    PortalType getPortalType();

}
