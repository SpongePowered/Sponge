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
package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.server.TicketTypeBridge;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.function.Function;

@Mixin(TicketType.class)
public abstract class TicketTypeMixin<N, T> implements TicketTypeBridge<N, T> {

    private Function<T, N> impl$spongeToNativeType;

    public void bridge$setTypeConverter(final Function<T, N> converter) {
        this.impl$spongeToNativeType = converter;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    @Override
    public N bridge$convertToNativeType(final T spongeType) {
        // I hate myself for even doing this, but hey ho.
        // The intention is that people will only use Sponge registered tickets.
        // If a mod provides a ticket and a plugin wants to use them, it'll have
        // to use the correct type.
        if (this.impl$spongeToNativeType == null) {
            final TicketType<N> thisType = (TicketType<N>) (Object) this;
            if (thisType == TicketType.POST_TELEPORT) {
                this.impl$spongeToNativeType = (Function) entity -> ((Entity) entity).getId();
            } else if (thisType == TicketType.START || thisType == TicketType.DRAGON) {
                this.impl$spongeToNativeType = (Function) notUsed -> Unit.INSTANCE;
            } else if (thisType == TicketType.FORCED || thisType == TicketType.LIGHT || thisType == TicketType.PLAYER ||
                    thisType == TicketType.UNKNOWN) {
                this.impl$spongeToNativeType = (Function) (Function<Vector3i, ChunkPos>) (VecHelper::toChunkPos);
            } else if (thisType == TicketType.PORTAL) {
                this.impl$spongeToNativeType = (Function) (Function<Vector3i, BlockPos>) (VecHelper::toBlockPos);
            } else {
                this.impl$spongeToNativeType = t -> (N) t;
            }
        }
        return this.impl$spongeToNativeType.apply(spongeType);
    }

}
