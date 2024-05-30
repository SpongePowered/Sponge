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
package org.spongepowered.common.mixin.api.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PortalProcessor;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.entity.PortalProcessorBridge;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

@Mixin(PortalProcessor.class)
public abstract class PortalProcessorMixin_API implements Portal, PortalProcessorBridge {

    // @formatter:off
    @Shadow private net.minecraft.world.level.block.Portal portal;
    @Shadow private BlockPos entryPosition;
    // @formatter:on

    @Override
    public Optional<PortalLogic> logic() {
        return Optional.of((PortalLogic) this.portal);
    }

    @Override
    public ServerLocation position() {
        if (this.bridge$level() instanceof ServerWorld world) {
            return ServerLocation.of(world, VecHelper.toVector3i(this.entryPosition));
        }
        throw new IllegalStateException("PortalProcessor was not initialized for sponge usage.");
    }

    @Override
    public Optional<AABB> boundingBox() {
        return Optional.empty(); // not known without potentially generating blocks
    }
}
