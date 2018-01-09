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
package org.spongepowered.common.event.tracking.context;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.lang.ref.WeakReference;
import java.util.Optional;

import javax.annotation.Nullable;

public final class CaptureBlockPos {

    @Nullable private BlockPos pos;
    @Nullable private WeakReference<IMixinWorldServer> mixinWorldReference;

    public CaptureBlockPos() {
    }

    public CaptureBlockPos(@Nullable BlockPos pos) {
        this.pos = pos;
    }

    public Optional<BlockPos> getPos() {
        return Optional.ofNullable(this.pos);
    }

    public void setPos(@Nullable BlockPos pos) {
        this.pos = pos;
    }

    public void setWorld(@Nullable IMixinWorldServer world) {
        if (world == null) {
            this.mixinWorldReference = null;
        } else {
            this.mixinWorldReference = new WeakReference<>(world);
        }
    }

    public void setWorld(@Nullable WorldServer world) {
        if (world == null) {
            this.mixinWorldReference = null;
        } else {
            this.mixinWorldReference = new WeakReference<>((IMixinWorldServer) world);
        }
    }

    public Optional<IMixinWorldServer> getMixinWorld() {
        return this.mixinWorldReference == null ? Optional.empty() : Optional.ofNullable(this.mixinWorldReference.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CaptureBlockPos that = (CaptureBlockPos) o;
        return com.google.common.base.Objects.equal(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(this.pos);
    }
}
