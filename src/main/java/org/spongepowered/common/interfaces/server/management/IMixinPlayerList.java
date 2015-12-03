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
package org.spongepowered.common.interfaces.server.management;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.context.ContextViewer;

import javax.annotation.Nullable;

public interface IMixinPlayerList {

    double getMovementFactor(WorldProvider worldProvider);

    void prepareEntityForPortal(Entity entityIn, WorldServer oldWorldIn, WorldServer toWorldIn);

    void transferPlayerToDimension(EntityPlayerMP playerIn, int targetDimensionId, net.minecraft.world.Teleporter teleporter);

    void transferEntityToWorld(Entity entityIn, int fromDimensionId, WorldServer fromWorld, WorldServer toWorld, net.minecraft.world
            .Teleporter teleporter);

    @Nullable
    EntityPlayerMP getPlayer(String name, @Nullable ContextViewer viewer);

}
